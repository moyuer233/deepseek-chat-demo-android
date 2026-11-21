import { useSettings } from '../settings.js'
import { streamMessage } from '../api/client.js'
import { estimateTokens, plainText } from './tokens.js'
import type { Message, Session } from './types.js'

/** 官方 /models 声明：deepseek-flash 与 deepseek-v4-pro 均为 1M 上下文 */
export const CONTEXT_WINDOW = 1_048_576
/** 阈值比例：DSH 默认 0.8，即 102.4k 触发 */
const DEFAULT_THRESHOLD_RATIO = 0.8
/** 最近内容保留比例：DSH 默认 0.16 */
const DEFAULT_RETAIN_RATIO = 0.16
/** 单次摘要请求最多送入字符数 */
const MAX_TRANSCRIPT_CHARS = 400_000

const renderTranscript = (messages: Message[]): string =>
  messages
    .map((message) => {
      const who = message.role === 'user' ? '用户' : '助手'
      const files =
        message.role === 'user' && message.files.length > 0
          ? `（附件：${message.files.map((file) => file.name).join('、')}）`
          : ''
      return `${who}：${plainText(message)}${files}`
    })
    .join('\n\n')
    .slice(0, MAX_TRANSCRIPT_CHARS)

/** DSH compaction-basic 的检查点结构（中文版） */
const COMPACTION_INSTRUCTION = [
  '你现在是对话压缩引擎。把上面的对话压缩成结构化检查点，让另一个模型在不丢失关键上下文的情况下继续工作。',
  '',
  '严格按下面的 Markdown 结构输出，保留每个小节和顺序；用简短要点，不要写段落；空小节写「（无）」，不要省略小节。',
  '',
  '## 主要请求与意图',
  '- [用户最初与变化后的目标；关键措辞要原样引用]',
  '',
  '## 关键技术概念',
  '- [技术、框架、模式与约定]',
  '',
  '## 文件与代码',
  '- [精确路径：为何重要、关键改动或片段]',
  '',
  '## 错误与修复',
  '- [错误：如何解决，及相应用户反馈]',
  '',
  '## 待办',
  '- [明确要求但尚未完成的工作]',
  '',
  '## 当前工作',
  '- [检查点时刻正在进行的精确内容]',
  '',
  '## 下一步',
  '- [与最近请求直接一致的下一个动作，或「（无）」]',
  '',
  '## 关键上下文',
  '- [决定与理由、约束、用户偏好、未决问题、继续所需数据]',
  '',
  '规则：保留精确文件路径、命令、错误字符串、标识符、数值、函数签名与语法片段；忠实记录用户反馈与明确指令，尤其是纠正；不要提及本次摘要请求或上下文已被压缩；只输出检查点文本。',
].join('\n')

const summarize = async (transcript: string): Promise<string> => {
  const controller = new AbortController()
  const timer = setTimeout(() => controller.abort(), 180_000)
  try {
    let summary = ''
    const body = {
      model: 'deepseek-flash' as const,
      system: '你是对话压缩引擎，只输出检查点文本。',
      messages: [
        { role: 'user' as const, content: transcript },
        { role: 'user' as const, content: COMPACTION_INSTRUCTION },
      ],
      thinking: { type: 'disabled' as const },
      tools: [],
      safety_check: { level: 'high' as const },
    }
    for await (const event of streamMessage(useSettings.getState().apiKey ?? '', body, controller.signal)) {
      if (event.type === 'content_block_delta' && event.delta.type === 'text_delta') {
        summary += event.delta.text
      }
    }
    return summary.trim()
  } finally {
    clearTimeout(timer)
  }
}

/** 压缩所需的会话读写能力，由 store 注入，避免 store ↔ compaction 循环依赖 */
export type CompactionStore = {
  getSession: (id: string) => Session | undefined
  updateCompaction: (id: string, compaction: { summary: string; throughId: string; updatedAt: number }) => void
}

/** 从末尾往前保留约 retainRatio 的 token，返回切点下标（保留这段之后的原文） */
const selectCutoffIndex = (messages: Message[], retainTokens: number): number => {
  let retained = 0
  let index = messages.length
  while (index > 0 && retained < retainTokens) {
    index -= 1
    retained += estimateTokens([messages[index]])
  }
  return Math.max(0, index - 1)
}

/**
 * DSH compaction-basic 同款：接近上下文上限时，把旧区间压缩成结构化检查点。
 * - 阈值 = 上下文窗口 × 0.8（或设置里的 compactThresholdTokens），可用 force 跳过；
 * - 保留最近约 15% token 的原文；
 * - 摘要失败静默，不影响聊天。
 */
export const compactSession = async (store: CompactionStore, sessionId: string, force = false): Promise<boolean> => {
  const settings = useSettings.getState()
  if (!settings.autoCompact && !force) {
    return false
  }
  const session = store.getSession(sessionId)
  if (!session || session.messages.length <= 2) {
    return false
  }
  const threshold = settings.compactThresholdTokens || Math.floor(CONTEXT_WINDOW * DEFAULT_THRESHOLD_RATIO)
  const total = estimateTokens(session.messages)
  if (!force && total < threshold) {
    return false
  }
  const retainTokens = Math.floor(CONTEXT_WINDOW * DEFAULT_RETAIN_RATIO)
  // 手动压缩：保留最近 4 条原文即可，让「立即压缩」在小会话上也能验证效果
  const cutoffIndex = force
    ? Math.max(0, session.messages.length - 5)
    : selectCutoffIndex(session.messages, retainTokens)
  if (cutoffIndex <= 0) {
    return false
  }
  const cutoff = session.messages[cutoffIndex]
  if (session.compaction?.throughId === cutoff.id && !force) {
    return false
  }
  const older = session.messages.slice(0, cutoffIndex + 1)
  try {
    const summary = await summarize(renderTranscript(older))
    if (!summary) {
      return false
    }
    const latest = store.getSession(sessionId)
    if (!latest || !latest.messages.some((message) => message.id === cutoff.id)) {
      return false
    }
    store.updateCompaction(sessionId, { summary, throughId: cutoff.id, updatedAt: Date.now() })
    return true
  } catch {
    return false
  }
}

/** 回合结束后自动触发（不阻塞） */
export const maybeCompact = async (store: CompactionStore, sessionId: string): Promise<void> => {
  await compactSession(store, sessionId)
}

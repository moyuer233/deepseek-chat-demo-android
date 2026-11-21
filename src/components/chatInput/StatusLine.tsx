import { useTranslate } from '../../i18n/index.js'
import { getSession, useChatStore } from '../../services/chat/store.js'
import { contextPressure, emptyUsage, formatTokens, sessionUsage } from '../../services/chat/usage.js'
import styles from './StatusLine.module.css'

/** 输入框下方的 DSH 风格状态行：模型 / 上下文占用 / tokens / 缓存命中 / 生成性能。 */
export const StatusLine = ({ sessionId }: { sessionId: string | null }) => {
  const t = useTranslate()
  const streaming = useChatStore((state) => (sessionId ? Boolean(state.streaming[sessionId]) : false))
  const session = sessionId ? (getSession(sessionId) ?? null) : null
  const usage = session ? sessionUsage(session) : emptyUsage()
  const pressure = contextPressure(session)
  const lastReply = session
    ? [...session.messages].reverse().find((message) => message.role === 'assistant')
    : undefined
  const model = lastReply && lastReply.role === 'assistant' ? (lastReply.model ?? 'deepseek-flash') : 'deepseek-flash'
  const promptTokens = usage.input + usage.cacheRead + usage.cacheWrite
  const cacheRate = promptTokens > 0 ? Math.round((usage.cacheRead / promptTokens) * 100) : 0
  const perf = lastReply && lastReply.role === 'assistant' ? lastReply.perf : undefined
  const speed =
    perf?.totalMs && lastReply && lastReply.role === 'assistant' && lastReply.usage
      ? lastReply.usage.output_tokens / (perf.totalMs / 1000)
      : null
  const firstToken = perf?.firstTokenMs ? (perf.firstTokenMs / 1000).toFixed(1) + 's' : null
  const percent = Math.round(pressure.ratio * 100)
  return (
    <div className={styles.statusLine} aria-live="polite">
      <span className={styles.model}>{model}</span>
      <span>{t('statusContext')} {formatTokens(pressure.tokens)} / {formatTokens(pressure.window)} ({percent}%)</span>
      <span>{formatTokens(usage.input + usage.cacheWrite + usage.cacheRead + usage.output)} tokens</span>
      <span>{t('statusCache')} {cacheRate}%</span>
      {speed !== null ? <span>{speed.toFixed(1)} tok/s</span> : null}
      {firstToken ? <span>{t('statusFirstToken')} {firstToken}</span> : null}
      {streaming ? <span className={styles.streaming}>{t('statusGenerating')}</span> : null}
    </div>
  )
}

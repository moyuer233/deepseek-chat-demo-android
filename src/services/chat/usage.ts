import { getSession, useChatStore } from './store.js'
import { estimateTokens } from './tokens.js'
import { CONTEXT_WINDOW } from './compaction.js'
import type { AssistantMessage, Session } from './types.js'

/** DeepSeek 官方价（CNY / 百万 token），2026-08-17 起；高峰价为基准 */
const PRICE = { input: 2, cacheRead: 0.04, output: 8 }
const OFF_PEAK_MULTIPLIER = 0.5

/** 北京时间工作日 09:00-12:00、14:00-18:00 为高峰，其余半价 */
export const isPeakTime = (time: number): boolean => {
  const date = new Date(time)
  const day = date.getDay()
  if (day === 0 || day === 6) {
    return false
  }
  const hour = date.getHours() + date.getMinutes() / 60
  return (hour >= 9 && hour < 12) || (hour >= 14 && hour < 18)
}

export type UsageStats = {
  input: number
  cacheRead: number
  cacheWrite: number
  output: number
  peakCost: number
  offPeakCost: number
  totalCost: number
  requests: number
}

export const emptyUsage = (): UsageStats => ({
  input: 0,
  cacheRead: 0,
  cacheWrite: 0,
  output: 0,
  peakCost: 0,
  offPeakCost: 0,
  totalCost: 0,
  requests: 0,
})

export const sessionUsage = (session: Session): UsageStats => {
  const stats = emptyUsage()
  for (const message of session.messages) {
    if (message.role !== 'assistant' || !message.usage) {
      continue
    }
    const usage = message.usage
    const cost =
      ((usage.input_tokens + usage.cache_creation_input_tokens) * PRICE.input +
        usage.cache_read_input_tokens * PRICE.cacheRead +
        usage.output_tokens * PRICE.output) /
      1_000_000
    stats.input += usage.input_tokens
    stats.cacheRead += usage.cache_read_input_tokens
    stats.cacheWrite += usage.cache_creation_input_tokens
    stats.output += usage.output_tokens
    stats.requests += 1
    if (isPeakTime(message.createdAt)) {
      stats.peakCost += cost
    } else {
      stats.offPeakCost += cost * OFF_PEAK_MULTIPLIER
    }
  }
  stats.totalCost = stats.peakCost + stats.offPeakCost
  return stats
}

export const totalUsage = (): UsageStats => {
  const summaries = useChatStore.getState().sessions
  const total = emptyUsage()
  for (const summary of summaries) {
    const session = getSession(summary.id)
    if (!session) {
      continue
    }
    const usage = sessionUsage(session)
    total.input += usage.input
    total.cacheRead += usage.cacheRead
    total.cacheWrite += usage.cacheWrite
    total.output += usage.output
    total.peakCost += usage.peakCost
    total.offPeakCost += usage.offPeakCost
    total.requests += usage.requests
  }
  total.totalCost = total.peakCost + total.offPeakCost
  return total
}

/** 当前会话上下文占用（按估算 token / 128k） */
/**
 * 上下文占用：优先用最近一次请求由 API 报告的提示词规模（未缓存输入 + 缓存读取 + 缓存写入），
 * 与 DSH 的 token-meter 口径一致——它包含图片、工具与历史思考，比本地字符估算准确。
 * 还没有请求记录时退回本地估算（含压缩摘要），此时图片不计入。
 */
export const contextPressure = (session: Session | null) => {
  let tokens = 0
  const lastUsage: AssistantMessage | undefined = session
    ? [...session.messages]
        .reverse()
        .find((message): message is AssistantMessage => message.role === 'assistant' && Boolean(message.usage))
    : undefined
  if (lastUsage?.usage) {
    const usage = lastUsage.usage
    tokens =
      usage.input_tokens + usage.cache_read_input_tokens + usage.cache_creation_input_tokens
  } else if (session) {
    const compaction = session.compaction
    const cutoffIndex = compaction
      ? session.messages.findIndex((message) => message.id === compaction.throughId)
      : -1
    if (compaction && cutoffIndex >= 0) {
      tokens =
        Math.ceil(compaction.summary.length / 2) +
        estimateTokens(session.messages.slice(cutoffIndex + 1))
    } else {
      tokens = estimateTokens(session.messages)
    }
  }
  return { tokens, window: CONTEXT_WINDOW, ratio: Math.min(1, tokens / CONTEXT_WINDOW) }
}

export const formatTokens = (value: number): string => {
  const trim = (scaled: number) => scaled.toFixed(1).replace(/\.0$/, '')
  if (value >= 1_000_000) {
    return trim(value / 1_000_000) + 'M'
  }
  if (value >= 1_000) {
    return trim(value / 1_000) + 'K'
  }
  return String(value)
}

export const formatMoney = (value: number): string => {
  if (value === 0) {
    return '¥0'
  }
  return value >= 1 ? '¥' + value.toFixed(2) : '¥' + value.toFixed(4)
}

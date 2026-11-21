import { getSession, useChatStore } from './store.js'
import { estimateTokens } from './tokens.js'
import { CONTEXT_WINDOW } from './compaction.js'
import type { Session } from './types.js'

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
export const contextPressure = (session: Session | null) => {
  const tokens = session ? estimateTokens(session.messages) : 0
  return { tokens, window: CONTEXT_WINDOW, ratio: Math.min(1, tokens / CONTEXT_WINDOW) }
}

export const formatTokens = (value: number): string => {
  if (value >= 1_000_000) {
    return (value / 1_000_000).toFixed(2) + 'M'
  }
  if (value >= 1_000) {
    return (value / 1_000).toFixed(1) + 'K'
  }
  return String(value)
}

export const formatMoney = (value: number): string => {
  if (value === 0) {
    return '¥0'
  }
  return value >= 1 ? '¥' + value.toFixed(2) : '¥' + value.toFixed(4)
}

import type { Session } from './types.js'

/** 每个会话的累计用量（独立于会话缓存，避免只统计已加载的 5 个会话）。 */
export type SessionUsageTotals = {
  input: number
  cacheRead: number
  cacheWrite: number
  output: number
  requests: number
}

const KEY = 'chat-demo:usage-totals'

const readAll = (): Record<string, SessionUsageTotals> => {
  try {
    const raw = localStorage.getItem(KEY)
    return raw ? (JSON.parse(raw) as Record<string, SessionUsageTotals>) : {}
  } catch {
    return {}
  }
}

const writeAll = (all: Record<string, SessionUsageTotals>) => {
  try {
    localStorage.setItem(KEY, JSON.stringify(all))
  } catch {
    // 存储失败不影响对话
  }
}

/** 从会话消息里的 api usage 汇总并持久化（每次保存会话时调用）。 */
export const recordSessionUsage = (session: Session) => {
  const totals: SessionUsageTotals = { input: 0, cacheRead: 0, cacheWrite: 0, output: 0, requests: 0 }
  for (const message of session.messages) {
    if (message.role !== 'assistant' || !message.usage) {
      continue
    }
    totals.input += message.usage.input_tokens
    totals.cacheRead += message.usage.cache_read_input_tokens
    totals.cacheWrite += message.usage.cache_creation_input_tokens
    totals.output += message.usage.output_tokens
    totals.requests += 1
  }
  const all = readAll()
  if (totals.requests === 0) {
    // oxlint-disable-next-line typescript/no-dynamic-delete -- 会话 id 作键
    delete all[session.id]
  } else {
    all[session.id] = totals
  }
  writeAll(all)
}

export const readSessionUsage = (): Record<string, SessionUsageTotals> => readAll()

export const removeSessionUsage = (sessionId: string) => {
  const all = readAll()
  if (!Object.hasOwn(all, sessionId)) {
    return
  }
  // oxlint-disable-next-line typescript/no-dynamic-delete -- 会话 id 作键
  delete all[sessionId]
  writeAll(all)
}

export const clearSessionUsage = () => writeAll({})

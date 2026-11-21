import { useTranslate } from '../../i18n/index.js'
import { getSession, useChatStore } from '../../services/chat/store.js'
import { contextPressure, emptyUsage, formatTokens, sessionUsage } from '../../services/chat/usage.js'
import { DshIcon } from './StatusIcons.js'
import styles from './StatusLine.module.css'

/** 输入框下方的 DSH 风格状态行：模型 / 上下文占用 / tokens / 缓存命中 / 生成性能。 */
export const StatusLine = ({ sessionId }: { sessionId: string | null }) => {
  const t = useTranslate()
  const streaming = useChatStore((state) => (sessionId ? Boolean(state.streaming[sessionId]) : false))
  const cachedSession = useChatStore((state) => (sessionId ? state.sessionCache[sessionId] : undefined))
  const session = cachedSession ?? (sessionId ? (getSession(sessionId) ?? null) : null)
  const usage = session ? sessionUsage(session) : emptyUsage()
  const pressure = contextPressure(session)
  const lastReply = session
    ? [...session.messages].reverse().find((message) => message.role === 'assistant')
    : undefined
  const model = lastReply && lastReply.role === 'assistant' ? (lastReply.model ?? 'deepseek-flash') : 'deepseek-flash'
  const hasMessages = Boolean(session && session.messages.length > 0)
  const hasUsage = usage.requests > 0
  const percent = Math.round(pressure.ratio * 100)
  return (
    <div className={styles.statusLine} aria-live="polite">
      <span className={styles.model}>
        <DshIcon name="sparkle" size={12} />
        {model}
      </span>
      {hasMessages ? (
        <span>
          <DshIcon name="context-injection-outline" size={12} />
          {t('statusContext')} {formatTokens(pressure.tokens)} tokens / {formatTokens(pressure.window)} ({percent}%)
        </span>
      ) : null}
      {hasUsage ? (
        <span>
          <DshIcon name="data-outline" size={12} />
          {formatTokens(usage.input + usage.cacheWrite + usage.cacheRead + usage.output)} tokens
        </span>
      ) : null}
      {streaming ? (
        <span className={styles.streaming}>
          <DshIcon name="loading-outline" size={12} />
          {t('statusGenerating')}
        </span>
      ) : null}
    </div>
  )
}

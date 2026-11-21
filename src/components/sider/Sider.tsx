import clsx from 'clsx'
import { useEffect, useMemo, useState } from 'react'
import { useTranslate } from '../../i18n/index.js'
import { IconSettingsOutline16, NewChatIcon } from '../base/icons.js'
import { groupSessions } from '../../services/chat/sessionGroups.js'
import { getSession, newChat, removeSession, selectSession, useChatStore } from '../../services/chat/store.js'
import type { SessionSummary } from '../../services/chat/types.js'
import { Button } from '../base/Button.js'
import { Modal } from '../base/Modal.js'
import { showToast } from '../base/toastState.js'
import { openSettings } from '../settings/settingsDialogState.js'
import { DshIcon } from '../chatInput/StatusIcons.js'
import { closeMobileSider, useMobileSider } from './mobileSiderState.js'
import { SessionItem } from './SessionItem.js'
import styles from './Sider.module.css'

/** 展开的侧边栏：名称与收起按钮、开启新对话、按时间分组的会话列表、底部设置；移动端放在抽屉里，收起按钮关闭抽屉 */
export const Sider = ({ mobile = false }: { mobile?: boolean }) => {
  const t = useTranslate()
  const sessions = useChatStore((state) => state.sessions)
  const activeId = useChatStore((state) => state.activeId)
  const [now, setNow] = useState(Date.now)
  const [query, setQuery] = useState('')
  const [contentHits, setContentHits] = useState<{ id: string; snippet: string }[]>([])
  const [searching, setSearching] = useState(false)
  const titleHits = useMemo(() => {
    const keyword = query.trim().toLowerCase()
    if (!keyword) {
      return []
    }
    return sessions
      .filter((session) => session.title.toLowerCase().includes(keyword))
      .sort((a, b) => b.updatedAt - a.updatedAt)
      .map((session) => ({ id: session.id, snippet: '' }))
  }, [sessions, query])

  useEffect(() => {
    const keyword = query.trim()
    if (!keyword) {
      setContentHits([])
      setSearching(false)
      return
    }
    let cancelled = false
    setSearching(true)
    // DSH 同款：250ms 防抖 + 查询变化时丢弃旧结果
    const timer = window.setTimeout(() => {
      const lower = keyword.toLowerCase()
      const hits: { id: string; snippet: string }[] = []
      for (const summary of sessions) {
        if (hits.length >= 20) {
          break
        }
        if (summary.title.toLowerCase().includes(lower)) {
          continue
        }
        const session = getSession(summary.id)
        if (!session) {
          continue
        }
        for (const message of session.messages) {
          const text =
            message.role === 'user'
              ? message.content
              : message.blocks
                  .filter((block) => block.type === 'text')
                  .map((block) => block.text)
                  .join('\n')
          const index = text.toLowerCase().indexOf(lower)
          if (index >= 0) {
            const from = Math.max(0, index - 30)
            const to = Math.min(text.length, index + keyword.length + 50)
            const prefix = from > 0 ? '…' : ''
            const suffix = to < text.length ? '…' : ''
            hits.push({ id: summary.id, snippet: prefix + text.slice(from, to) + suffix })
            break
          }
        }
      }
      if (!cancelled) {
        setContentHits(hits)
        setSearching(false)
      }
    }, 250)
    return () => {
      cancelled = true
      window.clearTimeout(timer)
    }
  }, [query, sessions])

  const searchResults = useMemo(() => {
    if (!query.trim()) {
      return []
    }
    const merged = [...titleHits]
    const seen = new Set(merged.map((hit) => hit.id))
    for (const hit of contentHits) {
      if (!seen.has(hit.id) && merged.length < 20) {
        seen.add(hit.id)
        merged.push(hit)
      }
    }
    return merged
  }, [query, titleHits, contentHits])

  const highlight = (text: string) => {
    const keyword = query.trim()
    if (!keyword) {
      return text
    }
    const index = text.toLowerCase().indexOf(keyword.toLowerCase())
    if (index < 0) {
      return text
    }
    return (
      <>
        {text.slice(0, index)}
        <mark className={styles.mark}>{text.slice(index, index + keyword.length)}</mark>
        {text.slice(index + keyword.length)}
      </>
    )
  }

  const groups = useMemo(() => {
    const grouped = groupSessions(sessions, now, t)
    const keyword = query.trim().toLowerCase()
    if (!keyword) {
      return grouped
    }
    return grouped
      .map((group) => ({
        ...group,
        sessions: group.sessions.filter((session) => session.title.toLowerCase().includes(keyword)),
      }))
      .filter((group) => group.sessions.length > 0)
  }, [sessions, now, t, query])
  const [deleting, setDeleting] = useState<SessionSummary | null>(null)
  const [deletePending, setDeletePending] = useState(false)

  useEffect(() => {
    let timer: ReturnType<typeof setTimeout>
    const refresh = () => {
      clearTimeout(timer)
      const time = Date.now()
      setNow(time)
      const midnight = new Date(time).setHours(24, 0, 0, 0)
      timer = setTimeout(refresh, midnight - time)
    }
    const onVisibilityChange = () => {
      if (document.visibilityState === 'visible') {
        refresh()
      }
    }
    refresh()
    document.addEventListener('visibilitychange', onVisibilityChange)
    window.addEventListener('focus', refresh)
    return () => {
      clearTimeout(timer)
      document.removeEventListener('visibilitychange', onVisibilityChange)
      window.removeEventListener('focus', refresh)
    }
  }, [])

  return (
    <div className={clsx(styles.sider, mobile && styles.mobile)}>
      <button
        type="button"
        className={styles.newChat}
        onClick={() => {
          newChat()
          closeMobileSider()
        }}
      >
        <span className={styles.newChatIcon}>
          <NewChatIcon />
        </span>
        {t('newChat')}
      </button>
      <div className={styles.searchBox}>
        <span className={styles.searchIcon}>
          <DshIcon name="search-outline" size={14} />
        </span>
        <input
          className={styles.search}
          type="search"
          value={query}
          placeholder={t('searchSessionsPlaceholder')}
          aria-label={t('searchSessionsPlaceholder')}
          onChange={(event) => setQuery(event.target.value)}
        />
        {query ? (
          <button
            type="button"
            className={styles.searchClear}
            aria-label={t('cancel')}
            onClick={() => setQuery('')}
          >
            <DshIcon name="close-outline" size={14} />
          </button>
        ) : null}
      </div>
      <div className={styles.listWrap}>
        <div className={styles.list}>
          {query.trim() ? (
            <>
              {searchResults.map((hit) => {
                const session = sessions.find((item) => item.id === hit.id)
                return (
                  <button
                    key={hit.id}
                    type="button"
                    className={styles.resultRow}
                    onClick={() => {
                      selectSession(hit.id)
                      setQuery('')
                      closeMobileSider()
                    }}
                  >
                    <span className={styles.resultTitle}>{session ? highlight(session.title) : hit.id}</span>
                    {hit.snippet ? <span className={styles.resultSnippet}>{highlight(hit.snippet)}</span> : null}
                  </button>
                )
              })}
              {searchResults.length === 0 && !searching ? (
                <div className={styles.empty}>{t('searchSessionsEmpty')}</div>
              ) : null}
              {searching ? <div className={styles.searchHint}>{t('searchSessionsSearching')}</div> : null}
              {searchResults.length >= 20 ? (
                <div className={styles.searchHint}>{t('searchSessionsNarrow')}</div>
              ) : null}
            </>
          ) : (
            <>
              {groups.map((group) => (
                <div key={group.key} className={styles.section}>
                  <div className={styles.sectionHeader}>{group.label}</div>
                  {group.sessions.map((session) => (
                    <SessionItem
                      key={session.id}
                      session={session}
                      active={session.id === activeId}
                      onDelete={() => setDeleting(session)}
                    />
                  ))}
                </div>
              ))}
            </>
          )}
        </div>
      </div>
      <button
        type="button"
        className={styles.fab}
        aria-label={t('newChat')}
        onClick={() => {
          newChat()
          closeMobileSider()
        }}
      >
        <NewChatIcon />
      </button>
      <div className={styles.bottom}>
        <button type="button" className={styles.settings} onClick={() => openSettings('general')}>
          <span className={styles.settingsIcon}>
            <IconSettingsOutline16 />
          </span>
          <span className={styles.settingsLabel}>{t('settings')}</span>
        </button>
      </div>
      {deleting ? (
        <Modal
          title={t('deleteSessionModalContentTitle')}
          onClose={() => {
            if (!deletePending) {
              setDeleting(null)
            }
          }}
          footer={
            <>
              <Button variant="outlined" disabled={deletePending} onClick={() => setDeleting(null)}>
                {t('cancel')}
              </Button>
              <Button
                variant="danger"
                disabled={deletePending}
                onClick={async () => {
                  setDeletePending(true)
                  try {
                    if (await removeSession(deleting.id)) {
                      setDeleting(null)
                    } else {
                      showToast({ key: 'sessionDeleteFailed', type: 'error' })
                    }
                  } finally {
                    setDeletePending(false)
                  }
                }}
              >
                {t('delete')}
              </Button>
            </>
          }
        >
          {t('deleteSessionWithFilesDescription')}
        </Modal>
      ) : null}
    </div>
  )
}

/** 移动端抽屉：浮在页面上方，点空白区域关闭。 */
export const MobileSider = () => {
  const open = useMobileSider((state) => state.open)
  const drag = useMobileSider((state) => state.drag)
  const [visibleSider, setVisibleSider] = useState(open || drag > 0)
  useEffect(() => {
    if (open || drag > 0) {
      setVisibleSider(true)
      return
    }
    // 关闭时保留挂载 240ms，让位移动画跑完再卸载
    const timer = window.setTimeout(() => setVisibleSider(false), 240)
    return () => window.clearTimeout(timer)
  }, [open, drag])
  const fraction = open ? 1 : drag
  if (!visibleSider) {
    return null
  }
  return (
    <>
      <div
        className={styles.drawerMask}
        style={{ opacity: fraction, transition: drag > 0 ? 'none' : 'opacity .22s ease' }}
        onClick={closeMobileSider}
      />
      <div
        className={styles.drawer}
        style={{
          transform: `translateX(${(fraction - 1) * 100}%)`,
          transition: drag > 0 ? 'none' : 'transform .22s ease',
        }}
      >
        <Sider mobile />
      </div>
    </>
  )
}

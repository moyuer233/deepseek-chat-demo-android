import clsx from 'clsx'
import { useState } from 'react'
import {
  cancelPreview,
  confirmImportAndCreateKey,
  openRecharge,
  resetAccountFlow,
  startLoginAndImport,
  useAccount,
  type AccountPhase,
} from '../../services/deepseek/account.js'
import type { ArchivePreview } from '../../services/deepseek/archive.js'
import { useSettings } from '../../services/settings.js'
import { useTranslate, type TranslationKey } from '../../i18n/index.js'
import { Button } from '../base/Button.js'
import { Modal } from '../base/Modal.js'
import styles from './AccountPane.module.css'

/** 预览清单先渲染的条数，避免几千条一次性插进 DOM。 */
const previewLimit = 200

/** 进行中的阶段对应的进度文案。 */
const progressKeys: Partial<Record<AccountPhase, TranslationKey>> = {
  chatLogin: 'accountProgressChatLogin',
  exporting: 'accountProgressExporting',
  importing: 'accountProgressImporting',
  platformLogin: 'accountProgressPlatformLogin',
  creatingKey: 'accountProgressCreatingKey',
}

/** updated_at 是 ISO 时间字符串；解析不出来就留空。 */
const formatUpdatedAt = (value: string) => {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return ''
  }
  const pad = (part: number) => String(part).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

/** 账户：登录 chat 导入会话并自动建 Key、充值入口；导入前先用弹窗确认清单。 */
export const AccountPane = () => {
  const t = useTranslate()
  const apiKey = useSettings((state) => state.apiKey)
  const phase = useAccount((state) => state.phase)
  const error = useAccount((state) => state.error)
  const preview = useAccount((state) => state.preview)
  const importedCount = useAccount((state) => state.importedCount)
  const [termsOpen, setTermsOpen] = useState(false)
  const progressKey = progressKeys[phase]
  // 预览清单挂在弹窗上，下面的按钮同样要停住
  const busy = Boolean(progressKey) || phase === 'preview'

  let status = apiKey ? t('accountLoggedIn') : t('accountLoginHint')
  if (progressKey) {
    status = t(progressKey)
  } else if (phase === 'done') {
    status = t('accountImported', { count: importedCount ?? 0 })
  } else if (phase === 'error') {
    status = error ?? t('unknownError')
  }

  return (
    <div className={styles.pane} aria-busy={busy}>
      <div className={clsx(styles.status, phase === 'error' && styles.error)}>{status}</div>
      <div className={styles.actions}>
        {phase === 'done' ? (
          <Button size="m" onClick={resetAccountFlow}>
            {t('accountDone')}
          </Button>
        ) : phase === 'error' ? (
          <Button size="m" onClick={resetAccountFlow}>
            {t('retry')}
          </Button>
        ) : (
          <>
            <Button size="m" disabled={busy} onClick={() => setTermsOpen(true)}>
              {apiKey ? t('accountReimportButton') : t('accountLoginButton')}
            </Button>
            <Button size="m" variant="outlined" disabled={busy} onClick={() => void openRecharge()}>
              {t('accountRecharge')}
            </Button>
          </>
        )}
      </div>
      {phase === 'done' || phase === 'error' ? null : (
        <div className={styles.hint}>{t('accountRechargeHint')}</div>
      )}
      {phase === 'preview' && preview ? <PreviewDialog preview={preview} /> : null}
      {termsOpen ? (
        <Modal
          title={t('accountTermsTitle')}
          onClose={() => setTermsOpen(false)}
          footer={
            <>
              <Button variant="outlined" size="m" onClick={() => setTermsOpen(false)}>
                {t('cancel')}
              </Button>
              <Button
                size="m"
                autoFocus
                onClick={() => {
                  setTermsOpen(false)
                  void startLoginAndImport()
                }}
              >
                {t('accountTermsAgree')}
              </Button>
            </>
          }
        >
          <div className={styles.terms}>{t('accountTermsBody')}</div>
        </Modal>
      ) : null}
    </div>
  )
}

/** 导入前的会话清单：确认后替换本机会话并继续建 Key。 */
const PreviewDialog = ({ preview }: { preview: ArchivePreview }) => {
  const t = useTranslate()
  return (
    <Modal
      title={t('accountPreviewTitle', { count: preview.sessions.length })}
      className={styles.preview}
      mainClassName={styles.previewMain}
      onClose={cancelPreview}
      footer={
        <>
          <Button variant="outlined" size="m" autoFocus onClick={cancelPreview}>
            {t('cancel')}
          </Button>
          <Button size="m" onClick={() => void confirmImportAndCreateKey()}>
            {t('accountPreviewConfirm')}
          </Button>
        </>
      }
    >
      <ul className={styles.previewList}>
        {preview.sessions.slice(0, previewLimit).map((session) => {
          const date = formatUpdatedAt(session.updatedAt)
          return (
            <li key={session.id} className={styles.previewItem}>
              <span className={styles.previewTitle}>
                {session.title ?? t('accountPreviewUntitled')}
              </span>
              <span className={styles.previewMeta}>
                {date ? `${date} · ` : ''}
                {t('accountPreviewMessageCount', { count: session.messageCount })}
              </span>
            </li>
          )
        })}
      </ul>
      <div className={styles.previewWarning}>{t('accountPreviewWarning')}</div>
    </Modal>
  )
}

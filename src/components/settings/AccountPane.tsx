import { Capacitor } from '@capacitor/core'
import clsx from 'clsx'
import { useEffect, useState } from 'react'
import {
  cancelPreview,
  confirmImportAndCreateKey,
  openRecharge,
  resetAccountFlow,
  useAccount,
  type AccountPhase,
} from '../../services/deepseek/account.js'
import type { ArchivePreview } from '../../services/deepseek/archive.js'
import { DeepSeekAccount } from '../../services/deepseek/plugin.js'
import { setAccountToken, setApiKey, useSettings } from '../../services/settings.js'
import { useTranslate, type TranslationKey } from '../../i18n/index.js'
import { Button } from '../base/Button.js'
import { Modal } from '../base/Modal.js'
import { fetchBalance, currencySymbol, type Balance } from '../../services/deepseek/balance.js'
import { DshIcon } from '../chatInput/StatusIcons.js'
import { useChatStore } from '../../services/chat/store.js'
import { nativeShell, shellPlatformAuthLogin } from '../../services/deepseek/shell.js'
import { showToast } from '../base/toastState.js'
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

/** 账户：登录 chat 导入会话、账号授权、充值入口；导入前先用弹窗确认清单。 */
export const AccountPane = () => {
  const t = useTranslate()
  const apiKey = useSettings((state) => state.apiKey)
  const accountToken = useSettings((state) => state.accountToken)
  const phase = useAccount((state) => state.phase)
  const error = useAccount((state) => state.error)
  const preview = useAccount((state) => state.preview)
  const importedCount = useAccount((state) => state.importedCount)
  const [logoutOpen, setLogoutOpen] = useState(false)
  const [balance, setBalance] = useState<Balance | null>(null)
  const [balanceFailed, setBalanceFailed] = useState(false)
  const streaming = useChatStore((state) => Object.keys(state.streaming).length > 0)

  const loadBalance = () => {
    setBalanceFailed(false)
    void fetchBalance()
      .then((data) => {
        setBalance(data)
        setBalanceFailed(!data)
      })
      .catch(() => setBalanceFailed(true))
  }

  useEffect(() => {
    loadBalance()
    const onVisible = () => {
      if (document.visibilityState === 'visible') {
        loadBalance()
        // 通知头部余额一起刷新（充值返回等场景）
        window.dispatchEvent(new Event('deepseek-balance-refresh'))
      }
    }
    window.addEventListener('focus', onVisible)
    document.addEventListener('visibilitychange', onVisible)
    return () => {
      window.removeEventListener('focus', onVisible)
      document.removeEventListener('visibilitychange', onVisible)
    }
  }, [])
  const progressKey = progressKeys[phase]
  // 预览清单挂在弹窗上，下面的按钮同样要停住
  const busy = Boolean(progressKey) || phase === 'preview'

  const signedIn = Boolean(apiKey || accountToken)
  let status = apiKey
    ? t('accountLoggedIn')
    : accountToken
      ? t('accountLoggedInAccount')
      : t('accountLoginHint')
  if (progressKey) {
    status = t(progressKey)
  } else if (phase === 'done') {
    status = t('accountImported', { count: importedCount ?? 0 })
  } else if (phase === 'error') {
    status = error ?? t('unknownError')
  }

  return (
    <div className={styles.pane} aria-busy={busy}>
      <div className={styles.card}>
        <span className={styles.avatar}>
          <DshIcon name="user-outline" size={20} />
        </span>
        <span className={styles.identity}>
          <span className={styles.identityName}>
            {accountToken
              ? t('accountAuthAccount')
              : apiKey
                ? t('accountAuthApiKey')
                : t('accountNotLoggedIn')}
          </span>
          <span className={styles.identitySub}>
            {accountToken
              ? t('accountAuthAccountLabel')
              : apiKey
                ? t('accountAuthApiKeyLabel')
                : t('accountLoginHint')}
          </span>
        </span>
      </div>

      <div className={styles.balanceCard}>
        <span className={styles.balanceLabel}>
          <DshIcon name="layers-outline" size={14} />
          {t('accountBalance')}
        </span>
        {balanceFailed ? (
          <button type="button" className={styles.balanceRetry} onClick={loadBalance}>
            {t('accountBalanceRetry')}
          </button>
        ) : (
          <span className={styles.balanceValue}>
            <span className={balance && balance.available && balance.total > 0.005 ? styles.dotOk : styles.dotError} />
            {balance ? currencySymbol(balance.currency) + balance.total.toFixed(2) : '—'}
            {balance && balance.granted > 0
              ? `（${t('balanceBonus')} ${currencySymbol(balance.currency)}${balance.granted.toFixed(2)}）`
              : null}
          </span>
        )}
      </div>

      {progressKey || phase === 'error' || phase === 'done' ? (
        <div
          className={clsx(styles.status, phase === 'error' && styles.error)}
          role={phase === 'error' ? 'alert' : 'status'}
          aria-live="polite"
        >
          {status}
        </div>
      ) : null}
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
            {signedIn ? null : (
              <Button
                size="m"
                disabled={busy}
                onClick={() => {
                  void shellPlatformAuthLogin()
                    .then(({ token }) => {
                      setAccountToken(token)
                      loadBalance()
                    })
                    .catch((error) => showToast(error instanceof Error ? error.message : String(error)))
                }}
              >
                {t('accountLoginButton')}
              </Button>
            )}
            <Button size="m" variant="outlined" disabled={busy} onClick={() => void openRecharge()}>
              {t('accountRecharge')}
            </Button>
            <Button
              size="m"
              variant="outlined"
              disabled={busy}
              onClick={() => nativeShell()?.openExternal?.('https://platform.deepseek.com/usage')}
            >
              {t('accountMoreInfo')}
            </Button>
            {signedIn ? (
              <Button size="m" variant="outlined" disabled={busy} onClick={() => setLogoutOpen(true)}>
                {t('accountLogout')}
              </Button>
            ) : null}
            {Capacitor.isNativePlatform() ? (
              <Button
                size="m"
                variant="outlined"
                disabled={busy}
                onClick={() => void DeepSeekAccount.openAccessibilitySettings()}
              >
                {t('accountScanAssist')}
              </Button>
            ) : null}
          </>
        )}
      </div>
      {phase === 'done' || phase === 'error' ? null : (
        <div className={styles.hint}>{t('accountRechargeHint')}</div>
      )}
      {phase === 'preview' && preview ? <PreviewDialog preview={preview} /> : null}
      {logoutOpen ? (
        <Modal
          title={t('accountLogout')}
          onClose={() => setLogoutOpen(false)}
          footer={
            <>
              <Button variant="outlined" size="m" onClick={() => setLogoutOpen(false)}>
                {t('cancel')}
              </Button>
              <Button
                variant="danger"
                size="m"
                onClick={() => {
                  setLogoutOpen(false)
                  setApiKey(null)
                  setAccountToken(null)
                  setBalance(null)
                }}
              >
                {t('accountLogoutConfirm')}
              </Button>
            </>
          }
        >
          <div className={styles.terms}>
            {streaming ? t('accountLogoutActive') : t('accountLogoutBody')}
          </div>
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

import { useEffect, useState } from 'react'
import { openRecharge } from '../../services/deepseek/account.js'
import { DeepSeekAccount } from '../../services/deepseek/plugin.js'
import { setAccountToken, setApiKey, setTopupAssist, useSettings } from '../../services/settings.js'
import { useTranslate } from '../../i18n/index.js'
import { Button } from '../base/Button.js'
import { Dialog } from '../base/Dialog.js'
import { fetchBalance, currencySymbol, type Balance } from '../../services/deepseek/balance.js'
import { verifyApiKey } from '../../services/api/client.js'
import { DshIcon } from '../chatInput/StatusIcons.js'
import { useChatStore } from '../../services/chat/store.js'
import { isNativeShell, nativeShell, openExternalUrl, shellPlatformAuthLogin } from '../../services/deepseek/shell.js'
import { showToast } from '../base/toastState.js'
import { pushBackHandler } from '../../services/backHandlers.js'
import styles from './AccountPane.module.css'
import nsStyles from './NativeSettings.module.css'
import { SettingsRow } from './SettingsRow.js'

const KeyIcon = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="8" cy="12" r="3.4" />
    <path d="M11.4 12H20M17 12v3M14 12v2" />
  </svg>
)

const LogoutIcon = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
    <path d="M14 4h4a1 1 0 0 1 1 1v14a1 1 0 0 1-1 1h-4" />
    <path d="M10 8l-4 4 4 4M6 12h9" />
  </svg>
)

/** 账户：账号授权、API Key 与充值入口。 */
export const AccountPane = () => {
  const t = useTranslate()
  const apiKey = useSettings((state) => state.apiKey)
  const topupAssist = useSettings((state) => state.topupAssist)
  const accountToken = useSettings((state) => state.accountToken)
  const [logoutOpen, setLogoutOpen] = useState(false)
  const [apiKeyOpen, setApiKeyOpen] = useState(false)
  const [apiKeyInput, setApiKeyInput] = useState('')
  const [balance, setBalance] = useState<Balance | null>(null)
  const [balanceFailed, setBalanceFailed] = useState(false)
  const [scanAssist, setScanAssist] = useState(false)
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

  // 系统返回：先关 API Key / 退出登录弹窗
  useEffect(() => {
    if (!apiKeyOpen && !logoutOpen) {
      return
    }
    return pushBackHandler(() => {
      if (logoutOpen) {
        setLogoutOpen(false)
        return true
      }
      setApiKeyOpen(false)
      return true
    })
  }, [apiKeyOpen, logoutOpen])

  const refreshScanAssist = () => {
    void DeepSeekAccount.isScanAssistEnabled()
      .then(setScanAssist)
      .catch(() => setScanAssist(false))
  }

  useEffect(() => {
    loadBalance()
    refreshScanAssist()
    const onVisible = () => {
      if (document.visibilityState === 'visible') {
        loadBalance()
        // 从系统无障碍设置返回后刷新开关状态
        refreshScanAssist()
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
  const signedIn = Boolean(apiKey || accountToken)

  return (
    <div className={styles.pane}>
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

      <div className={nsStyles.card}>
        {signedIn ? null : (
          <SettingsRow
            icon={<DshIcon name="user-outline" size={16} />}
            title={t('accountSignIn')}
            onClick={() => {
              void shellPlatformAuthLogin()
                .then(({ token }) => {
                  setAccountToken(token)
                  loadBalance()
                })
                .catch((error) => showToast(error instanceof Error ? error.message : String(error)))
            }}
          />
        )}
        <SettingsRow
          icon={<DshIcon name="layers-outline" size={16} />}
          title={t('accountRecharge')}
          onClick={() => void openRecharge(topupAssist)}
        />
        {isNativeShell() ? (
          <button
            type="button"
            role="switch"
            aria-checked={topupAssist}
            className={nsStyles.row}
            onClick={() => {
              const next = !topupAssist
              setTopupAssist(next)
              showToast(t(next ? 'accountTopupAssistOn' : 'accountTopupAssistOff'))
            }}
          >
            <span className={nsStyles.rowIcon} aria-hidden>
              <DshIcon name="settings-outline" size={16} />
            </span>
            <span className={nsStyles.rowTitle}>{t('accountTopupAssist')}</span>
            <span className={topupAssist ? styles.toggle + ' ' + styles.toggleOn : styles.toggle} aria-hidden>
              <span className={styles.toggleKnob} />
            </span>
          </button>
        ) : null}
        <SettingsRow
          icon={<DshIcon name="link-outline" size={16} />}
          title={t('accountMoreInfo')}
          onClick={() => {
            const url = 'https://platform.deepseek.com/usage'
            if (isNativeShell()) {
              void DeepSeekAccount.openPlatformPage({ url })
            } else {
              openExternalUrl(url)
            }
          }}
        />
        {isNativeShell() ? (
          <button
            type="button"
            role="switch"
            aria-checked={scanAssist}
            className={nsStyles.row}
            onClick={() => {
              showToast(t(scanAssist ? 'accountScanAssistEnabledHint' : 'accountScanAssistHint'))
              void DeepSeekAccount.openAccessibilitySettings()
            }}
          >
            <span className={nsStyles.rowIcon} aria-hidden>
              <DshIcon name="settings-outline" size={16} />
            </span>
            <span className={nsStyles.rowTitle}>{t('accountScanAssist')}</span>
            <span className={scanAssist ? `${styles.toggle} ${styles.toggleOn}` : styles.toggle} aria-hidden>
              <span className={styles.toggleKnob} />
            </span>
          </button>
        ) : null}
        <SettingsRow
          icon={<DshIcon name="settings-outline" size={16} />}
          title={t('apiKeyLabel')}
          value={apiKey ? t('apiKeySetTail', { tail: apiKey.slice(-4) }) : t('apiKeyNotSet')}
          onClick={() => {
            setApiKeyInput(apiKey ?? '')
            setApiKeyOpen(true)
          }}
        />
        {signedIn ? (
          <SettingsRow
            icon={<DshIcon name="close-outline" size={16} />}
            title={t('accountLogout')}
            danger
            onClick={() => setLogoutOpen(true)}
          />
        ) : null}
      </div>
      <div className={styles.hint}>{t('accountRechargeHint')}</div>
      {apiKeyOpen ? (
        <Dialog
          title={t('apiKeyLabel')}
          icon={<KeyIcon />}
          lead={t('apiKeyHint')}
          onClose={() => setApiKeyOpen(false)}
          footer={
            <>
              {apiKey ? (
                <Button
                  variant="outlined"
                  size="m"
                  onClick={() => {
                    setApiKey(null)
                    setApiKeyInput('')
                    setApiKeyOpen(false)
                    showToast({ key: 'apiKeyCleared' })
                  }}
                >
                  {t('apiKeyClear')}
                </Button>
              ) : null}
              <Button
                size="m"
                onClick={() => {
                  const value = apiKeyInput.trim()
                  if (!value) {
                    setApiKey(null)
                    setApiKeyOpen(false)
                    showToast({ key: 'apiKeyCleared' })
                    loadBalance()
                    return
                  }
                  void verifyApiKey(value)
                    .then(() => {
                      setApiKey(value)
                      setApiKeyOpen(false)
                      showToast({ key: 'apiKeySaved' })
                      loadBalance()
                    })
                    .catch(() => showToast(t('replyInvalidApiKey')))
                }}
              >
                {t('apiKeySave')}
              </Button>
            </>
          }
        >
          <input
            className={styles.apiKeyInput}
            type="password"
            value={apiKeyInput}
            placeholder="sk-..."
            autoComplete="off"
            onChange={(event) => setApiKeyInput(event.target.value)}
          />
        </Dialog>
      ) : null}
      {logoutOpen ? (
        <Dialog
          title={t('accountLogout')}
          icon={<LogoutIcon />}
          tone="danger"
          lead={streaming ? t('accountLogoutActive') : t('accountLogoutBody')}
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
                  nativeShell()?.clearToken?.()
                  setApiKey(null)
                  setAccountToken(null)
                  setBalance(null)
                }}
              >
                {t('accountLogoutConfirm')}
              </Button>
            </>
          }
        />
      ) : null}
    </div>
  )
}

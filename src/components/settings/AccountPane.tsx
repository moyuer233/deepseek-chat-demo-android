import { useEffect, useState } from 'react'
import { openRecharge } from '../../services/deepseek/account.js'
import { DeepSeekAccount } from '../../services/deepseek/plugin.js'
import { setAccountToken, setApiKey, useSettings } from '../../services/settings.js'
import { useTranslate } from '../../i18n/index.js'
import { Button } from '../base/Button.js'
import { Modal } from '../base/Modal.js'
import { fetchBalance, currencySymbol, type Balance } from '../../services/deepseek/balance.js'
import { verifyApiKey } from '../../services/api/client.js'
import { DshIcon } from '../chatInput/StatusIcons.js'
import { useChatStore } from '../../services/chat/store.js'
import { isNativeShell, nativeShell, shellPlatformAuthLogin } from '../../services/deepseek/shell.js'
import { showToast } from '../base/toastState.js'
import styles from './AccountPane.module.css'
import nsStyles from './NativeSettings.module.css'
import { SettingsRow } from './SettingsRow.js'

/** 账户：账号授权、API Key 与充值入口。 */
export const AccountPane = () => {
  const t = useTranslate()
  const apiKey = useSettings((state) => state.apiKey)
  const accountToken = useSettings((state) => state.accountToken)
  const [logoutOpen, setLogoutOpen] = useState(false)
  const [apiKeyOpen, setApiKeyOpen] = useState(false)
  const [apiKeyInput, setApiKeyInput] = useState('')
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
          icon={<DshIcon name="settings-outline" size={16} />}
          title={t('apiKeyLabel')}
          value={apiKey ? t('apiKeySetTail', { tail: apiKey.slice(-4) }) : t('apiKeyNotSet')}
          onClick={() => {
            setApiKeyInput(apiKey ?? '')
            setApiKeyOpen(true)
          }}
        />
        <SettingsRow
          icon={<DshIcon name="layers-outline" size={16} />}
          title={t('accountRecharge')}
          onClick={() => void openRecharge()}
        />
        <SettingsRow
          icon={<DshIcon name="link-outline" size={16} />}
          title={t('accountMoreInfo')}
          onClick={() => nativeShell()?.openExternal?.('https://platform.deepseek.com/usage')}
        />
        {signedIn ? (
          <SettingsRow
            icon={<DshIcon name="close-outline" size={16} />}
            title={t('accountLogout')}
            danger
            onClick={() => setLogoutOpen(true)}
          />
        ) : null}
        {isNativeShell() ? (
          <SettingsRow
            icon={<DshIcon name="settings-outline" size={16} />}
            title={t('accountScanAssist')}
            onClick={() => void DeepSeekAccount.openAccessibilitySettings()}
          />
        ) : null}
      </div>
      <div className={styles.hint}>{t('accountRechargeHint')}</div>
      {apiKeyOpen ? (
        <Modal
          title={t('apiKeyLabel')}
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
          <div className={styles.apiKeyForm}>
            <p className={styles.terms}>{t('apiKeyHint')}</p>
            <input
              className={styles.apiKeyInput}
              type="password"
              value={apiKeyInput}
              placeholder="sk-..."
              autoComplete="off"
              onChange={(event) => setApiKeyInput(event.target.value)}
            />
          </div>
        </Modal>
      ) : null}
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
        >
          <div className={styles.terms}>
            {streaming ? t('accountLogoutActive') : t('accountLogoutBody')}
          </div>
        </Modal>
      ) : null}
    </div>
  )
}

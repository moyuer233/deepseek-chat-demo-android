import { useIsMobile } from '../../app/viewport.js'
import { useTranslate } from '../../i18n/index.js'
import { useSettings } from '../../services/settings.js'
import { openSettings } from '../settings/settingsDialogState.js'
import { startPlatformOAuth } from '../../services/deepseek/account.js'
import { isNativeShell } from '../../services/deepseek/shell.js'
import { showToast } from '../base/toastState.js'
import { ChatInput } from '../chatInput/ChatInput.js'
import { MobileHeader } from './MobileHeader.js'
import styles from './Welcome.module.css'

/** 新对话：问候语 + 输入框。桌面端居中；移动端输入框吸底 */
export const Welcome = () => {
  const t = useTranslate()
  const isMobile = useIsMobile()
  const apiKey = useSettings((state) => state.apiKey)
  const accountToken = useSettings((state) => state.accountToken)
  return (
    <div className={styles.container}>
      {isMobile ? <MobileHeader /> : null}
      <div className={styles.welcome}>
        <div className={styles.greeting}>{t('welcomeMessageChat250808')}</div>
        <ChatInput sessionId={null} />
        {!apiKey && !accountToken ? (
          <button
            type="button"
            className={styles.login}
            onClick={() => {
              if (isNativeShell()) {
                void startPlatformOAuth()
                  .then(() => showToast('已授权 DeepSeek 账号'))
                  .catch((error: unknown) => showToast(error instanceof Error ? error.message : String(error)))
              } else {
                openSettings('account')
              }
            }}
          >
            {t('accountLoginShort')}
          </button>
        ) : null}
        {isMobile ? <div className={styles.caveat}>{t('contentCaveat')}</div> : null}
      </div>
    </div>
  )
}

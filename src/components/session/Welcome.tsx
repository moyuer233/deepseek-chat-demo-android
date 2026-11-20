import { useIsMobile } from '../../app/viewport.js'
import { useTranslate } from '../../i18n/index.js'
import { startLoginAndImport, useAccount } from '../../services/deepseek/account.js'
import { useSettings } from '../../services/settings.js'
import { ChatInput } from '../chatInput/ChatInput.js'
import { MobileHeader } from './MobileHeader.js'
import styles from './Welcome.module.css'

/** 新对话：问候语 + 输入框。桌面端居中；移动端输入框吸底 */
export const Welcome = () => {
  const t = useTranslate()
  const isMobile = useIsMobile()
  const apiKey = useSettings((state) => state.apiKey)
  // 登录导入进行中不再重复触发
  const phase = useAccount((state) => state.phase)
  const busy = phase !== 'idle' && phase !== 'error' && phase !== 'done'
  return (
    <div className={styles.container}>
      {isMobile ? <MobileHeader /> : null}
      <div className={styles.welcome}>
        <div className={styles.greeting}>{t('welcomeMessageChat250808')}</div>
        <ChatInput sessionId={null} />
        {!apiKey ? (
          <button
            type="button"
            className={styles.login}
            disabled={busy}
            onClick={() => void startLoginAndImport()}
          >
            {t('accountLoginShort')}
          </button>
        ) : null}
        {isMobile ? <div className={styles.caveat}>{t('contentCaveat')}</div> : null}
      </div>
    </div>
  )
}

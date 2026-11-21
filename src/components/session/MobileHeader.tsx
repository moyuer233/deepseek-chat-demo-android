import { useState } from 'react'
import { IconMenuOutline20, IconNewChatOutline20 } from '../base/icons.js'
import { useTranslate } from '../../i18n/index.js'
import { newChat, runManualCompaction, useChatStore } from '../../services/chat/store.js'
import { IconButton } from '../base/IconButton.js'
import { Button } from '../base/Button.js'
import { Modal } from '../base/Modal.js'
import { DshIcon } from '../chatInput/StatusIcons.js'
import { BalanceBadge } from './BalanceBadge.js'
import { openMobileSider } from '../sider/mobileSiderState.js'
import { showToast } from '../base/toastState.js'
import styles from './MobileHeader.module.css'

/** 列表要点前的对勾 */
const PointCheck = () => (
  <svg className={styles.pointIcon} width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden="true">
    <path
      d="m3.2 8.4 3.1 3.1 6.5-7"
      stroke="currentColor"
      strokeWidth="1.6"
      strokeLinecap="round"
      strokeLinejoin="round"
    />
  </svg>
)

/** 移动端顶栏：左侧菜单打开侧边栏；会话页再显示标题与开启新对话 */
export const MobileHeader = ({ title }: { title?: string }) => {
  const t = useTranslate()
  const activeId = useChatStore((state) => state.activeId)
  const [compactOpen, setCompactOpen] = useState(false)
  const compact = () => {
    setCompactOpen(false)
    if (!activeId) {
      return
    }
    void runManualCompaction(activeId).then((ok) => {
      showToast(ok ? t('compactDone') : t('compactFailed'))
    })
  }
  return (
    <>
      <div className={styles.header}>
        <IconButton
          size="xl"
          tone="primary"
          icon={<IconMenuOutline20 />}
          aria-label={t('expandSiderbarButton')}
          onClick={openMobileSider}
        />
        {title === undefined ? (
          <BalanceBadge />
        ) : (
          <>
            <div className={styles.title}>{title}</div>
            <BalanceBadge />
            <IconButton
              size="xl"
              tone="primary"
              aria-label={t('compactNow')}
              icon={
                <svg width="20" height="20" viewBox="0 0 36 36" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M7 9.5h22" />
                  <path d="M12 16.5 18 22.5l6-6" />
                  <path d="M13 27.5h10" />
                </svg>
              }
              onClick={() => setCompactOpen(true)}
            />
            <IconButton
              size="xl"
              tone="primary"
              icon={<IconNewChatOutline20 />}
              aria-label={t('newChat')}
              onClick={newChat}
            />
          </>
        )}
      </div>
      {compactOpen ? (
        <Modal
          title={
            <span className={styles.dialogTitle}>
              <span className={styles.dialogIcon}>
                <DshIcon name="compact-outline" size={18} />
              </span>
              {t('compactTitle')}
            </span>
          }
          onClose={() => setCompactOpen(false)}
          footer={
            <>
              <Button variant="outlined" size="m" onClick={() => setCompactOpen(false)}>
                {t('cancel')}
              </Button>
              <Button size="m" onClick={compact}>
                {t('compactAction')}
              </Button>
            </>
          }
        >
          <p className={styles.dialogLead}>{t('compactLead')}</p>
          <ul className={styles.dialogPoints}>
            <li>
              <PointCheck />
              {t('compactPointSummary')}
            </li>
            <li>
              <PointCheck />
              {t('compactPointRecent')}
            </li>
            <li>
              <PointCheck />
              {t('compactPointLocal')}
            </li>
          </ul>
        </Modal>
      ) : null}
    </>
  )
}

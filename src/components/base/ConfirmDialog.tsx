import type { ReactNode } from 'react'
import { useTranslate } from '../../i18n/index.js'
import { Button } from './Button.js'
import { Modal } from './Modal.js'
import styles from './ConfirmDialog.module.css'

/** 要点前的对勾 */
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

/**
 * 应用内确认弹窗（替代 window.confirm）：图标徽标 + 标题 + 一句说明 + 可选要点列表 + 双按钮。
 * 靠调用方条件渲染控制显隐。
 */
export const ConfirmDialog = ({
  title,
  lead,
  points,
  icon,
  tone = 'brand',
  confirmText,
  confirmVariant = 'primary',
  busy = false,
  onConfirm,
  onClose,
}: {
  title: string
  lead?: string
  /** 一句一条的要点，传已翻译文案 */
  points?: string[]
  icon?: ReactNode
  tone?: 'brand' | 'danger'
  confirmText: string
  confirmVariant?: 'primary' | 'danger'
  busy?: boolean
  onConfirm: () => void
  onClose: () => void
}) => {
  const t = useTranslate()
  return (
    <Modal
      title={
        <span className={styles.title}>
          {icon ? <span className={tone === 'danger' ? styles.iconDanger : styles.icon}>{icon}</span> : null}
          {title}
        </span>
      }
      onClose={onClose}
      footer={
        <>
          <Button variant="outlined" size="m" disabled={busy} autoFocus onClick={onClose}>
            {t('cancel')}
          </Button>
          <Button variant={confirmVariant} size="m" disabled={busy} onClick={onConfirm}>
            {confirmText}
          </Button>
        </>
      }
    >
      {lead ? <p className={styles.lead}>{lead}</p> : null}
      {points && points.length > 0 ? (
        <ul className={styles.points}>
          {points.map((point) => (
            <li key={point}>
              <PointCheck />
              {point}
            </li>
          ))}
        </ul>
      ) : null}
    </Modal>
  )
}

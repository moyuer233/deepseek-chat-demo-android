import clsx from 'clsx'
import type { ReactNode } from 'react'
import { useTranslate } from '../../i18n/index.js'
import { Button } from './Button.js'
import { Modal } from './Modal.js'
import styles from './Dialog.module.css'

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
 * 统一的应用内弹窗：图标徽标 + 标题 + 一句说明 + 可选要点 + 内容 + 取消/确认。
 * 所有移动端弹窗都走这里，保证头部、间距和按钮一致。
 */
export const Dialog = ({
  title,
  icon,
  tone = 'brand',
  lead,
  points,
  children,
  cancelText,
  confirmText,
  confirmVariant = 'primary',
  busy = false,
  onConfirm,
  onClose,
  footer,
}: {
  title: string
  icon?: ReactNode
  tone?: 'brand' | 'danger'
  lead?: string
  points?: string[]
  children?: ReactNode
  cancelText?: string
  confirmText?: string
  confirmVariant?: 'primary' | 'danger'
  busy?: boolean
  onConfirm?: () => void
  onClose: () => void
  /** 自定义底部按钮（如只有一个取消） */
  footer?: ReactNode
}) => {
  const t = useTranslate()
  return (
    <Modal
      title={
        <span className={styles.title}>
          {icon ? <span className={clsx(styles.icon, tone === 'danger' && styles.iconDanger)}>{icon}</span> : null}
          {title}
        </span>
      }
      onClose={onClose}
      footer={
        footer ?? (
          <>
            <Button variant="outlined" size="m" disabled={busy} autoFocus onClick={onClose}>
              {cancelText ?? t('cancel')}
            </Button>
            {onConfirm ? (
              <Button variant={confirmVariant} size="m" disabled={busy} onClick={onConfirm}>
                {confirmText}
              </Button>
            ) : null}
          </>
        )
      }
    >
      {lead ? <p className={styles.lead}>{lead}</p> : null}
      {points && points.length > 0 ? (
        <ul className={styles.points}>
          {points.map((point, index) => (
            <li key={index}>
              <PointCheck />
              {point}
            </li>
          ))}
        </ul>
      ) : null}
      {children ? <div className={styles.content}>{children}</div> : null}
    </Modal>
  )
}

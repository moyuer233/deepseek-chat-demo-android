import type { ReactNode } from 'react'
import styles from './NativeSettings.module.css'

/** 设置页统一样式的行按钮：图标 + 标题 + 值 + 右箭头。 */
export const SettingsRow = ({
  icon,
  title,
  value,
  danger,
  onClick,
}: {
  icon: ReactNode
  title: string
  value?: string
  danger?: boolean
  onClick?: () => void
}) => (
  <button type="button" className={styles.row} onClick={onClick} style={danger ? { color: '#d93025' } : undefined}>
    <span className={styles.rowIcon} aria-hidden>{icon}</span>
    <span className={styles.rowTitle}>{title}</span>
    {value ? <span className={styles.rowValue}>{value}</span> : null}
    {onClick ? <span className={styles.chevron}>›</span> : null}
  </button>
)

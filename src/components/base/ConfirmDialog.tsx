import type { ReactNode } from 'react'
import { Dialog } from './Dialog.js'

/** 确认弹窗：Dialog 的薄封装，保留语义化名字。靠调用方条件渲染控制显隐。 */
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
  points?: string[]
  icon?: ReactNode
  tone?: 'brand' | 'danger'
  confirmText: string
  confirmVariant?: 'primary' | 'danger'
  busy?: boolean
  onConfirm: () => void
  onClose: () => void
}) => (
  <Dialog
    title={title}
    lead={lead}
    points={points}
    icon={icon}
    tone={tone}
    confirmText={confirmText}
    confirmVariant={confirmVariant}
    busy={busy}
    onConfirm={onConfirm}
    onClose={onClose}
  />
)

import { useEffect } from 'react'
import styles from './ImageLightbox.module.css'

/** 附件图片全屏预览：点背景或图片关闭。 */
export const ImageLightbox = ({
  url,
  alt,
  onClose,
}: {
  url: string
  alt: string
  onClose: () => void
}) => {
  useEffect(() => {
    const onKey = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        onClose()
      }
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [onClose])

  return (
    <div className={styles.overlay} role="dialog" aria-modal="true" onClick={onClose}>
      <img className={styles.image} src={url} alt={alt} onClick={onClose} />
    </div>
  )
}

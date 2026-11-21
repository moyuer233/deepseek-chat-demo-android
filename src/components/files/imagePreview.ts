/** 原生壳里 blob: 链接无法直接打开：用全屏 DOM 预览代替。 */
export const showImagePreview = (url: string, alt: string) => {
  const overlay = document.createElement('div')
  overlay.style.cssText =
    'position:fixed;inset:0;z-index:3000;display:flex;align-items:center;justify-content:center;padding:24px;background:rgba(0,0,0,.82)'
  const image = document.createElement('img')
  image.src = url
  image.alt = alt
  image.style.cssText = 'max-width:100%;max-height:100%;object-fit:contain;border-radius:8px'
  const close = () => {
    overlay.remove()
    document.removeEventListener('keydown', onKey)
  }
  const onKey = (event: KeyboardEvent) => {
    if (event.key === 'Escape') {
      close()
    }
  }
  overlay.addEventListener('click', close)
  document.addEventListener('keydown', onKey)
  overlay.appendChild(image)
  document.body.appendChild(overlay)
}

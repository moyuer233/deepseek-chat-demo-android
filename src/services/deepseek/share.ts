import { Capacitor } from '@capacitor/core'
import { DeepSeekAccount } from './plugin.js'

/** 512 KiB 一段，避免一次性把整包 base64 塞进 JS↔原生通道。 */
const CHUNK = 512 * 1024

const toBase64 = (bytes: Uint8Array) => {
  let binary = ''
  const step = 0x8000
  for (let i = 0; i < bytes.length; i += step) {
    binary += String.fromCharCode(...bytes.subarray(i, i + step))
  }
  return btoa(binary)
}

/** 原生端把 Blob 写成缓存文件并弹系统分享；网页端仍走浏览器下载。 */
export const shareBlob = async (name: string, blob: Blob) => {
  if (Capacitor.isNativePlatform()) {
    const { path } = await DeepSeekAccount.beginExportFile({ name })
    const bytes = new Uint8Array(await blob.arrayBuffer())
    for (let offset = 0; offset < bytes.length; offset += CHUNK) {
      await DeepSeekAccount.appendExportChunk({
        path,
        base64: toBase64(bytes.subarray(offset, offset + CHUNK)),
      })
    }
    await DeepSeekAccount.shareFile({
      path,
      mime: blob.type || 'application/zip',
      title: name,
    })
    return
  }
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = name
  link.click()
  setTimeout(() => URL.revokeObjectURL(url), 10_000)
}

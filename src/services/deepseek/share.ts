import { Capacitor } from '@capacitor/core'
import { DeepSeekAccount } from './plugin.js'
import { nativeShell } from './shell.js'

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
  const shell = nativeShell()
  if (shell?.beginExport && shell.appendExport && shell.finishExport) {
    const path = shell.beginExport(name)
    let written = 0
    for (let offset = 0; offset < blob.size; offset += CHUNK) {
      const chunk = new Uint8Array(await blob.slice(offset, offset + CHUNK).arrayBuffer())
      const bytes = shell.appendExport(path, toBase64(chunk))
      if (bytes < 0) {
        throw new Error('导出文件写入失败，请重试')
      }
      written += chunk.length
    }
    if (written !== blob.size) {
      throw new Error('导出文件写入不完整，请重试')
    }
    shell.finishExport(path, blob.type || 'application/zip', name)
    return
  }
  if (Capacitor.isNativePlatform()) {
    const { path } = await DeepSeekAccount.beginExportFile({ name })
    let written = 0
    for (let offset = 0; offset < blob.size; offset += CHUNK) {
      // 按块读取，避免整个导出包再复制进 JS 堆。
      const chunk = new Uint8Array(await blob.slice(offset, offset + CHUNK).arrayBuffer())
      const result = await DeepSeekAccount.appendExportChunk({
        path,
        base64: toBase64(chunk),
      })
      written += result.bytes
    }
    if (written !== blob.size) {
      throw new Error('导出文件写入不完整，请重试')
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

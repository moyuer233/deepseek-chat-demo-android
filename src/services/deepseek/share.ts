import { translate } from '../../i18n/index.js'
import { nativeShell, type NativeShell } from './shell.js'

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

/** 原生流式落盘句柄；没有原生壳时返回 null（网页端走浏览器下载）。 */
export type NativeExportSink = {
  append: (chunk: Uint8Array) => void
  finish: () => void
  abort: () => void
}

type NativeExportShell = NativeShell &
  Required<Pick<NativeShell, 'beginExport' | 'appendExport' | 'finishExport'>>

const hasExportBridge = (shell: NativeShell | null): shell is NativeExportShell =>
  Boolean(shell?.beginExport && shell.appendExport && shell.finishExport)

export const createNativeExport = (name: string, mime = 'application/zip'): NativeExportSink | null => {
  const shell = nativeShell()
  if (!hasExportBridge(shell)) {
    return null
  }
  const path = shell.beginExport(name)
  return {
    append: (chunk) => {
      // ZIP 生成器可能一次给出整段会话 JSON；按块切开发给原生，避免单次跨桥数据过大
      for (let offset = 0; offset < chunk.byteLength; offset += CHUNK) {
        const slice = chunk.subarray(offset, offset + CHUNK)
        if (shell.appendExport(path, toBase64(slice)) < 0) {
          throw new Error(translate('errorExportWrite'))
        }
      }
    },
    finish: () => shell.finishExport(path, mime, name),
    abort: () => shell.deleteArchive?.(path),
  }
}

/** 原生端把 Blob 写成文件并交系统保存；网页端走浏览器下载。 */
export const shareBlob = async (name: string, blob: Blob) => {
  const sink = createNativeExport(name, blob.type || 'application/zip')
  if (sink) {
    try {
      let written = 0
      for (let offset = 0; offset < blob.size; offset += CHUNK) {
        const chunk = new Uint8Array(await blob.slice(offset, offset + CHUNK).arrayBuffer())
        sink.append(chunk)
        written += chunk.byteLength
      }
      if (written !== blob.size) {
        throw new Error(translate('errorExportIncomplete'))
      }
      sink.finish()
      return
    } catch (error) {
      sink.abort()
      throw error
    }
  }
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = name
  link.click()
  setTimeout(() => URL.revokeObjectURL(url), 10_000)
}

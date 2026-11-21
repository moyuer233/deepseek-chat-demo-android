import { translate } from '../../i18n/index.js'

/** 简易 WebView 壳桥：Android 注入 window.NativeShell，不依赖 Capacitor。 */
export type NativeShell = {
  getToken(): string
  startAuth(): void
  openExternal(url: string): void
  openTopUp?(assist: boolean): boolean
  startChatImport?(): void
  peekPendingArchive?(): string
  clearPendingArchive?(): void
  readArchiveChunk?(path: string, offset: number, size: number): string
  deleteArchive?(path: string): void
  beginExport?(name: string): string
  appendExport?(path: string, base64: string): number
  finishExport?(path: string, mime: string, name: string): void
  requestMic?(): void
  openAccessibilitySettings?(): void
  openPlatformPage?(url: string): void
  setAppLocale?(locale: string): void
  isScanAssistEnabled?(): boolean
  clearToken?(): void
}

export const nativeShell = (): NativeShell | null => {
  const shell = (window as unknown as { NativeShell?: NativeShell }).NativeShell
  return shell ?? null
}

export const isNativeShell = (): boolean => nativeShell() !== null

/** 外链：有原生壳走系统浏览器，否则新标签页打开。 */
export const openExternalUrl = (url: string) => {
  const shell = nativeShell()
  if (shell?.openExternal) {
    shell.openExternal(url)
    return
  }
  window.open(url, '_blank', 'noopener,noreferrer')
}

let authResolve: ((token: string) => void) | null = null
let authReject: ((message: string) => void) | null = null

/** Android 授权完成后回调（由原生 evaluateJavascript 调用）。 */
export const registerShellAuthCallbacks = () => {
  ;(window as unknown as { __onNativeAuth?: (token: string) => void }).__onNativeAuth = (token: string) => {
    authResolve?.(token)
    authResolve = null
    authReject = null
  }
  ;(window as unknown as { __onNativeAuthError?: (message: string) => void }).__onNativeAuthError = (message: string) => {
    authReject?.(message)
    authResolve = null
    authReject = null
  }
  ;(window as unknown as { __onMicPermission?: (granted: boolean) => void }).__onMicPermission = (granted: boolean) => {
    micResolve?.(granted)
    micResolve = null
  }
}

let micResolve: ((granted: boolean) => void) | null = null

/** 原生端先申请麦克风权限；没有原生壳时视为已授权（浏览器会自己弹权限）。 */
export const ensureMicPermission = (): Promise<boolean> =>
  new Promise((resolve) => {
    const shell = nativeShell()
    if (!shell?.requestMic) {
      resolve(true)
      return
    }
    micResolve = resolve
    shell.requestMic()
    setTimeout(() => {
      if (micResolve === resolve) {
        micResolve = null
        resolve(false)
      }
    }, 15000)
  })

/** 走原生 DSH 授权，等 token。 */
export const shellPlatformAuthLogin = (): Promise<{ token: string; probes: string }> =>
  new Promise((resolve, reject) => {
    const shell = nativeShell()
    if (!shell) {
      reject(new Error(translate('errorAuthUnavailable')))
      return
    }
    registerShellAuthCallbacks()
    authResolve = (token) => resolve({ token, probes: '' })
    authReject = reject
    shell.startAuth()
  })

let archiveResolve: ((value: { path: string; name: string; bytes: number }) => void) | null = null
let archiveReject: ((message: string) => void) | null = null

/** 走原生 chat.deepseek.com 登录 + 官方导出，返回下载好的 ZIP 路径。 */
export const requestChatArchive = (): Promise<{ path: string; name: string; bytes: number }> =>
  new Promise((resolve, reject) => {
    const shell = nativeShell()
    if (!shell?.startChatImport) {
      reject(new Error(translate('errorChatImportUnsupported')))
      return
    }
    ;(window as unknown as { __onChatArchiveReady?: (path: string, name: string, bytes: number) => void }).__onChatArchiveReady =
      (path, name, bytes) => {
        archiveResolve?.({ path, name, bytes })
        archiveResolve = null
        archiveReject = null
      }
    ;(window as unknown as { __onChatArchiveError?: (message: string) => void }).__onChatArchiveError = (message) => {
      archiveReject?.(message)
      archiveResolve = null
      archiveReject = null
    }
    archiveResolve = resolve
    archiveReject = reject
    shell.startChatImport()
  })

/** 分块读取归档并直接拼成 File，避免再复制一份完整 Uint8Array。 */
export const readArchiveFile = async (path: string, bytes: number, name: string): Promise<File> => {
  const shell = nativeShell()
  if (!shell?.readArchiveChunk) {
    throw new Error(translate('errorArchiveRead'))
  }
  const chunkSize = 512 * 1024
  const parts: BlobPart[] = []
  let offset = 0
  let sinceYield = 0
  while (offset < bytes) {
    const size = Math.min(chunkSize, bytes - offset)
    const b64 = shell.readArchiveChunk(path, offset, size)
    if (!b64) {
      throw new Error(translate('errorArchiveRead'))
    }
    const binary = atob(b64)
    if (binary.length !== size) {
      throw new Error(translate('errorArchiveChunk'))
    }
    const chunk = new Uint8Array(size)
    for (let i = 0; i < size; i += 1) {
      chunk[i] = binary.charCodeAt(i)
    }
    parts.push(chunk)
    offset += size
    // 每 8 块让出一次事件循环，避免 100MB+ 归档把所有帧都卡住
    sinceYield += 1
    if (sinceYield >= 8) {
      sinceYield = 0
      await new Promise((resolve) => setTimeout(resolve, 0))
    }
  }
  const blob = new Blob(parts, { type: 'application/zip' })
  return new File([blob], name, { type: 'application/zip' })
}

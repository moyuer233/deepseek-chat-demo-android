/** 简易 WebView 壳桥：Android 注入 window.NativeShell，不依赖 Capacitor。 */
export type NativeShell = {
  getToken(): string
  startAuth(): void
  openExternal(url: string): void
  openTopUp(): void
  startChatImport?(): void
  peekPendingArchive?(): string
  clearPendingArchive?(): void
  readArchiveChunk?(path: string, offset: number, size: number): string
  deleteArchive?(path: string): void
  beginExport?(name: string): string
  appendExport?(path: string, base64: string): number
  finishExport?(path: string, mime: string, name: string): void
  pickImages?(): void
  makeApiKey?(token: string, name: string): string
}

export const nativeShell = (): NativeShell | null => {
  const shell = (window as unknown as { NativeShell?: NativeShell }).NativeShell
  return shell ?? null
}

export const isNativeShell = (): boolean => nativeShell() !== null

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
}

/** 走原生 DSH 授权，等 token。 */
export const shellPlatformAuthLogin = (): Promise<{ token: string; probes: string }> =>
  new Promise((resolve, reject) => {
    const shell = nativeShell()
    if (!shell) {
      reject(new Error('原生授权不可用'))
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
      reject(new Error('当前版本不支持从 chat.deepseek.com 导入'))
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
export const readArchiveFile = (
  path: string,
  bytes: number,
  name: string,
  chunkSize = 512 * 1024
): File => {
  const shell = nativeShell()
  if (!shell?.readArchiveChunk) {
    throw new Error('读取归档失败')
  }
  const parts: BlobPart[] = []
  let offset = 0
  while (offset < bytes) {
    const size = Math.min(chunkSize, bytes - offset)
    const b64 = shell.readArchiveChunk(path, offset, size)
    if (!b64) {
      throw new Error('读取归档失败')
    }
    const binary = atob(b64)
    if (binary.length !== size) {
      throw new Error('读取归档失败：数据块长度不匹配')
    }
    const chunk = new Uint8Array(size)
    for (let i = 0; i < size; i += 1) {
      chunk[i] = binary.charCodeAt(i)
    }
    parts.push(chunk)
    offset += size
  }
  return new File(parts, name || 'deepseek_data.zip', { type: 'application/zip' })
}

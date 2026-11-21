/** 简易 WebView 壳桥：Android 注入 window.NativeShell，不依赖 Capacitor。 */
export type NativeShell = {
  getToken(): string
  startAuth(): void
  openExternal(url: string): void
  openTopUp(): void
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

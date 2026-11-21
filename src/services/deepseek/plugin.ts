import { registerPlugin } from '@capacitor/core'
import { nativeShell, shellPlatformAuthLogin } from './shell.js'

/** 安卓原生侧实现的 DeepSeek 账号能力；Web 环境不可用。 */
export interface DeepSeekAccountPlugin {
  /** DSH 式授权：auth_init → WebView 授权 → auth_exchange，返回 grant token 与探针结果 */
  platformAuthLogin(): Promise<{ token: string; probes: string }>
  /** 读取已保存的 grant token */
  platformAuthToken(): Promise<{ token: string }>
  /** 打开内置充值页（桌面 UA + 二维码解码，解出后拉起微信） */
  openTopUp(): Promise<void>
  /** 用系统浏览器打开外部链接 */
  openExternal(options: { url: string }): Promise<void>
  /** 打开系统无障碍设置，让用户开启扫码辅助 */
  openAccessibilitySettings(): Promise<void>
  /** 扫码辅助的无障碍服务是否已在系统设置里开启 */
  isScanAssistEnabled(): Promise<boolean>
}

/** WebView 壳里没有 Capacitor，用原生注入的 NativeShell 提供账号/授权/充值能力。 */
const shellAccount: DeepSeekAccountPlugin = {
  platformAuthLogin: () => shellPlatformAuthLogin(),
  platformAuthToken: () => Promise.resolve({ token: nativeShell()?.getToken() ?? '' }),
  openTopUp: () => Promise.resolve(nativeShell()?.openTopUp()),
  openExternal: ({ url }) => Promise.resolve(nativeShell()?.openExternal(url)),
  openAccessibilitySettings: () => Promise.resolve(nativeShell()?.openAccessibilitySettings?.()),
  isScanAssistEnabled: () => Promise.resolve(nativeShell()?.isScanAssistEnabled?.() ?? false),
}

export const DeepSeekAccount: DeepSeekAccountPlugin = nativeShell() ? shellAccount : registerPlugin<DeepSeekAccountPlugin>('DeepSeekAccount')

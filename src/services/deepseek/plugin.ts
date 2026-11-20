import { registerPlugin } from '@capacitor/core'

/** 安卓原生侧实现的 DeepSeek 账号能力；Web 环境不可用。 */
export interface DeepSeekAccountPlugin {
  /** 打开 chat.deepseek.com 登录页，等用户登录后返回 localStorage 里的 userToken */
  loginChat(): Promise<{ token: string }>
  /** 打开 platform.deepseek.com 登录页，返回该站自己的 userToken */
  loginPlatform(): Promise<{ token: string }>
  /** 用 platform token 为 App 创建一把 API Key */
  createApiKey(options: { token: string; name: string }): Promise<{ apiKey: string }>
  /** DSH 式授权：auth_init → WebView 授权 → auth_exchange，返回 grant token 与探针结果 */
  platformAuthLogin(): Promise<{ token: string; probes: string }>
  /** 读取已保存的 grant token */
  platformAuthToken(): Promise<{ token: string }>
  /** 用已保存的 grant 重跑探针（models / messages / 建 Key / chat Cookie） */
  runAccountProbes(): Promise<{ probes: string; chatCookie: string }>
  /** 触发官方导出、轮询到下载链接并下载 ZIP，返回本地文件路径 */
  fetchExportArchive(options: { token: string }): Promise<{ path: string; bytes: number; fileName: string }>
  /** 打开内置充值页（桌面 UA + 二维码解码，解出后拉起微信） */
  openTopUp(): Promise<void>
  /** 用系统浏览器打开外部链接 */
  openExternal(options: { url: string }): Promise<void>
  /** 在缓存里新建（或截断）一个待分享文件，返回绝对路径 */
  beginExportFile(options: { name: string }): Promise<{ path: string }>
  /** 追加一段 base64 数据 */
  appendExportChunk(options: { path: string; base64: string }): Promise<{ bytes: number }>
  /** 用系统分享面板分享文件 */
  shareFile(options: { path: string; mime: string; title: string }): Promise<void>
  /** 原生快捷选图（Android Photo Picker），返回图片字节 */
  pickImages(): Promise<{ files: { name: string; mime: string; base64: string }[] }>
}

export const DeepSeekAccount = registerPlugin<DeepSeekAccountPlugin>('DeepSeekAccount')

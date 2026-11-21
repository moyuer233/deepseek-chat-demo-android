import { Capacitor } from '@capacitor/core'
import { showToast } from '../../components/base/toastState.js'
import { create } from 'zustand'
import { importHistory } from '../chat/backup.js'
import { setAccountToken, setApiKey, useSettings } from '../settings.js'
import { readArchivePreview, type ArchivePreview } from './archive.js'
import { DeepSeekAccount } from './plugin.js'
import { isNativeShell, nativeShell, readArchiveFile, requestChatArchive } from './shell.js'

/** 一步一次：chat 登录 → 导出 → 预览 → 导入 → platform 登录 → 建 Key。 */
export type AccountPhase =
  | 'idle'
  | 'chatLogin'
  | 'exporting'
  | 'preview'
  | 'importing'
  | 'platformLogin'
  | 'creatingKey'
  | 'done'
  | 'error'

type AccountState = {
  phase: AccountPhase
  error: string | null
  preview: ArchivePreview | null
  importedCount: number | null
  archivePath: string | null
}

export const useAccount = create<AccountState>(() => ({
  phase: 'idle',
  error: null,
  preview: null,
  importedCount: null,
  archivePath: null,
}))

const set = (patch: Partial<AccountState>) => useAccount.setState(patch)

const requireNative = () => {
  if (!Capacitor.isNativePlatform() && !isNativeShell()) {
    throw new Error('登录导入只在 App 里可用')
  }
}

/** 读取原生下载好的 ZIP，交给现有导入器预览。 */
const loadArchive = async (path: string, fileName: string) => {
  const response = await fetch(Capacitor.convertFileSrc(path))
  if (!response.ok) {
    throw new Error('读取导出包失败')
  }
  const bytes = new Uint8Array(await response.arrayBuffer())
  return readArchivePreview(bytes, fileName)
}

/** 第一步：登录 chat → 等官方导出完成并下载 → 解析清单，停在预览。 */
export const startLoginAndImport = async () => {
  try {
    requireNative()
    if (isNativeShell()) {
      // 原生壳：一次性完成后台导入（原生登录/导出/分块读取），不做整包预览
      set({ phase: 'exporting', error: null, preview: null, importedCount: null })
      const archive = await requestChatArchive()
      set({ phase: 'importing' })
      const file = readArchiveFile(archive.path, archive.bytes, archive.name)
      const count = await importHistory(file)
      nativeShell()?.deleteArchive?.(archive.path)
      set({ phase: 'done', importedCount: count })
      return
    }
    set({ phase: 'chatLogin', error: null, preview: null, importedCount: null })
    const { token } = await DeepSeekAccount.loginChat()
    set({ phase: 'exporting' })
    const archive = await DeepSeekAccount.fetchExportArchive({ token })
    const preview = await loadArchive(archive.path, archive.fileName)
    if (preview.sessions.length === 0) {
      set({ phase: 'error', error: '账号里没有可导入的会话' })
      return
    }
    set({ phase: 'preview', preview, archivePath: archive.path })
  } catch (error) {
    set({ phase: 'error', error: error instanceof Error ? error.message : String(error) })
  }
}

/** 第二步：用户确认清单后导入，再登 platform 自动建 Key。 */
export const confirmImportAndCreateKey = async () => {
  const state = useAccount.getState()
  if (state.phase !== 'preview' || !state.archivePath || !state.preview) {
    return
  }
  try {
    set({ phase: 'importing', error: null })
    const response = await fetch(Capacitor.convertFileSrc(state.archivePath))
    if (!response.ok) {
      set({ phase: 'error', error: '导出包已失效，请重新导出' })
      return
    }
    const blob = await response.blob()
    const file = new File([blob], state.preview.fileName, { type: 'application/zip' })
    const importedCount = await importHistory(file)

    // 已有账号授权：导入完就结束，不再建 Key。
    if (useSettings.getState().accountToken) {
      set({ phase: 'done', importedCount })
      return
    }

    set({ phase: 'platformLogin', importedCount })
    try {
      // 免 Key 路线：一次 DSH 账号授权即可聊天
      await startPlatformOAuth()
      set({ phase: 'done' })
    } catch (oauthError) {
      const code = (oauthError as { code?: string } | null)?.code
      if (code === 'AUTH_CANCELLED' || String(oauthError).includes('取消')) {
        set({ phase: 'done' })
        return
      }
      // 兜底：老的 platform 网页登录 + 建 Key
      const { token } = await DeepSeekAccount.loginPlatform()
      set({ phase: 'creatingKey' })
      const { apiKey } = await DeepSeekAccount.createApiKey({ token, name: 'DeepSeek Chat Demo' })
      setApiKey(apiKey)
      set({ phase: 'done' })
    }
  } catch (error) {
    set({ phase: 'error', error: error instanceof Error ? error.message : String(error) })
  }
}

export const cancelPreview = () =>
  set({ phase: 'idle', preview: null, archivePath: null })

export const resetAccountFlow = () =>
  set({ phase: 'idle', error: null, preview: null, archivePath: null })

/** 实验：DSH 式账号授权，返回 grant token 与三条探针结果（JSON 字符串）。 */
export const startPlatformOAuth = async (): Promise<{ token: string; probes: string }> => {
  requireNative()
  const result = await DeepSeekAccount.platformAuthLogin()
  // 授权成功即切到免 Key 模式
  setAccountToken(result.token)
  return result
}

/** 用已保存的 grant 重跑探针。 */
export const runAccountProbes = async (): Promise<{ probes: string; chatCookie: string }> => {
  requireNative()
  return DeepSeekAccount.runAccountProbes()
}

const TOP_UP_URL = 'https://platform.deepseek.com/top_up'

/** 充值入口：原生内置页（桌面 UA + 二维码）；Web 端直接开官方页；失败只提示，不污染账户状态机。 */
export const openRecharge = async () => {
  if (!Capacitor.isNativePlatform()) {
    window.open(TOP_UP_URL, '_blank', 'noreferrer')
    return
  }
  try {
    await DeepSeekAccount.openTopUp()
  } catch {
    try {
      await DeepSeekAccount.openExternal({ url: TOP_UP_URL })
    } catch (error) {
      showToast(error instanceof Error ? error.message : String(error))
    }
  }
}

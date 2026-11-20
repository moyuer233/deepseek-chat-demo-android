import { Capacitor } from '@capacitor/core'
import { create } from 'zustand'
import { importHistory } from '../chat/backup.js'
import { setApiKey, useSettings } from '../settings.js'
import { readArchivePreview, type ArchivePreview } from './archive.js'
import { DeepSeekAccount } from './plugin.js'

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
  chatToken: string | null
}

export const useAccount = create<AccountState>(() => ({
  phase: 'idle',
  error: null,
  preview: null,
  importedCount: null,
  archivePath: null,
  chatToken: null,
}))

const set = (patch: Partial<AccountState>) => useAccount.setState(patch)

const requireNative = () => {
  if (!Capacitor.isNativePlatform()) {
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
    set({ phase: 'chatLogin', error: null, preview: null, importedCount: null })
    const { token } = await DeepSeekAccount.loginChat()
    set({ chatToken: token, phase: 'exporting' })
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
    const bytes = await response.arrayBuffer()
    const file = new File([bytes], state.preview.fileName, { type: 'application/zip' })
    const importedCount = await importHistory(file)

    set({ phase: 'platformLogin', importedCount })
    const { token } = await DeepSeekAccount.loginPlatform()
    set({ phase: 'creatingKey' })
    const { apiKey } = await DeepSeekAccount.createApiKey({ token, name: 'DeepSeek Chat Demo' })
    setApiKey(apiKey)
    set({ phase: 'done' })
  } catch (error) {
    set({ phase: 'error', error: error instanceof Error ? error.message : String(error) })
  }
}

export const cancelPreview = () => set({ phase: 'idle', preview: null })

export const resetAccountFlow = () => set({ phase: 'idle', error: null, preview: null })

/** 充值入口：内置页用桌面 UA 拿微信二维码并解码；打不开才退回系统浏览器。 */
export const openRecharge = async () => {
  try {
    requireNative()
    await DeepSeekAccount.openTopUp()
  } catch (error) {
    try {
      await DeepSeekAccount.openExternal({ url: 'https://platform.deepseek.com/top_up' })
    } catch (fallbackError) {
      set({
        phase: 'error',
        error: fallbackError instanceof Error ? fallbackError.message : String(fallbackError),
      })
    }
  }
}

/** 当前是否已经有可用的 Key（登录成功的标志）。 */
export const hasApiKey = () => Boolean(useSettings.getState().apiKey)

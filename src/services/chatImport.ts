import { importHistory } from './chat/backup.js'
import { nativeShell, readArchiveBytes, requestChatArchive } from './deepseek/shell.js'
import { showToast } from '../components/base/toastState.js'

/** 从 chat.deepseek.com 登录 → 官方导出 → 下载 → 分块读入 → 交给 chat-demo 导入器。 */
export const importFromChatDeepSeek = async (): Promise<void> => {
  try {
    const archive = await requestChatArchive()
    const bytes = readArchiveBytes(archive.path, archive.bytes)
    const file = new File([bytes], archive.name, { type: 'application/zip' })
    const count = await importHistory(file)
    showToast({ key: count === 1 ? 'historyImportedOne' : 'historyImported', params: { count } })
  } catch (error) {
    showToast(error instanceof Error ? error.message : String(error))
  }
}


/** 引导里已从 chat.deepseek.com 导出的归档：进入 chat-demo 后自动导入。 */
export const importPendingArchive = async (): Promise<void> => {
  try {
    const raw = nativeShell()?.takePendingArchive?.() ?? ''
    if (!raw) {
      return
    }
    const pending = JSON.parse(raw) as { path: string; name: string; bytes: number }
    const bytes = readArchiveBytes(pending.path, pending.bytes)
    const file = new File([bytes], pending.name || 'deepseek_data.zip', { type: 'application/zip' })
    const count = await importHistory(file)
    nativeShell()?.deleteArchive?.(pending.path)
    showToast({ key: count === 1 ? 'historyImportedOne' : 'historyImported', params: { count } })
  } catch (error) {
    showToast(error instanceof Error ? error.message : String(error))
  }
}

import { importHistory } from './chat/backup.js'
import { nativeShell, readArchiveBytes } from './deepseek/shell.js'
import { showToast } from '../components/base/toastState.js'

/** 引导里已从 chat.deepseek.com 导出的归档：进入 chat-demo 后自动导入。 */
export const importPendingArchive = async (): Promise<void> => {
  try {
    const raw = nativeShell()?.peekPendingArchive?.() ?? ''
    if (!raw) {
      return
    }
    const pending = JSON.parse(raw) as { path: string; name: string; bytes: number }
    const bytes = readArchiveBytes(pending.path, pending.bytes)
    const file = new File([bytes], pending.name || 'deepseek_data.zip', { type: 'application/zip' })
    const count = await importHistory(file)
    nativeShell()?.clearPendingArchive?.()
    nativeShell()?.deleteArchive?.(pending.path)
    showToast({ key: count === 1 ? 'historyImportedOne' : 'historyImported', params: { count } })
  } catch (error) {
    showToast(error instanceof Error ? error.message : String(error))
  }
}

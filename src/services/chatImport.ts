import { importHistory } from './chat/backup.js'
import { nativeShell, readArchiveFile } from './deepseek/shell.js'
import { showToast } from '../components/base/toastState.js'

/** 引导里已从 chat.deepseek.com 导出的归档：进入 chat-demo 后自动导入。 */
export const importPendingArchive = async (): Promise<void> => {
  let pending: { path: string; name: string; bytes: number } | null = null
  try {
    const raw = nativeShell()?.peekPendingArchive?.() ?? ''
    if (!raw) {
      return
    }
    pending = JSON.parse(raw) as { path: string; name: string; bytes: number }
    const file = await readArchiveFile(pending.path, pending.bytes, pending.name)
    const count = await importHistory(file, 'merge')
    nativeShell()?.clearPendingArchive?.()
    nativeShell()?.deleteArchive?.(pending.path)
    showToast({ key: count === 1 ? 'historyImportedOne' : 'historyImported', params: { count } })
  } catch (error) {
    // 归档损坏或版本不兼容时不要每次启动都重试：清掉 pending 与文件，并保留具体原因
    nativeShell()?.clearPendingArchive?.()
    if (pending) {
      nativeShell()?.deleteArchive?.(pending.path)
    }
    showToast(error instanceof Error ? error.message : String(error))
  }
}

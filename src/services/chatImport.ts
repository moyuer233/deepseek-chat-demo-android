import { exportHistory, importHistory } from './chat/backup.js'
import { readArchiveBytes, requestChatArchive } from './deepseek/shell.js'
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

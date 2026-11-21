import { formatDate } from '../../utils/format.js'
import * as db from './db.js'
import { applyPreparedImport, importIntoHistory, replaceHistory, useChatStore } from './store.js'
import type { Session } from './types.js'

export { BackupError } from './history/errors.js'

// 导入、导出与全部删除串行处理，避免读取归档时数据被清空或两个替换事务交错。
let backupQueue: Promise<unknown> = Promise.resolve()

const enqueueBackup = <T>(run: () => Promise<T>): Promise<T> => {
  const task = backupQueue.then(run)
  backupQueue = task.catch(() => {})
  return task
}

const backupFileName = (date: Date) => `deepseek_data-${formatDate(date)}.zip`

/** 导出为历史会话导出格式的 ZIP，可再次导入。 */
export const exportHistory = (): Promise<{ name: string; blob: Blob }> =>
  enqueueBackup(async () => {
    // 列表和已加载内容保留导出开始时的快照；未打开的会话逐条读取，不填满详情缓存。
    const { sessions, sessionCache } = useChatStore.getState()
    async function* readSessions(): AsyncGenerator<Session> {
      for (const summary of sessions) {
        const session = Object.hasOwn(sessionCache, summary.id)
          ? sessionCache[summary.id]
          : await db.loadSession(summary.id)
        if (!session) {
          throw new Error(`History session disappeared during export: ${summary.id}`)
        }
        // 详情读取期间可能重命名，归档标题与开始时的列表保持一致。
        yield { ...session, ...summary }
      }
    }
    const { exportArchive } = await import('./history/exportArchive.js')
    const now = new Date()
    const blob = await exportArchive(readSessions())
    return { name: backupFileName(now), blob }
  })

/**
 * 确认覆盖后调用：先检查 ZIP 格式与版本，校验失败时保留正在生成的回答；
 * 通过后从 ZIP 流式写入暂存，成功才替换原有会话与图片原件。返回导入的会话数。
 */
export type ImportMode = 'overwrite' | 'merge' | 'add'

export type ImportPreflight = {
  added: number
  updated: number
  identical: number
  localOnly: number
}

/** 解析归档到暂存并对比本地差异（不写入正式数据）；随后用 applyImport 提交。 */
export const prepareImport = (file: File): Promise<ImportPreflight> =>
  enqueueBackup(async () => {
    const { checkArchiveFormat, importArchive } = await import('./history/importArchive.js')
    await checkArchiveFormat(file)
    await db.clearStaging()
    await importArchive(file)
    return db.diffStaging()
  })

/** 提交已准备的导入；overwrite 整库替换，merge/add 合并。 */
export const applyImport = (mode: ImportMode): Promise<number> => enqueueBackup(() => applyPreparedImport(mode))

/** 取消导入时清掉暂存，正式数据保持不变。 */
export const discardImport = (): Promise<void> => enqueueBackup(() => db.clearStaging())

/**
 * 导入历史：overwrite 整库替换；merge 合并同 id 会话并保留本机内容；add 只新增本机没有的会话。
 * 覆盖模式沿用暂存 + 原子替换；合并模式只清空暂存并写回合并结果。
 */
export const importHistory = (file: File, mode: ImportMode = 'overwrite'): Promise<number> =>
  enqueueBackup(async () => {
    const { checkArchiveFormat, importArchive } = await import('./history/importArchive.js')
    await checkArchiveFormat(file)
    if (mode === 'overwrite') {
      return replaceHistory(() => importArchive(file))
    }
    return importIntoHistory(file, mode)
  })

/** 确认删除后调用：以空历史原子替换全部本地会话与附件，保留 API Key 和其他设置。 */
export const clearHistory = (): Promise<void> =>
  enqueueBackup(async () => {
    await replaceHistory(async () => {})
  })

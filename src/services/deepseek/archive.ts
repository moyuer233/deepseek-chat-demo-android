import { strFromU8, unzipSync } from 'fflate'
import type { SessionIndexEntry } from '../chat/history/format.js'
import { INDEX_PATH } from '../chat/history/paths.js'

/** 导入前给用户看的会话清单。 */
export type PreviewSession = {
  id: string
  title: string | null
  messageCount: number
  updatedAt: string
}

export type ArchivePreview = {
  fileName: string
  bytes: number
  sessions: PreviewSession[]
}

/** 只读 ZIP 里的 chat_session_index.json，不解压整包，用于“先看清单再导入”。 */
export const readArchivePreview = (bytes: Uint8Array, fileName: string): ArchivePreview => {
  let entries: Record<string, Uint8Array>
  try {
    // filter 让 fflate 只解压索引条目，避免 100MB+ 导出包整包展开进 JS 堆。
    entries = unzipSync(bytes, { filter: (file) => file.name === INDEX_PATH })
  } catch {
    throw new Error('这个文件不是有效的 ZIP')
  }
  const index = entries[INDEX_PATH]
  if (!index) {
    throw new Error('压缩包里没有 chat_session_index.json，不是 DeepSeek 导出包')
  }
  let list: unknown
  try {
    list = JSON.parse(strFromU8(index))
  } catch {
    throw new Error('chat_session_index.json 解析失败')
  }
  if (!Array.isArray(list)) {
    throw new Error('chat_session_index.json 格式不对')
  }
  const sessions: PreviewSession[] = (list as SessionIndexEntry[])
    .filter((item) => item && typeof item.id === 'string')
    .map((item) => ({
      id: item.id,
      title: item.title ?? null,
      messageCount: Number(item.message_count) || 0,
      updatedAt: String(item.updated_at ?? ''),
    }))
    .sort((a, b) => (a.updatedAt < b.updatedAt ? 1 : a.updatedAt > b.updatedAt ? -1 : 0))
  return { fileName, bytes: bytes.byteLength, sessions }
}

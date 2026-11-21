import * as db from '../db.js'
import type { Session } from '../types.js'
import { EXPORT_FORMAT_VERSION, type Manifest, type SessionIndexEntry } from './format.js'
import { INDEX_PATH, MANIFEST_PATH, imagePath, sessionPath } from './paths.js'
import { toIndexEntry, toSessionFile } from './toArchive.js'
import { writeArchive } from './zipWriter.js'

const json = (value: unknown) => new Blob([JSON.stringify(value)], { type: 'application/json' })

type ArchiveEntry = { name: string; blob: Blob }

/**
 * 按导出格式生成 ZIP：manifest → 每个会话 JSON + 该会话图片 → index。
 * 只写出本地有原件的图片，每个会话目录各写一份；其他附件只有元数据。
 * 条目逐条生成：一个会话的图片写完就释放，不把所有图片同时读进内存。
 * 传 onChunk 时边生成边交付（原生端直接落盘），不返回整包 Blob。
 */
export function exportArchive(sessions: Iterable<Session> | AsyncIterable<Session>): Promise<Blob>
export function exportArchive(
  sessions: Iterable<Session> | AsyncIterable<Session>,
  onChunk: (chunk: Uint8Array) => void,
): Promise<void>
export async function exportArchive(
  sessions: Iterable<Session> | AsyncIterable<Session>,
  onChunk?: (chunk: Uint8Array) => void,
): Promise<Blob | void> {
  const manifest: Manifest = { export_format_version: EXPORT_FORMAT_VERSION }
  const index: SessionIndexEntry[] = []
  async function* entries(): AsyncGenerator<ArchiveEntry> {
    yield { name: MANIFEST_PATH, blob: json(manifest) }
    for await (const session of sessions) {
      const originals = new Map<string, Blob | undefined>()
      const images = new Map<string, Blob>()
      for (const message of session.messages) {
        if (message.role !== 'user') {
          continue
        }
        for (const file of message.files) {
          if (file.isImage && !originals.has(file.id)) {
            originals.set(file.id, await db.loadFile(file.id))
          }
        }
      }
      const data = toSessionFile(session, ({ id, name, isImage }) => {
        const blob = originals.get(id)
        if (!blob || !isImage) {
          return null
        }
        const path = imagePath(session.id, id, name)
        images.set(path, blob)
        return path
      })
      yield { name: sessionPath(session.id), blob: json(data) }
      // 图片紧跟会话写出：导入端支持会话与图片交错、index 位于末尾
      for (const [name, blob] of images) {
        yield { name, blob }
      }
      index.push(toIndexEntry(data))
    }
    yield { name: INDEX_PATH, blob: json(index) }
  }
  if (onChunk) {
    await writeArchive(entries(), onChunk)
    return
  }
  return writeArchive(entries())
}

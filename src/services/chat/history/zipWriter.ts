import { Zip, ZipPassThrough } from 'fflate'

// ZIP32 字段中的哨兵值表示需要 ZIP64；当前格式不支持 ZIP64。
const ZIP64_SIZE = 0xffffffff
const ZIP64_COUNT = 0xffff

export class ArchiveLimitError extends Error {
  constructor() {
    super('History archive requires unsupported ZIP64 fields')
    this.name = 'ArchiveLimitError'
  }
}

const invalid = (): never => {
  throw new Error('Invalid history archive entry')
}

/** 与读取端保持相同路径规则，成品必须能再次导入。 */
const entryName = (name: string) => {
  const normalized = name.normalize('NFC')
  if (
    !normalized ||
    normalized.includes('\\') ||
    normalized.includes('\0') ||
    /^[A-Za-z]:/.test(normalized) ||
    normalized.split('/').some((part) => !part || part === '.' || part === '..')
  ) {
    invalid()
  }
  const bytes = new TextEncoder().encode(normalized)
  if (bytes.length > 0xffff) {
    throw new ArchiveLimitError()
  }
  // 拒绝孤立代理项，避免编码时替换成 U+FFFD 后改变附件的关联路径。
  if (new TextDecoder().decode(bytes) !== normalized) {
    invalid()
  }
  return { name: normalized, byteLength: bytes.length }
}

/** Android WebView 里从 content:// 取到的 File 可能 size 为 0，但流有数据；以实际读取长度为准。 */
const resolveSize = async (blob: Blob): Promise<number> =>
  blob.size > 0 ? blob.size : (await blob.arrayBuffer()).byteLength

/** STORE 流式写入；成品必须能再次导入。 */
export const writeArchive = async (entries: { name: string; blob: Blob }[]): Promise<Blob> => {
  if (entries.length >= ZIP64_COUNT) {
    throw new ArchiveLimitError()
  }
  // 串行测量 size=0 的 blob，避免同时把多个大图读进内存。
  const resolved: { name: string; blob: Blob; size: number }[] = []
  for (const entry of entries) {
    resolved.push({ ...entry, size: await resolveSize(entry.blob) })
  }
  const names = new Set<string>()
  let dataSize = 0
  let directorySize = 0
  const prepared = resolved.map(({ name, blob, size }) => {
    const normalized = entryName(name)
    if (names.has(normalized.name)) {
      invalid()
    }
    names.add(normalized.name)
    if (size >= ZIP64_SIZE) {
      throw new ArchiveLimitError()
    }
    dataSize += size + 46 + normalized.byteLength
    directorySize += 46 + normalized.byteLength
    if (dataSize >= ZIP64_SIZE || directorySize >= ZIP64_SIZE) {
      throw new ArchiveLimitError()
    }
    return { name: normalized.name, blob, size }
  })

  const expectedSize = dataSize + directorySize + 22
  const chunks: Blob[] = []
  let size = 0
  let completed = false
  const zip = new Zip((error, chunk, final) => {
    if (error) {
      throw error
    }
    size += chunk.byteLength
    if (size > expectedSize) {
      invalid()
    }
    chunks.push(new Blob([chunk]))
    completed = final
  })
  try {
    for (const { name, blob, size: blobSize } of prepared) {
      const entry = new ZipPassThrough(name)
      entry.mtime = new Date(1980, 0, 1)
      zip.add(entry)
      const reader = blob.stream().getReader()
      let written = 0
      try {
        while (true) {
          const { done, value } = await reader.read()
          if (done) {
            break
          }
          written += value.byteLength
          if (written > blobSize) {
            invalid()
          }
          entry.push(value)
        }
        if (written !== blobSize) {
          invalid()
        }
        entry.push(new Uint8Array(), true)
      } finally {
        await reader.cancel().catch(() => {})
        reader.releaseLock()
      }
    }
    zip.end()
    if (!completed || size !== expectedSize) {
      invalid()
    }
    return new Blob(chunks, { type: 'application/zip' })
  } finally {
    zip.terminate()
  }
}

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
const resolveSize = async (blob: Blob): Promise<{ size: number; buffer?: ArrayBuffer }> => {
  if (blob.size > 0) {
    return { size: blob.size }
  }
  // size=0 的 blob 只读一次，缓存 buffer 供写入阶段复用，避免重复整块读入内存
  const buffer = await blob.arrayBuffer()
  return { size: buffer.byteLength, buffer }
}

type ArchiveEntry = { name: string; blob: Blob }

/**
 * STORE 流式写入；成品必须能再次导入。
 * 条目按需读取（可传生成器）：图片等大附件逐条读入、写完即释放，不在内存里攒整份归档。
 * 传 onChunk 时边写边交付（原生端直接落盘），不保留整包 Blob。
 */
export function writeArchive(entries: Iterable<ArchiveEntry> | AsyncIterable<ArchiveEntry>): Promise<Blob>
export function writeArchive(
  entries: Iterable<ArchiveEntry> | AsyncIterable<ArchiveEntry>,
  onChunk: (chunk: Uint8Array) => void,
): Promise<void>
export async function writeArchive(
  entries: Iterable<ArchiveEntry> | AsyncIterable<ArchiveEntry>,
  onChunk?: (chunk: Uint8Array) => void,
): Promise<Blob | void> {
  const names = new Set<string>()
  let dataSize = 0
  let directorySize = 0
  let count = 0
  const chunks: Blob[] = []
  let size = 0
  let completed = false
  const zip = new Zip((error, chunk, final) => {
    if (error) {
      throw error
    }
    size += chunk.byteLength
    if (onChunk) {
      onChunk(chunk)
    } else {
      chunks.push(new Blob([chunk]))
    }
    completed = final
  })
  try {
    for await (const { name, blob } of entries) {
      count += 1
      if (count >= ZIP64_COUNT) {
        throw new ArchiveLimitError()
      }
      // 串行测量：size=0 的 blob 读完立刻写入，写完即释放
      const { size: blobSize, buffer } = await resolveSize(blob)
      const normalized = entryName(name)
      if (names.has(normalized.name)) {
        invalid()
      }
      names.add(normalized.name)
      if (blobSize >= ZIP64_SIZE) {
        throw new ArchiveLimitError()
      }
      dataSize += blobSize + 46 + normalized.byteLength
      directorySize += 46 + normalized.byteLength
      if (dataSize >= ZIP64_SIZE || directorySize >= ZIP64_SIZE) {
        throw new ArchiveLimitError()
      }
      const entry = new ZipPassThrough(normalized.name)
      entry.mtime = new Date(1980, 0, 1)
      zip.add(entry)
      if (buffer) {
        if (buffer.byteLength !== blobSize) {
          invalid()
        }
        entry.push(new Uint8Array(buffer), true)
      } else {
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
    }
    zip.end()
    // 大小在全部条目写完后才能确定，集中校验，避免半截归档
    const expectedSize = dataSize + directorySize + 22
    if (!completed || size !== expectedSize) {
      invalid()
    }
    if (onChunk) {
      return
    }
    return new Blob(chunks, { type: 'application/zip' })
  } finally {
    zip.terminate()
  }
}

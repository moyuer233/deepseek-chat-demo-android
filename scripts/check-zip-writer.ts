// zipWriter 自检：验证「整包 Blob」与「流式 onChunk」两条导出路径产物逐字节一致，
// 并确认 0 字节条目、条目顺序与解压内容正确。运行：node scripts/check-zip-writer.ts
import { unzipSync } from 'fflate'
import { writeArchive } from '../src/services/chat/history/zipWriter.ts'

const encoder = new TextEncoder()

const assert = (condition: boolean, message: string): void => {
  if (!condition) {
    console.error('FAIL:', message)
    process.exit(1)
  }
}

const entries = async function* () {
  yield { name: 'manifest.json', blob: new Blob([encoder.encode('{"export_format_version":3}')]) }
  yield { name: 'chat_sessions/a.json', blob: new Blob([encoder.encode('{"id":"a"}')]) }
  yield { name: 'chat_sessions/a/images/x.png', blob: new Blob([new Uint8Array([1, 2, 3])]) }
  yield { name: 'zero.txt', blob: new Blob([new Uint8Array(0)]) }
  yield { name: 'chat_session_index.json', blob: new Blob([encoder.encode('[]')]) }
}

const concat = (chunks: Uint8Array[]): Uint8Array => {
  const total = chunks.reduce((sum, chunk) => sum + chunk.byteLength, 0)
  const out = new Uint8Array(total)
  let offset = 0
  for (const chunk of chunks) {
    out.set(chunk, offset)
    offset += chunk.byteLength
  }
  return out
}

const blob = await writeArchive(entries())
const expected = new Uint8Array(await blob.arrayBuffer())

const chunks: Uint8Array[] = []
const returned = await writeArchive(entries(), (chunk) => chunks.push(chunk))
assert(returned === undefined, 'stream mode returns void')
const streamed = concat(chunks)
assert(streamed.byteLength === expected.byteLength, `stream length ${streamed.byteLength} != ${expected.byteLength}`)
assert(streamed.every((byte, index) => byte === expected[index]), 'stream bytes differ from blob bytes')

const files = unzipSync(expected)
const names = Object.keys(files)
assert(
  names.join(',') ===
    'manifest.json,chat_sessions/a.json,chat_sessions/a/images/x.png,zero.txt,chat_session_index.json',
  `entry order changed: ${names.join(',')}`,
)
assert(new TextDecoder().decode(files['manifest.json']) === '{"export_format_version":3}', 'manifest content')
assert(files['zero.txt'].byteLength === 0, 'zero-length entry')
assert(files['chat_sessions/a/images/x.png'].join(',') === '1,2,3', 'image bytes')

let duplicateRejected = false
try {
  await writeArchive([
    { name: 'a.txt', blob: new Blob(['x']) },
    { name: 'a.txt', blob: new Blob(['y']) },
  ])
} catch {
  duplicateRejected = true
}
assert(duplicateRejected, 'duplicate entry names must be rejected')

console.log('zipWriter self-check ok:', expected.byteLength, 'bytes,', names.length, 'entries')

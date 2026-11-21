import { BackupError, fail } from './errors.js'
import {
  EXPORT_FORMAT_VERSION,
  type ChatMessage,
  type FileEntry,
  type Fragment,
  type SearchResult,
  type SessionFile,
  type SessionIndexEntry,
} from './format.js'

/**
 * 按格式定义校验，返回只含已定义字段的新对象：容器与元数据严格校验，无法解析的内容片段丢弃。
 * 可空字段必须显式为 null；可选字段允许缺省，存在时严格校验。
 */

type Read<T> = (value: unknown) => T

const invalid = () => fail('backupIncomplete')

const record = (value: unknown): Record<string, unknown> =>
  value !== null && typeof value === 'object' && !Array.isArray(value)
    ? (value as Record<string, unknown>)
    : invalid()

const string: Read<string> = (value) => (typeof value === 'string' ? value : invalid())

const boolean: Read<boolean> = (value) => (typeof value === 'boolean' ? value : invalid())

const number: Read<number> = (value) =>
  typeof value === 'number' && Number.isFinite(value) ? value : invalid()

/** 非负安全整数（u32 等） */
const count: Read<number> = (value) =>
  Number.isSafeInteger(value) && (value as number) >= 0 ? (value as number) : invalid()

/** 可选的非负整数：格式不对时忽略该字段，而不是丢弃整段搜索结果 */
const optionalCount: Read<number | undefined> = (value) =>
  Number.isSafeInteger(value) && (value as number) >= 0 ? (value as number) : undefined

const time: Read<string> = (value) => {
  if (typeof value !== 'string') {
    return invalid()
  }
  // 官方导出是 RFC3339；除 Z 外也接受合法时区偏移（旧包出现过 +00:00）。
  const match = /^(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2})(?:\.(\d+))?(Z|[+-]\d{2}:\d{2})$/.exec(value)
  if (!match) {
    return invalid()
  }
  const parsed = new Date(value)
  if (!Number.isFinite(parsed.getTime())) {
    return invalid()
  }
  if (match[3] !== 'Z') {
    // 统一归一化成 UTC，保持 Iso8601Utc 契约与排序稳定。
    return parsed.toISOString()
  }
  // Date 只保留毫秒；校验时补齐/截断精度，返回时保留原文，避免丢失微秒或纳秒。
  const milliseconds = `${match[1]}.${(match[2] ?? '').padEnd(3, '0').slice(0, 3)}Z`
  return parsed.toISOString() === milliseconds ? value : invalid()
}

const nullable =
  <T>(read: Read<T>): Read<T | null> =>
  (value) =>
    value === null ? null : read(value)

const list =
  <T>(read: Read<T>): Read<T[]> =>
  (value) =>
    Array.isArray(value) ? value.map(read) : invalid()

const oneOf =
  <const T extends string>(...values: T[]): Read<T> =>
  (value) =>
    values.includes(value as T) ? (value as T) : invalid()

const tipStyle = oneOf('INFO', 'WARNING', 'CAUTION')
const searchStatus = oneOf('WIP', 'FINISHED', 'FAILED')
const toolMetadata = (item: Record<string, unknown>) => ({
  ...('stage_id' in item ? { stage_id: optionalCount(item.stage_id) } : {}),
  ...('group_id' in item ? { group_id: optionalCount(item.group_id) } : {}),
})

const searchResult: Read<SearchResult> = (value) => {
  const item = record(value)
  return {
    url: string(item.url),
    title: string(item.title),
    snippet: string(item.snippet),
    published_at: nullable(time)(item.published_at),
    site_name: nullable(string)(item.site_name),
    ...('site_icon' in item ? { site_icon: nullable(string)(item.site_icon) } : {}),
  }
}

const fileEntry: Read<FileEntry> = (value) => {
  const item = record(value)
  return {
    id: string(item.id),
    file_name: string(item.file_name),
    // 旧包可能没有 file_size（DSH 同样按 0 处理）。
    file_size: 'file_size' in item ? count(item.file_size) : 0,
    token_usage: 'token_usage' in item ? nullable(count)(item.token_usage) : null,
    inserted_at: time(item.inserted_at),
    export_path: 'export_path' in item ? nullable(string)(item.export_path) : null,
    is_image: boolean(item.is_image),
    width: 'width' in item ? nullable(count)(item.width) : null,
    height: 'height' in item ? nullable(count)(item.height) : null,
  }
}

const readFragment: Read<Fragment> = (value) => {
  const item = record(value)
  const type = string(item.type)
  switch (type) {
    case 'REQUEST':
      return { type, content: string(item.content) }
    case 'RESPONSE':
      return { type, content: string(item.content) }
    case 'TEMPLATE_RESPONSE':
      return { type, content: string(item.content) }
    case 'THINK':
      return {
        type,
        content: string(item.content),
        elapsed_secs: nullable(number)(item.elapsed_secs),
      }
    case 'TIP':
      return { type, content: string(item.content), style: tipStyle(item.style) }
    case 'CONTENT_RISK':
      return { type, content: string(item.content), style: tipStyle(item.style) }
    case 'PARTIAL_FILE_READ': {
      const ratio = number(item.ratio)
      return { type, ratio: ratio >= 0 && ratio <= 100 ? ratio : invalid() }
    }
    case 'TOOL_SEARCH':
      return {
        type,
        queries: list(string)(item.queries),
        results: list(searchResult)(item.results),
        ...('status' in item ? { status: searchStatus(item.status) } : {}),
        ...toolMetadata(item),
      }
    case 'TOOL_OPEN':
      return {
        type,
        status: searchStatus(item.status),
        url: nullable(string)(item.url),
        title: nullable(string)(item.title),
        ...('snippet' in item ? { snippet: nullable(string)(item.snippet) } : {}),
        ...('site_name' in item ? { site_name: nullable(string)(item.site_name) } : {}),
        published_at: nullable(time)(item.published_at),
        ...toolMetadata(item),
      }
    case 'TOOL_FIND':
      return {
        type,
        status: oneOf('WIP', 'FINISHED', 'NO_RESULT', 'FAILED')(item.status),
        ...('pattern' in item ? { pattern: string(item.pattern) } : {}),
        ...toolMetadata(item),
      }
    case 'READ_LINK':
      return {
        type,
        results: list((result) => {
          const entry = record(result)
          return {
            signature: string(entry.signature),
            ...('url' in entry ? { url: string(entry.url) } : {}),
            ...('title' in entry ? { title: nullable(string)(entry.title) } : {}),
          }
        })(item.results),
      }
    case 'FILE':
      return { type, files: list(fileEntry)(item.files) }
    case 'TOOL_IMAGE_SEARCH':
      return {
        type,
        status: searchStatus(item.status),
        ...('image_indexes' in item ? { image_indexes: list(count)(item.image_indexes) } : {}),
        ...toolMetadata(item),
        results: list((result) => {
          const entry = record(result)
          return {
            title: string(entry.title),
            source_page_url: string(entry.source_page_url),
            snippet: string(entry.snippet),
            site_name: nullable(string)(entry.site_name),
            published_at: nullable(time)(entry.published_at),
          }
        })(item.results),
      }
    default:
      return invalid()
  }
}

/** 单个内容片段不兼容不影响其余消息；容器、元数据和后续消息树校验仍严格执行。 */
const fragment: Read<Fragment | null> = (value) => {
  try {
    return readFragment(value)
  } catch (error) {
    if (error instanceof BackupError && error.key === 'backupIncomplete') {
      return null
    }
    throw error
  }
}

/** 官方未来可能新增状态值；未知状态按「未完成」处理，不让整条消息失败。 */
const messageStatus: Read<ChatMessage['status']> = (value) =>
  value === 'WIP' ||
  value === 'FINISHED' ||
  value === 'CONTENT_FILTER' ||
  value === 'FAILED' ||
  value === 'INCOMPLETE' ||
  value === 'CONTEXT_LENGTH_EXCEEDED' ||
  value === 'TIMEOUT'
    ? value
    : 'INCOMPLETE'

const message: Read<ChatMessage> = (value) => {
  const item = record(value)
  return {
    id: count(item.id),
    parent_id: nullable(count)(item.parent_id),
    role: oneOf('USER', 'ASSISTANT')(item.role),
    status: messageStatus(item.status),
    // 旧包可能不带这三项（DSH 已按默认值处理）。
    thinking_enabled: 'thinking_enabled' in item ? boolean(item.thinking_enabled) : false,
    search_enabled: 'search_enabled' in item ? boolean(item.search_enabled) : false,
    accumulated_token_usage:
      'accumulated_token_usage' in item ? count(item.accumulated_token_usage) : 0,
    inserted_at: time(item.inserted_at),
    fragments: list(fragment)(item.fragments).filter((item): item is Fragment => item !== null),
  }
}

/** 会话 ID 用于拼接条目名，不能含路径分隔符 */
const sessionId: Read<string> = (value) => {
  const id = string(value)
  return id && !/[/\\]/.test(id) && id !== '.' && id !== '..' ? id : invalid()
}

/** 版本号用于校验归档格式兼容性；字段结构由各字段的校验函数保证。 */
export const checkManifest = (value: unknown): void => {
  const version = record(value).export_format_version
  if (typeof version === 'number' && version > EXPORT_FORMAT_VERSION) {
    fail('backupNewerVersion')
  }
  if (version !== EXPORT_FORMAT_VERSION) {
    fail('backupInvalidVersion')
  }
}

export const readSessionIndex: Read<SessionIndexEntry[]> = list((value) => {
  const item = record(value)
  return {
    id: sessionId(item.id),
    title: nullable(string)(item.title),
    pinned: false,
    message_count: count(item.message_count),
    inserted_at: time(item.inserted_at),
    updated_at: time(item.updated_at),
  }
})

export const readSessionFile: Read<SessionFile> = (value) => {
  const item = record(value)
  return {
    id: sessionId(item.id),
    title: nullable(string)(item.title),
    pinned: false,
    // 官方允许缺失或 null；交给 toLocal 回退到最大 id 的叶子。
    current_message_id:
      item.current_message_id === undefined || item.current_message_id === null
        ? null
        : count(item.current_message_id),
    inserted_at: time(item.inserted_at),
    updated_at: time(item.updated_at),
    messages: list(message)(item.messages),
  }
}

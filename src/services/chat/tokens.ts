import type { AssistantMessage, Message } from './types.js'

export const assistantText = (message: AssistantMessage): string =>
  message.blocks
    .filter((block) => block.type === 'text')
    .map((block) => block.text)
    .join('\n')

export const plainText = (message: Message): string =>
  message.role === 'user' ? message.content : assistantText(message)

/** 粗估 token：中文约 2 字符/token，统一按 2 保守估计 */
export const estimateTokens = (messages: Message[]): number =>
  messages.reduce((sum, message) => sum + Math.ceil(plainText(message).length / 2) + 8, 0)

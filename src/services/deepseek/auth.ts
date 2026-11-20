import { Capacitor } from '@capacitor/core'
import { DeepSeekAccount } from './plugin.js'
import { setAccountToken, useSettings } from '../settings.js'

/** 启动时把原生保存的 grant 读回内存；Web 环境直接跳过。 */
export const loadAccountToken = async (): Promise<void> => {
  if (!Capacitor.isNativePlatform()) {
    return
  }
  try {
    const { token } = await DeepSeekAccount.platformAuthToken()
    if (token) {
      setAccountToken(token)
    }
  } catch (error) {
    // 没有授权是预期情况；其他异常留一条 warn 便于线上定位
    // eslint-disable-next-line no-console
    console.warn('[deepseek-auth] platformAuthToken failed', error)
  }
}

/** 是否已有可用凭证（API Key 或账号 grant）。 */
export const hasCredential = (): boolean =>
  Boolean(useSettings.getState().apiKey || useSettings.getState().accountToken)

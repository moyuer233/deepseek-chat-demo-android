import { showToast } from '../../components/base/toastState.js'
import { setAccountToken } from '../settings.js'
import { DeepSeekAccount } from './plugin.js'
import { isNativeShell, openExternalUrl } from './shell.js'
import { translate } from '../../i18n/index.js'

/** 只有原生壳能发起 DSH 授权。 */
const requireNative = () => {
  if (!isNativeShell()) {
    throw new Error(translate('errorAccountNativeOnly'))
  }
}

/** DSH 式账号授权：一次授权即可聊天（免 API Key）。 */
export const startPlatformOAuth = async (): Promise<{ token: string; probes: string }> => {
  requireNative()
  const result = await DeepSeekAccount.platformAuthLogin()
  // 授权成功即切到免 Key 模式
  setAccountToken(result.token)
  return result
}

const TOP_UP_URL = 'https://platform.deepseek.com/top_up'

/** 充值入口：原生内置页（桌面 UA + 二维码）；Web 端直接开官方页；失败只提示，不污染账户状态机。 */
export const openRecharge = async () => {
  if (!isNativeShell()) {
    openExternalUrl(TOP_UP_URL)
    return
  }
  // 原生充值页打不开时只提示，不再偷偷跳系统浏览器（浏览器里没有扫码辅助）
  const opened = await DeepSeekAccount.openTopUp()
  if (!opened) {
    showToast(translate('errorTopUpUnavailable'))
  }
}

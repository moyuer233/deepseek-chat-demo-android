import { translate } from '../../i18n/index.js'
import { setAccountToken, useSettings } from '../settings.js'

export type Balance = {
  /** 账户是否可用（is_available） */
  available: boolean
  currency: string
  /** 充值余额 + 赠送余额 */
  total: number
  granted: number
}

const CURRENCY_SYMBOL: Record<string, string> = { CNY: '¥', USD: '$' }

export const fetchBalance = async (): Promise<Balance | null> => {
  const token = useSettings.getState().accountToken ?? ''
  const apiKey = useSettings.getState().apiKey ?? ''
  if (!token && !apiKey) {
    return null
  }
  const headers: Record<string, string> = {}
  if (token) {
    headers['x-dsh-auth-token'] = token
  } else {
    headers.Authorization = `Bearer ${apiKey}`
  }
  const controller = new AbortController()
  const timer = setTimeout(() => controller.abort(), 15_000)
  const response = await fetch('https://api.deepseek.com/user/balance', {
    headers,
    signal: controller.signal,
  }).finally(() => clearTimeout(timer))
  if (!response.ok) {
    if (response.status === 401 && token && useSettings.getState().accountToken) {
      setAccountToken(null)
    }
    throw new Error(translate('errorBalanceFetch', { status: response.status }))
  }
  const data = (await response.json()) as {
    is_available?: boolean
    balance_infos?: { currency?: string; total_balance?: string; granted_balance?: string }[]
  }
  const info = data.balance_infos?.[0]
  if (!info) {
    return null
  }
  return {
    available: data.is_available !== false,
    currency: info.currency ?? 'CNY',
    total: Number(info.total_balance) || 0,
    granted: Number(info.granted_balance) || 0,
  }
}

export const currencySymbol = (currency: string) => CURRENCY_SYMBOL[currency] ?? `${currency} `

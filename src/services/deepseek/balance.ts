import { nativeShell } from './shell.js'
import { useSettings } from '../settings.js'

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
  const token = nativeShell()?.getToken?.() ?? ''
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
  const response = await fetch('https://api.deepseek.com/user/balance', { headers })
  if (!response.ok) {
    throw new Error(`余额获取失败（${response.status}）`)
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

export const currencySymbol = (currency: string) => CURRENCY_SYMBOL[currency] ?? ''

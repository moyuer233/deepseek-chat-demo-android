import { useEffect, useState } from 'react'
import { useTranslate } from '../../i18n/index.js'
import { currencySymbol, fetchBalance, type Balance } from '../../services/deepseek/balance.js'
import styles from './BalanceBadge.module.css'

/** 会话头部余额：一个状态点 + 金额（赠送部分单独上色）。 */
export const BalanceBadge = () => {
  const t = useTranslate()
  const [balance, setBalance] = useState<Balance | null>(null)
  const [failed, setFailed] = useState(false)

  useEffect(() => {
    let cancelled = false
    const load = () => {
      void fetchBalance()
        .then((data) => {
          if (cancelled) {
            return
          }
          setBalance(data)
          setFailed(false)
        })
        .catch(() => {
          if (!cancelled) {
            setFailed(true)
          }
        })
    }
    load()
    const timer = window.setInterval(load, 60_000)
    const onVisible = () => {
      if (document.visibilityState === 'visible') {
        load()
      }
    }
    document.addEventListener('visibilitychange', onVisible)
    return () => {
      cancelled = true
      window.clearInterval(timer)
      document.removeEventListener('visibilitychange', onVisible)
    }
  }, [])

  if (!balance) {
    return failed ? (
      <div className={styles.balance} title={t('balanceFailed')}>
        <span className={styles.dotError} />
        <span className={styles.value}>—</span>
      </div>
    ) : null
  }

  // 余额归零/账户不可用时点变红（与插件一致）
  const healthy = balance.available && balance.total > 0.005
  const symbol = currencySymbol(balance.currency)
  return (
    <div className={styles.balance} title={t('balanceTooltip')}>
      <span className={healthy ? styles.dotOk : styles.dotError} />
      <span className={styles.label}>{t('balanceLabel')}</span>
      <span className={styles.value}>{symbol}{balance.total.toFixed(2)}</span>
      {balance.granted > 0 ? (
        <span className={styles.bonus}>({t('balanceBonus')} {symbol}{balance.granted.toFixed(2)})</span>
      ) : null}
    </div>
  )
}

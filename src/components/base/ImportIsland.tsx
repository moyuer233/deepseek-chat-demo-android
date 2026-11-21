import { useEffect, useState } from 'react'
import { useTranslate } from '../../i18n/index.js'
import { useImportIsland } from './importIslandState.js'
import styles from './ImportIsland.module.css'

/**
 * 导入进度岛：仿灵动岛，挂在屏幕顶部，显示已处理条目数与呼吸进度条。
 * 解析归档是流式读取，总条数未知，所以显示「已处理 N 条」而不是百分比。
 */
export const ImportIsland = () => {
  const t = useTranslate()
  const island = useImportIsland((state) => state.island)
  const [mounted, setMounted] = useState(false)

  useEffect(() => {
    if (island) {
      setMounted(true)
    }
  }, [island])

  if (!island || !mounted) {
    return null
  }

  return (
    <div className={styles.island} role="status" aria-live="polite">
      <span className={styles.dot} aria-hidden />
      <span className={styles.label}>{t(island.phase === 'apply' ? 'importIslandApply' : 'importIslandPrepare')}</span>
      <span className={styles.count}>
        {island.total === null ? island.done : island.done + ' / ' + island.total}
      </span>
      <span className={styles.track} aria-hidden>
        <span className={styles.bar} />
      </span>
    </div>
  )
}

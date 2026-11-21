import { useTranslate } from '../../i18n/index.js'
import { useImportIsland } from './importIslandState.js'
import styles from './ImportIsland.module.css'

/**
 * 导入进度岛：仿灵动岛，挂在屏幕顶部，显示阶段文案与已处理条目数。
 * 解析归档是流式读取、总条数未知，所以只显示已处理数量而不是百分比。
 */
export const ImportIsland = () => {
  const t = useTranslate()
  const island = useImportIsland((state) => state.island)

  if (!island) {
    return null
  }

  return (
    <div className={styles.island} role="status" aria-live="polite" aria-atomic="true">
      <span className={styles.dot} aria-hidden />
      <span className={styles.label}>{t(island.phase === 'apply' ? 'importIslandApply' : 'importIslandPrepare')}</span>
      {/* 计数每秒变化数十次，对读屏隐藏，避免逐条播报刷屏；apply 阶段没有条目进度就不显示数字 */}
      {island.done === null ? null : (
        <span className={styles.count} aria-hidden>
          {island.done}
        </span>
      )}
      <span className={styles.track} aria-hidden>
        <span className={styles.bar} />
      </span>
    </div>
  )
}

import { create } from 'zustand'

/** 导入进度岛：解析归档时在顶部显示的实时进度（灵动岛样式） */
export type ImportIsland = {
  /** 已处理条目数；apply 阶段拿不到细粒度进度，为 null 时只显示扫光条 */
  done: number | null
  /** 当前阶段 */
  phase: 'prepare' | 'apply'
} | null

export const useImportIsland = create<{ island: ImportIsland }>(() => ({ island: null }))

/**
 * 当前持有进度岛的流程。并发导入时（如启动自动导入 + 设置页手动导入）
 * 只允许持有者更新/关闭，避免互相覆盖或提前关掉对方的岛。
 */
let owner: string | null = null

export const acquireImportIsland = (id: string) => {
  owner = id
}

export const showImportIsland = (island: NonNullable<ImportIsland>, id?: string) => {
  if (id !== undefined && owner !== null && owner !== id) {
    return
  }
  useImportIsland.setState({ island })
}

/** apply 阶段没有条目级进度，显示不定量扫光条而不是卡在 0 */
export const showImportIslandIndeterminate = (phase: 'prepare' | 'apply', id?: string) =>
  showImportIsland({ done: null, phase }, id)

export const hideImportIsland = (id?: string) => {
  if (id !== undefined && owner !== null && owner !== id) {
    return
  }
  owner = null
  useImportIsland.setState({ island: null })
}

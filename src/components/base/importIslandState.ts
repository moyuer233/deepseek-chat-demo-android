import { create } from 'zustand'

/** 导入进度岛：解析归档时在顶部显示的实时进度（灵动岛样式） */
export type ImportIsland = {
  /** 已处理条目数 */
  done: number
  /** 总条目数；归档流式解析时未知，为 null 时只显示已处理数量 */
  total: number | null
  /** 当前阶段文案 key，如「解析归档」「写入会话」 */
  phase: 'prepare' | 'apply'
} | null

export const useImportIsland = create<{ island: ImportIsland }>(() => ({ island: null }))

export const showImportIsland = (island: NonNullable<ImportIsland>) => useImportIsland.setState({ island })
export const hideImportIsland = () => useImportIsland.setState({ island: null })

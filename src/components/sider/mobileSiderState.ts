import { create } from 'zustand'

/** 移动端侧边栏抽屉：菜单按钮/左缘滑动打开，点遮罩、切换会话或开启新对话后关闭。 */
export const useMobileSider = create(() => ({ open: false, drag: 0 }))

export const openMobileSider = () => useMobileSider.setState({ open: true, drag: 0 })

export const closeMobileSider = () => useMobileSider.setState({ open: false, drag: 0 })

/** 手指拖动进度 0..1（原生壳通过 window.__siderDrag 调用）。 */
export const setSiderDrag = (drag: number) => useMobileSider.setState({ drag })

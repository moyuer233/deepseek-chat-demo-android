import {
  closeMobileSider,
  openMobileSider,
  setSiderDrag,
  useMobileSider,
} from '../components/sider/mobileSiderState.js'

/** 原生壳左缘横滑：拖动进度驱动抽屉位移，松手按进度/速度吸附。 */
export const installNativeSiderGesture = () => {
  const w = window as unknown as {
    __siderDrag?: (progress: number) => void
    __siderSwipeEnd?: (progress: number, velocity: number) => void
    __siderClose?: () => void
    NativeShell?: { setSiderOpen?: (open: boolean) => void }
  }
  w.__siderDrag = (progress: number) => setSiderDrag(Math.min(1, Math.max(0, progress)))
  w.__siderSwipeEnd = (progress: number, velocity: number) => {
    if (useMobileSider.getState().open) {
      // 收回：向左拖过 20% 或向左甩动即可，便于在侧边栏上直接滑动收起
      if (progress < 0.8 || velocity < -800) {
        closeMobileSider()
        return
      }
      openMobileSider()
      return
    }
    // 打开：必须拖过一半或快速向右甩动，避免轻扫误触
    if (progress > 0.5 || velocity > 1200) {
      openMobileSider()
    } else {
      closeMobileSider()
    }
  }
  w.__siderClose = () => closeMobileSider()
  // 页面刚加载时先同步一次真实状态，避免原生 siderOpen 残留旧值导致从屏幕中间滑动也能触发
  w.NativeShell?.setSiderOpen?.(useMobileSider.getState().open)
  return useMobileSider.subscribe((state) => {
    w.NativeShell?.setSiderOpen?.(state.open)
  })
}

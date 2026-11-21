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
    if (progress > 0.35 || velocity > 900) {
      openMobileSider()
    } else {
      closeMobileSider()
    }
  }
  w.__siderClose = () => closeMobileSider()
  useMobileSider.subscribe((state) => {
    w.NativeShell?.setSiderOpen?.(state.open)
  })
}

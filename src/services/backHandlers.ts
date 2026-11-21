/** 页面内返回栈：系统返回键/手势先交给这里，返回 true 表示已消费。 */
type BackHandler = () => boolean

const handlers: BackHandler[] = []

export const pushBackHandler = (handler: BackHandler): (() => void) => {
  handlers.push(handler)
  return () => {
    const index = handlers.indexOf(handler)
    if (index >= 0) {
      handlers.splice(index, 1)
    }
  }
}

/**
 * 由最上层页面处理返回：从栈顶向下逐个询问，第一个返回 true 的消费掉这次返回。
 * 不能只问栈顶——子页注册的处理器在「没有弹窗」时会返回 false，
 * 若就此收工，父页（如设置主页 → 关闭设置）就永远拿不到返回事件。
 */
export const runBackHandler = (): boolean => {
  for (let i = handlers.length - 1; i >= 0; i -= 1) {
    if (handlers[i]()) {
      return true
    }
  }
  return false
}

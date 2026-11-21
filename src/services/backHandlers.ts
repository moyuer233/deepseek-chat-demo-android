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

/** 由最上层页面处理返回；没有处理器时返回 false。 */
export const runBackHandler = (): boolean => {
  const top = handlers.at(-1)
  return top ? top() : false
}

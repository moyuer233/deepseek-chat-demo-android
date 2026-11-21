/** 页面内返回栈：系统返回键/手势先交给这里，返回 true 表示已消费。 */
type BackHandler = () => boolean

type Entry = {
  handler: BackHandler
  /** 数值越大越先被询问：子页 > 父页，弹窗 > 页面 */
  priority: number
  seq: number
}

const entries: Entry[] = []
let seq = 0

/**
 * 注册返回处理器。priority 越大越先被询问：
 * - 子页（如设置里的账户页）要比父页（设置主页）先拿到返回；
 * - 同优先级时后注册的先问（弹窗叠在页面上）。
 * React effect 是「先子后父」执行，所以不能靠注册顺序判断层级，必须显式给 priority。
 */
export const pushBackHandler = (handler: BackHandler, priority = 0): (() => void) => {
  seq += 1
  const entry: Entry = { handler, priority, seq }
  entries.push(entry)
  return () => {
    const index = entries.indexOf(entry)
    if (index >= 0) {
      entries.splice(index, 1)
    }
  }
}

/**
 * 由优先级最高的页面处理返回；逐个询问，第一个返回 true 的消费这次返回。
 * 单个 handler 抛异常时视为未消费，继续向下问——否则一次异常会让原生侧收到
 * 非 true 回包，进而被当成「已到最底层」而退出 App。
 */
export const runBackHandler = (): boolean => {
  const ordered = [...entries].sort((a, b) => b.priority - a.priority || b.seq - a.seq)
  for (const entry of ordered) {
    try {
      if (entry.handler()) {
        return true
      }
    } catch {
      // 忽略异常，继续问下一层
    }
  }
  return false
}

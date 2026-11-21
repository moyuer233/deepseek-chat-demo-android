// 由 copy/文案.mjs 生成网页与原生文案；改文案请只改 copy/文案.mjs，然后运行本脚本。
import { writeFile } from 'node:fs/promises'
import { sections } from '../copy/文案.mjs'

const q = (v) => JSON.stringify(v)

let zh = 'export const zh = {\n'
let en = 'export const en = {\n'
let md = '# 文案表（按界面分组）\n\n> 唯一入口：copy/文案.mjs。改完运行 node scripts/gen-copy.mjs 生成网页与原生文案。\n\n## 网页界面\n\n'

for (const section of sections) {
  if (!section.web) continue
  zh += '  // ===== ' + section.name + ' =====\n'
  en += '  // ===== ' + section.name + ' =====\n'
  md += '### ' + section.name + '\n\n| 中文 | English |\n|---|---|\n'
  for (const [key, value] of Object.entries(section.web.zh)) {
    zh += '  ' + key + ': ' + q(value) + ',\n'
    const english = section.web.en[key]
    if (english !== undefined) en += '  ' + key + ': ' + q(english) + ',\n'
    md += '| ' + value.replace(/\|/g, '\\|') + ' | ' + (english ?? '（英文缺失）').replace(/\|/g, '\\|') + ' |\n'
  }
  md += '\n'
}
zh += '}\n'
en += '}\n'

const nativeSection = sections.find((s) => s.native)
// Kotlin 字符串里 $ 是模板符，逐条转义，避免文案里出现 $ 时编译失败
const kq = (v) => q(v).replace(/\$/g, '\\$')
let kt = 'package com.moyuer233.deepseeknative\n\n'
kt += 'import java.util.Locale\n\n'
kt += '/** 自动生成：改 copy/文案.mjs 后运行 node scripts/gen-copy.mjs；不要直接改本文件。 */\n'
kt += 'object NativeCopy {\n'
kt += '    /** 默认跟随系统语言；Web 侧可通过 setLanguage 覆盖为应用内语言。 */\n'
kt += '    private var en = Locale.getDefault().language == "en"\n\n'
kt += '    fun setLanguage(locale: String) {\n'
kt += '        en = locale.startsWith("en")\n'
kt += '    }\n\n'
md += '## 原生界面\n\n| 界面 | 中文 | English |\n|---|---|---|\n'
for (const item of nativeSection?.native ?? []) {
  kt += '    // 界面：' + item.where + '\n'
  kt += '    val ' + item.name + ' = if (en) ' + kq(item.en ?? item.zh) + ' else ' + kq(item.zh) + '\n\n'
  md += '| ' + item.where + ' | ' + item.zh.replace(/\|/g, '\\|') + ' | ' + (item.en ?? '（英文缺失）').replace(/\|/g, '\\|') + ' |\n'
}
kt += '}\n'

await writeFile('src/i18n/zh.ts', zh)
await writeFile('src/i18n/en.ts', en)
await writeFile('native-app/app/src/main/java/com/moyuer233/deepseeknative/NativeCopy.kt', kt)
await writeFile('docs/文案表.md', md)
console.log('generated: zh.ts en.ts NativeCopy.kt docs/文案表.md')

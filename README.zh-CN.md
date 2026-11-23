<p align="center">
  <img src="docs/assets/icon.png" width="88" alt="DeepSeek Chat Demo Android" />
</p>

<h1 align="center">DeepSeek Chat Demo Android</h1>

<div align="center">

<a href="README.md">English</a> ｜ <b>简体中文</b>

<br>

<div>
<a href="https://github.com/moyuer233/deepseek-chat-demo-android/releases/latest"><img src="https://img.shields.io/github/v/release/moyuer233/deepseek-chat-demo-android?style=flat-square&labelColor=161b22&color=2088FF" alt="release" /></a>
<a href="https://github.com/moyuer233/deepseek-chat-demo-android/actions/workflows/ci.yml"><img src="https://img.shields.io/github/actions/workflow/status/moyuer233/deepseek-chat-demo-android/ci.yml?style=flat-square&labelColor=161b22&color=2088FF&label=ci" alt="ci" /></a>
<a href="https://github.com/moyuer233/deepseek-chat-demo-android/blob/main/LICENSE"><img src="https://img.shields.io/github/license/moyuer233/deepseek-chat-demo-android?style=flat-square&labelColor=161b22&color=3DA639" alt="license" /></a>
<a href="https://github.com/moyuer233/deepseek-chat-demo-android/releases"><img src="https://img.shields.io/badge/Android-7.0%2B-3DDC84?style=flat-square&labelColor=161b22" alt="android" /></a>
<a href="https://github.com/moyuer233/deepseek-chat-demo-android/releases"><img src="https://img.shields.io/badge/APK-2.9MB-2088FF?style=flat-square&labelColor=161b22" alt="apk" /></a>
<a href="https://github.com/moyuer233/deepseek-chat-demo-android/releases"><img src="https://img.shields.io/github/downloads/moyuer233/deepseek-chat-demo-android/total?style=flat-square&labelColor=161b22&color=3DA639&label=%E4%B8%8B%E8%BD%BD" alt="downloads" /></a>
<a href="https://github.com/moyuer233/deepseek-chat-demo-android/releases/latest"><img src="https://img.shields.io/github/downloads/moyuer233/deepseek-chat-demo-android/latest/total?style=flat-square&labelColor=161b22&color=3DA639&label=latest" alt="latest downloads" /></a>
<a href="https://github.com/moyuer233/deepseek-chat-demo-android/releases"><img src="https://img.shields.io/github/release-date/moyuer233/deepseek-chat-demo-android?style=flat-square&labelColor=161b22&color=2088FF" alt="release date" /></a>
<a href="https://github.com/moyuer233/deepseek-chat-demo-android/commits/main"><img src="https://img.shields.io/github/last-commit/moyuer233/deepseek-chat-demo-android?style=flat-square&labelColor=161b22&color=2088FF" alt="last commit" /></a>
<a href="https://github.com/moyuer233/deepseek-chat-demo-android/stargazers"><img src="https://img.shields.io/github/stars/moyuer233/deepseek-chat-demo-android?style=flat-square&labelColor=161b22&color=F5B301" alt="stars" /></a>
</div>

<br>

<a href="https://moyuer233.github.io/deepseek-chat-demo-android/">官网</a> ｜
<a href="https://github.com/moyuer233/deepseek-chat-demo-android/releases">下载</a> ｜
<a href="https://github.com/moyuer233/deepseek-chat-demo-android/issues">问题反馈</a> ｜
<a href="https://github.com/deepseek-ai/chat-demo">上游 chat-demo</a>

</div>

DeepSeek Chat Demo Android 是一个开源的非官方安卓客户端：用**你自己的 API Key 或 DeepSeek 开放平台账号授权**聊天，额度与账单都在你自己账号里，因此不受官方免费版限流影响；同时把 chat.deepseek.com 的历史会话整包导入本机，随时可再导出带走。网页内核复用 chat-demo（MIT），原生外壳用 Kotlin + Compose 编写。

<img src="docs/assets/shot-chat.png" width="260" alt="聊天页" />

## 和官方 App 的区别

官方 App 里有的聊天功能（深度思考、智能搜索、附件、朗读、语音、消息分支、会话管理……）这里都有，不再逐条列。下面只写不一样的地方。

| | 官方 App | 本项目 |
| --- | --- | --- |
| 额度来源 | 官方免费额度，用完排队等恢复 | **你自己的 API 额度**，不走免费版限流入口 |
| 上下文 | 受套餐限制 | **1M**，状态行实时显示占用；接近上限只在本机压摘要，**原文不删** |
| 历史数据 | 留在官方账号里 | **整包导入本机**，导入前给差异预览，随时可再导出 ZIP |
| 付费 | 官方内购 | 打开官方充值页，识别支付码后按你选的微信/支付宝拉起对应 App（可选扫码辅助） |
| 内核 | 官方闭源 | **开源**：网页内核复用 chat-demo（MIT），原生外壳 Kotlin + Compose |

## 界面

<table>
  <tr>
    <td align="center"><img src="docs/assets/shot-chat.png" width="220" alt="聊天页" /><br />聊天：状态行显示上下文占用</td>
    <td align="center"><img src="docs/assets/shot-sider.png" width="220" alt="侧栏" /><br />侧栏：搜索 + 按时间分组的会话</td>
  </tr>
  <tr>
    <td align="center"><img src="docs/assets/shot-settings.png" width="220" alt="设置页" /><br />设置：账户 / 应用 / 语音 / 关于</td>
    <td align="center"><img src="docs/assets/shot-account.png" width="220" alt="账户页" /><br />账户：余额、充值、API Key</td>
  </tr>
</table>

## 快速开始

### 直接安装

从 [Releases](https://github.com/moyuer233/deepseek-chat-demo-android/releases) 下载 APK（当前 **1.1.3**，versionCode 9，约 2.9 MB），Android 7.0 及以上，自签名。

装好后二选一：填自己的 API Key，或用 DeepSeek 开放平台账号授权（免 Key）。

### 从源码构建

需要 Node.js、pnpm、JDK 21、Android SDK 36：

```bash
pnpm install && pnpm build
rm -rf native-app/app/src/main/assets/web && mkdir -p native-app/app/src/main/assets/web
cp -r dist/. native-app/app/src/main/assets/web/
cd native-app && ./gradlew assembleRelease   # 签名配置在 native-app/keystore.properties
```

### 导入历史会话

1. 在浏览器打开 [chat.deepseek.com](https://chat.deepseek.com)，从官方导出入口拿到会话 ZIP。
2. App 里进入「设置 → 数据管理 → 导入历史对话」，选择该 ZIP。
3. 确认「新增 / 有更新 / 本地独有」差异后导入。

## 项目结构

| 路径 | 说明 |
| --- | --- |
| `src/` | chat-demo 网页客户端（React 18 + rspack） |
| `native-app/` | 安卓原生外壳（Kotlin + Compose + WebView + `window.NativeShell` 桥） |
| `copy/文案.mjs` | 网页与原生文案的唯一来源，`node scripts/gen-copy.mjs` 生成各端文件 |
| `site/` | 官网静态页（GitHub Pages 发布） |
| `docs/` | 截图与调研记录 |

## 已知限制

- 长会话目前只渲染最近 25 条，更早的消息按需展开；虚拟滚动尚未实现。
- 没有自动化测试，回归主要靠手动与代码审查。
- 充值页只返回银联聚合码，微信/支付宝的渠道靠页面选择与二维码内容判断。

## 说明

- 聊天请求直连 `api.deepseek.com`，不经过本项目的中转服务器；额度与计费归你的账号。
- 会话与导入的历史存在手机本地（IndexedDB）；登录在官方授权页完成，App 不接触密码，授权 token 由 Android Keystore 加密保存。
- 「扫码辅助」是可选的无障碍服务，默认关闭，只在充值拉起微信/支付宝后的 20 秒内动作，不读取、不存储聊天或支付信息。

## 许可

MIT © moyuer233。网页内核复用自 MIT 许可的 [chat-demo](https://github.com/deepseek-ai/chat-demo)，其版权声明保留在 [NOTICE](NOTICE)。DeepSeek 名称与图标归深度求索所有，本项目为非官方项目，与 DeepSeek 无隶属或背书关系。

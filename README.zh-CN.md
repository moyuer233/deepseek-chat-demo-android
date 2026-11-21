<p align="center">
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android">
    <img src="docs/assets/icon.png" width="112" alt="DeepSeek Chat Demo" />
  </a>
</p>

<h1 align="center">DeepSeek Chat Demo</h1>

<p align="center"><i>把 DeepSeek 装进安卓：1M 上下文、会话导入导出、账号授权免 Key，不排官方 App 的队。</i></p>

<p align="center">
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android/releases/latest"><img src="https://img.shields.io/github/v/release/moyuer233/deepseek-chat-demo-android?style=flat-square&labelColor=161b22&logo=github&logoColor=white&color=2088FF" alt="release" /></a>
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android/actions/workflows/ci.yml"><img src="https://img.shields.io/github/actions/workflow/status/moyuer233/deepseek-chat-demo-android/ci.yml?style=flat-square&labelColor=161b22&logo=githubactions&logoColor=white&color=2088FF&label=ci" alt="ci" /></a>
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android/blob/main/LICENSE"><img src="https://img.shields.io/github/license/moyuer233/deepseek-chat-demo-android?style=flat-square&labelColor=161b22&logo=opensourceinitiative&logoColor=white&color=3DA639" alt="license" /></a>
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android/releases"><img src="https://img.shields.io/badge/Android-7.0%2B-3DDC84?style=flat-square&labelColor=161b22&logo=android&logoColor=white" alt="android" /></a>
</p>

<p align="center">English | <b>简体中文</b></p>

> 基于 [deepseek-ai/chat-demo](https://github.com/deepseek-ai/chat-demo)（MIT）二次开发的**非官方**安卓客户端，与 DeepSeek 官方无隶属或背书关系。

## 不排官方 App 的队

| 官方 App：额度到顶要等 | 本 App：1M 上下文，照常聊 |
| --- | --- |
| <img src="docs/assets/screens-official-limit.jpg" width="260" alt="官方 App 额度上限提示" /> | <img src="docs/assets/screens-chat-1m.png" width="260" alt="本 App 聊天页：上下文 8.2K / 1M" /> |

官方 App 在用量到顶时会提示「服务器繁忙，使用额度已达上限」（左图）。本 App 直接调用 `api.deepseek.com` 的聊天接口：用你自己的 DeepSeek 账号授权或 API Key，**上下文窗口 1M token**，状态行实时显示「上下文 8.2K / 1M」；接近上限时只在本机把旧对话整理成摘要，最近消息原样保留，**本机聊天记录一条不删**，随时可以往上翻。

调用仍然消耗你账号的 API 额度——不排队，但也不是免费无限。

## 功能

- **账号授权免 Key**：登录 chat.deepseek.com 一次，导出并导入全部会话；之后用账号 grant 聊天，也可以手动填 API Key 兜底。
- **1M 上下文 + 本机压缩**：接近上限时把旧区间整理成摘要，只影响发给模型的内容，本机记录完整保留。
- **聊天能力**：深度思考、智能搜索、图片/文件上传、朗读、语音输入、Markdown（代码/表格/公式/图表）。
- **会话管理**：消息树与分支切换、重命名/删除、搜索、导出为 `deepseek_data-*.zip`（可再次导入）。
- **充值**：桌面版充值页自动识别支付码，按你选的微信/支付宝拉起对应 App；可选的扫码辅助会在 20 秒内自动进「扫一扫 → 相册」。
- **原生外壳**：纯 WebView + `window.NativeShell`，没有 Capacitor 桥；Release 开 R8 与资源收缩，APK 约 2.9 MB。

<p align="center"><img src="docs/assets/screens-attach.png" width="520" alt="输入框附件菜单：拍照 / 相册 / 文件" /></p>

## 下载

APK 在 [Releases](https://github.com/moyuer233/deepseek-chat-demo-android/releases) 里，自签名，支持 Android 7.0 及以上。

- **1.1.0（当前，native）**：`native-app/` 的 Jetpack Compose 外壳 + chat-demo 网页内核，原生桥负责账号授权、会话导入导出、相机/文件、无障碍扫码辅助。
- **0.2.x（legacy）**：早期的 Capacitor 套壳版本，只作历史保留。

## 构建

需要 Node.js、pnpm、JDK 21、Android SDK 36。在仓库根目录：

```bash
pnpm install
pnpm build

# 把网页产物放进原生壳
rm -rf native-app/app/src/main/assets/web
mkdir -p native-app/app/src/main/assets/web
cp -r dist/. native-app/app/src/main/assets/web/

cd native-app
./gradlew assembleRelease   # 签名配置放在 native-app/keystore.properties
```

Debug 包：`cd native-app && ./gradlew assembleDebug`，产物在 `app/build/outputs/apk/debug/`。

## 结构

- `src/` — chat-demo 网页客户端（React 18 + rspack）。
- `native-app/` — 原生安卓外壳（Kotlin/Compose + WebView + NativeShell 桥）。
- `copy/文案.mjs` — 网页与原生文案的唯一来源，`node scripts/gen-copy.mjs` 生成。
- `docs/` — 界面截图与调研记录。

## 许可与说明

MIT，保留上游 LICENSE 与出处。DeepSeek 名称与图标归深度求索所有，本项目仅作非官方客户端使用。

- 扫码辅助是可选的无障碍服务，默认关闭，只在充值拉起微信/支付宝后的 20 秒内动作，不读取、不存储聊天或支付信息。
- 语音输入需要麦克风权限；选图/选文件走系统选择器。
- 聊天数据只存在本机 IndexedDB，导出的 ZIP 由你自己保管。

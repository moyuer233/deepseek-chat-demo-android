<p align="center">
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android">
    <img src="docs/assets/icon.png" width="112" alt="DeepSeek Chat Demo" />
  </a>
</p>

<h1 align="center">DeepSeek Chat Demo</h1>

<p align="center"><i>官方 App 排队的时候，这里还能接着聊。</i></p>

<p align="center">
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android/releases/latest"><img src="https://img.shields.io/github/v/release/moyuer233/deepseek-chat-demo-android?style=flat-square&labelColor=161b22&logo=github&logoColor=white&color=2088FF" alt="release" /></a>
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android/actions/workflows/ci.yml"><img src="https://img.shields.io/github/actions/workflow/status/moyuer233/deepseek-chat-demo-android/ci.yml?style=flat-square&labelColor=161b22&logo=githubactions&logoColor=white&color=2088FF&label=ci" alt="ci" /></a>
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android/blob/main/LICENSE"><img src="https://img.shields.io/github/license/moyuer233/deepseek-chat-demo-android?style=flat-square&labelColor=161b22&logo=opensourceinitiative&logoColor=white&color=3DA639" alt="license" /></a>
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android/releases"><img src="https://img.shields.io/badge/Android-7.0%2B-3DDC84?style=flat-square&labelColor=161b22&logo=android&logoColor=white" alt="android" /></a>
</p>

<p align="center">English | <b>简体中文</b></p>

> 基于 [deepseek-ai/chat-demo](https://github.com/deepseek-ai/chat-demo)（MIT）二次开发的**非官方**安卓客户端。与 DeepSeek 无隶属或背书关系。

## 官方 App 排队的时候

| 官方 App：额度到顶，只能等 | 本 App：1M 上下文，照常聊 |
| --- | --- |
| <img src="docs/assets/screens-official-limit.jpg" width="270" alt="官方 App：服务器繁忙，使用额度已达上限" /> | <img src="docs/assets/screens-chat-1m.png" width="270" alt="本 App：上下文 8.2K / 1M" /> |

官方 App 的免费额度用完后会提示「服务器繁忙，使用额度已达上限，将于 … 恢复」。本 App 不走这条路：它直接调用 `api.deepseek.com` 的聊天接口，用你**自己的 DeepSeek 账号授权**或 API Key 聊天，上下文窗口 **1M token**，输入框下方实时显示「上下文 8.2K / 1M」和累计 tokens。

接近上限时，它只在本机把较早的对话整理成摘要——最近消息原样保留，**本机聊天记录一条不删**，随时可以往上翻。

调用仍然消耗你账号的 API 额度：不排队，但不是免费无限。

## 界面

<p align="center">
  <img src="docs/assets/screens-attach.png" width="300" alt="附件菜单：拍照 / 相册 / 文件" />
  <img src="docs/assets/screens-settings.png" width="300" alt="设置页：账户、数据管理、语言、主题、朗读音色" />
</p>

## 功能

**聊天**
- 深度思考、智能搜索；图片、文件上传；朗读、语音输入。
- Markdown：代码块、表格、公式、Mermaid 图表、引用。
- 消息树与分支切换、重新生成、编辑重发、继续生成、复制、赞踩、分享。

**会话**
- 新建、重命名、删除、搜索；按 Today / Yesterday / 7 Days / 30 Days 分组。
- 导出为 `deepseek_data-*.zip`，可再次导入；导入前先给差异预览（新增/更新/仅本机）。
- 上下文压缩：手动或自动，把旧区间压成摘要，本机原文保留。

**账号与充值**
- 登录 chat.deepseek.com 一次，导入该账号全部会话；之后用账号 grant 聊天，免填 API Key，也可手动填 Key 兜底。
- 账户页显示余额、赠送额度；内置充值页识别支付码，按你选的**微信/支付宝**拉起对应 App。
- 可选「扫码辅助」：无障碍服务在 20 秒内自动点「扫一扫 → 相册」，默认关闭，随时可在系统设置里关掉。

**原生外壳**
- 纯 WebView + `window.NativeShell`，没有 Capacitor 桥；返回键逐级后退、任意位置右滑开侧栏、左滑关闭。
- 相机、相册、文件、麦克风走系统能力；Release 开 R8 与资源收缩，APK 约 2.9 MB。

## 从官方 App 换过来

1. 在浏览器打开 [chat.deepseek.com](https://chat.deepseek.com)，从官方导出入口拿到会话 ZIP。
2. 安装本 App（[Releases](https://github.com/moyuer233/deepseek-chat-demo-android/releases) 下载 APK，或本地构建）。
3. 设置 → 数据管理 → 导入历史对话，选刚才的 ZIP；确认差异预览后导入。
4. 设置 → 账户 → 登录 DeepSeek 账号（账号授权），或填 API Key。之后就能直接聊。

本机数据只在设备上（IndexedDB）；导出的 ZIP 由你自己保管。

## 下载

APK 在 [Releases](https://github.com/moyuer233/deepseek-chat-demo-android/releases)。自签名，Android 7.0+。

- **1.1.0（当前，native）**：`native-app/` 的 Compose 外壳 + chat-demo 网页内核，原生桥接账号授权、导入导出、相机/文件、无障碍扫码辅助。
- **0.2.x（legacy）**：早期 Capacitor 套壳版本，只作历史保留。

## 构建

Node.js、pnpm、JDK 21、Android SDK 36。仓库根目录：

```bash
pnpm install
pnpm build

# 网页产物放进原生壳
rm -rf native-app/app/src/main/assets/web
mkdir -p native-app/app/src/main/assets/web
cp -r dist/. native-app/app/src/main/assets/web/

cd native-app
./gradlew assembleRelease   # 签名配置：native-app/keystore.properties
```

Debug：`cd native-app && ./gradlew assembleDebug`，产物在 `app/build/outputs/apk/debug/`。

## 结构

- `src/` — chat-demo 网页客户端（React 18 + rspack）。
- `native-app/` — 安卓原生外壳（Kotlin/Compose + WebView + NativeShell 桥）。
- `copy/文案.mjs` — 网页与原生文案的唯一来源；`node scripts/gen-copy.mjs` 生成。
- `docs/` — 截图与调研记录。

## 许可与隐私

MIT，保留上游 LICENSE 与出处。DeepSeek 名称与图标归深度求索所有，本项目仅作非官方客户端使用。

- 扫码辅助是可选的无障碍服务，默认关闭；只在充值拉起微信/支付宝后的 20 秒内动作，不读取、不存储聊天或支付信息。
- 语音输入需要麦克风权限；图片和文件走系统选择器。
- 聊天记录只存在本机；没有账号密码上传，授权 token 由 Android Keystore 加密保存。

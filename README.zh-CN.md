<p align="center">
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android">
    <img src="docs/assets/icon.png" width="128" alt="DeepSeek Chat Demo" />
  </a>
</p>

<h1 align="center">DeepSeek Chat Demo</h1>

<p align="center"><i>✨ DeepSeek 网页版聊天，装进安卓 App ✨</i></p>

<p align="center">
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android/releases/latest"><img src="https://img.shields.io/github/v/release/moyuer233/deepseek-chat-demo-android?style=flat-square&labelColor=161b22&logo=github&logoColor=white&color=2088FF" alt="release" /></a>
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android/blob/main/LICENSE"><img src="https://img.shields.io/github/license/moyuer233/deepseek-chat-demo-android?style=flat-square&labelColor=161b22&logo=opensourceinitiative&logoColor=white&color=3DA639" alt="license" /></a>
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android/actions/workflows/pages.yml"><img src="https://img.shields.io/github/actions/workflow/status/moyuer233/deepseek-chat-demo-android/pages.yml?style=flat-square&labelColor=161b22&logo=githubactions&logoColor=white&color=2088FF&label=pages" alt="pages" /></a>
</p>

[English](README.md) | 简体中文

基于 [deepseek-ai/chat-demo](https://github.com/deepseek-ai/chat-demo)（MIT）二次开发的非官方安卓 App，与 DeepSeek 官方无隶属或背书关系。

登录一次 DeepSeek 账号就能导入全部会话，之后用账号授权直接聊天，不用手动填 API Key。

## 两个版本

- **0.2.x（当前发布版）** — Capacitor 版：chat-demo 的网页客户端装进安卓壳。
- **0.3.0（开发中，原生版）** — `native-app/` 里的 Jetpack Compose 版，界面按官方 App 还原；内核仍然是 chat-demo 那套：DSH 账号授权（API Key 兜底）、chat-demo 归档导入导出、本地会话存储。暂未发布，可按下文在本地构建。

## 功能

- 登录 chat.deepseek.com 后，通过官方导出接口导入该账号的全部会话。
- 用账号 grant 聊天，不需要 API Key；也可以手动填 Key 兜底。
- 聊天页支持深度思考、智能搜索、图片上传和朗读。
- 内置充值页能识别支付二维码，把码存进相册并拉起微信。

## 下载

APK 在 [Releases](https://github.com/moyuer233/deepseek-chat-demo-android/releases) 里，自签名，支持 Android 7.0 及以上。

## 说明

- 聊天消耗 API 额度，账号没余额时会返回 402，App 里会给出充值入口。
- 微信支付二维码是银联码（qr.95516.com）。App 能打开微信，码本身需要在微信里扫。
- 语音输入需要麦克风权限；选图和选文件都走系统选择器。

## 构建

在仓库根目录执行，需要 Node.js、pnpm、JDK 21 和 Android SDK 36。

```bash
pnpm install
pnpm build
pnpm exec cap sync android
cd android && ./gradlew assembleDebug
```

debug APK 在 `android/app/build/outputs/apk/debug/app-debug.apk`；release 需要你自己的签名文件。

原生版（`0.3.0`）：

```bash
cd native-app
./gradlew assembleDebug
```

APK 在 `native-app/app/build/outputs/apk/debug/app-debug.apk`。

## 结构

- `src/` — chat-demo 原有 Web 客户端和安卓适配层。
- `src/services/deepseek/` — 账号流程、导入导出、原生桥接。
- `android/` — Capacitor 壳、登录/OAuth/充值三个 Activity。
- `native-app/` — 0.3.0 原生版（Compose）：界面还原官方 App，内核仍是 chat-demo。
- `docs/research/` — 接口与 UX 调研记录。

## 许可

MIT，基于 [deepseek-ai/chat-demo](https://github.com/deepseek-ai/chat-demo)，保留原版权声明，见 [LICENSE](LICENSE)。

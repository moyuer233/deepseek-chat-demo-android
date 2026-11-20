# DeepSeek Chat Demo（安卓版）

[English](README.md) | 简体中文

基于 [deepseek-ai/chat-demo](https://github.com/deepseek-ai/chat-demo)（MIT）二次开发的**非官方**安卓 App。

> 本项目与 DeepSeek 官方无隶属、无背书关系；"DeepSeek" 仅用于说明接口兼容性。

## 功能

- **登录即导入**：在原生 WebView 里登录 `chat.deepseek.com`，走官方导出接口一键导入全部会话（与 chat-demo 相同的 v3 归档格式）。
- **免 API Key 聊天**：用与 DeepSeek Harness 相同的账号 OAuth（PKCE，`auth_init` → `/dsh/authorize` → `auth_exchange`）拿到 grant，聊天请求带 `x-dsh-auth-token`；也保留手动填 API Key 的兜底。
- **充值页微信二维码识别（实验）**：桌面 UA 打开 platform 充值页，用 jsQR 解出支付二维码，`weixin://` 链接交给微信打开。
- 原生体验：返回键逐级后退、edge-to-edge 安全区、导出走系统分享、登录前条款确认。

## 状态

- M1（安卓壳 + 聊天界面 + 手动 Key）：完成。
- M2（登录导入全部会话 + 账号 grant 聊天）：导入流程已实现；账号 grant 已在模拟器实测（`GET /models`、`POST /anthropic/v1/messages` 均 200）。
- 真实账号的导入全链路、充值二维码链路仍在验证中。
- 暂未上架；从源码构建 debug APK。

## 构建

依赖：Node.js、pnpm、JDK 21、Android SDK（compileSdk 36）。

```bash
pnpm install
pnpm build
pnpm exec cap sync android
cd android && ./gradlew assembleDebug
```

产物：`android/app/build/outputs/apk/debug/app-debug.apk`。

## 结构

- `src/` — chat-demo 原有 Web 客户端 + 少量安卓适配层。
- `src/services/deepseek/` — 账号流程、导入导出、原生插件桥接。
- `android/` — Capacitor 壳、`DeepSeekAccountPlugin`、登录/OAuth/充值三个 WebView Activity。
- `docs/research/` — 实现所依据的接口与 UX 调研。

## 致谢与许可

基于 [deepseek-ai/chat-demo](https://github.com/deepseek-ai/chat-demo)（MIT），见 [LICENSE](LICENSE)，保留原版权声明。

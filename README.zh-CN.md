<p align="center">
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android">
    <img src="docs/assets/icon.png" width="96" alt="DeepSeek Chat Demo" />
  </a>
</p>

<h1 align="center">DeepSeek Chat Demo</h1>

<p align="center"><i>用 API 跑一个「免费版 DeepSeek」：不限流，历史也能带走。</i></p>

<p align="center">
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android/releases/latest"><img src="https://img.shields.io/github/v/release/moyuer233/deepseek-chat-demo-android?style=flat-square&labelColor=161b22&color=2088FF" alt="release" /></a>
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android/actions/workflows/ci.yml"><img src="https://img.shields.io/github/actions/workflow/status/moyuer233/deepseek-chat-demo-android/ci.yml?style=flat-square&labelColor=161b22&color=2088FF&label=ci" alt="ci" /></a>
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android/blob/main/LICENSE"><img src="https://img.shields.io/github/license/moyuer233/deepseek-chat-demo-android?style=flat-square&labelColor=161b22&color=3DA639" alt="license" /></a>
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android/releases"><img src="https://img.shields.io/badge/Android-7.0%2B-3DDC84?style=flat-square&labelColor=161b22" alt="android" /></a>
</p>

<p align="center">English | <b>简体中文</b></p>

> 独立项目，复用 [chat-demo](https://github.com/deepseek-ai/chat-demo)（MIT）的网页内核；非官方，与 DeepSeek 无隶属关系。

直接用你自己的 API Key 或 DeepSeek 账号授权聊天，额度走自己的账号，不受官方免费版限流；从 chat.deepseek.com 导入全部历史会话，本机保存。

## 功能

- 1M 上下文，状态行实时显示占用；接近上限只在本机压摘要，原文不删。
- 深度思考、智能搜索、图片/文件上传、朗读、语音输入、Markdown。
- 消息分支、重新生成、编辑重发、复制/赞踩/分享。
- 导入/导出 `deepseek_data-*.zip`；余额、充值、可选扫码辅助。
- Android 7.0+，自签名，APK 约 2.9 MB。

## 下载

[Releases](https://github.com/moyuer233/deepseek-chat-demo-android/releases)（当前 1.1.0）· [官网](https://moyuer233.github.io/deepseek-chat-demo-android/)

## 构建

需要 Node.js、pnpm、JDK 21、Android SDK 36：

```bash
pnpm install && pnpm build
rm -rf native-app/app/src/main/assets/web && mkdir -p native-app/app/src/main/assets/web
cp -r dist/. native-app/app/src/main/assets/web/
cd native-app && ./gradlew assembleRelease
```

## 许可

MIT，保留上游 LICENSE 与署名。DeepSeek 名称与图标归深度求索所有。

<p align="center">
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android">
    <img src="docs/assets/icon.png" width="96" alt="DeepSeek Chat Demo" />
  </a>
</p>

<h1 align="center">DeepSeek Chat Demo</h1>

<p align="center"><i>A “free-tier” DeepSeek on your own API: no throttling, and your history comes along.</i></p>

<p align="center">
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android/releases/latest"><img src="https://img.shields.io/github/v/release/moyuer233/deepseek-chat-demo-android?style=flat-square&labelColor=161b22&color=2088FF" alt="release" /></a>
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android/actions/workflows/ci.yml"><img src="https://img.shields.io/github/actions/workflow/status/moyuer233/deepseek-chat-demo-android/ci.yml?style=flat-square&labelColor=161b22&color=2088FF&label=ci" alt="ci" /></a>
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android/blob/main/LICENSE"><img src="https://img.shields.io/github/license/moyuer233/deepseek-chat-demo-android?style=flat-square&labelColor=161b22&color=3DA639" alt="license" /></a>
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android/releases"><img src="https://img.shields.io/badge/Android-7.0%2B-3DDC84?style=flat-square&labelColor=161b22" alt="android" /></a>
</p>

<p align="center"><b>English</b> | <a href="README.zh-CN.md">简体中文</a></p>

> An independent project that reuses the [chat-demo](https://github.com/deepseek-ai/chat-demo) (MIT) web core. Unofficial; not affiliated with DeepSeek.

Chat with your own API key or DeepSeek account grant — the quota is yours, so the free tier's throttling does not apply. Import your whole chat.deepseek.com history and keep it on the device.

## Features

- 1M context with live usage in the status line; near the limit only older turns are compacted on-device, originals kept.
- DeepThink, web search, image/file upload, read-aloud, voice input, Markdown.
- Message branches, regenerate, edit and resend, copy/vote/share.
- Import and export `deepseek_data-*.zip`; balance, top-up, optional Scan assist.
- Android 7.0+, self-signed, APK about 2.9 MB.

## Download

[Releases](https://github.com/moyuer233/deepseek-chat-demo-android/releases) (current 1.1.0) · [Website](https://moyuer233.github.io/deepseek-chat-demo-android/)

## Build

Node.js, pnpm, JDK 21 and Android SDK 36:

```bash
pnpm install && pnpm build
rm -rf native-app/app/src/main/assets/web && mkdir -p native-app/app/src/main/assets/web
cp -r dist/. native-app/app/src/main/assets/web/
cd native-app && ./gradlew assembleRelease
```

## Licence

MIT, with the upstream LICENSE and attribution kept. The DeepSeek name and logo belong to DeepSeek.

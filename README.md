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

English | [简体中文](README.zh-CN.md)

Unofficial Android app built on [deepseek-ai/chat-demo](https://github.com/deepseek-ai/chat-demo) (MIT). Not affiliated with or endorsed by DeepSeek.

Sign in to chat.deepseek.com once, import every conversation, then chat through the account grant instead of filling in an API key.

## Features

- Sign in to chat.deepseek.com and import all conversations through the official export API.
- Chat with the account grant (no API key); a manual API key still works as a fallback.
- DeepThink, web search, image upload and read-aloud work in the chat screen.
- Built-in top-up page detects the payment QR, saves it to the album and opens WeChat.

## Download

Grab the APK from [Releases](https://github.com/moyuer233/deepseek-chat-demo-android/releases). Self-signed, Android 7.0+.

## Notes

- Chat uses API quota. An account without balance gets a 402 and the app shows the top-up entry.
- The WeChat QR is a UnionPay code (qr.95516.com). The app can open WeChat; the code has to be scanned inside WeChat.
- Voice input needs microphone permission; photos and files go through the system pickers.

## Build

Run from the repository root with Node.js, pnpm, JDK 21 and Android SDK 36.

```bash
pnpm install
pnpm build
pnpm exec cap sync android
cd android && ./gradlew assembleDebug
```

The debug APK is at `android/app/build/outputs/apk/debug/app-debug.apk`. Release builds need your own keystore.

## Structure

- `src/` — chat-demo web client plus the Android adaptation layer.
- `src/services/deepseek/` — account flow, export/import glue, native bridge.
- `android/` — Capacitor shell, login/OAuth/top-up activities.
- `docs/research/` — API and UX notes.

## Licence

MIT. Based on [deepseek-ai/chat-demo](https://github.com/deepseek-ai/chat-demo); the original copyright notice is kept in [LICENSE](LICENSE).

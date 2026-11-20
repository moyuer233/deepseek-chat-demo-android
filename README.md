# DeepSeek Chat Demo (Android)

English | [简体中文](README.zh-CN.md)

An unofficial Android app built on top of [deepseek-ai/chat-demo](https://github.com/deepseek-ai/chat-demo) (MIT).

> This project is not affiliated with, endorsed by, or supported by DeepSeek. "DeepSeek" is used only to describe interoperability.

## What it does

- **Sign in and import everything.** Sign in to `chat.deepseek.com` in a native WebView, then import all conversations through the official export API (same v3 archive format chat-demo already reads).
- **Chat without an API key.** The app signs in to the DeepSeek account through the same OAuth flow DeepSeek Harness uses (PKCE, `auth_init` → `/dsh/authorize` → `auth_exchange`) and sends the account grant as `x-dsh-auth-token`. An API key can still be entered manually as a fallback.
- **Top-up page with WeChat QR detection (experimental).** Opens the platform top-up page with a desktop user agent, decodes the payment QR on the page with jsQR and hands the `weixin://` link to WeChat.
- Native Android behaviour: back-button hierarchy, edge-to-edge safe areas, system share for exported archives, terms confirmation before sign-in.

## Download

Prebuilt APK: [Releases · v0.1.0-android](https://github.com/moyuer233/deepseek-chat-demo-android/releases/tag/v0.1.0-android) (`app-release-signed.apk`, self-signed, Android 7.0+).

## Status

- M1 (Android shell, chat UI, manual API key): done.
- M2 (sign in → import all chats; account grant chat): import flow implemented; account grant verified on an emulator (`GET /models` and `POST /anthropic/v1/messages` both 200).
- Import with a real `chat.deepseek.com` account and the WeChat payment QR path are still being verified.
- No store release yet; build a debug APK from source.

## Build

Requirements: Node.js, pnpm, JDK 21, Android SDK (compileSdk 36).

```bash
pnpm install
pnpm build                 # builds the web bundle into dist/
pnpm exec cap sync android
cd android && ./gradlew assembleDebug
```

The APK is written to `android/app/build/outputs/apk/debug/app-debug.apk`.

## How it fits together

- `src/` — the unmodified chat-demo web client plus a small Android adaptation layer.
- `src/services/deepseek/` — account flow, export/import glue, native plugin bridge.
- `android/` — Capacitor shell, `DeepSeekAccountPlugin`, login/OAuth/top-up WebView activities.
- `docs/research/` — the API and UX research this implementation is based on.

## Credits and licence

Based on [deepseek-ai/chat-demo](https://github.com/deepseek-ai/chat-demo), MIT. See [LICENSE](LICENSE). The original copyright notice is kept in that file.

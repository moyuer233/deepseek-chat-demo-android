<p align="center">
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android">
    <img src="docs/assets/icon.png" width="112" alt="DeepSeek Chat Demo" />
  </a>
</p>

<h1 align="center">DeepSeek Chat Demo</h1>

<p align="center"><i>DeepSeek in your pocket: 1M context, chat import/export, sign-in without an API key — no queue behind the official app.</i></p>

<p align="center">
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android/releases/latest"><img src="https://img.shields.io/github/v/release/moyuer233/deepseek-chat-demo-android?style=flat-square&labelColor=161b22&logo=github&logoColor=white&color=2088FF" alt="release" /></a>
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android/actions/workflows/ci.yml"><img src="https://img.shields.io/github/actions/workflow/status/moyuer233/deepseek-chat-demo-android/ci.yml?style=flat-square&labelColor=161b22&logo=githubactions&logoColor=white&color=2088FF&label=ci" alt="ci" /></a>
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android/blob/main/LICENSE"><img src="https://img.shields.io/github/license/moyuer233/deepseek-chat-demo-android?style=flat-square&labelColor=161b22&logo=opensourceinitiative&logoColor=white&color=3DA639" alt="license" /></a>
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android/releases"><img src="https://img.shields.io/badge/Android-7.0%2B-3DDC84?style=flat-square&labelColor=161b22&logo=android&logoColor=white" alt="android" /></a>
</p>

<p align="center"><b>English</b> | <a href="README.zh-CN.md">简体中文</a></p>

> An **unofficial** Android client built on [deepseek-ai/chat-demo](https://github.com/deepseek-ai/chat-demo) (MIT). Not affiliated with, endorsed by, or sponsored by DeepSeek.

## No queue behind the official app

| Official app: wait for the quota | This app: 1M context, keep chatting |
| --- | --- |
| <img src="docs/assets/screens-official-limit.jpg" width="260" alt="Official app quota message" /> | <img src="docs/assets/screens-chat-1m.png" width="260" alt="This app: context 8.2K / 1M" /> |

The official app shows "server busy, usage limit reached" when its quota runs out (left). This app talks to `api.deepseek.com` directly with your own account grant or API key, with a **1M-token context window**; the status line under the composer shows "context 8.2K / 1M" as you go. Near the limit it only compacts older turns into a summary **on your device** — recent messages stay as they are, and your local chat history is never deleted.

Calls still count against your own API quota: no queue, but not unlimited free usage.

## Features

- **Sign in, no API key**: sign in to chat.deepseek.com once, import every conversation, then chat with the account grant. A manual API key still works as a fallback.
- **1M context + on-device compaction**: older turns are summarized near the limit; only what is sent to the model changes, local history stays complete.
- **Chat**: DeepThink, web search, image/file upload, read-aloud, voice input, Markdown (code, tables, math, diagrams).
- **Sessions**: message tree with branch switching, rename/delete, search, export to `deepseek_data-*.zip` (importable again).
- **Top-up**: the desktop top-up page detects the payment QR and opens the WeChat or Alipay app you picked; the optional Scan assist jumps to Scan → Album within 20 seconds.
- **Native shell**: plain WebView plus `window.NativeShell`, no Capacitor bridge. Release builds use R8 and resource shrinking; the APK is about 2.9 MB.

<p align="center"><img src="docs/assets/screens-attach.png" width="520" alt="Attachment menu: camera, gallery, files" /></p>

## Download

Grab the APK from [Releases](https://github.com/moyuer233/deepseek-chat-demo-android/releases). Self-signed, Android 7.0+.

- **1.1.0 (current, native)** — the Jetpack Compose shell in `native-app/` around the chat-demo web core, with native bridges for account grant, chat import/export, camera/files and the optional accessibility scan assist.
- **0.2.x (legacy)** — the early Capacitor wrapper, kept for history.

## Build

Node.js, pnpm, JDK 21 and Android SDK 36 are required. From the repository root:

```bash
pnpm install
pnpm build

# copy the web bundle into the native shell
rm -rf native-app/app/src/main/assets/web
mkdir -p native-app/app/src/main/assets/web
cp -r dist/. native-app/app/src/main/assets/web/

cd native-app
./gradlew assembleRelease   # signing config lives in native-app/keystore.properties
```

Debug build: `cd native-app && ./gradlew assembleDebug`, output in `app/build/outputs/apk/debug/`.

## Structure

- `src/` — the chat-demo web client (React 18 + rspack).
- `native-app/` — the Android shell (Kotlin/Compose, WebView and the NativeShell bridge).
- `copy/文案.mjs` — the single source of UI copy for web and native; run `node scripts/gen-copy.mjs`.
- `docs/` — screenshots and research notes.

## Licence

MIT, with the upstream LICENSE and attribution kept. The DeepSeek name and logo belong to DeepSeek; this project only uses them for an unofficial client.

- Scan assist is an optional accessibility service, off by default, active only for 20 seconds after the top-up flow opens WeChat/Alipay. It does not read or store chats or payment details.
- Voice input needs microphone permission; images and files use the system pickers.
- Chats live in the local IndexedDB only; exported ZIP files are yours to keep.

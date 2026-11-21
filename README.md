<p align="center">
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android">
    <img src="docs/assets/icon.png" width="112" alt="DeepSeek Chat Demo" />
  </a>
</p>

<h1 align="center">DeepSeek Chat Demo</h1>

<p align="center"><i>When the official app makes you wait, this one keeps talking.</i></p>

<p align="center">
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android/releases/latest"><img src="https://img.shields.io/github/v/release/moyuer233/deepseek-chat-demo-android?style=flat-square&labelColor=161b22&logo=github&logoColor=white&color=2088FF" alt="release" /></a>
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android/actions/workflows/ci.yml"><img src="https://img.shields.io/github/actions/workflow/status/moyuer233/deepseek-chat-demo-android/ci.yml?style=flat-square&labelColor=161b22&logo=githubactions&logoColor=white&color=2088FF&label=ci" alt="ci" /></a>
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android/blob/main/LICENSE"><img src="https://img.shields.io/github/license/moyuer233/deepseek-chat-demo-android?style=flat-square&labelColor=161b22&logo=opensourceinitiative&logoColor=white&color=3DA639" alt="license" /></a>
  <a href="https://github.com/moyuer233/deepseek-chat-demo-android/releases"><img src="https://img.shields.io/badge/Android-7.0%2B-3DDC84?style=flat-square&labelColor=161b22&logo=android&logoColor=white" alt="android" /></a>
</p>

<p align="center"><b>English</b> | <a href="README.zh-CN.md">简体中文</a></p>

> An **unofficial** Android client built on [deepseek-ai/chat-demo](https://github.com/deepseek-ai/chat-demo) (MIT). Not affiliated with, endorsed by, or sponsored by DeepSeek.

## When the official app makes you wait

| Official app: quota reached, wait it out | This app: 1M context, keep chatting |
| --- | --- |
| <img src="docs/assets/screens-official-limit.jpg" width="270" alt="Official app: server busy, usage limit reached" /> | <img src="docs/assets/screens-chat-1m.png" width="270" alt="This app: context 8.2K / 1M" /> |

When the official app's free quota runs out it says the server is busy and the usage limit has been reached. This app takes a different route: it calls `api.deepseek.com` with **your own DeepSeek account grant or API key**, with a **1M-token context window**, and shows "context 8.2K / 1M" plus total tokens right under the composer.

Near the limit it only compacts older turns into a summary **on your device** — recent messages stay untouched and your local chat history is never deleted.

Calls still count against your own API quota: no queue, but not unlimited free usage.

## Screens

<p align="center">
  <img src="docs/assets/screens-attach.png" width="300" alt="Attachment menu: camera, gallery, files" />
  <img src="docs/assets/screens-settings.png" width="300" alt="Settings: account, data, language, theme, voice" />
</p>

## Features

**Chat**
- DeepThink, web search, image and file upload, read-aloud, voice input.
- Markdown: code, tables, math, Mermaid diagrams, citations.
- Message tree and branch switching, regenerate, edit and resend, continue, copy, vote, share.

**Sessions**
- New, rename, delete, search; grouped as Today / Yesterday / 7 Days / 30 Days.
- Export to `deepseek_data-*.zip` and import it again; a diff preview (added / updated / local-only) comes first.
- Context compaction, manual or automatic: older turns become a summary, local originals stay.

**Account and top-up**
- Sign in to chat.deepseek.com once and import every conversation; chat with the account grant, no API key needed, and a manual key still works.
- Balance and bonus shown in the account page. The built-in top-up page detects the payment QR and opens the **WeChat or Alipay** app you picked.
- Optional Scan assist: an accessibility service that taps "Scan → Album" for you within 20 seconds. Off by default, can be turned off in Android settings.

**Native shell**
- Plain WebView plus `window.NativeShell`, no Capacitor bridge. Back goes up one level at a time, right-swipe opens the sidebar anywhere, left-swipe closes it.
- Camera, gallery, files and microphone use the system UI. Release builds use R8 and resource shrinking; the APK is about 2.9 MB.

## Moving over from the official app

1. Open [chat.deepseek.com](https://chat.deepseek.com) in a browser and export your chats to a ZIP.
2. Install this app from [Releases](https://github.com/moyuer233/deepseek-chat-demo-android/releases) (or build it below).
3. Settings → Data → Import chat history, pick the ZIP, check the diff preview and import.
4. Settings → Account → sign in with your DeepSeek account (account grant) or paste an API key. Then just chat.

Local data stays on the device (IndexedDB); exported ZIP files are yours to keep.

## Download

Grab the APK from [Releases](https://github.com/moyuer233/deepseek-chat-demo-android/releases). Self-signed, Android 7.0+.

- **1.1.0 (current, native)** — the Compose shell in `native-app/` around the chat-demo web core, with native bridges for account grant, import/export, camera/files and the optional accessibility scan assist.
- **0.2.x (legacy)** — the early Capacitor wrapper, kept for history.

## Build

Node.js, pnpm, JDK 21 and Android SDK 36. From the repository root:

```bash
pnpm install
pnpm build

# copy the web bundle into the native shell
rm -rf native-app/app/src/main/assets/web
mkdir -p native-app/app/src/main/assets/web
cp -r dist/. native-app/app/src/main/assets/web/

cd native-app
./gradlew assembleRelease   # signing config: native-app/keystore.properties
```

Debug build: `cd native-app && ./gradlew assembleDebug`, output in `app/build/outputs/apk/debug/`.

## Structure

- `src/` — the chat-demo web client (React 18 + rspack).
- `native-app/` — the Android shell (Kotlin/Compose, WebView and the NativeShell bridge).
- `copy/文案.mjs` — the single source of UI copy for web and native; run `node scripts/gen-copy.mjs`.
- `docs/` — screenshots and research notes.

## Licence and privacy

MIT, with the upstream LICENSE and attribution kept. The DeepSeek name and logo belong to DeepSeek; this project uses them only for an unofficial client.

- Scan assist is an optional accessibility service, off by default. It is active only for 20 seconds after the top-up flow opens WeChat/Alipay and does not read or store chats or payment details.
- Voice input needs microphone permission; images and files use the system pickers.
- Chats stay on the device; no account password is uploaded, and the grant token is encrypted with Android Keystore.

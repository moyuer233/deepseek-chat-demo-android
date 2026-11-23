![DeepSeek Chat Demo Android](docs/assets/icon.png)

<div align="center">

<b>English</b> ｜ <a href="README.zh-CN.md">简体中文</a>

<br>

<div>
<a href="https://github.com/moyuer233/deepseek-chat-demo-android/releases/latest"><img src="https://img.shields.io/github/v/release/moyuer233/deepseek-chat-demo-android?style=flat-square&labelColor=161b22&color=2088FF" alt="release" /></a>
<a href="https://github.com/moyuer233/deepseek-chat-demo-android/actions/workflows/ci.yml"><img src="https://img.shields.io/github/actions/workflow/status/moyuer233/deepseek-chat-demo-android/ci.yml?style=flat-square&labelColor=161b22&color=2088FF&label=ci" alt="ci" /></a>
<a href="https://github.com/moyuer233/deepseek-chat-demo-android/blob/main/LICENSE"><img src="https://img.shields.io/github/license/moyuer233/deepseek-chat-demo-android?style=flat-square&labelColor=161b22&color=3DA639" alt="license" /></a>
<a href="https://github.com/moyuer233/deepseek-chat-demo-android/releases"><img src="https://img.shields.io/badge/Android-7.0%2B-3DDC84?style=flat-square&labelColor=161b22" alt="android" /></a>
<a href="https://github.com/moyuer233/deepseek-chat-demo-android/releases"><img src="https://img.shields.io/badge/APK-2.9MB-2088FF?style=flat-square&labelColor=161b22" alt="apk" /></a>
<a href="https://github.com/moyuer233/deepseek-chat-demo-android/stargazers"><img src="https://img.shields.io/github/stars/moyuer233/deepseek-chat-demo-android?style=flat-square&labelColor=161b22&color=F5B301" alt="stars" /></a>
</div>

<br>

<a href="https://moyuer233.github.io/deepseek-chat-demo-android/">Website</a> ｜
<a href="https://github.com/moyuer233/deepseek-chat-demo-android/releases">Download</a> ｜
<a href="https://github.com/moyuer233/deepseek-chat-demo-android/issues">Issues</a> ｜
<a href="https://github.com/deepseek-ai/chat-demo">Upstream chat-demo</a>

</div>

DeepSeek Chat Demo Android is an open-source, unofficial Android client: chat with **your own API key or DeepSeek Platform account grant**, so the quota and the bill stay on your account and the free tier's throttling does not apply. It also imports your whole chat.deepseek.com history onto the device and exports it again whenever you want. The web core is chat-demo (MIT); the native shell is Kotlin + Compose.

<img src="docs/assets/shot-chat.png" width="260" alt="Chat screen" />

## Features

1. 💬 **Your own API**: talks straight to `api.deepseek.com` with an API key or a DeepSeek Platform account grant — no relay server in between.
2. 🧠 **1M context, visible**: the status line under the composer shows the model, context usage and total tokens; near the limit only older turns are compacted on-device, and local originals are never deleted.
3. 🗂️ **Whole-history import**: export a ZIP from chat.deepseek.com, review the added / updated / local-only diff, then merge, add-only or replace; export `deepseek_data-*.zip` again any time.
4. 🔍 **DeepThink and web search**: the thinking block expands separately from the answer; search results appear as citations.
5. 🖼️ **Attachments and voice**: camera, gallery and files; images are described, PDFs are read; read answers aloud, hold to talk and release to send.
6. 🌿 **Message branches**: regenerate and edit-and-resend keep branches; switch left and right without losing older answers.
7. 💳 **Account and top-up**: balance and bonus in the account page; the built-in top-up page detects the payment QR and opens WeChat or Alipay, with an optional Scan assist that jumps to Scan → Album within 20 seconds.
8. 📱 **Native shell**: camera, gallery, files and microphone use system UI; back goes up one level, right-swipe opens the sidebar; release builds use R8, APK about 2.9 MB.
9. 🌏 **Bilingual UI**: Chinese is the source of truth; English follows it.

## Screens

<table>
  <tr>
    <td align="center"><img src="docs/assets/shot-chat.png" width="220" alt="Chat" /><br />Chat: context usage in the status line</td>
    <td align="center"><img src="docs/assets/shot-sider.png" width="220" alt="Sidebar" /><br />Sidebar: search and time-grouped chats</td>
  </tr>
  <tr>
    <td align="center"><img src="docs/assets/shot-settings.png" width="220" alt="Settings" /><br />Settings: account, app, voice, about</td>
    <td align="center"><img src="docs/assets/shot-account.png" width="220" alt="Account" /><br />Account: balance, top-up, API key</td>
  </tr>
</table>

## Quick start

### Install

Download the APK from [Releases](https://github.com/moyuer233/deepseek-chat-demo-android/releases) — current **1.1.3**, versionCode 9, about 2.9 MB, self-signed, Android 7.0+. Then either paste your own API key or authorize with a DeepSeek Platform account (no key needed).

### Build from source

Node.js, pnpm, JDK 21 and Android SDK 36 are required:

```bash
pnpm install && pnpm build
rm -rf native-app/app/src/main/assets/web && mkdir -p native-app/app/src/main/assets/web
cp -r dist/. native-app/app/src/main/assets/web/
cd native-app && ./gradlew assembleRelease   # signing config in native-app/keystore.properties
```

### Import your history

1. Open [chat.deepseek.com](https://chat.deepseek.com) in a browser and export your chats to a ZIP.
2. In the app: Settings → Data → Import chat history, pick the ZIP.
3. Review the added / updated / local-only diff and import.

## Capabilities

| Area | Details |
| --- | --- |
| Models | `deepseek-flash`, `deepseek-v4-pro` (both 1M context) |
| Chat | streaming, stop, regenerate, edit and resend, continue, branch switching, copy, vote, read aloud |
| Attachments | camera, gallery, files; image description, PDF reading; attachments travel with the archive |
| Voice | hold to talk (swipe up to cancel), account-voice read-aloud |
| Data | import/export `deepseek_data-*.zip`, diff preview, context compaction, usage stats |
| Account | balance and bonus, top-up via WeChat/Alipay, API key management, optional Scan assist |
| Platform | Android 7.0+ (minSdk 24), targetSdk 36 |

## Layout

| Path | What it is |
| --- | --- |
| `src/` | the chat-demo web client (React 18 + rspack) |
| `native-app/` | the Android shell (Kotlin + Compose + WebView + `window.NativeShell` bridge) |
| `copy/文案.mjs` | single source of UI copy for web and native; `node scripts/gen-copy.mjs` generates the rest |
| `site/` | the static website published on GitHub Pages |
| `docs/` | screenshots and research notes |

## Known limitations

- Long chats render the latest 25 messages and load earlier ones on demand; virtual scrolling is not implemented yet.
- There is no automated test suite; regressions rely on manual checks and code review.
- The top-up page only returns a UnionPay aggregate QR; the WeChat/Alipay channel is inferred from the selected button and the QR payload.

## Notes

- Chat requests go straight to `api.deepseek.com`; nothing is relayed through a server owned by this project. Usage is billed to your own account.
- Chats and imported history stay in the device's IndexedDB; sign-in happens on the official page, the app never sees your password, and the grant token is encrypted with Android Keystore.
- Scan assist is an optional accessibility service, off by default, active only for 20 seconds after the top-up flow opens WeChat/Alipay, and it does not read or store chats or payment details.

## Licence

MIT © moyuer233. The web core is reused from [chat-demo](https://github.com/deepseek-ai/chat-demo) under MIT, with its notice kept in [NOTICE](NOTICE). The DeepSeek name and logo belong to DeepSeek; this is an unofficial project with no affiliation or endorsement.

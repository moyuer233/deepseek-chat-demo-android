# 官方 DeepSeek App（com.deepseek.chat）逆向调研

> 样本：`D:\Users\Downloads\base(1).apk`（22,296,584 B，sha256 前缀 489ff75c）
> 版本：2.8.0（versionCode 297），minSdk 24 / targetSdk 36，compileSdk 37
> 日期：2026-11-20 ｜ 方法：apkanalyzer + dex 静态字符串扫描 + 模拟器截图；**未登录、未运行其业务逻辑**

## 0. 一句话结论

官方 App 是**纯原生 Android 应用**（Jetpack Compose 页面 + 腾讯组件：WCDB / MMKV / LiteAV-TRTC / 微信 OpenSDK），
不是 WebView 套壳；后端接口就是 `/api/v0/*`（和网页版同一套）。

对我们的直接影响：**“操作体验”上的差距主要来自原生渲染，而不是接口**。能抄的是交互规则（登录条款、返回层级、安全区、分享面板），
抄不了的是原生控件手感与语音通话（TRTC）。另外，官方 App 里**没有发现充值/微信支付的调用痕迹**——它很可能也不在 App 内充值。

## 1. 技术栈证据

| 证据 | 结论 |
|---|---|
| `lib/arm64-v8a/libWCDB.so`、`libmmkv.so` | 数据层用腾讯 WCDB（SQLite）+ MMKV（键值），不是 WebView 的 IndexedDB |
| `lib/libliteavsdk.so`、`libtxffmpeg.so`、`libtxsoundtouch.so`、dex 里 `com.tencent.liteav.trtc.*` | 实时语音/通话用腾讯 TRTC |
| dex 里 `androidx/compose/ui/*`（40+ 引用）、`com.deepseek.chat.ui.pages.*` | UI 是 Jetpack Compose 原生页面 |
| 没有 `libflutter.so` / `libhermes.so` / `com.getcapacitor` | 不是 Flutter / React Native / Capacitor 套壳 |
| `assets/org/scilab/forge/jlatexmath/*` | 公式用 jlatexmath 原生渲染（不是 KaTeX） |
| `com.deepseek.chat.ui.pages.root.mermaid.*` | Mermaid 有自研原生渲染实现 |
| `libEncryptorP.so`、`lib/flipped`、`com.deepseek.crypto.PowCalculator` | 加密、加固/热修（Flipped 影子插件）、聊天 PoW 挑战计算 |
| `libapminsighta/b`、`libvolc_log.so` | 火山引擎 APM / 日志 |
| `com.tencent.mm.opensdk`、`com.deepseek.chat.wxapi.WXEntryActivity` | 微信 OpenSDK：登录绑定、分享；**没有** App 内支付调用 |

## 2. 包结构与功能面

```
com.deepseek.chat
├── ui.pages.root / chat / settings / authv2 / call / camera / photo / table
├── ui.markdown(.parser)      原生 Markdown；mermaid；table 预览 Activity
├── ui.components(.blob/.textfield)
├── services.foreground.media MediaPlaybackForegroundService（朗读后台播）
├── services.tile              桌面快捷开关（新建对话/拍照问/图片问）
├── feature.call               TRTC 通话 + CallForegroundService
├── system                     AppFileProvider / ShareResultReceiver
├── wxapi.WXEntryActivity      微信回调
└── crypto.PowCalculator       聊天请求的 PoW 挑战
```

Manifest 里可见的 Activity：`MainActivity`、`ShareProxyActivity`、`TablePreviewActivity`、`WXEntryActivity`；
还有 `NEW_CHAT / OPEN_CHAT / CAMERA_ASK / PICTURE_ASK` 四个快捷动作。权限含 CAMERA、RECORD_AUDIO、
READ_MEDIA_IMAGES、POST_NOTIFICATIONS、前台服务（麦克风/媒体播放）。

## 3. 登录

原生登录页（截图见 `.scratch/official-2.png`）：logo 居中，底部「Continue with Phone」实心蓝按钮 +
「Continue with Password」描边按钮，底部一条条款勾选；未勾选条款点登录会先弹 **Terms of Use and Privacy Policy**
的 Cancel/Agree 对话框（`.scratch/official-phone.png`）。

接口（dex 字符串）：`/api/v0/users/login`、`/api/v0/users/login_by_mobile_sms`、
`/api/v0/users/oauth/wechat/{login,bind,mobile_verification,unbind}`、`/api/v0/users/mfa/*`。
即：手机验证码 / 密码 / 微信登录（绑定），和我们网页版登录是**同一个账号后端**。

## 4. 聊天与语音

- 对话接口：`/api/v0/chat/completion`、`regenerate`、`continue`、`edit_message`、`resume_stream`、`stop_stream`、`create_pow_challenge`
- 会话：`chat_session/fetch_page`、`create`、`delete`、`update_title`、`batch_update_pinned`
- 语音：`/api/v0/asr/ws`（WebSocket 流式识别）、`/api/v0/chat/tts/{,voice,voices}`
- 导出：`/api/v0/export_all` + `/api/v0/download_export_history`（我们在网页登录导入时用的就是这两个）
- 通话：`/api/v0/call/{start,stop,update,config,rating}` + TRTC

## 5. 支付：官方 App 内没有证据

- 没有 `com.alipay` 相关类（dex 计数 0）。
- 微信 OpenSDK 里虽然有 `PayReq` 字符串，但它们是 **SDK 自身的类**（classes3.dex，`_wxapi_payreq_*` 资源键），
  应用层没有 `top_up / cashier / unifiedorder / prepay_id` 这类业务字符串。
- 没有 `platform.deepseek.com` 的链接出现在 dex 里。

⇒ 官方 App 很可能**不在 App 内做充值**，用户充值走 platform 网页；这和网页端「移动端浏览器不支持微信支付」的结论一致。
我们采用「设置页充值入口 → 系统浏览器打开 top_up → 官方提示去 PC/微信内」的做法与官方行为不冲突。

## 6. 对我们的取舍（DeepseekChatApp）

| 维度 | 官方 App | 我们（Capacitor 8） | 结论 |
|---|---|---|---|
| 渲染 | Compose 原生 | WebView + chat-demo | 改不了；只能补手势/安全区/状态栏等外壳体验 |
| 登录 | 原生页面 + 条款弹窗 | WebView 打开 chat.deepseek.com 登录页 | 可加一个条款确认前置页，对齐“先同意后登录” |
| 充值/支付 | 疑似无 App 内入口 | 系统浏览器打开 top_up | 与官方一致，不做 App 内微信支付 |
| 语音通话 | TRTC 实时通话 | 无 | 不做 |
| 数据 | WCDB + MMKV | IndexedDB | 不迁移；导出/导入已打通 |
| 公式/Mermaid | 原生渲染器 | KaTeX + mermaid（Web） | 效果可比，不重写 |
| 快捷入口 | 桌面快捷开关 | 无 | 不做（用户没要求） |

## 7. 下一步该照抄的交互细节

1. **登录前条款勾选**：未同意时点登录先弹确认框（Cancel / Agree），同意后才进登录。
2. **返回层级**：弹窗 → 抽屉/面板 → 会话 → 退出（已实现，待真机验）。
3. **登录页布局**：logo 居中、主次按钮吸底、蓝色主按钮（#4D6BFE 系）。
4. **分享/导出**：走系统分享面板（已实现 Chunked 写入 + FileProvider 分享）。
5. **状态栏/安全区**：官方内容顶到状态栏下、状态栏透明；我们待补 edge-to-edge 与 `viewport-fit=cover`。

## 8. 复现命令

```bash
aapt2 dump badging 'D:/Users/Downloads/base(1).apk'
apkanalyzer manifest print 'D:/Users/Downloads/base(1).apk'
apkanalyzer dex packages 'D:/Users/Downloads/base(1).apk' > dex-packages.txt
# 关键字符串
grep -a -o -E '/api/v0/[a-zA-Z0-9_/\-]+' classes*.dex | sort -u
grep -a -o -E 'https://[a-zA-Z0-9.-]*deepseek[a-zA-Z0-9./_-]*' classes*.dex | sort -u
```

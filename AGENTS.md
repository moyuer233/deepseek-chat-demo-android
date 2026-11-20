# DeepSeek Chat Demo 项目规范

> 本文件是工作区级规范，每次会话自动注入。目标与决策变了就改这里，别只留在聊天里。

## 这是什么

把 DeepSeek 官方的 chat-demo（MIT，纯静态网页聊天客户端）做成安卓 App，开源在
https://github.com/moyuer233/deepseek-chat-demo-android （基于 deepseek-ai/chat-demo 二次开发的独立仓库，非 fork；保留上游署名，README 标明非官方）。

## 目标（用户拍板）

1. 用户在 App 里登录 chat.deepseek.com 账号后：
   - App 自动为它自己创建一把 API Key（不让用户去 platform 手动建）；
   - 一键导入该账号下的全部会话。
2. 聊天走 api.deepseek.com，界面与交互沿用 chat-demo（不重做视觉）。
3. 登录一次之后就该"能直接聊"，不要求用户手动填 Key、手动导数据。
4. 内置充值入口：内置 TopUpActivity 用**桌面 UA** 打开 https://platform.deepseek.com/top_up，页面出微信二维码后注入 jsQR
   自动解码，weixin://wxpay 用 Intent 拉起微信、其他复制链接；打不开再回退系统浏览器。
   （2026-11-20 用户改口径：原先「按官方提示引导」作废，改做这个 hack。）
5. 开源：MIT、保留上游 LICENSE 与出处、APK 挂 GitHub Releases。

**已确认（2026-11-20）**：一个重要目标是还原官方 DeepSeek App 的操作体验——不只是把网页装进壳里。
所以除聊天界面沿用 chat-demo 外，登录、账户、导入、充值、返回键、安全区、启动图、外链、导出保存都要按原生 App 的习惯做。
对照用的官方界面素材还没拿到（见待办）。

## 技术路线

- Android 优先，Capacitor 8 套壳（WebView 装现有 dist），不重写 UI；iOS 暂不做。
- 应用 id：com.moyuer233.deepseekchatdemo；显示名：DeepSeek Chat Demo。
- 聊天请求走 api.deepseek.com（App 自己的 Key）；登录/导出/建 Key 走原生 HTTP，绕开 CORS。
- 改动尽量少：新增 `capacitor.config.ts`、`android/`、登录插件、导入服务；上游文件只在必要处补 Android 适配
  （返回键、安全区/edge-to-edge、外链用系统浏览器、历史导出改系统分享保存）。

## 已核实的事实（可复现）

- 上游 v0.1.3（commit 8ad7b56），React 18 + rspack；`pnpm build` 本机 453ms，dist 4.6MB / 153 个文件。
- 上游自带历史导入导出，归档格式 `deepseek_data-日期.zip`：manifest.json（export_format_version: 3）
  + chat_sessions/*.json + chat_session_index.json + user.json（导入时跳过 user.json）。见 src/services/chat/history/README.md。
- 官方 2026-11-13 开放"全家桶应用 API"，文档写明 chat-demo 可"导入从 DeepSeek Chat 导出的聊天记录"
  （https://api-docs.deepseek.com/guides/build_a_chatbot）。
- 网页版导出接口（需登录态，token 在 chat.deepseek.com 的 localStorage `userToken`）：
  - `GET /api/v0/export_all` 触发导出，有频控；
  - `GET /api/v0/download_export_history` 轮询：status 为 INIT/PENDING/FINISHED/EMPTY，
    返回 history_download_url / legacy_history_download_url / exported_at / expires_at，下载链接 7 天有效。
- 分享链接导入（备选）：`GET /api/v0/share/content?share_id=<id>` 匿名可用；接口无 CORS 头，必须原生 HTTP。
- DeepSeek API 的 CORS 会回显任意 Origin（`https://localhost` 实测可用），WebView 里可直接请求 api.deepseek.com。
- 自动建 API Key：platform 是**独立登录态**，chat 的 userToken 调 platform 接口返回 40003；
  创建接口是 `POST /api/v0/users/edit_api_keys`（`action=create`，无验证码），成功返回完整 `sensitive_id`。
  方案：一次引导里让用户先登 chat、再登 platform，App 用 platform token 自动建 Key。详见 docs/research/platform-api-key.md。
- 登录 WebView 已在模拟器实测：点「登录并导入」会打开 `LoginActivity`，chat.deepseek.com 登录页正常渲染（手机号+验证码/密码/Apple）。
  真正登录后的 token 提取、导出、导入、建 Key 还没验证过（需要用户账号）。
- 微信支付：手机 UA 下充值页**不提供微信支付**（321 个 chunk 里 weixin:// 命中 0）；但桌面 UA 下会走 PC 二维码流程。
  2026-11-20 用户改口径做 hack，已实现 TopUpActivity（桌面 UA + jsQR + Intent），登录二维码在跨域 iframe 里扫不到也不该扫；
  支付二维码的解码/拉起链路**尚未用真实账号验证**。详见 docs/research/wechat-pay.md。
- 官方 App 支付实证（REA + jadx-headless-mcp 0.7.1）：全包 Pay 命中 7 个全是微信 SDK 自带类，
  trace-android-references 查 PayReq 只有 2 条 SDK 内部引用、应用层零引用 → 官方 App 没接微信支付。
- REA Android 反编译：JAR 在 H:\tools\jadx-headless-mcp\jadx-headless-mcp-0.7.1-all.jar，
  REA_JADX_MCP_JAR 已写入用户环境变量；CLI 用法 rea inspect-android-package <apk>。

## 本机环境（换机器要重看）

- JDK 21：`C:\Program Files\Java\jdk-21.0.10`
- Android SDK：`H:\Android\Sdk`（已装 platform-tools、platforms;android-36、build-tools;36.0.0、emulator、system-images;android-36;google_apis;x86_64）。
- Gradle：`GRADLE_USER_HOME=H:\Android\gradle-home`；8.14.3 发行包从腾讯镜像下载（官方 CDN 约 1MB/min，太慢）；
  `init.d/mirror.gradle` 已配阿里云 maven 镜像（本机 maven.google.com 不通）。
- 模拟器：AVD 名 `dsapp`（pixel_6 / Android 16 / google_apis x86_64），目录在 `H:\Android\avd`，WHPX 可用。
  启动：`ANDROID_AVD_HOME=H:\Android\avd H:\Android\Sdk\emulator\emulator.exe -avd dsapp -no-window -no-audio -no-boot-anim -gpu swiftshader_indirect -no-snapshot`
  装机：`adb install -r android/app/build/outputs/apk/debug/app-debug.apk`；截图：`adb exec-out screencap -p > shot.png`
- C 盘只剩约 2.4GB，SDK、Gradle 缓存、AVD 一律放 H:。
- git 全局代理 `127.0.0.1:7897` 当前 TLS 握手失败：git 命令加 `-c http.proxy= -c https.proxy=` 走直连；
  `gh` 不要设 HTTPS_PROXY。npm 走 npmmirror；`maven.google.com` 直连不通，Gradle 需配阿里云镜像。
- 会话沙箱为 danger-full-access，不需要申请审批。

## 构建

```
pnpm install
pnpm build                     # Windows 上已用 cross-env 处理 NODE_ENV
pnpm exec cap sync android
cd android && ./gradlew assembleDebug    # Windows: gradlew.bat assembleDebug
```

产物：`android/app/build/outputs/apk/debug/app-debug.apk`

## 红线

- 不改上游聊天/设置界面的结构与文案口径；新增入口要先问，不加"暂未开放"这类假控件。
- 不提交 `.dsh-meow/`、`.scratch/`、`dist/`、`node_modules/`、keystore、`local.properties`。
- 保留上游 LICENSE 与出处；README 首段写明非官方、与 DeepSeek 无隶属关系。
- 签名密钥不进仓库；Release 只附构建产物。
- 真机验证之前，不对外说"登录/导入已经可用"。

## 待办

1. ~~安装 SDK 组件，出第一个 debug APK~~（已完成：app-debug.apk 6.18MB，模拟器上已跑起来，主界面与 chat-demo 一致）。
2. 登录 WebView 插件：打开 chat.deepseek.com → 用户自己登录 → 取 `userToken`。
3. 验证 platform.deepseek.com 建 Key 的内部接口；不通就退回手动填 Key。
4. 导入流程：export_all → 轮询 download_export_history → 下载 ZIP → 交给现有 importArchive；验证与 DeepSeek 导出包兼容。
5. 真机验证：登录、建 Key、全量导入、聊天流式、图片上传、语音输入、朗读、历史导出、返回键、刘海屏。
6. 充值 hack 真机验证：登录 platform → 选微信支付 → 页面出码 → 应自动弹「打开微信」；失败回退系统浏览器。
7. ~~需要官方 App 做 UX 参照~~（2026-11-20 用户确认给 APK；只在本机截图对照，不进仓库、不反编译分发）。
   拿到后：装在模拟器上逐屏截图 → 列差异清单 → 逐项改（返回键/状态栏/安全区/启动图/登录页/导出保存/外链）。
8. README（中英）、Release APK、push（GitHub 网络恢复后）。

## 里程碑

- M1：能装、能聊（手动填 Key），界面与 chat-demo 一致。
- M2：登录 → 自动建 Key → 全量导入会话。
- M3：充值 hack（桌面 UA 二维码 → 拉起微信），失败回退系统浏览器。

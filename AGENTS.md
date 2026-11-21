# DeepSeek Chat Demo Android 项目规范

> 本文件是工作区级规范，每次会话自动注入。目标与决策变了就改这里，别只留在聊天里。

## 这是什么

把 DeepSeek 官方的 chat-demo（MIT，纯静态网页聊天客户端）做成安卓 App，开源在
https://github.com/moyuer233/deepseek-chat-demo-android （基于 deepseek-ai/chat-demo 二次开发的独立仓库，非 fork；保留上游署名，README 标明非官方）。

## 项目定位（用户原话，2026-11-21）

> 我们的项目定位是用API的「免费版 Deepseek」，用自己的API聊天，不受免费版限流限制，支持历史导入的独立项目，但是用了chat-demo。

- **独立项目**：不是 chat-demo 的 fork；复用它的网页内核（MIT），保留署名与 LICENSE。
- **用 API 的「免费版 DeepSeek」**：聊天直接调 `api.deepseek.com`，用用户自己的 API Key 或 DSH 账号授权；额度与计费归用户账号，因此不受官方免费版限流影响。
- **历史导入**：从 chat.deepseek.com 导出并导入全部会话，本机保存，可再次导出/导入。
- **非官方**：与 DeepSeek 无隶属或背书关系。对外文案（README/Release）统一按此口径，不写成官方客户端。

## 目标（用户拍板）

1. 用 API 的「免费版 DeepSeek」：用户自己的 API Key 或 DSH 账号授权即可聊天，额度走自己的 API，不受官方免费版限流。
   - 从 chat.deepseek.com 导出并一键导入该账号下的全部会话（导入自身需要 chat.deepseek.com 登录态）。
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

> 2026-11-21 起当前实现是 `native-app/`：Kotlin + Compose 外壳，WebView 加载 `assets/web` 的 chat-demo 构建产物，桥接层是 `window.NativeShell`（授权/导入导出/充值/相机/麦克风/侧滑/返回）。下面的 Capacitor 段落是历史路线，不要再按它改。

- Android 优先，Capacitor 8 套壳（WebView 装现有 dist），不重写 UI；iOS 暂不做。
- 应用 id：当前 `com.moyuer233.deepseeknative`（历史 Capacitor 版为 `com.moyuer233.deepseekchatdemo`）；显示名：DeepSeek Chat Demo Android。
- 聊天请求走 api.deepseek.com：账号模式下 `x-dsh-auth-token`（grant），无 grant 时回退 API Key；登录/导出走原生 HTTP，绕开 CORS。
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
- API Key 方案（已降级为 fallback）：platform 是独立登录态，chat token 调 platform 返回 40003；
  创建接口是 `POST /api/v0/users/edit_api_keys`（action=create，无验证码）。详见 docs/research/platform-api-key.md。
  2026-11-20 起主路线改为 DSH 账号 grant（见下），只有 grant 失败时才回退建 Key。
- **导入全链路已实测通过（2026-11-20，真实账号）**：`LoginActivity` 从 `localStorage.userToken` 的 `value` 字段取 token
  （结构是 `{value:"...",__version__:"0"}`，不是 `{token}`）→ `export_all` → 轮询 `download_export_history` →
  下载 115MB ZIP → 预览显示 150 个会话 → 导入成功，侧边栏按 Today/Yesterday/7 Days 正常分组。
- DSH 账号 grant 实测（2026-11-20 真实账号）：`GET api.deepseek.com/models` 200、`POST /anthropic/v1/messages` 200、
  `GET platform/api/v0/users/get_api_keys` 200；`edit_api_keys` 404（x-dsh）/40003（Bearer）；
  `chat.deepseek.com/api/v0/export_all` 40002、Cookie 只有 WAF → 无 SSO。结论：**聊天免 Key，导入仍需 chat 登录**。
- 导出修复（2026-11-20）：Android WebView 从 `content://` 取到的 File 可能 `size=0` 但流里有字节；
  `zipWriter` 改为按实际读取长度记账，否则整次导出报 `Invalid history archive entry`。修复后导出 ZIP 已走通系统分享。
- 功能实测（2026-11-20 模拟器）：深度思考（思考块）、智能搜索（18 网页 + 图像结果）、图片上传（Files API + grant，模型能描述图）、
  朗读（grant 鉴权通过，模拟器音频设备报 I/O 错属环境问题）、导入 150 会话、导出分享，均通过；语音输入需真机麦克风验证。
- 首启引导（2026-11-20）：4 步向导（欢迎 → 导入会话（可跳过）→ 账号授权（可跳过）→ 进入应用），
  用 localStorage `chat-demo:onboarded` 只展示一次；看过之后启动不再强制弹设置页。
- 微信支付：手机 UA 下充值页**不提供微信支付**（321 个 chunk 里 weixin:// 命中 0）；但桌面 UA 下会走 PC 二维码流程。
  2026-11-20 用户改口径做 hack，已实现 TopUpActivity（桌面 UA + jsQR + Intent），登录二维码在跨域 iframe 里扫不到也不该扫；
  支付二维码链路已实测（2026-11-20 真机）：jsQR 解码出银联 URL → 存图到相册 → 打开微信扫一扫/相册 → 微信显示「聚合收款 杭州深度求索… ¥10.00 确认付款」页，用户手动确认成功（未实际付款）。接口只返回银联 url、无 weixin://，所以无法自动直跳微信收银台。详见 docs/research/wechat-pay.md。
- 官方 App 支付实证（REA + jadx-headless-mcp 0.7.1）：全包 Pay 命中 7 个全是微信 SDK 自带类，
  trace-android-references 查 PayReq 只有 2 条 SDK 内部引用、应用层零引用 → 官方 App 没接微信支付。
- REA Android 反编译：需要本机安装 jadx-headless-mcp，并把 JAR 路径写进环境变量 `REA_JADX_MCP_JAR`；CLI 用法 `rea inspect-android-package <apk>`。

## 代码审查（2026-11-20，OCR delegate 全量）

范围：8ad7b56..HEAD 的 58 个可审文件（跳过 jsqr.js 第三方）。三个子代理分别审安卓/服务层/UI，主代理汇总并修了高危项：

- **minSdk24 兼容**：DshAuth 用 android.util.Base64 替代 java.util.Base64（API 26+），否则 Android 7.x 点授权会崩。
- **OAuth state 校验**：回调 state 与 init 的 state 比对（RFC 6749），OAuthActivity 一起回传。
- **manifest 按名扫描**：importArchive 先用 skipEntry 消费非 manifest 条目，否则 readArchive 的未读完校验会抛错（容错实际未生效）。
- **预览内存**：readArchivePreview 用 fflate filter 只解压 index，不再把 100MB+ 包整包解进 JS 堆。
- **导出**：zipWriter 串行测 size=0 blob；share.ts 按 512KB 分块读取并校验写入字节数；FileProvider 只暴露 cache/export 与 cache/share。
- **安全**：allowBackup=false；beginExportFile/append/share 做路径穿越校验；download 精确校验 chat.deepseek.com；TopUp 的 JS 桥只接受 deepseek 域页面 + weixin://wxpay/https；登录路径不再跑探针。
- **UX**：欢迎页登录入口改为打开账户设置；向导授权失败不再当成功；导入确认 autoFocus 给「取消」；卡片 max-height 可滚动；账户页状态 aria-live。
- **失效 grant**：401 时清掉 accountToken，回退 API Key；登录 WebView 加 5 分钟超时。
- **遗留（未改）**：LoginActivity 复用旧会话导致重新导入可能直接用旧 token（有超时但无强制清登录态）；openTopUp 的「回退系统浏览器」实际不可达；i18n 仍有少量未用键（已删 onboardingManual）；无自动化测试（建议补 import/export 自检脚本）。

## 构建环境要求（换机器按本机实际情况填写）

- JDK 21（`JAVA_HOME` 指向它）。
- Android SDK，需含：platform-tools、platforms;android-36、build-tools;36.0.0、emulator 与 android-36 google_apis x86_64 镜像。
- Gradle 依赖缓存目录可用 `GRADLE_USER_HOME` 指定；国内网络建议在 `init.d/mirror.gradle` 配阿里云 maven 镜像（maven.google.com 常不通）。
- 模拟器：任意 API 34+ 的 x86_64 AVD 即可；无窗口启动示例
  `emulator -avd <AVD 名> -no-window -no-audio -no-boot-anim -gpu swiftshader_indirect -no-snapshot`。
  装机：`adb install -r native-app/app/build/outputs/apk/debug/app-debug.apk`；截图：`adb exec-out screencap -p > shot.png`。
- 依赖下载占空间较大，SDK、Gradle 缓存、AVD 建议放在空间充足的盘。
- 国内网络：npm 走 npmmirror；git 若需代理用 `-c http.proxy=<代理地址> -c https.proxy=<代理地址>`，代理不通时改直连。
- 具体盘符、用户名、代理端口、设备序列号等本机事实不写进仓库，放在本地笔记里。

## 发布

- 版本：0.2.0（versionCode 2，versionName 0.2.0）；上一版 0.1.0 对应 tag v0.1.0-android
- 签名密钥：keystore 与密码文件都放在仓库外（路径写进 `native-app/keystore.properties`，该文件已 gitignore），仓库内不出现任何密钥与密码
- Release：v0.2.0 → https://github.com/moyuer233/deepseek-chat-demo-android/releases/tag/v0.2.0 （APK 自签名）；历史版 v0.1.0-android
- **gh 命令必须带 `-R moyuer233/deepseek-chat-demo-android`**：本地 `origin` 指向 chat-demo fork，不带 -R 会操作错仓库。

## 构建

```
pnpm install
pnpm build                     # Windows 上已用 cross-env 处理 NODE_ENV
pnpm exec cap sync android
cd android && ./gradlew assembleDebug    # Windows: gradlew.bat assembleDebug
```

产物：`android/app/build/outputs/apk/debug/app-debug.apk`

## 红线

- 所有图标统一**空心（outline/stroke）风格**，不使用实心填充图标；新增图标先确认符合该风格。
- 不改上游聊天/设置界面的结构与文案口径；新增入口要先问，不加"暂未开放"这类假控件。
- 不提交 `.dsh-meow/`、`.scratch/`、`dist/`、`node_modules/`、keystore、`local.properties`。
- 保留上游 LICENSE 与出处；README 首段写明非官方、与 DeepSeek 无隶属关系。
- 签名密钥不进仓库；Release 只附构建产物。
- 真机验证之前，不对外说"登录/导入已经可用"。

## 待办

1. ~~安装 SDK 组件，出第一个 debug APK~~（已完成：app-debug.apk 6.18MB，模拟器上已跑起来，主界面与 chat-demo 一致）。
2. 登录 WebView 插件：打开 chat.deepseek.com → 用户自己登录 → 取 `userToken`。
3. ~~验证 platform 建 Key~~（已验证；现为 fallback，主路线是 DSH 账号 grant）。
4. 导入流程：export_all → 轮询 download_export_history → 下载 ZIP → 交给现有 importArchive；验证与 DeepSeek 导出包兼容。
5. 真机验证：chat 登录 → 全量导入、账号授权 → 聊天流式、图片上传（grant 是否支持 Files API 待测）、语音输入、朗读、历史导出、返回键、刘海屏。
6. 充值 hack 真机验证：登录 platform → 选微信支付 → 页面出码 → 应自动弹「打开微信」；失败回退系统浏览器。
7. ~~需要官方 App 做 UX 参照~~（2026-11-20 用户确认给 APK；只在本机截图对照，不进仓库、不反编译分发）。
   拿到后：装在模拟器上逐屏截图 → 列差异清单 → 逐项改（返回键/状态栏/安全区/启动图/登录页/导出保存/外链）。
8. README（中英）、Release APK、push（GitHub 网络恢复后）。

## 里程碑

- M1：能装、能聊（手动填 Key），界面与 chat-demo 一致。
- M2：登录 → 账号授权（免 Key）→ 全量导入会话。
- M3：充值 hack（桌面 UA 二维码 → 拉起微信），失败回退系统浏览器。

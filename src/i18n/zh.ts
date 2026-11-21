export const zh = {
  // 位置：src/components/chatInput/ChatInput.tsx · 语音录音条：松手发送
  voiceReleaseToSend: '松开发送',
  // 位置：src/components/chatInput/ChatInput.tsx · 语音录音条：滑到取消区后松手
  voiceReleaseCancel: '松开取消',
  // 位置：src/components/chatInput/ChatInput.tsx · 语音录音条：上滑取消提示
  voiceSlideUpCancel: '上滑取消',
  // 位置：src/services/chat/store.ts · 发送失败 toast：自动压缩后请重试
  contextOverflowCompacted: '上下文已压缩，请重试',
  // 位置：src/services/chat/store.ts · 发送失败 toast：压缩也失败
  contextOverflowCompactFailed: '上下文已超限，压缩失败，请重试',
  // 位置：当前未使用 · 设置→对话：分组标题
  usageStats: '用量与上下文',
  // 位置：src/components/settings/NativeSettings.tsx · 用量行：请求数
  usageRequests: '请求数',
  // 位置：src/components/settings/NativeSettings.tsx · 用量行：输入 tokens
  usageInput: '输入 tokens',
  // 位置：src/components/settings/NativeSettings.tsx · 用量行：缓存命中 tokens
  usageCacheRead: '缓存命中 tokens',
  // 位置：src/components/settings/NativeSettings.tsx · 用量行：输出 tokens
  usageOutput: '输出 tokens',
  // 位置：当前未使用 · 用量行：费用（当前不显示）
  usageCost: '费用',
  // 位置：src/components/settings/NativeSettings.tsx · 用量行：上下文占用
  usageContext: '上下文占用',
  // 位置：src/components/settings/NativeSettings.tsx · 用量分组：本会话
  usageSession: '本会话',
  // 位置：src/components/settings/NativeSettings.tsx · 用量分组：全部会话
  usageTotal: '全部会话',
  // 位置：src/components/settings/settingsDialogState.ts · 未登录时点发送/语音/附件 的 toast
  loginRequiredHint: '请先登录 DeepSeek 账号或填写 API Key',
  // 位置：src/components/settings/AccountPane.tsx · 账户页：余额卡标签
  accountBalance: '余额',
  // 位置：src/components/settings/AccountPane.tsx · 账户页：余额失败重试按钮
  accountBalanceRetry: '余额读取失败，重试',
  // 位置：src/components/settings/AccountPane.tsx · 账户页：更多账号信息行
  accountMoreInfo: '更多账号信息',
  // 位置：src/components/settings/AccountPane.tsx · 账户页：退出登录行 + 弹窗标题
  accountLogout: '退出登录',
  // 位置：src/components/settings/AccountPane.tsx · 退出登录弹窗：确认按钮
  accountLogoutConfirm: '确认退出',
  // 位置：src/components/settings/AccountPane.tsx · 退出登录弹窗正文
  accountLogoutBody: '退出后本机聊天记录保留，下次登录可继续使用。',
  // 位置：src/components/settings/AccountPane.tsx · 退出登录弹窗正文（生成中）
  accountLogoutActive: '当前有回复正在生成，退出会中断生成，本机聊天记录仍保留。',
  // 位置：src/components/settings/AccountPane.tsx · 账户卡：已授权账号标题
  accountAuthAccount: '已授权 DeepSeek 账号',
  // 位置：src/components/settings/AccountPane.tsx · 账户卡：API Key 标题
  accountAuthApiKey: '使用 API Key',
  // 位置：src/components/settings/AccountPane.tsx · 账户卡：授权方式副标题
  accountAuthAccountLabel: '账号授权（DSH grant）',
  // 位置：src/components/settings/AccountPane.tsx · 账户卡：API Key 副标题
  accountAuthApiKeyLabel: 'API Key（本机保存）',
  // 位置：src/components/settings/AccountPane.tsx · 账户卡：未登录标题
  accountNotLoggedIn: '未登录',
  // 位置：src/components/settings/NativeSettings.tsx · 导入弹窗顶部统计（占位符：added/updated/localOnly）
  importStats: '新增 {added} · 有更新 {updated} · 本地独有 {localOnly}',
  // 位置：src/components/settings/NativeSettings.tsx · 导入前对比一致时的 toast
  importUpToDate: '已是最新，无需导入',
  // 位置：src/components/settings/NativeSettings.tsx · 导入弹窗标题
  importModeTitle: '选择导入方式',
  // 位置：src/components/settings/NativeSettings.tsx · 导入弹窗说明
  importModeHint: '同一个归档可能有官方更新、也可能是重复导入，请选择处理方式：',
  // 位置：src/components/settings/NativeSettings.tsx · 导入方式：合并
  importModeMerge: '合并（推荐）',
  // 位置：src/components/settings/NativeSettings.tsx · 导入方式：合并说明
  importModeMergeDesc: '按会话合并：本机没有的新增；已有的合并消息并保留本地新内容；不会删除本机会话。',
  // 位置：src/components/settings/NativeSettings.tsx · 导入方式：仅新增
  importModeAdd: '仅新增',
  // 位置：src/components/settings/NativeSettings.tsx · 导入方式：仅新增说明
  importModeAddDesc: '只导入本机不存在的会话；已存在的会话保持不变。',
  // 位置：src/components/settings/NativeSettings.tsx · 导入方式：覆盖替换
  importModeOverwrite: '覆盖替换',
  // 位置：src/components/settings/NativeSettings.tsx · 导入方式：覆盖说明
  importModeOverwriteDesc: '用归档替换本机全部会话（导入后新聊的内容会丢失）。',
  // 位置：src/components/settings/NativeSettings.tsx · 设置：对话子页标题
  chatSettings: '对话',
  // 位置：src/components/settings/NativeSettings.tsx · 设置→对话：模型行
  modelSetting: '模型',
  // 位置：src/components/chatInput/StatusLine.tsx · 输入框下状态行：上下文
  statusContext: '上下文',
  // 位置：当前未使用 · 状态行：缓存（已不显示）
  statusCache: '缓存',
  // 位置：当前未使用 · 状态行：首字（已不显示）
  statusFirstToken: '首字',
  // 位置：src/components/chatInput/StatusLine.tsx · 状态行：生成中
  statusGenerating: '生成中…',
  // 位置：src/components/session/MobileHeader.tsx · 会话头部压缩按钮：确认标题
  compactNow: '压缩上下文',
  // 位置：src/components/session/MobileHeader.tsx · 会话头部压缩按钮：确认正文
  compactConfirm: '把当前会话压缩成摘要？旧消息会被摘要替换，之后无法直接查看原文。',
  // 位置：src/components/session/MobileHeader.tsx · 压缩完成 toast
  compactDone: '上下文已压缩',
  // 位置：src/components/session/MobileHeader.tsx · 压缩失败 toast
  compactFailed: '压缩失败，请稍后重试',
  // 位置：src/components/settings/NativeSettings.tsx · 设置→对话：上下文压缩开关
  contextCompression: '上下文压缩',
  // 位置：src/components/settings/NativeSettings.tsx、src/services/api/types.ts · 通用：开
  enabled: '开',
  // 位置：src/components/settings/NativeSettings.tsx、src/services/api/types.ts · 通用：关
  disabled: '关',
  // 位置：src/components/sider/Sider.tsx · 侧栏搜索框占位
  searchSessionsPlaceholder: '搜索会话',
  // 位置：src/components/sider/Sider.tsx · 搜索：结果过多提示
  searchSessionsNarrow: '结果较多，请输入更精确的关键词',
  // 位置：src/components/sider/Sider.tsx · 搜索：搜索中
  searchSessionsSearching: '搜索中…',
  // 位置：src/components/sider/Sider.tsx · 搜索：无结果
  searchSessionsEmpty: '没有匹配的会话',
  // 位置：src/components/settings/NativeSettings.tsx · 设置→关于：检查更新行
  checkUpdate: '检查更新',
  // 位置：src/components/settings/NativeSettings.tsx · 检查更新：已最新 toast
  updateLatest: '已是最新版本',
  // 位置：src/components/settings/NativeSettings.tsx · 检查更新：发现新版本 toast
  updateAvailable: '发现新版本',
  // 位置：src/components/chatInput/ChatInput.tsx · 语音：按住说话
  voiceHoldToSpeak: '按住说话',
  // 位置：src/components/chatInput/ChatInput.tsx · 附件面板：拍照
  attachCamera: '拍照',
  // 位置：src/components/chatInput/ChatInput.tsx · 附件面板：相册
  attachGallery: '相册',
  // 位置：src/components/chatInput/ChatInput.tsx · 附件面板：文件
  attachFile: '文件',
  // 位置：src/components/settings/NativeSettings.tsx · 设置分组：账户
  nativeSettingsAccount: '账户',
  // 位置：src/components/settings/NativeSettings.tsx · 设置分组：应用
  nativeSettingsApp: '应用',
  // 位置：src/components/settings/NativeSettings.tsx · 设置分组：语音
  nativeSettingsVoice: '语音',
  // 位置：src/components/settings/NativeSettings.tsx · 设置分组：关于
  nativeSettingsAbout: '关于',
  // 位置：src/components/settings/NativeSettings.tsx · 设置→数据管理：导入行
  nativeSettingsImportChats: '从 chat.deepseek.com 导入会话',
  // 位置：src/components/settings/NativeSettings.tsx · 返回按钮无障碍文案
  back: '返回',
  // 位置：src/components/session/BalanceBadge.tsx · 顶部余额：标签
  balanceLabel: '余额',
  // 位置：src/components/session/BalanceBadge.tsx · 顶部余额：长按提示
  balanceTooltip: 'DeepSeek 账户余额',
  // 位置：src/components/session/BalanceBadge.tsx、src/components/settings/AccountPane.tsx · 余额：赠送
  balanceBonus: '赠送',
  // 位置：src/components/session/BalanceBadge.tsx · 顶部余额：获取失败
  balanceFailed: '余额获取失败',
  welcomeMessageChat250808: '今天有什么可以帮到你？',
  contentCaveat: '内容由 AI 生成，请仔细甄别',
  expandSiderbarButton: '打开边栏',
  collapseSiderbarButton: '收起边栏',
  newChat: '开启新对话',
  untitledChat: '新对话',
  sessionLoading: '正在加载对话...',
  sessionListLoadFailed: '对话列表加载失败，请重试',
  sessionLoadFailed: '对话加载失败，请重试',
  sessionSaveFailed: '对话或附件尚未保存到本机，关闭页面可能丢失，请重试',
  sessionDeleteFailed: '删除对话失败，请重试',
  settings: '设置',
  appName: 'API 使用 demo',
  deleteSessionModalContentTitle: '永久删除对话',
  deleteSessionWithFilesDescription: '删除后，该对话及其中上传的文件将不可恢复。确认删除吗？',
  cancel: '取消',
  delete: '删除',
  sessionItemMenuRenameOption: '重命名',
  more: '更多',
  close: '关闭',
  sessionHistoryTimelineLabelToday: '今天',
  sessionHistoryTimelineLabelYesterday: '昨天',
  sessionHistoryTimelineLabel7days: '7 天内',
  sessionHistoryTimelineLabel30days: '30 天内',
  copy: '复制',
  copied: '已复制',
  copyFailed: '复制失败，请长按或选中文本后复制',
  send: '发送',
  editMessage: '编辑消息',
  regenerate: '重新生成',
  continueGenerating: '继续生成',
  changeApiKey: '更换 API Key',
  topUp: '前往充值',
  messageSearchSummary: '1 个网页',
  messageSearchSummaries: '{count} 个网页',
  messageVersions: '消息版本',
  previousVersion: '上一个版本',
  nextVersion: '下一个版本',
  searchResult: '搜索结果',
  mermaidDiagram: '图表',
  mermaidCode: '代码',
  mermaidEmpty: '图表为空',
  mermaidLoading: '图表生成中...',
  mermaidRenderFail: '渲染失败',
  mermaidSwitchLabel: 'Mermaid 图表与代码切换',
  toolSearchIntro: '搜索',
  messageSearching: '搜索中...',
  imageSearching: '图像搜索中...',
  searchStopped: '搜索已停止',
  toolSearchFail: '联网搜索暂不可用',
  toolSearchNoResult: '未搜索到合适内容',
  messageSearchNoResult: '未搜索到相关内容',
  toolSearch1Result: '搜索到 1 个网页',
  toolSearchResults: '搜索到 {count} 个网页',
  messageSearchResult: '已阅读 1 个网页',
  messageSearchResults: '已阅读 {count} 个网页',
  toolOpenOneIntro: '浏览 1 个页面',
  toolOpensIntro: '浏览 {count} 个页面',
  toolOpenFail: '浏览页面失败',
  toolOpenFailTooltip: '浏览失败',
  toolOpenViewAll: '查看全部',
  toolFindFail: '查找失败',
  toolFindIntro: '查找关键词',
  toolFindFailTooltip: '未查找到相关内容',
  readingLink: '正在阅读',
  commonNetworkErrorToast: '网络异常，请检查你的网络状况',
  replyInvalidApiKey: 'API Key 无效或已被删除，请更换后重试',
  replyInsufficientBalance: '账户余额不足，请充值后重试',
  replyRateLimited: '请求过于频繁，请稍后再试',
  completionServerBusyToast: '服务器繁忙，请稍后再试',
  unknownError: '未知错误',
  replyRefused: '你好，这个问题我暂时无法回答，让我们换个话题再聊聊吧。',
  messageThinkDuration: '已思考（用时 {count} 秒）',
  messageThinkDurationPlural: '已思考（用时 {count} 秒）',
  messageThinkStopped: '已停止',
  deepSearchSearching: '正在搜索',
  groupTitleToolOpen: '正在浏览网页',
  messageThinking: '正在思考',
  backupInvalidJson: '文件不是有效的 JSON',
  backupUnrecognized: '无法识别该文件，请选择 DeepSeek 导出的历史对话 ZIP 文件',
  backupNewerVersion: '文件版本过新，请升级后再导入',
  backupInvalidVersion: '文件版本无效，无法导入',
  backupIncomplete: '文件内容不完整，无法导入',
  backupInvalidBranches: '文件中的消息分支已损坏，无法导入',
  backupInvalidFiles: '文件中的附件数据已损坏，无法导入',
  settingsLocalePreference: '语言',
  settingsTtsVoice: '朗读音色',
  settingsTtsVoiceStella: '海星（女声）',
  settingsTtsVoiceTide: '暗潮（男声）',
  settingsLocation: '位置信息',
  settingsLocationProvince: '请选择省份/地区',
  settingsLocationCity: '请选择市',
  settingsLocationRegion: '请选择国家或地区',
  settingDialogAsideGeneralOptionLabel: '通用设置',
  settingDialogAsideDataOptionLabel: '数据管理',
  avatarMenuSettingsDialogThemeItemLabel: '主题',
  avatarMenuSettingsDialogThemeSelectLightOption: '浅色',
  avatarMenuSettingsDialogThemeSelectDarkOption: '深色',
  avatarMenuSettingsDialogThemeSelectSystemOption: '跟随系统',
  apiKeyStart: '输入 API Key 开始使用',
  apiKeyEdit: '修改 API Key',
  apiKeyPlaceholder: '请输入 API Key',
  apiKeyVerifying: '正在验证...',
  confirm: '确定',
  apiKeyInvalid: 'API Key 无效，请检查后重试',
  apiKeyVerificationFailed: '校验失败：{message}',
  historyImport: '导入历史对话',
  historyImportConfirm: '确认导入',
  historyImportConfirmTitle: '确认导入历史对话？',
  historyImportConfirmDescription: '上传的历史对话会覆盖当前所有对话记录，是否确认导入？',
  historyImportDescription: '导入历史对话会覆盖全部本地对话和附件，支持 DeepSeek 导出的 ZIP 文件。',
  historyImportedOne: '已导入 1 个对话',
  historyImported: '已导入 {count} 个对话',
  historyImportFailed:
    '文件导入失败，可能是文件格式错误或使用了旧版的对话导出 ZIP 文件，请前往 DeepSeek 重新导出历史对话文件后再次导入。',
  historyExportFailed: '导出失败：{message}',
  exportAllLabel: '导出所有历史对话',
  historyExportDescription:
    '包含所有历史对话及本地图片原件；其他附件仅保留文件信息，不包含 API Key。',
  exportAllDownload: '下载',
  deleteAllSessions: '删除所有对话',
  deleteAllSessionsModalTitle: '删除后，所有对话都将被清空且无法找回',
  deleteAllSessionsModalDescription: '本地保存的附件及未发送的草稿也将被删除。',
  historyDeleting: '删除中...',
  historyDeleted: '删除成功',
  historyDeleteFailed: '删除失败，请重试',
  upload: '上传',
  uploadUnsupportedFormat:
    '上传的文件包含暂未支持的文件格式，请转换文件格式后重试。支持 PDF、DOC、XLSX、PPT、图片、文本、代码等。',
  uploadFileStatusFailed: '上传失败',
  uploadRetry: '{message}，点击重试',
  chatInputPlaceholderChat: '请输入消息',
  deactivatedThinkTooltip: '先思考后回答，解决推理问题',
  thinkingSwitch: '深度思考',
  deactivatedSearchTooltip: '按需搜索网页',
  search: '智能搜索',
  uploadFile: '上传文件',
  chatInputStopButtonTooltip: '停止生成',
  uploadMaxMBSize: '单个文件大小超过限制（{size}MB），请调整后重试',
  uploadMaxFiles: '上传文件最多支持 {count} 个',
  uploadFileStatusUploading: '上传中...',
  uploadServerBusy: '服务器繁忙',
  uploadParseFailed: '解析失败',
  uploadFileCountLimit: '已上传文件数达到上限（{limit} 个），可 ',
  uploadStorageLimit: '已上传文件占用空间达到上限（{limit}GB），可 ',
  uploadDeleteFiles: '删除文件',
  dragFileToUpload: '文件拖动到此处即可上传',
  dragFileDesc: '最多 {count} 个，每个 {size}MB',
  voiceInput: '语音输入',
  voiceInputTimer: '录音中 {seconds}s',
  voiceInputStop: '结束录音并转写',
  voiceInputCancel: '正在转写，点击取消',
  voiceInputPermissionDenied: '无法使用麦克风，请在浏览器设置中允许访问',
  voiceInputNoDevice: '未检测到麦克风',
  voiceInputTooLong: '录音过长，请缩短后重试',
  voiceInputFailed: '语音识别失败，请稍后再试',
  voiceInputEmpty: '未识别到语音内容',
  voiceInputLimitReached: '已达到单次录音时长上限',
  voiceInputRequiresHttps: '非 HTTPS 页面无法使用语音输入',
  apiKeyInvalidRetry: 'API Key 无效，请更换后重试',
  insufficientBalance: '账户余额不足',
  rateLimited: '操作过于频繁，请稍后再试',
  ttsPlay: '朗读',
  ttsStop: '停止朗读',
  ttsUnavailable: '这条回答暂时无法朗读',
  ttsFailed: '朗读失败，请稍后再试',
  retry: '重试',
  latestReminderLanguage: '中文',
  settingsLocaleZhCN: '简体中文',
  settingsLocaleEnUS: 'English',
  accountTab: '账户',
  accountLoginHint: '登录 DeepSeek 账号，自动导入会话并创建 API Key。',
  accountLoginButton: '登录 DeepSeek 账号并导入',
  accountLoginShort: '登录 DeepSeek 账号',
  accountRecharge: '充值',
  accountScanAssist: '开启扫码辅助',
  accountRechargeHint: '充值页以电脑版打开，出微信二维码后会自动识别并拉起微信',
  accountTermsTitle: '使用条款',
  accountTermsBody:
    '继续登录即表示你已阅读并同意 DeepSeek 的《使用条款》和《隐私政策》。登录成功后，App 会导入该账号的会话，并用账号授权直接聊天（不需要 API Key）。',
  accountTermsAgree: '同意并继续',
  onboardingTitle: '欢迎使用 DeepSeek Chat Demo',
  onboardingPitch:
    '把 DeepSeek 网页版聊天搬到安卓：登录一次，导入全部会话；用 DeepSeek 账号授权直接聊天，不需要 API Key。',
  onboardingStep1: '登录 chat.deepseek.com，导入该账号的全部会话',
  onboardingStep2: '授权 DeepSeek 账号，App 直接用 API 额度聊天',
  onboardingStep3: '不消耗网页版次数；余额不足时可在 App 内充值',
  onboardingStart: '开始使用',
  onboardingNote: '非官方应用，与 DeepSeek 无隶属关系。',
  aboutTitle: '关于',
  aboutVersion: '版本',
  aboutSource: '开源地址',
  aboutLicense: '许可',
  aboutDisclaimer:
    '非官方应用，基于 DeepSeek 官方 chat-demo（MIT）二次开发，与 DeepSeek 无隶属关系。',
  onboardingImportTitle: '第 1 步：导入聊天记录',
  onboardingImportPitch:
    '登录 chat.deepseek.com 后，App 会拉取该账号的导出包并导入；也可以先跳过，之后在设置 → 账户里导入。',
  onboardingLoginImport: '登录并导入',
  onboardingSkip: '跳过',
  onboardingAuthTitle: '第 2 步：授权 DeepSeek 账号',
  onboardingAuthPitch:
    '授权后 App 用你的 API 额度聊天，不消耗网页版次数，也不需要手动创建 API Key。',
  onboardingAuthButton: '授权并继续',
  onboardingNext: '下一步',
  onboardingDoneTitle: '准备好了',
  onboardingDonePitch: '可以开始聊天了。随时可以在设置 → 账户里重新导入或授权。',
  onboardingEnter: '进入应用',
  accountLoggedIn: '已登录，API Key 已保存在本机',
  accountLoggedInAccount: '已通过 DeepSeek 账号登录（免 API Key）',
  accountReimportButton: '重新导入',
  accountProgressChatLogin: '正在打开 DeepSeek 登录页...',
  accountProgressExporting: '正在导出会话，可能需要几分钟...',
  accountProgressImporting: '正在导入会话...',
  accountProgressPlatformLogin: '正在登录 DeepSeek 开放平台...',
  accountProgressCreatingKey: '正在创建 API Key...',
  accountImported: '已导入 {count} 个会话',
  accountDone: '完成',
  accountPreviewTitle: '发现 {count} 个会话',
  accountPreviewUntitled: '无标题',
  accountPreviewMessageCount: '{count} 条消息',
  accountPreviewWarning: '导入会替换本机全部会话',
  accountPreviewConfirm: '导入',
} as const

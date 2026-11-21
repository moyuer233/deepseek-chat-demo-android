import type { zh } from './zh.js'

export const en = {
  // 位置：src/components/chatInput/ChatInput.tsx · 语音录音条：松手发送
  voiceReleaseToSend: 'Release to send',
  // 位置：src/components/chatInput/ChatInput.tsx · 语音录音条：滑到取消区后松手
  voiceReleaseCancel: 'Release to cancel',
  // 位置：src/components/chatInput/ChatInput.tsx · 语音录音条：上滑取消提示
  voiceSlideUpCancel: 'Slide up to cancel',
  // 位置：src/services/chat/store.ts · 发送失败 toast：自动压缩后请重试
  contextOverflowCompacted: 'Context compacted, please retry',
  // 位置：src/services/chat/store.ts · 发送失败 toast：压缩也失败
  contextOverflowCompactFailed: 'Context overflowed and compaction failed, please retry',
  // 位置：当前未使用 · 设置→对话：分组标题
  usageStats: 'Usage & context',
  // 位置：src/components/settings/NativeSettings.tsx · 用量行：请求数
  usageRequests: 'Requests',
  // 位置：src/components/settings/NativeSettings.tsx · 用量行：输入 tokens
  usageInput: 'Input tokens',
  // 位置：src/components/settings/NativeSettings.tsx · 用量行：缓存命中 tokens
  usageCacheRead: 'Cache read tokens',
  // 位置：src/components/settings/NativeSettings.tsx · 用量行：输出 tokens
  usageOutput: 'Output tokens',
  // 位置：当前未使用 · 用量行：费用（当前不显示）
  usageCost: 'Cost',
  // 位置：src/components/settings/NativeSettings.tsx · 用量行：上下文占用
  usageContext: 'Context usage',
  // 位置：src/components/settings/NativeSettings.tsx · 用量分组：本会话
  usageSession: 'This session',
  // 位置：src/components/settings/NativeSettings.tsx · 用量分组：全部会话
  usageTotal: 'All sessions',
  // 位置：src/components/settings/settingsDialogState.ts · 未登录时点发送/语音/附件 的 toast
  loginRequiredHint: 'Sign in with a DeepSeek account or set an API key first',
  // 位置：src/components/settings/AccountPane.tsx · 账户页：余额卡标签
  accountBalance: 'Balance',
  // 位置：src/components/settings/AccountPane.tsx · 账户页：余额失败重试按钮
  accountBalanceRetry: 'Failed to load balance, retry',
  // 位置：src/components/settings/AccountPane.tsx · 账户页：更多账号信息行
  accountMoreInfo: 'More account info',
  // 位置：src/components/settings/AccountPane.tsx · 账户页：退出登录行 + 弹窗标题
  accountLogout: 'Sign out',
  // 位置：src/components/settings/AccountPane.tsx · 退出登录弹窗：确认按钮
  accountLogoutConfirm: 'Sign out',
  // 位置：src/components/settings/AccountPane.tsx · 退出登录弹窗正文
  accountLogoutBody: 'Local chats are kept and will still be here after signing in again.',
  // 位置：src/components/settings/AccountPane.tsx · 退出登录弹窗正文（生成中）
  accountLogoutActive: 'A reply is being generated; signing out interrupts it. Local chats are kept.',
  // 位置：src/components/settings/AccountPane.tsx · 账户卡：已授权账号标题
  accountAuthAccount: 'DeepSeek account authorized',
  // 位置：src/components/settings/AccountPane.tsx · 账户卡：API Key 标题
  accountAuthApiKey: 'Using API key',
  // 位置：src/components/settings/AccountPane.tsx · 账户卡：授权方式副标题
  accountAuthAccountLabel: 'Account grant (DSH)',
  // 位置：src/components/settings/AccountPane.tsx · 账户卡：API Key 副标题
  accountAuthApiKeyLabel: 'API key (stored locally)',
  // 位置：src/components/settings/AccountPane.tsx · 账户卡：未登录标题
  accountNotLoggedIn: 'Not signed in',
  // 位置：src/components/settings/NativeSettings.tsx · 导入弹窗顶部统计（占位符：added/updated/localOnly）
  importStats: '{added} new · {updated} updated · {localOnly} local only',
  // 位置：src/components/settings/NativeSettings.tsx · 导入前对比一致时的 toast
  importUpToDate: 'Already up to date, nothing to import',
  // 位置：src/components/settings/NativeSettings.tsx · 导入弹窗标题
  importModeTitle: 'Choose import mode',
  // 位置：src/components/settings/NativeSettings.tsx · 导入弹窗说明
  importModeHint: 'The same archive may have newer content or be a duplicate. Choose how to apply it:',
  // 位置：src/components/settings/NativeSettings.tsx · 导入方式：合并
  importModeMerge: 'Merge (recommended)',
  // 位置：src/components/settings/NativeSettings.tsx · 导入方式：合并说明
  importModeMergeDesc: 'Merge by session: add missing ones, merge messages and keep local changes; nothing local is deleted.',
  // 位置：src/components/settings/NativeSettings.tsx · 导入方式：仅新增
  importModeAdd: 'Add new only',
  // 位置：src/components/settings/NativeSettings.tsx · 导入方式：仅新增说明
  importModeAddDesc: 'Import only sessions that do not exist locally; existing ones stay untouched.',
  // 位置：src/components/settings/NativeSettings.tsx · 导入方式：覆盖替换
  importModeOverwrite: 'Replace all',
  // 位置：src/components/settings/NativeSettings.tsx · 导入方式：覆盖说明
  importModeOverwriteDesc: 'Replace every local session with the archive (chats created after the last import are lost).',
  // 位置：src/components/settings/NativeSettings.tsx · 设置：对话子页标题
  chatSettings: 'Chat',
  // 位置：src/components/settings/NativeSettings.tsx · 设置→对话：模型行
  modelSetting: 'Model',
  // 位置：src/components/chatInput/StatusLine.tsx · 输入框下状态行：上下文
  statusContext: 'Context',
  // 位置：当前未使用 · 状态行：缓存（已不显示）
  statusCache: 'Cache',
  // 位置：当前未使用 · 状态行：首字（已不显示）
  statusFirstToken: 'First token',
  // 位置：src/components/chatInput/StatusLine.tsx · 状态行：生成中
  statusGenerating: 'Generating…',
  // 位置：src/components/session/MobileHeader.tsx · 会话头部压缩按钮：确认标题
  compactNow: 'Compact context',
  // 位置：src/components/session/MobileHeader.tsx · 会话头部压缩按钮：确认正文
  compactConfirm: 'Compact this conversation into a summary? Older messages will be replaced by the summary.',
  // 位置：src/components/session/MobileHeader.tsx · 压缩完成 toast
  compactDone: 'Context compacted',
  // 位置：src/components/session/MobileHeader.tsx · 压缩失败 toast
  compactFailed: 'Compaction failed, please retry',
  // 位置：src/components/settings/NativeSettings.tsx · 设置→对话：上下文压缩开关
  contextCompression: 'Context compression',
  // 位置：src/components/settings/NativeSettings.tsx、src/services/api/types.ts · 通用：开
  enabled: 'On',
  // 位置：src/components/settings/NativeSettings.tsx、src/services/api/types.ts · 通用：关
  disabled: 'Off',
  // 位置：src/components/sider/Sider.tsx · 侧栏搜索框占位
  searchSessionsPlaceholder: 'Search chats',
  // 位置：src/components/sider/Sider.tsx · 搜索：结果过多提示
  searchSessionsNarrow: 'Too many results, try a more specific query',
  // 位置：src/components/sider/Sider.tsx · 搜索：搜索中
  searchSessionsSearching: 'Searching…',
  // 位置：src/components/sider/Sider.tsx · 搜索：无结果
  searchSessionsEmpty: 'No matching chats',
  // 位置：src/components/settings/NativeSettings.tsx · 设置→关于：检查更新行
  checkUpdate: 'Check for updates',
  // 位置：src/components/settings/NativeSettings.tsx · 检查更新：已最新 toast
  updateLatest: 'You are up to date',
  // 位置：src/components/settings/NativeSettings.tsx · 检查更新：发现新版本 toast
  updateAvailable: 'New version available',
  // 位置：src/components/chatInput/ChatInput.tsx · 语音：按住说话
  voiceHoldToSpeak: 'Hold to talk',
  // 位置：src/components/chatInput/ChatInput.tsx · 附件面板：拍照
  attachCamera: 'Camera',
  // 位置：src/components/chatInput/ChatInput.tsx · 附件面板：相册
  attachGallery: 'Photos',
  // 位置：src/components/chatInput/ChatInput.tsx · 附件面板：文件
  attachFile: 'File',
  // 位置：src/components/settings/NativeSettings.tsx · 设置分组：账户
  nativeSettingsAccount: 'Account',
  // 位置：src/components/settings/NativeSettings.tsx · 设置分组：应用
  nativeSettingsApp: 'App',
  // 位置：src/components/settings/NativeSettings.tsx · 设置分组：语音
  nativeSettingsVoice: 'Voice',
  // 位置：src/components/settings/NativeSettings.tsx · 设置分组：关于
  nativeSettingsAbout: 'About',
  // 位置：src/components/settings/NativeSettings.tsx · 设置→数据管理：导入行
  nativeSettingsImportChats: 'Import chats from chat.deepseek.com',
  // 位置：src/components/settings/NativeSettings.tsx · 返回按钮无障碍文案
  back: 'Back',
  // 位置：src/components/session/BalanceBadge.tsx · 顶部余额：标签
  balanceLabel: 'Balance',
  // 位置：src/components/session/BalanceBadge.tsx · 顶部余额：长按提示
  balanceTooltip: 'DeepSeek account balance',
  // 位置：src/components/session/BalanceBadge.tsx、src/components/settings/AccountPane.tsx · 余额：赠送
  balanceBonus: 'bonus',
  // 位置：src/components/session/BalanceBadge.tsx · 顶部余额：获取失败
  balanceFailed: 'Failed to load balance',
  welcomeMessageChat250808: 'How can I help you?',
  contentCaveat: 'AI-generated, for reference only',
  expandSiderbarButton: 'Open sidebar',
  collapseSiderbarButton: 'Close sidebar',
  newChat: 'New chat',
  untitledChat: 'New chat',
  sessionLoading: 'Loading chats...',
  sessionListLoadFailed: 'Unable to load the chat list. Please try again.',
  sessionLoadFailed: 'Unable to load this chat. Please try again.',
  sessionSaveFailed:
    'Chats or attachments have not been saved locally. Retry before closing this page.',
  sessionDeleteFailed: 'Unable to delete this chat. Please try again.',
  settings: 'Settings',
  appName: 'API usage demo',
  deleteSessionModalContentTitle: 'Delete chat?',
  deleteSessionWithFilesDescription:
    'This chat and its uploaded files will be permanently deleted and cannot be recovered. Delete this chat?',
  cancel: 'Cancel',
  delete: 'Delete',
  sessionItemMenuRenameOption: 'Rename',
  more: 'More',
  close: 'Close',
  sessionHistoryTimelineLabelToday: 'Today',
  sessionHistoryTimelineLabelYesterday: 'Yesterday',
  sessionHistoryTimelineLabel7days: '7 Days',
  sessionHistoryTimelineLabel30days: '30 Days',
  copy: 'Copy',
  copied: 'Copied',
  copyFailed: 'Copy failed. Please long-press or select the text to copy.',
  send: 'Send',
  editMessage: 'Edit message',
  regenerate: 'Regenerate',
  continueGenerating: 'Continue',
  changeApiKey: 'Change API Key',
  topUp: 'Top up',
  messageSearchSummary: '1 web page',
  messageSearchSummaries: '{count} web pages',
  messageVersions: 'Message versions',
  previousVersion: 'Previous version',
  nextVersion: 'Next version',
  searchResult: 'Search results',
  mermaidDiagram: 'Diagram',
  mermaidCode: 'Code',
  mermaidEmpty: 'This diagram is empty.',
  mermaidLoading: 'Rendering diagram...',
  mermaidRenderFail: 'Mermaid rendering failed.',
  mermaidSwitchLabel: 'Toggle between Mermaid diagram and code',
  toolSearchIntro: 'Searching for',
  messageSearching: 'Searching...',
  imageSearching: 'Searching by image...',
  searchStopped: 'Search stopped',
  toolSearchFail: 'Search currently unavailable',
  toolSearchNoResult: 'No suitable results found',
  messageSearchNoResult: 'No relevant results found',
  toolSearch1Result: 'Found 1 web page',
  toolSearchResults: 'Found {count} web pages',
  messageSearchResult: 'Read 1 web page',
  messageSearchResults: 'Read {count} web pages',
  toolOpenOneIntro: 'Read 1 page',
  toolOpensIntro: 'Read {count} pages',
  toolOpenFail: 'Read failed',
  toolOpenFailTooltip: 'Read failed',
  toolOpenViewAll: 'View All',
  toolFindFail: 'Find failed',
  toolFindIntro: 'Find keyword',
  toolFindFailTooltip: 'No related content found',
  readingLink: 'Reading',
  commonNetworkErrorToast: 'Network Error. Please check your network status.',
  replyInvalidApiKey:
    'The API Key is invalid or has been deleted. Please replace it and try again.',
  replyInsufficientBalance: 'Insufficient account balance. Please top up and try again.',
  replyRateLimited: 'Too many requests. Please try again later.',
  completionServerBusyToast: 'Server busy, please try again later.',
  unknownError: 'Unknown error',
  replyRefused: "Sorry, that's beyond my current scope. Let's talk about something else.",
  messageThinkDuration: 'Thought for {count} second',
  messageThinkDurationPlural: 'Thought for {count} seconds',
  messageThinkStopped: 'Stopped',
  deepSearchSearching: 'Searching the web',
  groupTitleToolOpen: 'Browsing the web',
  messageThinking: 'Thinking',
  backupInvalidJson: 'The file is not valid JSON.',
  backupUnrecognized: 'File not recognized. Please choose a DeepSeek chat history ZIP.',
  backupNewerVersion: 'This file uses a newer version. Please update the app before importing.',
  backupInvalidVersion: 'The file version is invalid and cannot be imported.',
  backupIncomplete: 'The file is incomplete and cannot be imported.',
  backupInvalidBranches: 'The message branches in this file are corrupted and cannot be imported.',
  backupInvalidFiles: 'The attachment data in this file is corrupted and cannot be imported.',
  settingsLocalePreference: 'Language',
  settingsTtsVoice: 'Read-aloud voice',
  settingsTtsVoiceStella: 'Stella (female)',
  settingsTtsVoiceTide: 'Tide (male)',
  settingsLocation: 'Location',
  settingsLocationProvince: 'Select province/region',
  settingsLocationCity: 'Select city',
  settingsLocationRegion: 'Select country or region',
  settingDialogAsideGeneralOptionLabel: 'General',
  settingDialogAsideDataOptionLabel: 'Data',
  avatarMenuSettingsDialogThemeItemLabel: 'Theme',
  avatarMenuSettingsDialogThemeSelectLightOption: 'Light',
  avatarMenuSettingsDialogThemeSelectDarkOption: 'Dark',
  avatarMenuSettingsDialogThemeSelectSystemOption: 'System',
  apiKeyStart: 'Enter an API Key to get started',
  apiKeyEdit: 'Edit API Key',
  apiKeyPlaceholder: 'Enter API Key',
  apiKeyVerifying: 'Verifying...',
  confirm: 'Confirm',
  apiKeyInvalid: 'Invalid API Key. Please check it and try again.',
  apiKeyVerificationFailed: 'Verification failed: {message}',
  historyImport: 'Import chat history',
  historyImportConfirm: 'Import',
  historyImportConfirmTitle: 'Import chat history?',
  historyImportConfirmDescription:
    'The uploaded chat history will replace all your current chats. Import anyway?',
  historyImportDescription:
    'Importing chat history replaces all local chats and attachments. Supports ZIP files exported from DeepSeek.',
  historyImportedOne: 'Imported 1 chat',
  historyImported: 'Imported {count} chats',
  historyImportFailed:
    'File import failed. The file format may be invalid, or the ZIP file may use an older chat export format. Please export your chat history from DeepSeek again and retry the import.',
  historyExportFailed: 'Export failed: {message}',
  exportAllLabel: 'Export all chats',
  historyExportDescription:
    'Includes all chat history and locally stored images. Other attachments retain file details only. Your API Key is not included.',
  exportAllDownload: 'Download',
  deleteAllSessions: 'Delete all chats',
  deleteAllSessionsModalTitle: 'Once deleted, all chats will be cleared and cannot be recovered.',
  deleteAllSessionsModalDescription:
    'Locally saved attachments and unsent drafts will also be deleted.',
  historyDeleting: 'Deleting...',
  historyDeleted: 'Successfully deleted.',
  historyDeleteFailed: 'Failed to delete chats. Please try again.',
  upload: 'Upload',
  uploadUnsupportedFormat:
    'The selected files include unsupported formats. Please convert them and try again. Supported: PDF, DOC, XLSX, PPT, images, text, code, and more.',
  uploadFileStatusFailed: 'Upload failed',
  uploadRetry: '{message}. Click to retry',
  chatInputPlaceholderChat: 'Enter a message',
  deactivatedThinkTooltip: 'Think before responding to solve reasoning problems',
  thinkingSwitch: 'DeepThink',
  deactivatedSearchTooltip: 'Search the web when necessary',
  search: 'Search',
  uploadFile: 'Upload files',
  chatInputStopButtonTooltip: 'Stop',
  uploadMaxMBSize: 'File exceeds the size limit ({size} MB). Please adjust and try again.',
  uploadMaxFiles: 'You can upload up to {count} files',
  uploadFileStatusUploading: 'Uploading...',
  uploadServerBusy: 'Server busy',
  uploadParseFailed: 'Parsing failed',
  uploadFileCountLimit: 'Uploaded file limit reached ({limit} files). ',
  uploadStorageLimit: 'Uploaded file storage limit reached ({limit} GB). ',
  uploadDeleteFiles: 'Delete files',
  dragFileToUpload: 'Drop files here to upload',
  dragFileDesc: 'Up to {count} files, {size} MB each',
  voiceInput: 'Voice input',
  voiceInputTimer: 'Recording {seconds}s',
  voiceInputStop: 'Stop recording and transcribe',
  voiceInputCancel: 'Transcribing. Click to cancel',
  voiceInputPermissionDenied:
    'Microphone access is blocked. Please allow it in your browser settings.',
  voiceInputNoDevice: 'No microphone detected',
  voiceInputTooLong: 'Recording is too long. Please shorten it and try again.',
  voiceInputFailed: 'Speech recognition failed. Please try again later.',
  voiceInputEmpty: 'No speech detected',
  voiceInputLimitReached: 'Maximum recording length reached',
  voiceInputRequiresHttps: 'Voice input requires HTTPS',
  apiKeyInvalidRetry: 'Invalid API Key. Please change it and try again.',
  insufficientBalance: 'Insufficient balance',
  rateLimited: 'Too many requests. Please try again later.',
  ttsPlay: 'Read aloud',
  ttsStop: 'Stop reading',
  ttsUnavailable: 'This answer cannot be read aloud right now',
  ttsFailed: 'Failed to read aloud. Please try again later.',
  retry: 'Retry',
  latestReminderLanguage: 'English',
  settingsLocaleZhCN: 'Simplified Chinese',
  settingsLocaleEnUS: 'English',
  accountTab: 'Account',
  accountLoginHint: 'Sign in to import your chats and create an API Key.',
  accountLoginButton: 'Sign in and import',
  accountLoginShort: 'Sign in to DeepSeek',
  accountRecharge: 'Top up',
  accountScanAssist: 'Scan assist',
  accountRechargeHint:
    'Opens the desktop top-up page, detects the WeChat QR code and opens WeChat',
  accountTermsTitle: 'Terms of Use',
  accountTermsBody:
    'By continuing you confirm that you have read and agree to the DeepSeek Terms of Use and Privacy Policy. After signing in, the app imports your chats and chats through your DeepSeek account (no API key needed).',
  accountTermsAgree: 'Agree and continue',
  onboardingTitle: 'Welcome to DeepSeek Chat Demo',
  onboardingPitch:
    'DeepSeek web chat, on Android: sign in once, import every conversation, then chat through your DeepSeek account without an API key.',
  onboardingStep1: 'Sign in to chat.deepseek.com and import all conversations',
  onboardingStep2: 'Authorize your DeepSeek account and chat on your API quota',
  onboardingStep3: 'Not subject to web limits; top up from inside the app when needed',
  onboardingStart: 'Get started',
  onboardingNote: 'Unofficial app. Not affiliated with DeepSeek.',
  aboutTitle: 'About',
  aboutVersion: 'Version',
  aboutSource: 'Source code',
  aboutLicense: 'License',
  aboutDisclaimer:
    'Unofficial app built on DeepSeek chat-demo (MIT). Not affiliated with DeepSeek.',
  onboardingImportTitle: 'Step 1: import your chats',
  onboardingImportPitch:
    'After you sign in to chat.deepseek.com the app pulls that account export and imports it. You can skip and import later from Settings → Account.',
  onboardingLoginImport: 'Sign in and import',
  onboardingSkip: 'Skip',
  onboardingAuthTitle: 'Step 2: authorize your DeepSeek account',
  onboardingAuthPitch:
    'Chat through your API quota instead of the web limits, without creating an API key by hand.',
  onboardingAuthButton: 'Authorize and continue',
  onboardingNext: 'Next',
  onboardingDoneTitle: 'Ready',
  onboardingDonePitch: 'You can start chatting now. Import or authorize again anytime in Settings → Account.',
  onboardingEnter: 'Enter the app',
  accountLoggedIn: 'Signed in. API Key saved on this device.',
  accountLoggedInAccount: 'Signed in with your DeepSeek account (no API key needed)',
  accountReimportButton: 'Import again',
  accountProgressChatLogin: 'Opening DeepSeek sign-in...',
  accountProgressExporting: 'Exporting chats, this may take a few minutes...',
  accountProgressImporting: 'Importing chats...',
  accountProgressPlatformLogin: 'Signing in to DeepSeek Platform...',
  accountProgressCreatingKey: 'Creating your API Key...',
  accountImported: 'Imported {count} chats',
  accountDone: 'Done',
  accountPreviewTitle: 'Found {count} chats',
  accountPreviewUntitled: 'Untitled',
  accountPreviewMessageCount: '{count} messages',
  accountPreviewWarning: 'Importing replaces all chats on this device',
  accountPreviewConfirm: 'Import',
} satisfies Record<keyof typeof zh, string>

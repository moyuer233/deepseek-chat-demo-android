export const zh = {
  // 位置：src/components/chatInput/ChatInput.tsx:230
  voiceReleaseToSend: '松开发送',
  // 位置：src/components/chatInput/ChatInput.tsx:230
  voiceReleaseCancel: '松开取消',
  // 位置：src/components/chatInput/ChatInput.tsx:231
  voiceSlideUpCancel: '上滑取消',
  // 位置：src/services/chat/store.ts:1211
  contextOverflowCompacted: '上下文已压缩，请重试',
  // 位置：src/services/chat/store.ts:1212
  contextOverflowCompactFailed: '上下文已超限，压缩失败，请重试',
  // 位置：当前未使用
  usageStats: '用量与上下文',
  // 位置：src/components/settings/NativeSettings.tsx:204
  usageRequests: '请求数',
  // 位置：src/components/settings/NativeSettings.tsx:205
  usageInput: '输入 tokens',
  // 位置：src/components/settings/NativeSettings.tsx:206
  usageCacheRead: '缓存命中 tokens',
  // 位置：src/components/settings/NativeSettings.tsx:207
  usageOutput: '输出 tokens',
  // 位置：当前未使用
  usageCost: '费用',
  // 位置：src/components/settings/NativeSettings.tsx:212
  usageContext: '上下文占用',
  // 位置：src/components/settings/NativeSettings.tsx:200
  usageSession: '本会话',
  // 位置：src/components/settings/NativeSettings.tsx:214
  usageTotal: '全部会话',
  // 位置：src/components/settings/settingsDialogState.ts:24
  loginRequiredHint: '请先登录 DeepSeek 账号或填写 API Key',
  // 位置：src/components/settings/AccountPane.tsx:181
  apiKeyLabel: 'API Key',
  // 位置：src/components/settings/AccountPane.tsx:182
  apiKeySetTail: '已设置（尾号 {tail}）',
  // 位置：src/components/settings/AccountPane.tsx:182
  apiKeyNotSet: '未设置',
  // 位置：src/components/settings/AccountPane.tsx:266
  apiKeyHint: '没有账号授权时可用 API Key 聊天（本机保存，不上传）。到 platform.deepseek.com 创建。',
  // 位置：src/components/settings/AccountPane.tsx:260
  apiKeySave: '保存',
  // 位置：src/components/settings/AccountPane.tsx:247
  apiKeyClear: '清除',
  // 位置：src/components/settings/AccountPane.tsx:256
  apiKeySaved: 'API Key 已保存',
  // 位置：src/components/settings/AccountPane.tsx:244
  apiKeyCleared: 'API Key 已清除',
  // 位置：src/components/settings/AccountPane.tsx:165
  accountSignIn: '登录 DeepSeek 账号',
  // 位置：src/components/settings/AccountPane.tsx:135
  accountBalance: '余额',
  // 位置：src/components/settings/AccountPane.tsx:139
  accountBalanceRetry: '余额读取失败，重试',
  // 位置：src/components/settings/AccountPane.tsx:199
  accountMoreInfo: '更多账号信息',
  // 位置：src/components/settings/AccountPane.tsx:205
  accountLogout: '退出登录',
  // 位置：src/components/settings/AccountPane.tsx:297
  accountLogoutConfirm: '确认退出',
  // 位置：src/components/settings/AccountPane.tsx:303
  accountLogoutBody: '退出后本机聊天记录保留，下次登录可继续使用。',
  // 位置：src/components/settings/AccountPane.tsx:303
  accountLogoutActive: '当前有回复正在生成，退出会中断生成，本机聊天记录仍保留。',
  // 位置：src/components/settings/AccountPane.tsx:117
  accountAuthAccount: '已授权 DeepSeek 账号',
  // 位置：src/components/settings/AccountPane.tsx:119
  accountAuthApiKey: '使用 API Key',
  // 位置：src/components/settings/AccountPane.tsx:124
  accountAuthAccountLabel: '账号授权（DSH grant）',
  // 位置：src/components/settings/AccountPane.tsx:126
  accountAuthApiKeyLabel: 'API Key（本机保存）',
  // 位置：src/components/settings/AccountPane.tsx:120
  accountNotLoggedIn: '未登录',
  // 位置：src/components/settings/NativeSettings.tsx:255
  importStats: '新增 {added} · 有更新 {updated} · 本地独有 {localOnly}',
  // 位置：src/components/settings/NativeSettings.tsx:65
  importUpToDate: '已是最新，无需导入',
  // 位置：src/components/settings/NativeSettings.tsx:246
  importModeTitle: '选择导入方式',
  // 位置：src/components/settings/NativeSettings.tsx:256
  importModeHint: '同一个归档可能有官方更新、也可能是重复导入，请选择处理方式：',
  // 位置：src/components/settings/NativeSettings.tsx:258
  importModeMerge: '合并（推荐）',
  // 位置：src/components/settings/NativeSettings.tsx:259
  importModeMergeDesc: '按会话合并：本机没有的新增；已有的合并消息并保留本地新内容；不会删除本机会话。',
  // 位置：src/components/settings/NativeSettings.tsx:262
  importModeAdd: '仅新增',
  // 位置：src/components/settings/NativeSettings.tsx:263
  importModeAddDesc: '只导入本机不存在的会话；已存在的会话保持不变。',
  // 位置：src/components/settings/NativeSettings.tsx:266
  importModeOverwrite: '覆盖替换',
  // 位置：src/components/settings/NativeSettings.tsx:267
  importModeOverwriteDesc: '用归档替换本机全部会话（导入后新聊的内容会丢失）。',
  // 位置：src/components/settings/NativeSettings.tsx:184
  chatSettings: '对话',
  // 位置：src/components/settings/NativeSettings.tsx:189
  modelSetting: '模型',
  // 位置：src/components/chatInput/StatusLine.tsx:28
  statusContext: '上下文',
  // 位置：当前未使用
  statusCache: '缓存',
  // 位置：当前未使用
  statusFirstToken: '首字',
  // 位置：src/components/chatInput/StatusLine.tsx:37
  statusGenerating: '生成中…',
  // 位置：src/components/session/MobileHeader.tsx:44
  compactNow: '压缩上下文',
  // 位置：src/components/session/MobileHeader.tsx:19
  compactConfirm: '把当前会话压缩成摘要？旧消息会被摘要替换，之后无法直接查看原文。',
  // 位置：src/components/session/MobileHeader.tsx:23
  compactDone: '上下文已压缩',
  // 位置：src/components/session/MobileHeader.tsx:23
  compactFailed: '压缩失败，请稍后重试',
  // 位置：src/components/settings/NativeSettings.tsx:195
  contextCompression: '上下文压缩',
  // 位置：src/components/settings/NativeSettings.tsx:196、src/services/api/types.ts:179
  enabled: '开',
  // 位置：src/components/settings/NativeSettings.tsx:196、src/services/api/types.ts:179
  disabled: '关',
  // 位置：src/components/sider/Sider.tsx:174
  searchSessionsPlaceholder: '搜索会话',
  // 位置：src/components/sider/Sider.tsx:216
  searchSessionsNarrow: '结果较多，请输入更精确的关键词',
  // 位置：src/components/sider/Sider.tsx:214
  searchSessionsSearching: '搜索中…',
  // 位置：src/components/sider/Sider.tsx:212
  searchSessionsEmpty: '没有匹配的会话',
  // 位置：src/components/settings/NativeSettings.tsx:315
  checkUpdate: '检查更新',
  // 位置：src/components/settings/NativeSettings.tsx:136
  updateLatest: '已是最新版本',
  // 位置：src/components/settings/NativeSettings.tsx:131
  updateAvailable: '发现新版本',
  // 位置：src/components/chatInput/ChatInput.tsx:234
  voiceHoldToSpeak: '按住说话',
  // 位置：src/components/chatInput/ChatInput.tsx:423
  attachCamera: '拍照',
  // 位置：src/components/chatInput/ChatInput.tsx:424
  attachGallery: '相册',
  // 位置：src/components/chatInput/ChatInput.tsx:425
  attachFile: '文件',
  // 位置：src/components/settings/NativeSettings.tsx:294
  nativeSettingsAccount: '账户',
  // 位置：src/components/settings/NativeSettings.tsx:300
  nativeSettingsApp: '应用',
  // 位置：src/components/settings/NativeSettings.tsx:307
  nativeSettingsVoice: '语音',
  // 位置：src/components/settings/NativeSettings.tsx:312
  nativeSettingsAbout: '关于',
  // 位置：src/components/settings/NativeSettings.tsx:234
  nativeSettingsImportChats: '从 chat.deepseek.com 导入会话',
  // 位置：src/components/settings/NativeSettings.tsx:158
  back: '返回',
  // 位置：src/components/session/BalanceBadge.tsx:72
  balanceLabel: '余额',
  // 位置：src/components/session/BalanceBadge.tsx:70
  balanceTooltip: 'DeepSeek 账户余额',
  // 位置：src/components/session/BalanceBadge.tsx:75、src/components/settings/AccountPane.tsx:146
  balanceBonus: '赠送',
  // 位置：src/components/session/BalanceBadge.tsx:59
  balanceFailed: '余额获取失败',
  // 位置：src/components/session/Welcome.tsx:25
  welcomeMessageChat250808: '今天有什么可以帮到你？',
  // 位置：src/components/session/SessionView.tsx:40、src/components/session/Welcome.tsx:45
  contentCaveat: '内容由 AI 生成，请仔细甄别',
  // 位置：src/components/session/MobileHeader.tsx:32、src/components/sider/CollapsedSider.tsx:18
  expandSiderbarButton: '打开边栏',
  // 位置：当前未使用
  collapseSiderbarButton: '收起边栏',
  // 位置：src/components/session/MobileHeader.tsx:58、src/components/sider/CollapsedSider.tsx:25
  newChat: '开启新对话',
  // 位置：src/components/session/SessionView.tsx:22、src/components/sider/SessionItem.tsx:23
  untitledChat: '新对话',
  // 位置：src/app/App.tsx:227
  sessionLoading: '正在加载对话...',
  // 位置：src/app/App.tsx:226
  sessionListLoadFailed: '对话列表加载失败，请重试',
  // 位置：src/app/App.tsx:226
  sessionLoadFailed: '对话加载失败，请重试',
  // 位置：src/app/App.tsx:207
  sessionSaveFailed: '对话或附件尚未保存到本机，关闭页面可能丢失，请重试',
  // 位置：src/components/sider/Sider.tsx:279
  sessionDeleteFailed: '删除对话失败，请重试',
  // 位置：src/components/settings/NativeSettings.tsx:292、src/components/settings/SettingsDialog.tsx:95
  settings: '设置',
  // 位置：当前未使用
  appName: 'API 使用 demo',
  // 位置：src/components/sider/Sider.tsx:259
  deleteSessionModalContentTitle: '永久删除对话',
  // 位置：src/components/sider/Sider.tsx:291
  deleteSessionWithFilesDescription: '删除后，该对话及其中上传的文件将不可恢复。确认删除吗？',
  // 位置：src/components/messages/UserMessage.tsx:108、src/components/onboarding/Onboarding.tsx:117
  cancel: '取消',
  // 位置：src/components/chatInput/DraftAttachments.tsx:86、src/components/files/FileCard.tsx:90
  delete: '删除',
  // 位置：src/components/sider/SessionItem.tsx:64
  sessionItemMenuRenameOption: '重命名',
  // 位置：src/components/sider/SessionItem.tsx:78
  more: '更多',
  // 位置：src/components/base/Modal.tsx:71、src/components/search/SearchPanel.tsx:24
  close: '关闭',
  // 位置：src/services/chat/sessionGroups.ts:16
  sessionHistoryTimelineLabelToday: '今天',
  // 位置：src/services/chat/sessionGroups.ts:18
  sessionHistoryTimelineLabelYesterday: '昨天',
  // 位置：src/services/chat/sessionGroups.ts:20
  sessionHistoryTimelineLabel7days: '7 天内',
  // 位置：src/services/chat/sessionGroups.ts:22
  sessionHistoryTimelineLabel30days: '30 天内',
  // 位置：src/components/markdown/CodeBlock.tsx:66、src/components/messages/CopyButton.tsx:18
  copy: '复制',
  // 位置：src/components/markdown/CodeBlock.tsx:66
  copied: '已复制',
  // 位置：src/components/markdown/CodeBlock.tsx:58、src/components/messages/CopyButton.tsx:25
  copyFailed: '复制失败，请长按或选中文本后复制',
  // 位置：src/components/chatInput/ChatInput.tsx:411、src/components/messages/UserMessage.tsx:111
  send: '发送',
  // 位置：src/components/messages/UserMessage.tsx:88
  editMessage: '编辑消息',
  // 位置：src/components/messages/AssistantMessage.tsx:158
  regenerate: '重新生成',
  // 位置：src/components/messages/AssistantMessage.tsx:178
  continueGenerating: '继续生成',
  // 位置：src/components/messages/AssistantMessage.tsx:109
  changeApiKey: '更换 API Key',
  // 位置：src/components/messages/AssistantMessage.tsx:113、src/services/chat/describe.ts:10
  topUp: '前往充值',
  // 位置：src/components/messages/AssistantMessage.tsx:142
  messageSearchSummary: '1 个网页',
  // 位置：src/components/messages/AssistantMessage.tsx:142
  messageSearchSummaries: '{count} 个网页',
  // 位置：src/components/messages/BranchSwitcher.tsx:35
  messageVersions: '消息版本',
  // 位置：src/components/messages/BranchSwitcher.tsx:41
  previousVersion: '上一个版本',
  // 位置：src/components/messages/BranchSwitcher.tsx:50
  nextVersion: '下一个版本',
  // 位置：src/components/search/SearchPanel.tsx:20
  searchResult: '搜索结果',
  // 位置：src/components/markdown/CodeBlock.tsx:39
  mermaidDiagram: '图表',
  // 位置：src/components/markdown/CodeBlock.tsx:47
  mermaidCode: '代码',
  // 位置：src/components/markdown/MermaidDiagram.tsx:83
  mermaidEmpty: '图表为空',
  // 位置：src/components/markdown/MermaidDiagram.tsx:86
  mermaidLoading: '图表生成中...',
  // 位置：src/components/markdown/MermaidDiagram.tsx:85
  mermaidRenderFail: '渲染失败',
  // 位置：src/components/markdown/CodeBlock.tsx:32
  mermaidSwitchLabel: 'Mermaid 图表与代码切换',
  // 位置：src/components/messages/ReplyArea.tsx:212
  toolSearchIntro: '搜索',
  // 位置：src/components/messages/ReplyArea.tsx:218
  messageSearching: '搜索中...',
  // 位置：src/components/messages/ReplyArea.tsx:218、src/services/chat/view.ts:399
  imageSearching: '图像搜索中...',
  // 位置：src/components/messages/ReplyArea.tsx:221
  searchStopped: '搜索已停止',
  // 位置：src/components/messages/ReplyArea.tsx:223
  toolSearchFail: '联网搜索暂不可用',
  // 位置：src/components/messages/ReplyArea.tsx:226
  toolSearchNoResult: '未搜索到合适内容',
  // 位置：src/components/messages/ReplyArea.tsx:226
  messageSearchNoResult: '未搜索到相关内容',
  // 位置：src/components/messages/ReplyArea.tsx:232
  toolSearch1Result: '搜索到 1 个网页',
  // 位置：src/components/messages/ReplyArea.tsx:233
  toolSearchResults: '搜索到 {count} 个网页',
  // 位置：src/components/messages/ReplyArea.tsx:235
  messageSearchResult: '已阅读 1 个网页',
  // 位置：src/components/messages/ReplyArea.tsx:236
  messageSearchResults: '已阅读 {count} 个网页',
  // 位置：src/components/messages/ReplyArea.tsx:329
  toolOpenOneIntro: '浏览 1 个页面',
  // 位置：src/components/messages/ReplyArea.tsx:329
  toolOpensIntro: '浏览 {count} 个页面',
  // 位置：src/components/messages/ReplyArea.tsx:337
  toolOpenFail: '浏览页面失败',
  // 位置：src/components/messages/ReplyArea.tsx:342
  toolOpenFailTooltip: '浏览失败',
  // 位置：src/components/messages/ReplyArea.tsx:354
  toolOpenViewAll: '查看全部',
  // 位置：src/components/messages/ReplyArea.tsx:373
  toolFindFail: '查找失败',
  // 位置：src/components/messages/ReplyArea.tsx:376
  toolFindIntro: '查找关键词',
  // 位置：src/components/messages/ReplyArea.tsx:380
  toolFindFailTooltip: '未查找到相关内容',
  // 位置：src/components/messages/ReplyArea.tsx:403、src/services/chat/view.ts:408
  readingLink: '正在阅读',
  // 位置：src/components/settings/useApiKeyForm.ts:20、src/services/chat/describe.ts:32
  commonNetworkErrorToast: '网络异常，请检查你的网络状况',
  // 位置：src/services/chat/describe.ts:36
  replyInvalidApiKey: 'API Key 无效或已被删除，请更换后重试',
  // 位置：src/services/chat/describe.ts:38
  replyInsufficientBalance: '账户余额不足，请充值后重试',
  // 位置：src/services/chat/describe.ts:40
  replyRateLimited: '请求过于频繁，请稍后再试',
  // 位置：src/services/chat/describe.ts:42
  completionServerBusyToast: '服务器繁忙，请稍后再试',
  // 位置：src/components/onboarding/Onboarding.tsx:103、src/components/settings/AccountPane.tsx:105
  unknownError: '未知错误',
  // 位置：src/services/chat/describe.ts:54
  replyRefused: '你好，这个问题我暂时无法回答，让我们换个话题再聊聊吧。',
  // 位置：src/services/chat/view.ts:392
  messageThinkDuration: '已思考（用时 {count} 秒）',
  // 位置：src/services/chat/view.ts:392
  messageThinkDurationPlural: '已思考（用时 {count} 秒）',
  // 位置：src/services/chat/view.ts:395
  messageThinkStopped: '已停止',
  // 位置：src/services/chat/view.ts:399
  deepSearchSearching: '正在搜索',
  // 位置：src/services/chat/view.ts:405
  groupTitleToolOpen: '正在浏览网页',
  // 位置：src/services/chat/view.ts:410
  messageThinking: '正在思考',
  // 位置：src/services/chat/history/importArchive.ts:49
  backupInvalidJson: '文件不是有效的 JSON',
  // 位置：src/services/chat/history/importArchive.ts:59
  backupUnrecognized: '无法识别该文件，请选择 DeepSeek 导出的历史对话 ZIP 文件',
  // 位置：src/services/chat/history/schema.ts:250
  backupNewerVersion: '文件版本过新，请升级后再导入',
  // 位置：src/services/chat/history/schema.ts:253
  backupInvalidVersion: '文件版本无效，无法导入',
  // 位置：src/services/chat/history/importArchive.ts:73、src/services/chat/history/schema.ts:19
  backupIncomplete: '文件内容不完整，无法导入',
  // 位置：src/services/chat/history/toArchive.ts:221
  backupInvalidBranches: '文件中的消息分支已损坏，无法导入',
  // 位置：src/services/chat/history/toLocal.ts:316
  backupInvalidFiles: '文件中的附件数据已损坏，无法导入',
  // 位置：src/components/settings/NativeSettings.tsx:303、src/components/settings/SettingsDialog.tsx:323
  settingsLocalePreference: '语言',
  // 位置：src/components/settings/NativeSettings.tsx:309、src/components/settings/SettingsDialog.tsx:346
  settingsTtsVoice: '朗读音色',
  // 位置：src/components/settings/SettingsDialog.tsx:353
  settingsTtsVoiceStella: '海星（女声）',
  // 位置：src/components/settings/SettingsDialog.tsx:354
  settingsTtsVoiceTide: '暗潮（男声）',
  // 位置：src/components/settings/SettingsDialog.tsx:265
  settingsLocation: '位置信息',
  // 位置：src/components/settings/SettingsDialog.tsx:273
  settingsLocationProvince: '请选择省份/地区',
  // 位置：src/components/settings/SettingsDialog.tsx:285
  settingsLocationCity: '请选择市',
  // 位置：src/components/settings/SettingsDialog.tsx:302
  settingsLocationRegion: '请选择国家或地区',
  // 位置：src/components/settings/SettingsDialog.tsx:69
  settingDialogAsideGeneralOptionLabel: '通用设置',
  // 位置：src/components/settings/NativeSettings.tsx:228、src/components/settings/SettingsDialog.tsx:72
  settingDialogAsideDataOptionLabel: '数据管理',
  // 位置：src/components/settings/NativeSettings.tsx:304、src/components/settings/SettingsDialog.tsx:416
  avatarMenuSettingsDialogThemeItemLabel: '主题',
  // 位置：src/components/settings/NativeSettings.tsx:53、src/components/settings/SettingsDialog.tsx:394
  avatarMenuSettingsDialogThemeSelectLightOption: '浅色',
  // 位置：src/components/settings/NativeSettings.tsx:54、src/components/settings/SettingsDialog.tsx:399
  avatarMenuSettingsDialogThemeSelectDarkOption: '深色',
  // 位置：src/components/settings/NativeSettings.tsx:51、src/components/settings/SettingsDialog.tsx:404
  avatarMenuSettingsDialogThemeSelectSystemOption: '跟随系统',
  // 位置：src/components/settings/ApiKeyField.tsx:29
  apiKeyStart: '输入 API Key 开始使用',
  // 位置：src/components/settings/ApiKeyField.tsx:36
  apiKeyEdit: '修改 API Key',
  // 位置：src/components/settings/ApiKeyField.tsx:44
  apiKeyPlaceholder: '请输入 API Key',
  // 位置：src/components/settings/ApiKeyField.tsx:74
  apiKeyVerifying: '正在验证...',
  // 位置：src/components/settings/ApiKeyField.tsx:74
  confirm: '确定',
  // 位置：src/components/settings/useApiKeyForm.ts:18
  apiKeyInvalid: 'API Key 无效，请检查后重试',
  // 位置：src/components/settings/useApiKeyForm.ts:21
  apiKeyVerificationFailed: '校验失败：{message}',
  // 位置：src/components/settings/SettingsDialog.tsx:528
  historyImport: '导入历史对话',
  // 位置：src/components/settings/SettingsDialog.tsx:381
  historyImportConfirm: '确认导入',
  // 位置：src/components/settings/SettingsDialog.tsx:372、src/services/deepseek/account.ts:72
  historyImportConfirmTitle: '确认导入历史对话？',
  // 位置：src/components/settings/SettingsDialog.tsx:386、src/services/deepseek/account.ts:72
  historyImportConfirmDescription: '上传的历史对话会覆盖当前所有对话记录，是否确认导入？',
  // 位置：src/components/settings/SettingsDialog.tsx:529
  historyImportDescription: '导入历史对话会覆盖全部本地对话和附件，支持 DeepSeek 导出的 ZIP 文件。',
  // 位置：src/components/settings/NativeSettings.tsx:83、src/components/settings/SettingsDialog.tsx:502
  historyImportedOne: '已导入 1 个对话',
  // 位置：src/components/settings/NativeSettings.tsx:83、src/components/settings/SettingsDialog.tsx:502
  historyImported: '已导入 {count} 个对话',
  // 位置：src/components/settings/NativeSettings.tsx:70、src/components/settings/SettingsDialog.tsx:505
  historyImportFailed:
    '文件导入失败，可能是文件格式错误或使用了旧版的对话导出 ZIP 文件，请前往 DeepSeek 重新导出历史对话文件后再次导入。',
  // 位置：src/components/messages/MessageFile.tsx:161、src/components/settings/NativeSettings.tsx:102
  historyExportFailed: '导出失败：{message}',
  // 位置：src/components/settings/SettingsDialog.tsx:584
  exportAllLabel: '导出所有历史对话',
  // 位置：src/components/settings/SettingsDialog.tsx:585
  historyExportDescription:
    '包含所有历史对话及本地图片原件；其他附件仅保留文件信息，不包含 API Key。',
  // 位置：src/components/settings/NativeSettings.tsx:241、src/components/settings/SettingsDialog.tsx:596
  exportAllDownload: '下载',
  // 位置：src/components/settings/NativeSettings.tsx:242、src/components/settings/SettingsDialog.tsx:157
  deleteAllSessions: '删除所有对话',
  // 位置：src/components/settings/SettingsDialog.tsx:190
  deleteAllSessionsModalTitle: '删除后，所有对话都将被清空且无法找回',
  // 位置：src/components/settings/NativeSettings.tsx:145、src/components/settings/SettingsDialog.tsx:213
  deleteAllSessionsModalDescription: '本地保存的附件及未发送的草稿也将被删除。',
  // 位置：src/components/settings/SettingsDialog.tsx:208
  historyDeleting: '删除中...',
  // 位置：src/components/settings/NativeSettings.tsx:150、src/components/settings/SettingsDialog.tsx:232
  historyDeleted: '删除成功',
  // 位置：src/components/settings/NativeSettings.tsx:152、src/components/settings/SettingsDialog.tsx:234
  historyDeleteFailed: '删除失败，请重试',
  // 位置：src/components/messages/MessageList.tsx:25、src/components/settings/NativeSettings.tsx:231
  upload: '上传',
  // 位置：src/services/attachments.ts:51
  uploadUnsupportedFormat:
    '上传的文件包含暂未支持的文件格式，请转换文件格式后重试。支持 PDF、DOC、XLSX、PPT、图片、文本、代码等。',
  // 位置：src/components/chatInput/DraftAttachments.tsx:59、src/components/files/FileCard.tsx:46
  uploadFileStatusFailed: '上传失败',
  // 位置：src/components/chatInput/DraftAttachments.tsx:59、src/components/files/FileCard.tsx:61
  uploadRetry: '{message}，点击重试',
  // 位置：src/components/chatInput/ChatInput.tsx:241
  chatInputPlaceholderChat: '请输入消息',
  // 位置：src/components/chatInput/ChatInput.tsx:277
  deactivatedThinkTooltip: '先思考后回答，解决推理问题',
  // 位置：src/components/chatInput/ChatInput.tsx:281、src/services/chat/view.ts:390
  thinkingSwitch: '深度思考',
  // 位置：src/components/chatInput/ChatInput.tsx:289
  deactivatedSearchTooltip: '按需搜索网页',
  // 位置：src/components/chatInput/ChatInput.tsx:293、src/components/messages/ReplyArea.tsx:109
  search: '智能搜索',
  // 位置：src/components/chatInput/ChatInput.tsx:307
  uploadFile: '上传文件',
  // 位置：src/components/chatInput/ChatInput.tsx:399
  chatInputStopButtonTooltip: '停止生成',
  // 位置：src/services/attachments.ts:55
  uploadMaxMBSize: '单个文件大小超过限制（{size}MB），请调整后重试',
  // 位置：src/services/attachments.ts:58
  uploadMaxFiles: '上传文件最多支持 {count} 个',
  // 位置：src/components/files/FileCard.tsx:40
  uploadFileStatusUploading: '上传中...',
  // 位置：src/services/attachments.ts:69
  uploadServerBusy: '服务器繁忙',
  // 位置：src/services/attachments.ts:72
  uploadParseFailed: '解析失败',
  // 位置：src/services/attachments.ts:96
  uploadFileCountLimit: '已上传文件数达到上限（{limit} 个），可 ',
  // 位置：src/services/attachments.ts:99
  uploadStorageLimit: '已上传文件占用空间达到上限（{limit}GB），可 ',
  // 位置：src/services/attachments.ts:94
  uploadDeleteFiles: '删除文件',
  // 位置：src/components/chatInput/DragMask.tsx:68
  dragFileToUpload: '文件拖动到此处即可上传',
  // 位置：src/components/chatInput/DragMask.tsx:70
  dragFileDesc: '最多 {count} 个，每个 {size}MB',
  // 位置：src/components/chatInput/ChatInput.tsx:351、src/components/chatInput/VoiceButton.tsx:36
  voiceInput: '语音输入',
  // 位置：src/components/chatInput/VoiceButton.tsx:40
  voiceInputTimer: '录音中 {seconds}s',
  // 位置：src/components/chatInput/VoiceButton.tsx:33
  voiceInputStop: '结束录音并转写',
  // 位置：src/components/chatInput/VoiceButton.tsx:35
  voiceInputCancel: '正在转写，点击取消',
  // 位置：src/components/chatInput/useVoiceInput.ts:19
  voiceInputPermissionDenied: '无法使用麦克风，请在浏览器设置中允许访问',
  // 位置：src/components/chatInput/useVoiceInput.ts:22
  voiceInputNoDevice: '未检测到麦克风',
  // 位置：当前未使用
  voiceInputTooLong: '录音过长，请缩短后重试',
  // 位置：src/components/chatInput/useVoiceInput.ts:25
  voiceInputFailed: '语音识别失败，请稍后再试',
  // 位置：src/components/chatInput/useVoiceInput.ts:85
  voiceInputEmpty: '未识别到语音内容',
  // 位置：src/components/chatInput/useVoiceInput.ts:116
  voiceInputLimitReached: '已达到单次录音时长上限',
  // 位置：src/components/chatInput/VoiceButton.tsx:31
  voiceInputRequiresHttps: '非 HTTPS 页面无法使用语音输入',
  // 位置：src/services/speech/readAloud.ts:66
  apiKeyInvalidRetry: 'API Key 无效，请更换后重试',
  // 位置：src/services/api/errors.ts:9、src/services/chat/describe.ts:37
  insufficientBalance: '账户余额不足',
  // 位置：src/services/api/errors.ts:11、src/services/chat/describe.ts:39
  rateLimited: '操作过于频繁，请稍后再试',
  // 位置：src/components/messages/ReadAloudButton.tsx:19
  ttsPlay: '朗读',
  // 位置：src/components/messages/ReadAloudButton.tsx:19
  ttsStop: '停止朗读',
  // 位置：src/services/speech/readAloud.ts:62
  ttsUnavailable: '这条回答暂时无法朗读',
  // 位置：src/services/speech/readAloud.ts:72
  ttsFailed: '朗读失败，请稍后再试',
  // 位置：src/app/App.tsx:214、src/components/onboarding/Onboarding.tsx:126
  retry: '重试',
  // 位置：src/services/chat/latestReminder.ts:28
  latestReminderLanguage: '中文',
  // 位置：src/components/settings/SettingsDialog.tsx:330
  settingsLocaleZhCN: '简体中文',
  // 位置：src/components/settings/SettingsDialog.tsx:331
  settingsLocaleEnUS: 'English',
  // 位置：src/components/settings/NativeSettings.tsx:167、src/components/settings/SettingsDialog.tsx:73
  accountTab: '账户',
  // 位置：src/components/settings/AccountPane.tsx:99
  accountLoginHint: '登录 DeepSeek 账号，自动导入会话并创建 API Key。',
  // 位置：当前未使用
  accountLoginButton: '登录 DeepSeek 账号并导入',
  // 位置：src/components/session/Welcome.tsx:42
  accountLoginShort: '登录 DeepSeek 账号',
  // 位置：src/components/settings/AccountPane.tsx:190
  accountRecharge: '充值',
  // 位置：src/components/settings/AccountPane.tsx:217
  accountScanAssist: '开启扫码辅助',
  // 位置：src/components/settings/AccountPane.tsx:227
  accountRechargeHint: '充值页以电脑版打开，出微信二维码后会自动识别并拉起微信',
  // 位置：当前未使用
  accountTermsTitle: '使用条款',
  // 位置：当前未使用
  accountTermsBody:
    '继续登录即表示你已阅读并同意 DeepSeek 的《使用条款》和《隐私政策》。登录成功后，App 会导入该账号的会话，并用账号授权直接聊天（不需要 API Key）。',
  // 位置：当前未使用
  accountTermsAgree: '同意并继续',
  // 位置：src/components/onboarding/Onboarding.tsx:78
  onboardingTitle: '欢迎使用 DeepSeek Chat Demo',
  // 位置：src/components/onboarding/Onboarding.tsx:80
  onboardingPitch:
    '把 DeepSeek 网页版聊天搬到安卓：登录一次，导入全部会话；用 DeepSeek 账号授权直接聊天，不需要 API Key。',
  // 位置：src/components/onboarding/Onboarding.tsx:82
  onboardingStep1: '登录 chat.deepseek.com，导入该账号的全部会话',
  // 位置：src/components/onboarding/Onboarding.tsx:83
  onboardingStep2: '授权 DeepSeek 账号，App 直接用 API 额度聊天',
  // 位置：src/components/onboarding/Onboarding.tsx:84
  onboardingStep3: '不消耗网页版次数；余额不足时可在 App 内充值',
  // 位置：src/components/onboarding/Onboarding.tsx:88
  onboardingStart: '开始使用',
  // 位置：src/components/onboarding/Onboarding.tsx:91
  onboardingNote: '非官方应用，与 DeepSeek 无隶属关系。',
  // 位置：src/components/settings/SettingsDialog.tsx:128
  aboutTitle: '关于',
  // 位置：src/components/settings/NativeSettings.tsx:314、src/components/settings/SettingsDialog.tsx:130
  aboutVersion: '版本',
  // 位置：src/components/settings/NativeSettings.tsx:323、src/components/settings/SettingsDialog.tsx:139
  aboutSource: '开源地址',
  // 位置：src/components/settings/SettingsDialog.tsx:143
  aboutLicense: '许可',
  // 位置：src/components/settings/NativeSettings.tsx:325、src/components/settings/SettingsDialog.tsx:146
  aboutDisclaimer:
    '非官方应用，基于 DeepSeek 官方 chat-demo（MIT）二次开发，与 DeepSeek 无隶属关系。',
  // 位置：src/components/onboarding/Onboarding.tsx:98
  onboardingImportTitle: '第 1 步：导入聊天记录',
  // 位置：src/components/onboarding/Onboarding.tsx:100
  onboardingImportPitch:
    '登录 chat.deepseek.com 后，App 会拉取该账号的导出包并导入；也可以先跳过，之后在设置 → 账户里导入。',
  // 位置：src/components/onboarding/Onboarding.tsx:135
  onboardingLoginImport: '登录并导入',
  // 位置：src/components/onboarding/Onboarding.tsx:129
  onboardingSkip: '跳过',
  // 位置：src/components/onboarding/Onboarding.tsx:150
  onboardingAuthTitle: '第 2 步：授权 DeepSeek 账号',
  // 位置：src/components/onboarding/Onboarding.tsx:152
  onboardingAuthPitch:
    '授权后 App 用你的 API 额度聊天，不消耗网页版次数，也不需要手动创建 API Key。',
  // 位置：src/components/onboarding/Onboarding.tsx:177
  onboardingAuthButton: '授权并继续',
  // 位置：src/components/onboarding/Onboarding.tsx:158
  onboardingNext: '下一步',
  // 位置：src/components/onboarding/Onboarding.tsx:197
  onboardingDoneTitle: '准备好了',
  // 位置：src/components/onboarding/Onboarding.tsx:202
  onboardingDonePitch: '可以开始聊天了。随时可以在设置 → 账户里重新导入或授权。',
  // 位置：src/components/onboarding/Onboarding.tsx:206
  onboardingEnter: '进入应用',
  // 位置：src/components/settings/AccountPane.tsx:96
  accountLoggedIn: '已登录，API Key 已保存在本机',
  // 位置：src/components/onboarding/Onboarding.tsx:153、src/components/settings/AccountPane.tsx:98
  accountLoggedInAccount: '已通过 DeepSeek 账号登录（免 API Key）',
  // 位置：当前未使用
  accountReimportButton: '重新导入',
  // 位置：src/components/onboarding/Onboarding.tsx:18、src/components/settings/AccountPane.tsx:31
  accountProgressChatLogin: '正在打开 DeepSeek 登录页...',
  // 位置：src/components/onboarding/Onboarding.tsx:19、src/components/settings/AccountPane.tsx:32
  accountProgressExporting: '正在导出会话，可能需要几分钟...',
  // 位置：src/components/onboarding/Onboarding.tsx:20、src/components/settings/AccountPane.tsx:33
  accountProgressImporting: '正在导入会话...',
  // 位置：src/components/onboarding/Onboarding.tsx:21、src/components/settings/AccountPane.tsx:34
  accountProgressPlatformLogin: '正在登录 DeepSeek 开放平台...',
  // 位置：src/components/onboarding/Onboarding.tsx:22、src/components/settings/AccountPane.tsx:35
  accountProgressCreatingKey: '正在创建 API Key...',
  // 位置：src/components/onboarding/Onboarding.tsx:201、src/components/settings/AccountPane.tsx:103
  accountImported: '已导入 {count} 个会话',
  // 位置：当前未使用
  accountDone: '完成',
  // 位置：src/components/onboarding/Onboarding.tsx:108、src/components/settings/AccountPane.tsx:316
  accountPreviewTitle: '发现 {count} 个会话',
  // 位置：src/components/settings/AccountPane.tsx:337
  accountPreviewUntitled: '无标题',
  // 位置：src/components/settings/AccountPane.tsx:341
  accountPreviewMessageCount: '{count} 条消息',
  // 位置：src/components/onboarding/Onboarding.tsx:110、src/components/settings/AccountPane.tsx:347
  accountPreviewWarning: '导入会替换本机全部会话',
  // 位置：src/components/onboarding/Onboarding.tsx:120、src/components/settings/AccountPane.tsx:326
  accountPreviewConfirm: '导入',
} as const

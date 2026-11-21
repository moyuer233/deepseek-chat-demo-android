import type { zh } from './zh.js'

export const en = {
  // 位置：src/components/chatInput/ChatInput.tsx:230
  voiceReleaseToSend: 'Release to send',
  // 位置：src/components/chatInput/ChatInput.tsx:230
  voiceReleaseCancel: 'Release to cancel',
  // 位置：src/components/chatInput/ChatInput.tsx:231
  voiceSlideUpCancel: 'Slide up to cancel',
  // 位置：src/services/chat/store.ts:1211
  contextOverflowCompacted: 'Context compacted, please retry',
  // 位置：src/services/chat/store.ts:1212
  contextOverflowCompactFailed: 'Context overflowed and compaction failed, please retry',
  // 位置：当前未使用
  usageStats: 'Usage & context',
  // 位置：src/components/settings/NativeSettings.tsx:204
  usageRequests: 'Requests',
  // 位置：src/components/settings/NativeSettings.tsx:205
  usageInput: 'Input tokens',
  // 位置：src/components/settings/NativeSettings.tsx:206
  usageCacheRead: 'Cache read tokens',
  // 位置：src/components/settings/NativeSettings.tsx:207
  usageOutput: 'Output tokens',
  // 位置：当前未使用
  usageCost: 'Cost',
  // 位置：src/components/settings/NativeSettings.tsx:212
  usageContext: 'Context usage',
  // 位置：src/components/settings/NativeSettings.tsx:200
  usageSession: 'This session',
  // 位置：src/components/settings/NativeSettings.tsx:214
  usageTotal: 'All sessions',
  // 位置：src/components/settings/settingsDialogState.ts:24
  loginRequiredHint: 'Sign in with a DeepSeek account or set an API key first',
  // 位置：src/components/settings/AccountPane.tsx:181
  apiKeyLabel: 'API Key',
  // 位置：src/components/settings/AccountPane.tsx:182
  apiKeySetTail: 'Set (ends {tail})',
  // 位置：src/components/settings/AccountPane.tsx:182
  apiKeyNotSet: 'Not set',
  // 位置：src/components/settings/AccountPane.tsx:266
  apiKeyHint: 'Used when no account grant is available (stored locally, never uploaded). Create one at platform.deepseek.com.',
  // 位置：src/components/settings/AccountPane.tsx:260
  apiKeySave: 'Save',
  // 位置：src/components/settings/AccountPane.tsx:247
  apiKeyClear: 'Clear',
  // 位置：src/components/settings/AccountPane.tsx:256
  apiKeySaved: 'API key saved',
  // 位置：src/components/settings/AccountPane.tsx:244
  apiKeyCleared: 'API key cleared',
  // 位置：src/components/settings/AccountPane.tsx:165
  accountSignIn: 'Sign in to DeepSeek',
  // 位置：src/components/settings/AccountPane.tsx:135
  accountBalance: 'Balance',
  // 位置：src/components/settings/AccountPane.tsx:139
  accountBalanceRetry: 'Failed to load balance, retry',
  // 位置：src/components/settings/AccountPane.tsx:199
  accountMoreInfo: 'More account info',
  // 位置：src/components/settings/AccountPane.tsx:205
  accountLogout: 'Sign out',
  // 位置：src/components/settings/AccountPane.tsx:297
  accountLogoutConfirm: 'Sign out',
  // 位置：src/components/settings/AccountPane.tsx:303
  accountLogoutBody: 'Local chats are kept and will still be here after signing in again.',
  // 位置：src/components/settings/AccountPane.tsx:303
  accountLogoutActive: 'A reply is being generated; signing out interrupts it. Local chats are kept.',
  // 位置：src/components/settings/AccountPane.tsx:117
  accountAuthAccount: 'DeepSeek account authorized',
  // 位置：src/components/settings/AccountPane.tsx:119
  accountAuthApiKey: 'Using API key',
  // 位置：src/components/settings/AccountPane.tsx:124
  accountAuthAccountLabel: 'Account grant (DSH)',
  // 位置：src/components/settings/AccountPane.tsx:126
  accountAuthApiKeyLabel: 'API key (stored locally)',
  // 位置：src/components/settings/AccountPane.tsx:120
  accountNotLoggedIn: 'Not signed in',
  // 位置：src/components/settings/NativeSettings.tsx:255
  importStats: '{added} new · {updated} updated · {localOnly} local only',
  // 位置：src/components/settings/NativeSettings.tsx:65
  importUpToDate: 'Already up to date, nothing to import',
  // 位置：src/components/settings/NativeSettings.tsx:246
  importModeTitle: 'Choose import mode',
  // 位置：src/components/settings/NativeSettings.tsx:256
  importModeHint: 'The same archive may have newer content or be a duplicate. Choose how to apply it:',
  // 位置：src/components/settings/NativeSettings.tsx:258
  importModeMerge: 'Merge (recommended)',
  // 位置：src/components/settings/NativeSettings.tsx:259
  importModeMergeDesc: 'Merge by session: add missing ones, merge messages and keep local changes; nothing local is deleted.',
  // 位置：src/components/settings/NativeSettings.tsx:262
  importModeAdd: 'Add new only',
  // 位置：src/components/settings/NativeSettings.tsx:263
  importModeAddDesc: 'Import only sessions that do not exist locally; existing ones stay untouched.',
  // 位置：src/components/settings/NativeSettings.tsx:266
  importModeOverwrite: 'Replace all',
  // 位置：src/components/settings/NativeSettings.tsx:267
  importModeOverwriteDesc: 'Replace every local session with the archive (chats created after the last import are lost).',
  // 位置：src/components/settings/NativeSettings.tsx:184
  chatSettings: 'Chat',
  // 位置：src/components/settings/NativeSettings.tsx:189
  modelSetting: 'Model',
  // 位置：src/components/chatInput/StatusLine.tsx:28
  statusContext: 'Context',
  // 位置：当前未使用
  statusCache: 'Cache',
  // 位置：当前未使用
  statusFirstToken: 'First token',
  // 位置：src/components/chatInput/StatusLine.tsx:37
  statusGenerating: 'Generating…',
  // 位置：src/components/session/MobileHeader.tsx:44
  compactNow: 'Compact context',
  // 位置：src/components/session/MobileHeader.tsx:19
  compactConfirm: 'Compact this conversation into a summary? Older messages will be replaced by the summary.',
  // 位置：src/components/session/MobileHeader.tsx:23
  compactDone: 'Context compacted',
  // 位置：src/components/session/MobileHeader.tsx:23
  compactFailed: 'Compaction failed, please retry',
  // 位置：src/components/settings/NativeSettings.tsx:195
  contextCompression: 'Context compression',
  // 位置：src/components/settings/NativeSettings.tsx:196、src/services/api/types.ts:179
  enabled: 'On',
  // 位置：src/components/settings/NativeSettings.tsx:196、src/services/api/types.ts:179
  disabled: 'Off',
  // 位置：src/components/sider/Sider.tsx:174
  searchSessionsPlaceholder: 'Search chats',
  // 位置：src/components/sider/Sider.tsx:216
  searchSessionsNarrow: 'Too many results, try a more specific query',
  // 位置：src/components/sider/Sider.tsx:214
  searchSessionsSearching: 'Searching…',
  // 位置：src/components/sider/Sider.tsx:212
  searchSessionsEmpty: 'No matching chats',
  // 位置：src/components/settings/NativeSettings.tsx:315
  checkUpdate: 'Check for updates',
  // 位置：src/components/settings/NativeSettings.tsx:136
  updateLatest: 'You are up to date',
  // 位置：src/components/settings/NativeSettings.tsx:131
  updateAvailable: 'New version available',
  // 位置：src/components/chatInput/ChatInput.tsx:234
  voiceHoldToSpeak: 'Hold to talk',
  // 位置：src/components/chatInput/ChatInput.tsx:423
  attachCamera: 'Camera',
  // 位置：src/components/chatInput/ChatInput.tsx:424
  attachGallery: 'Photos',
  // 位置：src/components/chatInput/ChatInput.tsx:425
  attachFile: 'File',
  // 位置：src/components/settings/NativeSettings.tsx:294
  nativeSettingsAccount: 'Account',
  // 位置：src/components/settings/NativeSettings.tsx:300
  nativeSettingsApp: 'App',
  // 位置：src/components/settings/NativeSettings.tsx:307
  nativeSettingsVoice: 'Voice',
  // 位置：src/components/settings/NativeSettings.tsx:312
  nativeSettingsAbout: 'About',
  // 位置：src/components/settings/NativeSettings.tsx:234
  nativeSettingsImportChats: 'Import chats from chat.deepseek.com',
  // 位置：src/components/settings/NativeSettings.tsx:158
  back: 'Back',
  // 位置：src/components/session/BalanceBadge.tsx:72
  balanceLabel: 'Balance',
  // 位置：src/components/session/BalanceBadge.tsx:70
  balanceTooltip: 'DeepSeek account balance',
  // 位置：src/components/session/BalanceBadge.tsx:75、src/components/settings/AccountPane.tsx:146
  balanceBonus: 'bonus',
  // 位置：src/components/session/BalanceBadge.tsx:59
  balanceFailed: 'Failed to load balance',
  // 位置：src/components/session/Welcome.tsx:25
  welcomeMessageChat250808: 'How can I help you?',
  // 位置：src/components/session/SessionView.tsx:40、src/components/session/Welcome.tsx:45
  contentCaveat: 'AI-generated, for reference only',
  // 位置：src/components/session/MobileHeader.tsx:32、src/components/sider/CollapsedSider.tsx:18
  expandSiderbarButton: 'Open sidebar',
  // 位置：当前未使用
  collapseSiderbarButton: 'Close sidebar',
  // 位置：src/components/session/MobileHeader.tsx:58、src/components/sider/CollapsedSider.tsx:25
  newChat: 'New chat',
  // 位置：src/components/session/SessionView.tsx:22、src/components/sider/SessionItem.tsx:23
  untitledChat: 'New chat',
  // 位置：src/app/App.tsx:227
  sessionLoading: 'Loading chats...',
  // 位置：src/app/App.tsx:226
  sessionListLoadFailed: 'Unable to load the chat list. Please try again.',
  // 位置：src/app/App.tsx:226
  sessionLoadFailed: 'Unable to load this chat. Please try again.',
  // 位置：src/app/App.tsx:207
  sessionSaveFailed:
    'Chats or attachments have not been saved locally. Retry before closing this page.',
  // 位置：src/components/sider/Sider.tsx:279
  sessionDeleteFailed: 'Unable to delete this chat. Please try again.',
  // 位置：src/components/settings/NativeSettings.tsx:292、src/components/settings/SettingsDialog.tsx:95
  settings: 'Settings',
  // 位置：当前未使用
  appName: 'API usage demo',
  // 位置：src/components/sider/Sider.tsx:259
  deleteSessionModalContentTitle: 'Delete chat?',
  // 位置：src/components/sider/Sider.tsx:291
  deleteSessionWithFilesDescription:
    'This chat and its uploaded files will be permanently deleted and cannot be recovered. Delete this chat?',
  // 位置：src/components/messages/UserMessage.tsx:108、src/components/onboarding/Onboarding.tsx:117
  cancel: 'Cancel',
  // 位置：src/components/chatInput/DraftAttachments.tsx:86、src/components/files/FileCard.tsx:90
  delete: 'Delete',
  // 位置：src/components/sider/SessionItem.tsx:64
  sessionItemMenuRenameOption: 'Rename',
  // 位置：src/components/sider/SessionItem.tsx:78
  more: 'More',
  // 位置：src/components/base/Modal.tsx:71、src/components/search/SearchPanel.tsx:24
  close: 'Close',
  // 位置：src/services/chat/sessionGroups.ts:16
  sessionHistoryTimelineLabelToday: 'Today',
  // 位置：src/services/chat/sessionGroups.ts:18
  sessionHistoryTimelineLabelYesterday: 'Yesterday',
  // 位置：src/services/chat/sessionGroups.ts:20
  sessionHistoryTimelineLabel7days: '7 Days',
  // 位置：src/services/chat/sessionGroups.ts:22
  sessionHistoryTimelineLabel30days: '30 Days',
  // 位置：src/components/markdown/CodeBlock.tsx:66、src/components/messages/CopyButton.tsx:18
  copy: 'Copy',
  // 位置：src/components/markdown/CodeBlock.tsx:66
  copied: 'Copied',
  // 位置：src/components/markdown/CodeBlock.tsx:58、src/components/messages/CopyButton.tsx:25
  copyFailed: 'Copy failed. Please long-press or select the text to copy.',
  // 位置：src/components/chatInput/ChatInput.tsx:411、src/components/messages/UserMessage.tsx:111
  send: 'Send',
  // 位置：src/components/messages/UserMessage.tsx:88
  editMessage: 'Edit message',
  // 位置：src/components/messages/AssistantMessage.tsx:158
  regenerate: 'Regenerate',
  // 位置：src/components/messages/AssistantMessage.tsx:178
  continueGenerating: 'Continue',
  // 位置：src/components/messages/AssistantMessage.tsx:109
  changeApiKey: 'Change API Key',
  // 位置：src/components/messages/AssistantMessage.tsx:113、src/services/chat/describe.ts:10
  topUp: 'Top up',
  // 位置：src/components/messages/AssistantMessage.tsx:142
  messageSearchSummary: '1 web page',
  // 位置：src/components/messages/AssistantMessage.tsx:142
  messageSearchSummaries: '{count} web pages',
  // 位置：src/components/messages/BranchSwitcher.tsx:35
  messageVersions: 'Message versions',
  // 位置：src/components/messages/BranchSwitcher.tsx:41
  previousVersion: 'Previous version',
  // 位置：src/components/messages/BranchSwitcher.tsx:50
  nextVersion: 'Next version',
  // 位置：src/components/search/SearchPanel.tsx:20
  searchResult: 'Search results',
  // 位置：src/components/markdown/CodeBlock.tsx:39
  mermaidDiagram: 'Diagram',
  // 位置：src/components/markdown/CodeBlock.tsx:47
  mermaidCode: 'Code',
  // 位置：src/components/markdown/MermaidDiagram.tsx:83
  mermaidEmpty: 'This diagram is empty.',
  // 位置：src/components/markdown/MermaidDiagram.tsx:86
  mermaidLoading: 'Rendering diagram...',
  // 位置：src/components/markdown/MermaidDiagram.tsx:85
  mermaidRenderFail: 'Mermaid rendering failed.',
  // 位置：src/components/markdown/CodeBlock.tsx:32
  mermaidSwitchLabel: 'Toggle between Mermaid diagram and code',
  // 位置：src/components/messages/ReplyArea.tsx:212
  toolSearchIntro: 'Searching for',
  // 位置：src/components/messages/ReplyArea.tsx:218
  messageSearching: 'Searching...',
  // 位置：src/components/messages/ReplyArea.tsx:218、src/services/chat/view.ts:399
  imageSearching: 'Searching by image...',
  // 位置：src/components/messages/ReplyArea.tsx:221
  searchStopped: 'Search stopped',
  // 位置：src/components/messages/ReplyArea.tsx:223
  toolSearchFail: 'Search currently unavailable',
  // 位置：src/components/messages/ReplyArea.tsx:226
  toolSearchNoResult: 'No suitable results found',
  // 位置：src/components/messages/ReplyArea.tsx:226
  messageSearchNoResult: 'No relevant results found',
  // 位置：src/components/messages/ReplyArea.tsx:232
  toolSearch1Result: 'Found 1 web page',
  // 位置：src/components/messages/ReplyArea.tsx:233
  toolSearchResults: 'Found {count} web pages',
  // 位置：src/components/messages/ReplyArea.tsx:235
  messageSearchResult: 'Read 1 web page',
  // 位置：src/components/messages/ReplyArea.tsx:236
  messageSearchResults: 'Read {count} web pages',
  // 位置：src/components/messages/ReplyArea.tsx:329
  toolOpenOneIntro: 'Read 1 page',
  // 位置：src/components/messages/ReplyArea.tsx:329
  toolOpensIntro: 'Read {count} pages',
  // 位置：src/components/messages/ReplyArea.tsx:337
  toolOpenFail: 'Read failed',
  // 位置：src/components/messages/ReplyArea.tsx:342
  toolOpenFailTooltip: 'Read failed',
  // 位置：src/components/messages/ReplyArea.tsx:354
  toolOpenViewAll: 'View All',
  // 位置：src/components/messages/ReplyArea.tsx:373
  toolFindFail: 'Find failed',
  // 位置：src/components/messages/ReplyArea.tsx:376
  toolFindIntro: 'Find keyword',
  // 位置：src/components/messages/ReplyArea.tsx:380
  toolFindFailTooltip: 'No related content found',
  // 位置：src/components/messages/ReplyArea.tsx:403、src/services/chat/view.ts:408
  readingLink: 'Reading',
  // 位置：src/components/settings/useApiKeyForm.ts:20、src/services/chat/describe.ts:32
  commonNetworkErrorToast: 'Network Error. Please check your network status.',
  // 位置：src/services/chat/describe.ts:36
  replyInvalidApiKey:
    'The API Key is invalid or has been deleted. Please replace it and try again.',
  // 位置：src/services/chat/describe.ts:38
  replyInsufficientBalance: 'Insufficient account balance. Please top up and try again.',
  // 位置：src/services/chat/describe.ts:40
  replyRateLimited: 'Too many requests. Please try again later.',
  // 位置：src/services/chat/describe.ts:42
  completionServerBusyToast: 'Server busy, please try again later.',
  // 位置：src/components/onboarding/Onboarding.tsx:103、src/components/settings/AccountPane.tsx:105
  unknownError: 'Unknown error',
  // 位置：src/services/chat/describe.ts:54
  replyRefused: "Sorry, that's beyond my current scope. Let's talk about something else.",
  // 位置：src/services/chat/view.ts:392
  messageThinkDuration: 'Thought for {count} second',
  // 位置：src/services/chat/view.ts:392
  messageThinkDurationPlural: 'Thought for {count} seconds',
  // 位置：src/services/chat/view.ts:395
  messageThinkStopped: 'Stopped',
  // 位置：src/services/chat/view.ts:399
  deepSearchSearching: 'Searching the web',
  // 位置：src/services/chat/view.ts:405
  groupTitleToolOpen: 'Browsing the web',
  // 位置：src/services/chat/view.ts:410
  messageThinking: 'Thinking',
  // 位置：src/services/chat/history/importArchive.ts:49
  backupInvalidJson: 'The file is not valid JSON.',
  // 位置：src/services/chat/history/importArchive.ts:59
  backupUnrecognized: 'File not recognized. Please choose a DeepSeek chat history ZIP.',
  // 位置：src/services/chat/history/schema.ts:250
  backupNewerVersion: 'This file uses a newer version. Please update the app before importing.',
  // 位置：src/services/chat/history/schema.ts:253
  backupInvalidVersion: 'The file version is invalid and cannot be imported.',
  // 位置：src/services/chat/history/importArchive.ts:73、src/services/chat/history/schema.ts:19
  backupIncomplete: 'The file is incomplete and cannot be imported.',
  // 位置：src/services/chat/history/toArchive.ts:221
  backupInvalidBranches: 'The message branches in this file are corrupted and cannot be imported.',
  // 位置：src/services/chat/history/toLocal.ts:316
  backupInvalidFiles: 'The attachment data in this file is corrupted and cannot be imported.',
  // 位置：src/components/settings/NativeSettings.tsx:303、src/components/settings/SettingsDialog.tsx:323
  settingsLocalePreference: 'Language',
  // 位置：src/components/settings/NativeSettings.tsx:309、src/components/settings/SettingsDialog.tsx:346
  settingsTtsVoice: 'Read-aloud voice',
  // 位置：src/components/settings/SettingsDialog.tsx:353
  settingsTtsVoiceStella: 'Stella (female)',
  // 位置：src/components/settings/SettingsDialog.tsx:354
  settingsTtsVoiceTide: 'Tide (male)',
  // 位置：src/components/settings/SettingsDialog.tsx:265
  settingsLocation: 'Location',
  // 位置：src/components/settings/SettingsDialog.tsx:273
  settingsLocationProvince: 'Select province/region',
  // 位置：src/components/settings/SettingsDialog.tsx:285
  settingsLocationCity: 'Select city',
  // 位置：src/components/settings/SettingsDialog.tsx:302
  settingsLocationRegion: 'Select country or region',
  // 位置：src/components/settings/SettingsDialog.tsx:69
  settingDialogAsideGeneralOptionLabel: 'General',
  // 位置：src/components/settings/NativeSettings.tsx:228、src/components/settings/SettingsDialog.tsx:72
  settingDialogAsideDataOptionLabel: 'Data',
  // 位置：src/components/settings/NativeSettings.tsx:304、src/components/settings/SettingsDialog.tsx:416
  avatarMenuSettingsDialogThemeItemLabel: 'Theme',
  // 位置：src/components/settings/NativeSettings.tsx:53、src/components/settings/SettingsDialog.tsx:394
  avatarMenuSettingsDialogThemeSelectLightOption: 'Light',
  // 位置：src/components/settings/NativeSettings.tsx:54、src/components/settings/SettingsDialog.tsx:399
  avatarMenuSettingsDialogThemeSelectDarkOption: 'Dark',
  // 位置：src/components/settings/NativeSettings.tsx:51、src/components/settings/SettingsDialog.tsx:404
  avatarMenuSettingsDialogThemeSelectSystemOption: 'System',
  // 位置：src/components/settings/ApiKeyField.tsx:29
  apiKeyStart: 'Enter an API Key to get started',
  // 位置：src/components/settings/ApiKeyField.tsx:36
  apiKeyEdit: 'Edit API Key',
  // 位置：src/components/settings/ApiKeyField.tsx:44
  apiKeyPlaceholder: 'Enter API Key',
  // 位置：src/components/settings/ApiKeyField.tsx:74
  apiKeyVerifying: 'Verifying...',
  // 位置：src/components/settings/ApiKeyField.tsx:74
  confirm: 'Confirm',
  // 位置：src/components/settings/useApiKeyForm.ts:18
  apiKeyInvalid: 'Invalid API Key. Please check it and try again.',
  // 位置：src/components/settings/useApiKeyForm.ts:21
  apiKeyVerificationFailed: 'Verification failed: {message}',
  // 位置：src/components/settings/SettingsDialog.tsx:528
  historyImport: 'Import chat history',
  // 位置：src/components/settings/SettingsDialog.tsx:381
  historyImportConfirm: 'Import',
  // 位置：src/components/settings/SettingsDialog.tsx:372、src/services/deepseek/account.ts:72
  historyImportConfirmTitle: 'Import chat history?',
  // 位置：src/components/settings/SettingsDialog.tsx:386、src/services/deepseek/account.ts:72
  historyImportConfirmDescription:
    'The uploaded chat history will replace all your current chats. Import anyway?',
  // 位置：src/components/settings/SettingsDialog.tsx:529
  historyImportDescription:
    'Importing chat history replaces all local chats and attachments. Supports ZIP files exported from DeepSeek.',
  // 位置：src/components/settings/NativeSettings.tsx:83、src/components/settings/SettingsDialog.tsx:502
  historyImportedOne: 'Imported 1 chat',
  // 位置：src/components/settings/NativeSettings.tsx:83、src/components/settings/SettingsDialog.tsx:502
  historyImported: 'Imported {count} chats',
  // 位置：src/components/settings/NativeSettings.tsx:70、src/components/settings/SettingsDialog.tsx:505
  historyImportFailed:
    'File import failed. The file format may be invalid, or the ZIP file may use an older chat export format. Please export your chat history from DeepSeek again and retry the import.',
  // 位置：src/components/messages/MessageFile.tsx:161、src/components/settings/NativeSettings.tsx:102
  historyExportFailed: 'Export failed: {message}',
  // 位置：src/components/settings/SettingsDialog.tsx:584
  exportAllLabel: 'Export all chats',
  // 位置：src/components/settings/SettingsDialog.tsx:585
  historyExportDescription:
    'Includes all chat history and locally stored images. Other attachments retain file details only. Your API Key is not included.',
  // 位置：src/components/settings/NativeSettings.tsx:241、src/components/settings/SettingsDialog.tsx:596
  exportAllDownload: 'Download',
  // 位置：src/components/settings/NativeSettings.tsx:242、src/components/settings/SettingsDialog.tsx:157
  deleteAllSessions: 'Delete all chats',
  // 位置：src/components/settings/SettingsDialog.tsx:190
  deleteAllSessionsModalTitle: 'Once deleted, all chats will be cleared and cannot be recovered.',
  // 位置：src/components/settings/NativeSettings.tsx:145、src/components/settings/SettingsDialog.tsx:213
  deleteAllSessionsModalDescription:
    'Locally saved attachments and unsent drafts will also be deleted.',
  // 位置：src/components/settings/SettingsDialog.tsx:208
  historyDeleting: 'Deleting...',
  // 位置：src/components/settings/NativeSettings.tsx:150、src/components/settings/SettingsDialog.tsx:232
  historyDeleted: 'Successfully deleted.',
  // 位置：src/components/settings/NativeSettings.tsx:152、src/components/settings/SettingsDialog.tsx:234
  historyDeleteFailed: 'Failed to delete chats. Please try again.',
  // 位置：src/components/messages/MessageList.tsx:25、src/components/settings/NativeSettings.tsx:231
  upload: 'Upload',
  // 位置：src/services/attachments.ts:51
  uploadUnsupportedFormat:
    'The selected files include unsupported formats. Please convert them and try again. Supported: PDF, DOC, XLSX, PPT, images, text, code, and more.',
  // 位置：src/components/chatInput/DraftAttachments.tsx:59、src/components/files/FileCard.tsx:46
  uploadFileStatusFailed: 'Upload failed',
  // 位置：src/components/chatInput/DraftAttachments.tsx:59、src/components/files/FileCard.tsx:61
  uploadRetry: '{message}. Click to retry',
  // 位置：src/components/chatInput/ChatInput.tsx:241
  chatInputPlaceholderChat: 'Enter a message',
  // 位置：src/components/chatInput/ChatInput.tsx:277
  deactivatedThinkTooltip: 'Think before responding to solve reasoning problems',
  // 位置：src/components/chatInput/ChatInput.tsx:281、src/services/chat/view.ts:390
  thinkingSwitch: 'DeepThink',
  // 位置：src/components/chatInput/ChatInput.tsx:289
  deactivatedSearchTooltip: 'Search the web when necessary',
  // 位置：src/components/chatInput/ChatInput.tsx:293、src/components/messages/ReplyArea.tsx:109
  search: 'Search',
  // 位置：src/components/chatInput/ChatInput.tsx:307
  uploadFile: 'Upload files',
  // 位置：src/components/chatInput/ChatInput.tsx:399
  chatInputStopButtonTooltip: 'Stop',
  // 位置：src/services/attachments.ts:55
  uploadMaxMBSize: 'File exceeds the size limit ({size} MB). Please adjust and try again.',
  // 位置：src/services/attachments.ts:58
  uploadMaxFiles: 'You can upload up to {count} files',
  // 位置：src/components/files/FileCard.tsx:40
  uploadFileStatusUploading: 'Uploading...',
  // 位置：src/services/attachments.ts:69
  uploadServerBusy: 'Server busy',
  // 位置：src/services/attachments.ts:72
  uploadParseFailed: 'Parsing failed',
  // 位置：src/services/attachments.ts:96
  uploadFileCountLimit: 'Uploaded file limit reached ({limit} files). ',
  // 位置：src/services/attachments.ts:99
  uploadStorageLimit: 'Uploaded file storage limit reached ({limit} GB). ',
  // 位置：src/services/attachments.ts:94
  uploadDeleteFiles: 'Delete files',
  // 位置：src/components/chatInput/DragMask.tsx:68
  dragFileToUpload: 'Drop files here to upload',
  // 位置：src/components/chatInput/DragMask.tsx:70
  dragFileDesc: 'Up to {count} files, {size} MB each',
  // 位置：src/components/chatInput/ChatInput.tsx:351、src/components/chatInput/VoiceButton.tsx:36
  voiceInput: 'Voice input',
  // 位置：src/components/chatInput/VoiceButton.tsx:40
  voiceInputTimer: 'Recording {seconds}s',
  // 位置：src/components/chatInput/VoiceButton.tsx:33
  voiceInputStop: 'Stop recording and transcribe',
  // 位置：src/components/chatInput/VoiceButton.tsx:35
  voiceInputCancel: 'Transcribing. Click to cancel',
  // 位置：src/components/chatInput/useVoiceInput.ts:19
  voiceInputPermissionDenied:
    'Microphone access is blocked. Please allow it in your browser settings.',
  // 位置：src/components/chatInput/useVoiceInput.ts:22
  voiceInputNoDevice: 'No microphone detected',
  // 位置：当前未使用
  voiceInputTooLong: 'Recording is too long. Please shorten it and try again.',
  // 位置：src/components/chatInput/useVoiceInput.ts:25
  voiceInputFailed: 'Speech recognition failed. Please try again later.',
  // 位置：src/components/chatInput/useVoiceInput.ts:85
  voiceInputEmpty: 'No speech detected',
  // 位置：src/components/chatInput/useVoiceInput.ts:116
  voiceInputLimitReached: 'Maximum recording length reached',
  // 位置：src/components/chatInput/VoiceButton.tsx:31
  voiceInputRequiresHttps: 'Voice input requires HTTPS',
  // 位置：src/services/speech/readAloud.ts:66
  apiKeyInvalidRetry: 'Invalid API Key. Please change it and try again.',
  // 位置：src/services/api/errors.ts:9、src/services/chat/describe.ts:37
  insufficientBalance: 'Insufficient balance',
  // 位置：src/services/api/errors.ts:11、src/services/chat/describe.ts:39
  rateLimited: 'Too many requests. Please try again later.',
  // 位置：src/components/messages/ReadAloudButton.tsx:19
  ttsPlay: 'Read aloud',
  // 位置：src/components/messages/ReadAloudButton.tsx:19
  ttsStop: 'Stop reading',
  // 位置：src/services/speech/readAloud.ts:62
  ttsUnavailable: 'This answer cannot be read aloud right now',
  // 位置：src/services/speech/readAloud.ts:72
  ttsFailed: 'Failed to read aloud. Please try again later.',
  // 位置：src/app/App.tsx:214、src/components/onboarding/Onboarding.tsx:126
  retry: 'Retry',
  // 位置：src/services/chat/latestReminder.ts:28
  latestReminderLanguage: 'English',
  // 位置：src/components/settings/SettingsDialog.tsx:330
  settingsLocaleZhCN: 'Simplified Chinese',
  // 位置：src/components/settings/SettingsDialog.tsx:331
  settingsLocaleEnUS: 'English',
  // 位置：src/components/settings/NativeSettings.tsx:167、src/components/settings/SettingsDialog.tsx:73
  accountTab: 'Account',
  // 位置：src/components/settings/AccountPane.tsx:99
  accountLoginHint: 'Sign in to import your chats and create an API Key.',
  // 位置：当前未使用
  accountLoginButton: 'Sign in and import',
  // 位置：src/components/session/Welcome.tsx:42
  accountLoginShort: 'Sign in to DeepSeek',
  // 位置：src/components/settings/AccountPane.tsx:190
  accountRecharge: 'Top up',
  // 位置：src/components/settings/AccountPane.tsx:217
  accountScanAssist: 'Scan assist',
  // 位置：src/components/settings/AccountPane.tsx:227
  accountRechargeHint:
    'Opens the desktop top-up page, detects the WeChat QR code and opens WeChat',
  // 位置：当前未使用
  accountTermsTitle: 'Terms of Use',
  // 位置：当前未使用
  accountTermsBody:
    'By continuing you confirm that you have read and agree to the DeepSeek Terms of Use and Privacy Policy. After signing in, the app imports your chats and chats through your DeepSeek account (no API key needed).',
  // 位置：当前未使用
  accountTermsAgree: 'Agree and continue',
  // 位置：src/components/onboarding/Onboarding.tsx:78
  onboardingTitle: 'Welcome to DeepSeek Chat Demo',
  // 位置：src/components/onboarding/Onboarding.tsx:80
  onboardingPitch:
    'DeepSeek web chat, on Android: sign in once, import every conversation, then chat through your DeepSeek account without an API key.',
  // 位置：src/components/onboarding/Onboarding.tsx:82
  onboardingStep1: 'Sign in to chat.deepseek.com and import all conversations',
  // 位置：src/components/onboarding/Onboarding.tsx:83
  onboardingStep2: 'Authorize your DeepSeek account and chat on your API quota',
  // 位置：src/components/onboarding/Onboarding.tsx:84
  onboardingStep3: 'Not subject to web limits; top up from inside the app when needed',
  // 位置：src/components/onboarding/Onboarding.tsx:88
  onboardingStart: 'Get started',
  // 位置：src/components/onboarding/Onboarding.tsx:91
  onboardingNote: 'Unofficial app. Not affiliated with DeepSeek.',
  // 位置：src/components/settings/SettingsDialog.tsx:128
  aboutTitle: 'About',
  // 位置：src/components/settings/NativeSettings.tsx:314、src/components/settings/SettingsDialog.tsx:130
  aboutVersion: 'Version',
  // 位置：src/components/settings/NativeSettings.tsx:323、src/components/settings/SettingsDialog.tsx:139
  aboutSource: 'Source code',
  // 位置：src/components/settings/SettingsDialog.tsx:143
  aboutLicense: 'License',
  // 位置：src/components/settings/NativeSettings.tsx:325、src/components/settings/SettingsDialog.tsx:146
  aboutDisclaimer:
    'Unofficial app built on DeepSeek chat-demo (MIT). Not affiliated with DeepSeek.',
  // 位置：src/components/onboarding/Onboarding.tsx:98
  onboardingImportTitle: 'Step 1: import your chats',
  // 位置：src/components/onboarding/Onboarding.tsx:100
  onboardingImportPitch:
    'After you sign in to chat.deepseek.com the app pulls that account export and imports it. You can skip and import later from Settings → Account.',
  // 位置：src/components/onboarding/Onboarding.tsx:135
  onboardingLoginImport: 'Sign in and import',
  // 位置：src/components/onboarding/Onboarding.tsx:129
  onboardingSkip: 'Skip',
  // 位置：src/components/onboarding/Onboarding.tsx:150
  onboardingAuthTitle: 'Step 2: authorize your DeepSeek account',
  // 位置：src/components/onboarding/Onboarding.tsx:152
  onboardingAuthPitch:
    'Chat through your API quota instead of the web limits, without creating an API key by hand.',
  // 位置：src/components/onboarding/Onboarding.tsx:177
  onboardingAuthButton: 'Authorize and continue',
  // 位置：src/components/onboarding/Onboarding.tsx:158
  onboardingNext: 'Next',
  // 位置：src/components/onboarding/Onboarding.tsx:197
  onboardingDoneTitle: 'Ready',
  // 位置：src/components/onboarding/Onboarding.tsx:202
  onboardingDonePitch: 'You can start chatting now. Import or authorize again anytime in Settings → Account.',
  // 位置：src/components/onboarding/Onboarding.tsx:206
  onboardingEnter: 'Enter the app',
  // 位置：src/components/settings/AccountPane.tsx:96
  accountLoggedIn: 'Signed in. API Key saved on this device.',
  // 位置：src/components/onboarding/Onboarding.tsx:153、src/components/settings/AccountPane.tsx:98
  accountLoggedInAccount: 'Signed in with your DeepSeek account (no API key needed)',
  // 位置：当前未使用
  accountReimportButton: 'Import again',
  // 位置：src/components/onboarding/Onboarding.tsx:18、src/components/settings/AccountPane.tsx:31
  accountProgressChatLogin: 'Opening DeepSeek sign-in...',
  // 位置：src/components/onboarding/Onboarding.tsx:19、src/components/settings/AccountPane.tsx:32
  accountProgressExporting: 'Exporting chats, this may take a few minutes...',
  // 位置：src/components/onboarding/Onboarding.tsx:20、src/components/settings/AccountPane.tsx:33
  accountProgressImporting: 'Importing chats...',
  // 位置：src/components/onboarding/Onboarding.tsx:21、src/components/settings/AccountPane.tsx:34
  accountProgressPlatformLogin: 'Signing in to DeepSeek Platform...',
  // 位置：src/components/onboarding/Onboarding.tsx:22、src/components/settings/AccountPane.tsx:35
  accountProgressCreatingKey: 'Creating your API Key...',
  // 位置：src/components/onboarding/Onboarding.tsx:201、src/components/settings/AccountPane.tsx:103
  accountImported: 'Imported {count} chats',
  // 位置：当前未使用
  accountDone: 'Done',
  // 位置：src/components/onboarding/Onboarding.tsx:108、src/components/settings/AccountPane.tsx:316
  accountPreviewTitle: 'Found {count} chats',
  // 位置：src/components/settings/AccountPane.tsx:337
  accountPreviewUntitled: 'Untitled',
  // 位置：src/components/settings/AccountPane.tsx:341
  accountPreviewMessageCount: '{count} messages',
  // 位置：src/components/onboarding/Onboarding.tsx:110、src/components/settings/AccountPane.tsx:347
  accountPreviewWarning: 'Importing replaces all chats on this device',
  // 位置：src/components/onboarding/Onboarding.tsx:120、src/components/settings/AccountPane.tsx:326
  accountPreviewConfirm: 'Import',
} satisfies Record<keyof typeof zh, string>

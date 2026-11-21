package com.moyuer233.deepseeknative.ui

/**
 * 原生界面（Compose / Activity）全部用户可见文案，集中在这里方便直接改。
 * 网页聊天界面的文案在 src/i18n/zh.ts 与 en.ts（有同样注释）。
 *
 * 改完运行：pnpm build → 复制 dist 到 native-app/app/src/main/assets/web → ./gradlew.bat assembleRelease
 */
object NativeCopy {
    // ===== 首启引导页（4 页标题 / 说明）· 使用于 ui/OnboardingScreen.kt =====
    /** 第 1 页标题（欢迎页） */
    const val onboardingTitle1 = "欢迎使用 DeepSeek Chat Demo"
    /** 第 1 页说明 */
    const val onboardingDesc1 = "和官方 App 一样的外观，聊天与数据都由你自己掌控。"
    /** 第 2 页标题 */
    const val onboardingTitle2 = "导入历史会话"
    /** 第 2 页说明 */
    const val onboardingDesc2 = "从 chat.deepseek.com 登录并导出全部会话；进入 chat-demo 后自动导入。"
    /** 第 3 页标题 */
    const val onboardingTitle3 = "连接聊天内核"
    /** 第 3 页说明 */
    const val onboardingDesc3 = "用 DeepSeek 账号授权或 API Key 即可聊天，无需验证码。"
    /** 第 4 页标题 */
    const val onboardingTitle4 = "准备就绪"
    /** 第 4 页说明 */
    const val onboardingDesc4 = "随时可以在设置里更换主题、字体和授权方式。"

    // ===== 引导页按钮与状态 · 使用于 ui/OnboardingScreen.kt =====
    /** 下一步（前三页主按钮） */
    const val next = "下一步"
    /** 开始使用（最后一页主按钮） */
    const val start = "开始使用"
    /** 跳过（每页底部） */
    const val skip = "跳过"
    /** 第 2 页：从 chat.deepseek.com 导入会话按钮 */
    const val importChats = "从 chat.deepseek.com 导入会话"
    /** 第 2 页：导出中状态 */
    const val importingFromChat = "正在从 chat.deepseek.com 导出…"
    /** 第 2 页：导出前取消 */
    const val importCancelled = "已取消导入"
    /** 第 2 页：导出完成提示（页面状态） */
    const val importReady = "已获取归档，进入 chat-demo 后自动导入"
    /** 第 2 页：导出完成 toast */
    const val importDoneToast = "导出完成，完成后会自动导入"
    /** 第 2 页：导出失败前缀（后面拼错误信息） */
    const val importFailedPrefix = "导出失败："
    /** 第 3 页：授权按钮 */
    const val authorize = "使用 DeepSeek 账号授权"
    /** 第 3 页：已授权状态 */
    const val authorized = "已授权"
    /** 第 3 页：授权成功 toast */
    const val authSuccessToast = "授权成功"
    /** 第 3 页：授权失败前缀（后面拼错误信息） */
    const val authFailedPrefix = "授权失败："
    /** 第 3 页：授权初始化失败前缀（后面拼错误信息） */
    const val authInitFailedPrefix = "授权初始化失败："
    /** 取消授权（用户返回） */
    const val authCancel = "已取消"
    /** 登录页：标题 */
    const val chatLoginTitle = "登录 DeepSeek（完成登录后自动返回）"

    // ===== 主界面 WebView 壳（返回/相机/导入导出）· 使用于 MainActivity.kt =====
    /** 授权流程被用户取消 */
    const val authCancelled = "已取消授权"
    /** 导入时未登录 / 登录页返回 */
    const val importLoginCancelled = "已取消登录"
    /** 导出失败兜底 */
    const val exportFailed = "导出失败"
    /** 通用「确定」 */
    const val confirm = "确定"
    /** 通用「取消」 */
    const val cancel = "取消"
    /** 无法打开相机时 toast */
    const val cameraFallback = "无法打开相机，改用文件选择"
    /** 授权初始化失败（回传网页） */
    const val authInitFailed = "授权初始化失败"
    /** 导出保存到指定路径成功（后面拼绝对路径） */
    const val exportedToPrefix = "已导出到 "
    /** 导出到下载目录成功 */
    const val exportedToDownloads = "已导出到 下载/DeepSeekNative"
    /** 导出失败前缀（后面拼错误信息） */
    const val exportFailedPrefix = "导出失败："
    /** 错误信息为空时的兜底 */
    const val unknownError = "未知错误"
    /** 无法创建下载文件 */
    const val cannotCreateDownload = "无法创建下载文件"
    /** 无法写入下载文件 */
    const val cannotWriteDownload = "无法写入下载文件"

    // ===== OAuth 授权页 · 使用于 ui/OAuthActivity.kt；chatLoginTitle 使用于 ui/ChatLoginActivity.kt =====
    /** 用户关闭授权页 */
    const val oauthCancelled = "授权被取消"
    /** 授权失败兜底（后面拼错误信息） */
    const val oauthFailed = "授权失败"
}

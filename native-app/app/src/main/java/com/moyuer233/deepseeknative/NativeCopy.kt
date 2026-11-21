package com.moyuer233.deepseeknative

import java.util.Locale

/** 自动生成：改 copy/文案.mjs 后运行 node scripts/gen-copy.mjs；不要直接改本文件。 */
object NativeCopy {
    /** 默认跟随系统语言；Web 侧可通过 setLanguage 覆盖为应用内语言。 */
    private var en = Locale.getDefault().language == "en"

    fun setLanguage(locale: String) {
        en = locale.startsWith("en")
    }

    // 界面：引导页 · 第 1 页标题
    val onboardingTitle1 = if (en) "Welcome to Chat Demo Android" else "欢迎使用Chat Demo Android"

    // 界面：引导页 · 第 1 页说明
    val onboardingDesc1 = if (en) "Go beyond the free DeepSeek context window and usage limits" else "突破 DeepSeek 免费版上下文长度/使用额度限制"

    // 界面：引导页 · 第 2 页标题
    val onboardingTitle2 = if (en) "Import chat history" else "导入历史会话"

    // 界面：引导页 · 第 2 页说明
    val onboardingDesc2 = if (en) "Sign in to chat.deepseek.com, export all chats and import them automatically." else "从 chat.deepseek.com 登录并导出全部会话并自动导入。"

    // 界面：引导页 · 第 3 页标题
    val onboardingTitle3 = if (en) "Set up API Key" else "配置API Key"

    // 界面：引导页 · 第 3 页说明
    val onboardingDesc3 = if (en) "Authorize with a DeepSeek Platform account to chat, no new API Key needed" else "用 DeepSeek开放平台 账号授权即可聊天，无须新建API Key"

    // 界面：引导页 · 第 4 页标题
    val onboardingTitle4 = if (en) "You're all set" else "准备就绪"

    // 界面：引导页 · 第 4 页说明
    val onboardingDesc4 = if (en) "You can re-import chats and switch accounts in Settings anytime" else "随时可以在设置里重新导入以及切换账号"

    // 界面：引导页 · 主按钮（前三页）
    val next = if (en) "Next" else "下一步"

    // 界面：引导页 · 主按钮（最后一页）
    val start = if (en) "Get started" else "开始使用"

    // 界面：引导页 · 底部跳过
    val skip = if (en) "Skip" else "跳过"

    // 界面：引导页 · 第 2 页「导入会话」按钮
    val importChats = if (en) "Import chats from chat.deepseek.com" else "从 chat.deepseek.com 导入会话"

    // 界面：引导页 · 第 2 页 导出中按钮文案
    val importingFromChat = if (en) "Exporting from chat.deepseek.com…" else "正在从 chat.deepseek.com 导出…"

    // 界面：引导页 · 第 2 页 取消提示
    val importCancelled = if (en) "Import cancelled" else "已取消导入"

    // 界面：引导页 · 第 2 页 状态提示
    val importReady = if (en) "Archive ready. It will be imported automatically after entering Chat Demo." else "已获取数据，进入 Chat Demo 后自动导入"

    // 界面：引导页 · 第 2 页 导出完成 Toast
    val importDoneToast = if (en) "Export complete. It will be imported automatically." else "导出完成，完成后会自动导入"

    // 界面：引导页 · 第 2 页 导出失败提示前缀
    val importFailedPrefix = if (en) "Export failed: " else "导出失败："

    // 界面：引导页 · 第 3 页 授权按钮
    val authorize = if (en) "Authorize with DeepSeek account" else "使用 DeepSeek 账号授权"

    // 界面：引导页 · 第 3 页 已授权状态
    val authorized = if (en) "Authorized" else "已授权"

    // 界面：引导页 · 第 3 页 授权成功 Toast
    val authSuccessToast = if (en) "Authorization successful" else "授权成功"

    // 界面：引导页 · 第 3 页 授权失败 Toast 前缀
    val authFailedPrefix = if (en) "Authorization failed: " else "授权失败："

    // 界面：引导页 · 第 3 页 授权初始化失败 Toast 前缀
    val authInitFailedPrefix = if (en) "Failed to start authorization: " else "授权初始化失败："

    // 界面：引导页 · 第 3 页 取消兜底
    val authCancel = if (en) "Cancelled" else "已取消"

    // 界面：登录页 · 顶部标题
    val chatLoginTitle = if (en) "Sign in to DeepSeek (returns automatically when done)" else "登录 DeepSeek（完成登录后自动返回）"

    // 界面：主界面 · 授权取消提示
    val authCancelled = if (en) "Authorization cancelled" else "已取消授权"

    // 界面：主界面 · 导入取消提示
    val importLoginCancelled = if (en) "Sign-in cancelled" else "已取消登录"

    // 界面：主界面 · 导出失败兜底
    val exportFailed = if (en) "Export failed" else "导出失败"

    // 界面：通用确认框 · 确认按钮
    val confirm = if (en) "OK" else "确定"

    // 界面：通用确认框 · 取消按钮
    val cancel = if (en) "Cancel" else "取消"

    // 界面：主界面 · 相机不可用 Toast
    val cameraFallback = if (en) "Can't open the camera, using the file picker instead" else "无法打开相机，改用文件选择"

    // 界面：主界面 · 授权初始化失败
    val authInitFailed = if (en) "Authorization init failed" else "授权初始化失败"

    // 界面：主界面 · 导出成功提示前缀
    val exportedToPrefix = if (en) "Exported to " else "已导出到 "

    // 界面：主界面 · 导出到下载目录提示
    val exportedToDownloads = if (en) "Exported to Downloads/DeepSeekNative" else "已导出到 下载/DeepSeekNative"

    // 界面：主界面 · 导出失败提示前缀
    val exportFailedPrefix = if (en) "Export failed: " else "导出失败："

    // 界面：主界面 · 未知错误兜底
    val unknownError = if (en) "Unknown error" else "未知错误"

    // 界面：主界面 · 无法创建下载文件
    val cannotCreateDownload = if (en) "Can't create the download file" else "无法创建下载文件"

    // 界面：主界面 · 无法写入下载文件
    val cannotWriteDownload = if (en) "Can't write the download file" else "无法写入下载文件"

    // 界面：授权页 · 用户取消
    val oauthCancelled = if (en) "Authorization cancelled" else "授权被取消"

    // 界面：授权页 · 失败兜底
    val oauthFailed = if (en) "Authorization failed" else "授权失败"

    // 界面：导出 · 账号没有可导出的会话
    val chatExportEmpty = if (en) "This account has no chats to export" else "账号内没有可导出的会话"

    // 界面：导出 · 导出任务失败
    val chatExportFailed = if (en) "Export failed. Please try again later." else "导出失败，请稍后重试"

    // 界面：导出 · 等待导出超时
    val chatExportTimeout = if (en) "Export timed out. Please try again later." else "导出超时，请稍后重试"

    // 界面：导出 · 下载失败（后接状态码）
    val downloadFailedHttp = if (en) "Download failed. HTTP " else "下载失败 HTTP "

    // 界面：充值 · 打开微信提示
    val topupSavedWeChat = if (en) "QR saved to Photos, WeChat opened: + → Scan → Album" else "已保存二维码到相册并打开微信：+ → 扫一扫 → 相册"

    // 界面：充值 · 打开支付宝提示
    val topupSavedAlipay = if (en) "QR saved to Photos, Alipay opened: Scan → Album" else "已保存二维码到相册并打开支付宝：扫一扫 → 相册"

    // 界面：充值 · 二维码保存失败
    val topupQrSaveFailed = if (en) "Couldn't save the QR image; payment content copied instead" else "二维码保存失败，已复制支付内容"

    // 界面：充值 · 未登录提示
    val topupSignInHint = if (en) "Sign in to DeepSeek Platform first; the payment QR will be detected automatically." else "请先用 DeepSeek 开放平台账号登录，登录后会自动识别支付二维码"

    // 界面：充值 · 复制支付内容提示
    val topupCopied = if (en) "Payment content copied" else "已复制支付内容"

    // 界面：充值 · 复制按钮
    val topupCopy = if (en) "Copy" else "复制"

    // 界面：充值 · 检测到微信支付链接
    val topupWechatLinkTitle = if (en) "WeChat Pay link detected" else "检测到微信支付链接"

    // 界面：充值 · 打开微信按钮
    val topupOpenWeChat = if (en) "Open WeChat" else "打开微信"

    // 界面：充值 · 相册写入失败
    val topupSaveFailed = if (en) "Couldn't write to Photos" else "无法写入相册"

    // 界面：应用内网页 · 加载失败
    val pageLoadFailed = if (en) "Failed to load the page. Check your network and try again." else "页面加载失败，请检查网络后重试"

    // 界面：充值 · 没有安装微信
    val topupNoWeChat = if (en) "WeChat not found" else "没有找到微信"

    // 界面：充值 · 没有安装支付宝
    val topupNoAlipay = if (en) "Alipay not found" else "没有找到支付宝"

}

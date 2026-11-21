package com.moyuer233.deepseeknative.ui

import com.moyuer233.deepseeknative.NativeCopy

import com.moyuer233.deepseeknative.BuildConfig
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import android.app.AlertDialog
import java.io.File
import java.io.FileOutputStream

/** 充值：桌面 UA 打开 top_up，注入 jsQR 解码二维码，保存图片并拉起微信。 */
class TopUpActivity : ComponentActivity() {
    private var webView: WebView? = null
    private var signInHinted = false
    /** 当前页面是否 DeepSeek 域；在 onPageFinished 里更新，避免桥线程直接读 WebView */
    @Volatile
    private var trustedPage = false
    /** 防抖：同一张码 30 秒内只处理一次，避免后台重复存图/重复拉起 */
    @Volatile
    private var lastHandledQr = ""
    @Volatile
    private var lastHandledAt = 0L
    private val jsQrSource by lazy { readAsset("jsqr.js") }
    private val scanSource by lazy { readAsset("topup-scan.js") }
    private val hookSource by lazy { readAsset("topup-hook.js") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = WebView(this)
        webView = view
        view.settings.javaScriptEnabled = true
        view.settings.domStorageEnabled = true
        view.settings.userAgentString =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
        view.addJavascriptInterface(Bridge(), "DSTopUp")
        view.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: android.webkit.WebResourceRequest): Boolean =
                handleNavigation(request.url.toString())

            @Suppress("DEPRECATION")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean = handleNavigation(url)

            override fun onPageFinished(v: WebView, url: String?) {
                super.onPageFinished(v, url)
                // 只在 DeepSeek 自己的页面上注入扫码脚本；外域一律不在本 WebView 里停留
                val page = url ?: return
                trustedPage = isDeepSeekUrl(page)
                if (!trustedPage) {
                    return
                }
                if (page.contains("sign_in") && !signInHinted) {
                    signInHinted = true
                    Toast.makeText(
                        this@TopUpActivity,
                        NativeCopy.topupSignInHint,
                        Toast.LENGTH_LONG,
                    ).show()
                }
                if (jsQrSource.isNotEmpty()) v.evaluateJavascript(jsQrSource, null)
                if (hookSource.isNotEmpty()) v.evaluateJavascript(hookSource, null)
                if (scanSource.isNotEmpty()) v.evaluateJavascript(scanSource, null)
            }
        }
        setContentView(view)
        view.loadUrl("https://platform.deepseek.com/top_up")
    }

    private fun readAsset(name: String): String = try {
        assets.open(name).use { it.readBytes().toString(Charsets.UTF_8) }
    } catch (e: Exception) {
        ""
    }

    /** 只认 deepseek.com 及其子域；页面内导航不能离开这些域。 */
    private fun isDeepSeekUrl(url: String?): Boolean {
        val host = runCatching { Uri.parse(url ?: "") }.getOrNull()?.host ?: return false
        return host == "deepseek.com" || host.endsWith(".deepseek.com")
    }

    /** 允许的域在 WebView 内导航（返回 false），其余 http(s) 交给系统浏览器并拦截。 */
    private fun handleNavigation(url: String): Boolean {
        if (isDeepSeekUrl(url)) {
            return false
        }
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return true
        if (uri.scheme == "https" || uri.scheme == "http") {
            runCatching {
                startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        }
        return true
    }

    /** 按用户选中的支付方式打开对应 App：微信 / 支付宝。 */
    private fun openPayApp(channel: String) {
        val pkg = if (channel == "alipay") "com.eg.android.AlipayGphone" else "com.tencent.mm"
        val launch = packageManager.getLaunchIntentForPackage(pkg)
        if (launch != null) {
            // 让无障碍服务在接下来 20 秒内自动进「扫一扫 → 相册」
            com.moyuer233.deepseeknative.ScanAssistService.requestScan()
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(launch)
            val tip = if (channel == "alipay") NativeCopy.topupSavedAlipay else NativeCopy.topupSavedWeChat
            Toast.makeText(this, tip, Toast.LENGTH_LONG).show()
            return
        }
        // 没有 launch intent 时用 scheme 兜底（支付宝：alipays://；微信：weixin://）
        val scheme = if (channel == "alipay") "alipays://" else "weixin://"
        try {
            val fallback = Intent(Intent.ACTION_VIEW, Uri.parse(scheme))
            fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(fallback)
            return
        } catch (e: Exception) {
            // 走下面的提示
        }
        val missing = if (channel == "alipay") NativeCopy.topupNoAlipay else NativeCopy.topupNoWeChat
        Toast.makeText(this, missing, Toast.LENGTH_LONG).show()
    }

    private fun saveQrImage(dataUrl: String): String? {
        return try {
            val base64 = dataUrl.substring(dataUrl.indexOf(',') + 1)
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val name = "deepseek_qr_" + System.currentTimeMillis() + ".png"
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, name)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/DeepSeekNative")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null
                try {
                    contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                        ?: throw IllegalStateException(NativeCopy.topupSaveFailed)
                } catch (e: Exception) {
                    contentResolver.delete(uri, null, null)
                    return null
                }
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                contentResolver.update(uri, values, null, null)
                "Pictures/DeepSeekNative/$name"
            } else {
                val dir = File(getExternalFilesDir(null), "qr").apply { mkdirs() }
                val file = File(dir, "deepseek_qr_" + System.currentTimeMillis() + ".png")
                FileOutputStream(file).use { it.write(bytes) }
                file.absolutePath
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun copyText(text: String) {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("pay", text))
        Toast.makeText(this, NativeCopy.topupCopied, Toast.LENGTH_SHORT).show()
    }

    inner class Bridge {
        @JavascriptInterface
        private fun trusted(): Boolean = trustedPage

        @JavascriptInterface
        fun onQrImage(dataUrl: String, text: String, method: String) {
            if (!trusted()) return
            val now = System.currentTimeMillis()
            if (text == lastHandledQr && now - lastHandledAt < 30_000L) {
                if (BuildConfig.DEBUG) android.util.Log.d("DSTopUp", "skip duplicate qr")
                return
            }
            runOnUiThread {
                // 优先按二维码内容判断，其次按页面上选中的支付方式
                val channel = when {
                    text.startsWith("weixin://", true) -> "wechat"
                    text.startsWith("alipays://", true) || text.contains("qr.alipay.com", true) -> "alipay"
                    method == "alipay" -> "alipay"
                    else -> "wechat"
                }
                copyText(text)
                // 解码 + 写相册放后台线程，避免主线程卡顿
                Thread {
                    val saved = saveQrImage(dataUrl)
                    runOnUiThread {
                        if (saved != null) {
                            lastHandledQr = text
                            lastHandledAt = System.currentTimeMillis()
                            if (BuildConfig.DEBUG) android.util.Log.d("DSTopUp", "qr saved channel=" + channel)
                            openPayApp(channel)
                        } else {
                            // 失败不记去重，允许用户原地重试
                            lastHandledQr = ""
                            Toast.makeText(this@TopUpActivity, NativeCopy.topupQrSaveFailed, Toast.LENGTH_LONG).show()
                        }
                    }
                }.start()
            }
        }

        @JavascriptInterface
        fun onQr(text: String) {
            if (!trusted()) return
            runOnUiThread {
                if (text.startsWith("weixin://")) {
                    AlertDialog.Builder(this@TopUpActivity)
                        .setTitle(NativeCopy.topupWechatLinkTitle)
                        .setMessage(text.take(240))
                        .setPositiveButton(NativeCopy.topupOpenWeChat) { _, _ ->
                            runCatching {
                                startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(text))
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }.onFailure { copyText(text) }
                        }
                        .setNegativeButton(NativeCopy.topupCopy) { _, _ -> copyText(text) }
                        .setNeutralButton(NativeCopy.cancel, null as android.content.DialogInterface.OnClickListener?)
                        .show()
                }
            }
        }

        @JavascriptInterface
        fun diag(message: String) {
            if (!trusted() || !BuildConfig.DEBUG) return
            android.util.Log.d("DSTopUp", message)
        }
    }

    override fun onPause() {
        super.onPause()
        // 切到微信/支付宝后停止扫码，避免后台一直识别和存图
        webView?.evaluateJavascript("window.__dsTopupSetPaused && window.__dsTopupSetPaused(true)", null)
    }

    override fun onResume() {
        super.onResume()
        webView?.evaluateJavascript("window.__dsTopupSetPaused && window.__dsTopupSetPaused(false)", null)
    }

    override fun onDestroy() {
        webView?.destroy()
        webView = null
        super.onDestroy()
    }
}

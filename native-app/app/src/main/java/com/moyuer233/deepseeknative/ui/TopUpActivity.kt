package com.moyuer233.deepseeknative.ui

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
                if (!isDeepSeekUrl(page)) {
                    return
                }
                if (page.contains("sign_in") && !signInHinted) {
                    signInHinted = true
                    Toast.makeText(
                        this@TopUpActivity,
                        "请先用 DeepSeek 开放平台账号登录，登录后会自动识别支付二维码",
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

    private fun openWeChat() {
        val launch = packageManager.getLaunchIntentForPackage("com.tencent.mm")
        if (launch != null) {
            // 让无障碍服务在接下来 20 秒内自动进「扫一扫 → 相册」
            com.moyuer233.deepseeknative.ScanAssistService.requestScan()
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(launch)
            Toast.makeText(this, "已保存二维码到相册并打开微信：+ → 扫一扫 → 相册", Toast.LENGTH_LONG).show()
            return
        }
        try {
            val fallback = Intent(Intent.ACTION_VIEW, Uri.parse("weixin://"))
            fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(fallback)
        } catch (e: Exception) {
            Toast.makeText(this, "没有找到微信", Toast.LENGTH_LONG).show()
        }
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
                        ?: throw IllegalStateException("无法写入相册")
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
        Toast.makeText(this, "已复制支付内容", Toast.LENGTH_SHORT).show()
    }

    inner class Bridge {
        @JavascriptInterface
        private fun trusted(): Boolean {
            val host = runCatching { android.net.Uri.parse(webView?.url ?: "").host }.getOrNull() ?: return false
            return host == "deepseek.com" || host.endsWith(".deepseek.com")
        }

        @JavascriptInterface
        fun onQrImage(dataUrl: String, text: String) {
            if (!trusted()) return
            runOnUiThread {
                val saved = saveQrImage(dataUrl)
                copyText(text)
                if (saved != null) openWeChat() else Toast.makeText(
                    this@TopUpActivity,
                    "二维码保存失败，已复制支付内容",
                    Toast.LENGTH_LONG,
                ).show()
            }
        }

        @JavascriptInterface
        fun onQr(text: String) {
            if (!trusted()) return
            runOnUiThread {
                if (text.startsWith("weixin://")) {
                    AlertDialog.Builder(this@TopUpActivity)
                        .setTitle("检测到微信支付链接")
                        .setMessage(text.take(240))
                        .setPositiveButton("打开微信") { _, _ ->
                            runCatching {
                                startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(text))
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }.onFailure { copyText(text) }
                        }
                        .setNegativeButton("复制") { _, _ -> copyText(text) }
                        .setNeutralButton("取消", null)
                        .show()
                }
            }
        }

        @JavascriptInterface
        fun diag(message: String) {
            if (!trusted()) return
            android.util.Log.d("DSTopUp", message)
        }
    }

    override fun onDestroy() {
        webView?.destroy()
        webView = null
        super.onDestroy()
    }
}

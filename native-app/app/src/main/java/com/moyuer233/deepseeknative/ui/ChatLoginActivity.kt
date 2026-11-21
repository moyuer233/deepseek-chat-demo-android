package com.moyuer233.deepseeknative.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity

/** chat.deepseek.com 登录页：用户自己登录，本页面轮询 localStorage 拿 userToken 返回。 */
class ChatLoginActivity : ComponentActivity() {
    private var webView: WebView? = null
    private val handler = Handler(Looper.getMainLooper())
    private var finished = false

    private val poll = object : Runnable {
        override fun run() {
            if (finished) return
            webView?.evaluateJavascript(READ_TOKEN_JS) { raw ->
                if (!finished) {
                    val token = unquote(raw)
                    if (token.isNotEmpty()) deliver(token)
                }
            }
            handler.postDelayed(this, 700)
        }
    }

    private fun unquote(raw: String?): String {
        val value = raw?.trim().orEmpty()
        if (value.isEmpty() || value == "null") return ""
        return if (value.length >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            value.substring(1, value.length - 1)
        } else {
            value
        }
    }

    private fun deliver(token: String) {
        if (finished) return
        finished = true
        handler.removeCallbacks(poll)
        setResult(Activity.RESULT_OK, Intent().putExtra(EXTRA_TOKEN, token))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.WHITE)
        }
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, bars.top, 0, bars.bottom)
            insets
        }
        val bar = TextView(this).apply {
            text = "登录 DeepSeek（完成登录后自动返回）"
            textSize = 15f
            setTextColor(android.graphics.Color.parseColor("#111111"))
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(48, 0, 48, 0)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 140)
            setOnClickListener { finish() }
        }
        root.addView(bar)
        val web = WebView(this)
        webView = web
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.databaseEnabled = true
        web.settings.userAgentString =
            "Mozilla/5.0 (Linux; Android 14; Pixel 6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true)
        web.webViewClient = WebViewClient()
        root.addView(web, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        setContentView(root)
        web.loadUrl("https://chat.deepseek.com/")
        handler.postDelayed(poll, 1000)
    }

    override fun onDestroy() {
        finished = true
        handler.removeCallbacks(poll)
        webView?.destroy()
        webView = null
        super.onDestroy()
    }

    companion object {
        const val EXTRA_TOKEN = "token"
        private const val READ_TOKEN_JS =
            "(function(){try{var raw=localStorage.getItem('userToken');if(!raw)return '';var o=JSON.parse(raw);if(!o)return '';return String(o.token||o.access_token||o.value||'');}catch(e){return '';}})()"
    }
}

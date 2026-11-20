package com.moyuer233.deepseeknative.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import org.json.JSONObject

/** 数美 SmCaptcha 人机验证容器：加载本地 HTML，通过 JS 桥返回 rid。 */
class CaptchaActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private var finished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = WebViewClient()
        webView.addJavascriptInterface(Bridge(), "AndroidCaptcha")
        webView.webChromeClient = object : android.webkit.WebChromeClient() {
            override fun onConsoleMessage(message: android.webkit.ConsoleMessage): Boolean {
                Log.d("ShumeiCaptcha", "console: " + message.message())
                return true
            }
        }
        setContentView(webView)
        webView.loadUrl("file:///android_asset/captcha.html")
    }

    inner class Bridge {
        @JavascriptInterface
        fun postMessage(message: String) {
            runOnUiThread {
                if (finished) return@runOnUiThread
                val json = runCatching { JSONObject(message) }.getOrNull() ?: return@runOnUiThread
                val error = json.optString("error", "")
                if (error.isNotEmpty()) {
                    setResult(RESULT_CANCELED, Intent().putExtra(EXTRA_ERROR, error))
                    finished = true
                    finish()
                    return@runOnUiThread
                }
                Log.d("ShumeiCaptcha", "bridge: " + message)
                val pass = json.optBoolean("pass", false)
                val rid = json.optString("rid", "")
                if (pass && rid.isNotEmpty()) {
                    setResult(RESULT_OK, Intent().putExtra(EXTRA_RID, rid))
                    finished = true
                    finish()
                }
            }
        }
    }

    companion object {
        const val EXTRA_RID = "rid"
        const val EXTRA_ERROR = "error"
    }
}

package com.moyuer233.deepseeknative.ui

import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity

/** 打开 chat.deepseek.com 网页登录，抓 localStorage.userToken 作本机聊天凭证。 */
class WebLoginActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private var finished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.databaseEnabled = true
        webView.settings.userAgentString =
            "Mozilla/5.0 (Linux; Android 13; Pixel 6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"
        webView.webViewClient = WebViewClient()
        webView.addJavascriptInterface(Bridge(), "AndroidLogin")
        setContentView(webView)
        webView.loadUrl("https://chat.deepseek.com")
        webView.postDelayed(poller, 1200)
    }

    private val poller = object : Runnable {
        override fun run() {
            if (finished) return
            webView.evaluateJavascript(
                "(function(){try{var raw=localStorage.getItem('userToken');if(!raw)return;var o=JSON.parse(raw);var v=(o&&o.value)||(typeof o==='string'?o:'');if(v)window.AndroidLogin.onToken(v);}catch(e){}})()",
                null
            )
            webView.postDelayed(this, 1200)
        }
    }

    inner class Bridge {
        @JavascriptInterface
        fun onToken(token: String) {
            runOnUiThread {
                if (finished) return@runOnUiThread
                finished = true
                setResult(RESULT_OK, Intent().putExtra(EXTRA_TOKEN, token))
                finish()
            }
        }
    }

    override fun onDestroy() {
        webView.removeCallbacks(poller)
        super.onDestroy()
    }

    companion object {
        const val EXTRA_TOKEN = "token"
    }
}

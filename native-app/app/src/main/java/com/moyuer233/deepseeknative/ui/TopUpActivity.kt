package com.moyuer233.deepseeknative.ui

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity

/** 充值页：桌面 UA 打开 platform.deepseek.com/top_up（PC 端会出微信二维码）。 */
class TopUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.userAgentString =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
        webView.webViewClient = WebViewClient()
        setContentView(webView)
        webView.loadUrl("https://platform.deepseek.com/top_up")
    }
}

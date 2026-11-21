package com.moyuer233.deepseeknative.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import com.moyuer233.deepseeknative.data.DshAuth

/** DSH 账号授权 WebView：拦截 127.0.0.1 回调，换 grant 后返回。 */
class OAuthActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private var exchanging = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val authorizeUrl = intent.getStringExtra(EXTRA_URL)
        if (authorizeUrl == null) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }
        webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.userAgentString =
            "Mozilla/5.0 (Linux; Android 16) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
                return handle(url)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: android.webkit.WebResourceRequest): Boolean {
                return handle(request.url.toString())
            }
        }
        setContentView(webView)
        webView.loadUrl(authorizeUrl)
    }

    private fun handle(url: String): Boolean {
        val redirect = android.net.Uri.parse(DshAuth.REDIRECT_URI)
        val actual = android.net.Uri.parse(url)
        if (actual.scheme != redirect.scheme || actual.host != redirect.host ||
            actual.port != redirect.port || actual.path != redirect.path
        ) {
            return false
        }
        if (exchanging) return true
        exchanging = true
        val uri = Uri.parse(url)
        val code = uri.getQueryParameter("code")
        val state = uri.getQueryParameter("state")
        if (code == null) {
            setResult(RESULT_CANCELED, Intent().putExtra(EXTRA_ERROR, NativeCopy.oauthCancelled))
            finish()
            return true
        }
        Thread {
            try {
                val token = DshAuth.exchange(this, code, state)
                runOnUiThread {
                    setResult(RESULT_OK, Intent().putExtra(EXTRA_TOKEN, token))
                    finish()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    setResult(RESULT_CANCELED, Intent().putExtra(EXTRA_ERROR, e.message ?: NativeCopy.oauthFailed))
                    finish()
                }
            }
        }.start()
        return true
    }

    companion object {
        const val EXTRA_URL = "url"
        const val EXTRA_TOKEN = "token"
        const val EXTRA_ERROR = "error"
    }
}

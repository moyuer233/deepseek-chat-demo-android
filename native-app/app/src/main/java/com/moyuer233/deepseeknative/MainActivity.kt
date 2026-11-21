package com.moyuer233.deepseeknative

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.core.content.FileProvider
import androidx.webkit.WebViewAssetLoader
import com.moyuer233.deepseeknative.data.ChatExport
import com.moyuer233.deepseeknative.data.DshAuth
import com.moyuer233.deepseeknative.data.SessionStore
import com.moyuer233.deepseeknative.ui.ChatLoginActivity
import com.moyuer233.deepseeknative.ui.OAuthActivity
import com.moyuer233.deepseeknative.ui.OnboardingScreen
import com.moyuer233.deepseeknative.ui.TopUpActivity
import com.moyuer233.deepseeknative.ui.theme.DeepSeekTheme
import org.json.JSONObject

/** 只保留 chat-demo WebView 壳与必要原生桥（登录授权/导入导出/充值/相机/麦克风）。 */
class MainActivity : ComponentActivity() {
    private var webViewRef: SwipeWebView? = null
    private var authLauncher: ActivityResultLauncher<Intent>? = null
    private var chatImportLauncher: ActivityResultLauncher<Intent>? = null
    private var fileChooserLauncher: ActivityResultLauncher<Array<String>>? = null
    private var fileChooserCallback: ValueCallback<Array<Uri>>? = null
    private var cameraLauncher: ActivityResultLauncher<Uri>? = null
    private var cameraCallback: ValueCallback<Array<Uri>>? = null
    private var cameraUri: Uri? = null
    private var permissionsLauncher: ActivityResultLauncher<Array<String>>? = null
    private var pendingPermissionRequest: PermissionRequest? = null
    private var siderBackCallback: Any? = null
    private var legacyBackCallback: OnBackPressedCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        registerLaunchers()
        setupBackHandling()

        if (!SessionStore.isOnboarded(this)) {
            setContent {
                DeepSeekTheme {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        OnboardingScreen(onDone = {
                            SessionStore.setOnboarded(this)
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        })
                    }
                }
            }
            return
        }
        showChatDemo()
    }

    private fun registerLaunchers() {
        fileChooserLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            fileChooserCallback?.onReceiveValue(if (uri != null) arrayOf(uri) else emptyArray())
            fileChooserCallback = null
        }
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
            val cb = cameraCallback
            val uri = cameraUri
            cameraCallback = null
            cameraUri = null
            cb?.onReceiveValue(if (ok && uri != null) arrayOf(uri) else emptyArray())
        }
        permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            val request = pendingPermissionRequest
            pendingPermissionRequest = null
            if (request == null) return@registerForActivityResult
            if (grants.values.all { it }) request.grant(request.resources) else request.deny()
        }
        authLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val ok = result.resultCode == Activity.RESULT_OK
            val token = if (ok) DshAuth.token(this) ?: "" else ""
            val script = if (ok && token.isNotEmpty()) {
                "window.__onNativeAuth && window.__onNativeAuth(" + JSONObject.quote(token) + ")"
            } else {
                "window.__onNativeAuthError && window.__onNativeAuthError(" + JSONObject.quote(
                    result.data?.getStringExtra(OAuthActivity.EXTRA_ERROR) ?: "已取消授权"
                ) + ")"
            }
            webViewRef?.evaluateJavascript(script, null)
        }
        chatImportLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val token = if (result.resultCode == Activity.RESULT_OK) {
                result.data?.getStringExtra(ChatLoginActivity.EXTRA_TOKEN)
            } else null
            if (token.isNullOrEmpty()) {
                webViewRef?.evaluateJavascript("window.__onChatArchiveError && window.__onChatArchiveError('已取消登录')", null)
                return@registerForActivityResult
            }
            Thread {
                try {
                    val r = ChatExport.exportAndDownload(token, cacheDir)
                    val js = "window.__onChatArchiveReady && window.__onChatArchiveReady(" +
                        JSONObject.quote(r.file.absolutePath) + "," + JSONObject.quote(r.fileName) + "," + r.bytes + ")"
                    webViewRef?.evaluateJavascript(js, null)
                } catch (e: Exception) {
                    webViewRef?.evaluateJavascript(
                        "window.__onChatArchiveError && window.__onChatArchiveError(" +
                            JSONObject.quote(e.message ?: "导出失败") + ")",
                        null,
                    )
                }
            }.start()
        }
    }

    private fun setupBackHandling() {
        val legacy = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val web = webViewRef
                if (web != null && web.siderOpen) {
                    web.evaluateJavascript("window.__siderClose && window.__siderClose()", null)
                    return
                }
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }
        legacyBackCallback = legacy
        onBackPressedDispatcher.addCallback(this, legacy)
    }

    private fun showChatDemo() {
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        val web = SwipeWebView(this)
        webViewRef = web
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.allowFileAccess = false
        web.settings.allowContentAccess = false
        web.settings.mediaPlaybackRequiresUserGesture = false
        web.addJavascriptInterface(Bridge(), "NativeShell")
        val loader = WebViewAssetLoader.Builder()
            .addPathHandler("/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()
        web.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: android.webkit.WebResourceRequest): android.webkit.WebResourceResponse? =
                loader.shouldInterceptRequest(request.url)

            override fun shouldOverrideUrlLoading(view: WebView, request: android.webkit.WebResourceRequest): Boolean {
                val url = request.url
                if (url.host == "appassets.androidplatform.net") return false
                if (url.scheme == "https" || url.scheme == "http") {
                    runCatching { startActivity(Intent(Intent.ACTION_VIEW, url)) }
                }
                return true
            }
        }
        web.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(msg: android.webkit.ConsoleMessage?): Boolean {
                android.util.Log.d("WebConsole", (msg?.message() ?: "") + " @" + (msg?.lineNumber() ?: 0))
                return true
            }

            override fun onPermissionRequest(request: PermissionRequest) {
                if (!request.resources.contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE)) {
                    request.deny()
                    return
                }
                if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    request.grant(request.resources)
                } else {
                    pendingPermissionRequest = request
                    permissionsLauncher?.launch(arrayOf(android.Manifest.permission.RECORD_AUDIO)) ?: request.deny()
                }
            }

            override fun onShowFileChooser(view: WebView, callback: ValueCallback<Array<Uri>>, params: FileChooserParams): Boolean {
                fileChooserCallback?.onReceiveValue(emptyArray())
                fileChooserCallback = callback
                val capture = runCatching { params.isCaptureEnabled }.getOrDefault(false)
                if (capture) {
                    try {
                        val dir = java.io.File(cacheDir, "camera").apply { mkdirs() }
                        val file = java.io.File(dir, "photo_" + System.currentTimeMillis() + ".jpg")
                        val uri = FileProvider.getUriForFile(this@MainActivity, packageName + ".fileprovider", file)
                        cameraUri = uri
                        cameraLauncher?.launch(uri)
                        return true
                    } catch (e: Exception) {
                        android.util.Log.w("Camera", "capture failed", e)
                    }
                }
                val types = params.acceptTypes.filter { it.isNotBlank() }.flatMap { accept ->
                    when {
                        accept.contains("*") || accept.contains("/") -> listOf(accept)
                        accept.startsWith(".") -> when (accept.lowercase()) {
                            ".zip" -> listOf("application/zip", "application/x-zip-compressed")
                            ".pdf" -> listOf("application/pdf")
                            ".png" -> listOf("image/png")
                            ".jpg", ".jpeg" -> listOf("image/jpeg")
                            else -> listOf("*/*")
                        }
                        else -> listOf("*/*")
                    }
                }.distinct().toTypedArray()
                fileChooserLauncher?.launch(if (types.isEmpty()) arrayOf("*/*") else types)
                return true
            }
        }
        val rootView = android.widget.FrameLayout(this)
        val res = resources
        val statusId = res.getIdentifier("status_bar_height", "dimen", "android")
        val navId = res.getIdentifier("navigation_bar_height", "dimen", "android")
        val top = if (statusId > 0) res.getDimensionPixelSize(statusId) else 0
        val bottom = if (navId > 0) res.getDimensionPixelSize(navId) else 0
        rootView.setPadding(0, top, 0, bottom)
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val bars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.ime())
            view.setPadding(0, bars.top, 0, maxOf(bars.bottom, ime.bottom))
            insets
        }
        rootView.addView(web, android.widget.FrameLayout.LayoutParams(-1, -1))
        setContentView(rootView)
        web.loadUrl("https://appassets.androidplatform.net/web/index.html")
    }

    private fun registerSiderBack() {
        if (android.os.Build.VERSION.SDK_INT < 34 || siderBackCallback != null) return
        val cb = object : android.window.OnBackAnimationCallback {
            override fun onBackStarted(backEvent: android.window.BackEvent) {
                webViewRef?.evaluateJavascript("window.__siderDrag && window.__siderDrag(1)", null)
            }

            override fun onBackProgressed(backEvent: android.window.BackEvent) {
                val fraction = (1f - backEvent.progress).coerceIn(0f, 1f)
                webViewRef?.evaluateJavascript("window.__siderDrag && window.__siderDrag($fraction)", null)
            }

            override fun onBackInvoked() {
                webViewRef?.evaluateJavascript("window.__siderClose && window.__siderClose()", null)
                unregisterSiderBack()
            }

            override fun onBackCancelled() {
                webViewRef?.evaluateJavascript("window.__siderSwipeEnd && window.__siderSwipeEnd(1, 0)", null)
                unregisterSiderBack()
            }
        }
        onBackInvokedDispatcher.registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT, cb)
        siderBackCallback = cb
    }

    private fun unregisterSiderBack() {
        val cb = siderBackCallback
        if (cb != null && android.os.Build.VERSION.SDK_INT >= 34) {
            onBackInvokedDispatcher.unregisterOnBackInvokedCallback(cb as android.window.OnBackAnimationCallback)
        }
        siderBackCallback = null
    }

    inner class Bridge {
        @JavascriptInterface
        fun getToken(): String = DshAuth.token(this@MainActivity) ?: ""

        @JavascriptInterface
        fun takePendingArchive(): String = com.moyuer233.deepseeknative.data.PendingArchive.take(this@MainActivity)

        @JavascriptInterface
        fun startAuth() {
            Thread {
                try {
                    val url = DshAuth.init(this@MainActivity).getString("authorizeUrl")
                    runOnUiThread {
                        authLauncher?.launch(Intent(this@MainActivity, OAuthActivity::class.java).putExtra(OAuthActivity.EXTRA_URL, url))
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        webViewRef?.evaluateJavascript(
                            "window.__onNativeAuthError && window.__onNativeAuthError(" + JSONObject.quote(e.message ?: "授权初始化失败") + ")",
                            null,
                        )
                    }
                }
            }.start()
        }

        @JavascriptInterface
        fun setSiderOpen(open: Boolean) {
            webViewRef?.siderOpen = open
            if (android.os.Build.VERSION.SDK_INT >= 34) {
                legacyBackCallback?.isEnabled = !open
            }
            if (open) registerSiderBack() else unregisterSiderBack()
        }

        @JavascriptInterface
        fun openTopUp() {
            startActivity(Intent(this@MainActivity, TopUpActivity::class.java))
        }

        @JavascriptInterface
        fun openExternal(url: String) {
            val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return
            if (uri.scheme != "https" && uri.scheme != "http") return
            runCatching {
                startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        }

        @JavascriptInterface
        fun startChatImport() {
            runOnUiThread {
                chatImportLauncher?.launch(Intent(this@MainActivity, ChatLoginActivity::class.java))
            }
        }

        @JavascriptInterface
        fun readArchiveChunk(path: String, offset: Int, size: Int): String {
            return try {
                val expected = java.io.File(cacheDir, "export").canonicalPath
                val file = java.io.File(path)
                if (file.canonicalPath != expected && !file.canonicalPath.startsWith(expected + java.io.File.separator)) return ""
                java.io.RandomAccessFile(file, "r").use { raf ->
                    raf.seek(offset.toLong())
                    val len = minOf(size, (file.length() - offset).toInt().coerceAtLeast(0))
                    val buf = ByteArray(len)
                    raf.readFully(buf, 0, len)
                    Base64.encodeToString(buf, Base64.NO_WRAP)
                }
            } catch (e: Exception) {
                ""
            }
        }

        @JavascriptInterface
        fun deleteArchive(path: String) {
            runCatching {
                val expected = java.io.File(cacheDir, "export").canonicalPath
                val file = java.io.File(path)
                if (file.canonicalPath != expected && file.canonicalPath.startsWith(expected + java.io.File.separator)) file.delete()
            }
        }

        @JavascriptInterface
        fun beginExport(name: String): String {
            val dir = java.io.File(cacheDir, "export").apply { mkdirs() }
            val safe = name.replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "deepseek_export.zip" }
            return java.io.File(dir, safe).apply { outputStream().use { } }.absolutePath
        }

        @JavascriptInterface
        fun appendExport(path: String, base64: String): Int {
            return try {
                val expected = java.io.File(cacheDir, "export").canonicalPath
                val file = java.io.File(path)
                if (file.canonicalPath != expected && !file.canonicalPath.startsWith(expected + java.io.File.separator)) return -1
                val bytes = Base64.decode(base64, Base64.DEFAULT)
                java.io.FileOutputStream(file, true).use { it.write(bytes) }
                file.length().toInt()
            } catch (e: Exception) {
                -1
            }
        }

        @JavascriptInterface
        fun finishExport(path: String, mime: String, name: String) {
            try {
                val expected = java.io.File(cacheDir, "export").canonicalPath
                val file = java.io.File(path)
                if (!file.exists()) return
                        if (file.canonicalPath != expected && !file.canonicalPath.startsWith(expected + java.io.File.separator)) return
                val values = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, name)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, mime.ifBlank { "application/zip" })
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "Download/DeepSeekNative")
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        put(android.provider.MediaStore.MediaColumns.IS_PENDING, 1)
                    }
                }
                val uri = contentResolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                uri?.let { u ->
                    contentResolver.openOutputStream(u)?.use { out -> file.inputStream().use { it.copyTo(out) } }
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        values.clear()
                        values.put(android.provider.MediaStore.MediaColumns.IS_PENDING, 0)
                        contentResolver.update(u, values, null, null)
                    }
                }
                runOnUiThread {
                    android.widget.Toast.makeText(this@MainActivity, "已导出到 下载/DeepSeekNative", android.widget.Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                android.util.Log.w("NativeShell", "finishExport failed", e)
            }
        }
    }
}

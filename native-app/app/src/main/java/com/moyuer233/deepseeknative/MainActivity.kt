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
    /** 这次录音权限请求是否由 JS 的 NativeShell.requestMic 发起（决定是否回写 __onMicPermission）。 */
    private var micRequestFromBridge = false
    private var siderBackCallback: Any? = null
    private var backPending = false
    private var legacyBackCallback: OnBackPressedCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // 用应用内语言偏好初始化原生文案（Web 侧启动后会再同步一次）
        NativeCopy.setLanguage(
            getSharedPreferences("app", MODE_PRIVATE).getString("locale", null)
                ?: java.util.Locale.getDefault().language,
        )
        registerLaunchers()
        setupBackHandling()
        cleanupExportCache()

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
            val granted = grants[android.Manifest.permission.RECORD_AUDIO] == true
            // 只有 Bridge.requestMic 发起的请求才回写 JS；WebRTC 的 onPermissionRequest 走 grant/deny
            if (micRequestFromBridge) {
                micRequestFromBridge = false
                webViewRef?.evaluateJavascript(
                    "window.__onMicPermission && window.__onMicPermission(" + granted + ")",
                    null,
                )
            }
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
                    result.data?.getStringExtra(OAuthActivity.EXTRA_ERROR) ?: NativeCopy.authCancelled
                ) + ")"
            }
            webViewRef?.evaluateJavascript(script, null)
        }
        chatImportLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val token = if (result.resultCode == Activity.RESULT_OK) {
                result.data?.getStringExtra(ChatLoginActivity.EXTRA_TOKEN)
            } else null
            if (token.isNullOrEmpty()) {
                webViewRef?.evaluateJavascript("window.__onChatArchiveError && window.__onChatArchiveError('" + NativeCopy.importLoginCancelled + "')", null)
                return@registerForActivityResult
            }
            Thread {
                try {
                    val r = ChatExport.exportAndDownload(token, cacheDir)
                    val js = "window.__onChatArchiveReady && window.__onChatArchiveReady(" +
                        JSONObject.quote(r.file.absolutePath) + "," + JSONObject.quote(r.fileName) + "," + r.bytes + ")"
                    runOnUiThread { webViewRef?.evaluateJavascript(js, null) }
                } catch (e: Exception) {
                    runOnUiThread {
                        webViewRef?.evaluateJavascript(
                            "window.__onChatArchiveError && window.__onChatArchiveError(" +
                                JSONObject.quote(e.message ?: NativeCopy.exportFailed) + ")",
                            null,
                        )
                    }
                }
            }.start()
        }
    }

    private fun setupBackHandling() {
        val legacy = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val web = webViewRef
                // 侧边栏打开时先收抽屉（原生直接处理，不等网页回包）
                if (web != null && web.siderOpen) {
                    web.evaluateJavascript("window.__siderClose && window.__siderClose()", null)
                    return
                }
                if (backPending) return
                if (web == null) {
                    finish()
                    return
                }
                backPending = true
                // 防抖：网页回包慢时避免连按触发两次后退
                android.os.Handler(mainLooper).postDelayed({ backPending = false }, 400)
                web.evaluateJavascript("window.__onBack ? window.__onBack() : false") { value ->
                    backPending = false
                    // 只有网页明确表示「已到最底层」才退出；不再用 isEnabled=false 关掉回调，
                    // 否则一次异常回包会让返回键永久失效，只能靠系统把 Activity 杀掉。
                    if (value?.trim('"') != "true" && !isFinishing) {
                        finish()
                    }
                }
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

            override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: android.webkit.JsResult?): Boolean {
                android.app.AlertDialog.Builder(this@MainActivity)
                    .setMessage(message ?: "")
                    .setPositiveButton(NativeCopy.confirm) { _, _ -> result?.confirm() }
                    .setNegativeButton(NativeCopy.cancel) { _, _ -> result?.cancel() }
                    .setOnCancelListener { result?.cancel() }
                    .show()
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
                        val launcher = cameraLauncher
                        if (launcher != null) {
                            // 相机结果要回给这次 file chooser，必须把 callback 交给相机分支
                            cameraCallback = callback
                            fileChooserCallback = null
                            cameraUri = uri
                            launcher.launch(uri)
                            return true
                        }
                        android.widget.Toast.makeText(this@MainActivity, NativeCopy.cameraFallback, android.widget.Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        android.util.Log.w("Camera", "capture failed", e)
                        // 相机没起来：把 callback 还给文件选择器，避免 <input type=file> 永远等不到结果
                        cameraCallback = null
                        cameraUri = null
                        if (fileChooserCallback == null) {
                            fileChooserCallback = callback
                        }
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

    override fun onDestroy() {
        webViewRef?.let { web ->
            web.loadUrl("about:blank")
            web.removeAllViews()
            web.destroy()
        }
        webViewRef = null
        super.onDestroy()
    }

    @Suppress("unused")
    private fun registerSiderBack() {
        // 已停用：见 setSiderOpen 的注释（与 OnBackPressedDispatcher 冲突）。
        if (true) return
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

    /** 导出/分享共用的临时目录。 */
    private fun exportDir() = java.io.File(cacheDir, "export")

    /**
     * 把 path 解析成 export 目录内的文件；不在目录内（含目录本身）返回 null。
     * 先取 canonicalPath 再比较，软链与 .. 都会被解析，防止路径穿越。
     */
    private fun resolveInExportDir(path: String): java.io.File? = try {
        val expected = exportDir().canonicalPath
        val file = java.io.File(path)
        if (file.canonicalPath.startsWith(expected + java.io.File.separator)) file else null
    } catch (e: Exception) {
        null
    }

    /** 启动时清理 6 小时前的导出残留，避免 cache/export 长期占用空间。 */
    private fun cleanupExportCache() {
        Thread {
            runCatching {
                val dir = exportDir()
                if (!dir.isDirectory) return@runCatching
                val cutoff = System.currentTimeMillis() - EXPORT_CACHE_MAX_AGE_MS
                dir.listFiles()?.forEach { file ->
                    if (file.isFile && file.lastModified() < cutoff) {
                        file.delete()
                    }
                }
            }
        }.start()
    }

    private companion object {
        /** 导出临时文件最长保留 6 小时。 */
        const val EXPORT_CACHE_MAX_AGE_MS = 6 * 60 * 60 * 1000L
    }

    inner class Bridge {
        /** Web 录音前先申请麦克风权限，结果通过 window.__onMicPermission 回传。 */
        @JavascriptInterface
        fun requestMic() {
            runOnUiThread {
                if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    webViewRef?.evaluateJavascript("window.__onMicPermission && window.__onMicPermission(true)", null)
                } else if (permissionsLauncher != null) {
                    // 标记来源：结果回调里只有这个标记才会回写 __onMicPermission
                    micRequestFromBridge = true
                    permissionsLauncher?.launch(arrayOf(android.Manifest.permission.RECORD_AUDIO))
                } else {
                    webViewRef?.evaluateJavascript("window.__onMicPermission && window.__onMicPermission(false)", null)
                }
            }
        }

        @JavascriptInterface
        fun getToken(): String = DshAuth.token(this@MainActivity) ?: ""

        /** 登出或确认 grant 失效时调用：删掉原生保存的授权，避免下次启动又被读回来。 */
        @JavascriptInterface
        fun clearToken() {
            DshAuth.clear(this@MainActivity)
        }

        @JavascriptInterface
        fun peekPendingArchive(): String = com.moyuer233.deepseeknative.data.PendingArchive.peek(this@MainActivity)

        @JavascriptInterface
        fun clearPendingArchive() {
            com.moyuer233.deepseeknative.data.PendingArchive.clear(this@MainActivity)
        }

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
                            "window.__onNativeAuthError && window.__onNativeAuthError(" + JSONObject.quote(e.message ?: NativeCopy.authInitFailed) + ")",
                            null,
                        )
                    }
                }
            }.start()
        }

        @JavascriptInterface
        fun setSiderOpen(open: Boolean) {
            // 只记录状态：返回事件统一由 setupBackHandling 里的 OnBackPressedCallback 处理。
            // 这里绝不能再注册 OnBackInvokedCallback——它和 OnBackPressedDispatcher 抢同一个
            // PRIORITY_DEFAULT，抽屉开合一次后返回事件会落到已注销的回调上，表现为「按返回直接退出 App」。
            webViewRef?.siderOpen = open
        }

        /** 返回 false 表示没打开（供 Web 侧提示，避免静默失败）。 */
        @JavascriptInterface
        fun openTopUp(assist: Boolean): Boolean = runCatching {
            startActivity(
                Intent(this@MainActivity, TopUpActivity::class.java)
                    .putExtra(TopUpActivity.EXTRA_ASSIST, assist),
            )
            true
        }.getOrDefault(false)

        @JavascriptInterface
        fun isScanAssistEnabled(): Boolean {
            val enabled = android.provider.Settings.Secure.getString(
                contentResolver,
                android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            ) ?: return false
            val target = ScanAssistService::class.java.name
            return enabled.split(':').any { entry ->
                val component = android.content.ComponentName.unflattenFromString(entry) ?: return@any false
                component.packageName == packageName && component.className == target
            }
        }

        @JavascriptInterface
        fun openAccessibilitySettings() {
            runCatching {
                startActivity(
                    Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            }
        }

        @JavascriptInterface
        fun setAppLocale(locale: String) {
            getSharedPreferences("app", MODE_PRIVATE).edit().putString("locale", locale).apply()
            NativeCopy.setLanguage(locale)
        }

        @JavascriptInterface
        fun openPlatformPage(url: String) {
            val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return
            val host = uri.host ?: return
            if (uri.scheme != "https" || (host != "deepseek.com" && !host.endsWith(".deepseek.com"))) return
            runOnUiThread {
                runCatching {
                    startActivity(
                        Intent(this@MainActivity, com.moyuer233.deepseeknative.ui.PlatformPageActivity::class.java)
                            .putExtra(com.moyuer233.deepseeknative.ui.PlatformPageActivity.EXTRA_URL, url),
                    )
                }
            }
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
                val file = resolveInExportDir(path) ?: return ""
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
            resolveInExportDir(path)?.delete()
        }

        @JavascriptInterface
        fun beginExport(name: String): String {
            val dir = exportDir().apply { mkdirs() }
            val safe = name.replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "deepseek_export.zip" }
            return java.io.File(dir, safe).apply { outputStream().use { } }.absolutePath
        }

        @JavascriptInterface
        fun appendExport(path: String, base64: String): Long {
            val file = resolveInExportDir(path) ?: return -1L
            return try {
                val bytes = Base64.decode(base64, Base64.DEFAULT)
                java.io.FileOutputStream(file, true).use { it.write(bytes) }
                file.length()
            } catch (e: Exception) {
                -1L
            }
        }

        @JavascriptInterface
        fun finishExport(path: String, mime: String, name: String) {
            try {
                val file = resolveInExportDir(path) ?: return
                if (!file.exists()) return
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
                    val dir = java.io.File(getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS), "DeepSeekNative").apply { mkdirs() }
                    val safeName = name.replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "deepseek_export.zip" }
                    val target = java.io.File(dir, safeName)
                    file.inputStream().use { input -> target.outputStream().use { input.copyTo(it) } }
                    runOnUiThread {
                        android.widget.Toast.makeText(this@MainActivity, NativeCopy.exportedToPrefix + target.absolutePath, android.widget.Toast.LENGTH_LONG).show()
                    }
                    return
                }
                val values = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, name)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, mime.ifBlank { "application/zip" })
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "Download/DeepSeekNative")
                    put(android.provider.MediaStore.MediaColumns.IS_PENDING, 1)
                }
                val uri = contentResolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: throw IllegalStateException(NativeCopy.cannotCreateDownload)
                values.clear()
                values.put(android.provider.MediaStore.MediaColumns.IS_PENDING, 0)
                // 100MB+ 拷贝放后台线程，避免阻塞 WebView 的 JS 线程
                Thread {
                    try {
                        contentResolver.openOutputStream(uri)?.use { out -> file.inputStream().use { it.copyTo(out) } }
                            ?: throw IllegalStateException(NativeCopy.cannotWriteDownload)
                        contentResolver.update(uri, values, null, null)
                        runOnUiThread {
                            android.widget.Toast.makeText(this@MainActivity, NativeCopy.exportedToDownloads, android.widget.Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        runCatching { contentResolver.delete(uri, null, null) }
                        runOnUiThread {
                            android.widget.Toast.makeText(this@MainActivity, NativeCopy.exportFailedPrefix + (e.message ?: NativeCopy.unknownError), android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                }.start()
            } catch (e: Exception) {
                android.util.Log.w("NativeShell", "finishExport failed", e)
                runOnUiThread {
                    android.widget.Toast.makeText(this@MainActivity, NativeCopy.exportFailedPrefix + (e.message ?: NativeCopy.unknownError), android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

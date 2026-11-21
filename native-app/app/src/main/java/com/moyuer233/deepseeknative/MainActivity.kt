package com.moyuer233.deepseeknative

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import androidx.webkit.WebViewAssetLoader
import com.moyuer233.deepseeknative.data.SessionStore
import com.moyuer233.deepseeknative.data.LocalSession
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.moyuer233.deepseeknative.ui.OAuthActivity
import com.moyuer233.deepseeknative.ui.OnboardingScreen
import com.moyuer233.deepseeknative.data.DshAuth
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.moyuer233.deepseeknative.ui.ChatHomeScreen
import com.moyuer233.deepseeknative.ui.ChatScreen
import com.moyuer233.deepseeknative.ui.AccountManageScreen
import com.moyuer233.deepseeknative.ui.AgreementScreen
import com.moyuer233.deepseeknative.ui.DataManageScreen
import com.moyuer233.deepseeknative.ui.DeviceManageScreen
import com.moyuer233.deepseeknative.ui.MinorModeScreen
import com.moyuer233.deepseeknative.ui.SettingsScreen
import com.moyuer233.deepseeknative.ui.PasswordLoginScreen
import com.moyuer233.deepseeknative.ui.PhoneLoginScreen
import com.moyuer233.deepseeknative.ui.WelcomeScreen
import com.moyuer233.deepseeknative.ui.theme.DeepSeekTheme

private enum class Screen { Onboarding, Welcome, PhoneLogin, PasswordLogin, Chat, Session, Settings, AccountManage, DataManage, Agreement, Devices, MinorMode }

class MainActivity : ComponentActivity() {
    private var webViewRef: WebView? = null
    private var authLauncher: androidx.activity.result.ActivityResultLauncher<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        authLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
            val ok = result.resultCode == android.app.Activity.RESULT_OK
            val token = if (ok) com.moyuer233.deepseeknative.data.DshAuth.token(this) ?: "" else ""
            val script = if (ok && token.isNotEmpty()) {
                "window.__onNativeAuth && window.__onNativeAuth(" + org.json.JSONObject.quote(token) + ")"
            } else {
                "window.__onNativeAuthError && window.__onNativeAuthError(" + org.json.JSONObject.quote(
                    result.data?.getStringExtra(com.moyuer233.deepseeknative.ui.OAuthActivity.EXTRA_ERROR) ?: "已取消授权"
                ) + ")"
            }
            webViewRef?.evaluateJavascript(script, null)
        }
        // 默认启动：直接展示 chat-demo 界面（WebView 套壳）；带 screen/native 参数时才进原生调试页。
        if (!intent.hasExtra("screen") && !intent.hasExtra("native") && !intent.hasExtra("importLocal")) {
            if (!SessionStore.isOnboarded(this)) {
                // 首次启动：先走 4 步引导，完成后进入 chat-demo 界面
                setContent {
                    DeepSeekTheme {
                        OnboardingScreen(onDone = {
                            SessionStore.setOnboarded(this)
                            // 用全新 Activity 进入 chat-demo，避免原地重建时 WebView 初始化卡加载
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        })
                    }
                }
                return
            }
            val web = WebView(this)
            web.settings.javaScriptEnabled = true
            web.settings.domStorageEnabled = true
            web.settings.allowFileAccess = true
            web.settings.mediaPlaybackRequiresUserGesture = false
            val assetLoader = WebViewAssetLoader.Builder()
                .addPathHandler("/", WebViewAssetLoader.AssetsPathHandler(this))
                .build()
            webViewRef = web
            web.addJavascriptInterface(object {
                @android.webkit.JavascriptInterface
                fun getToken(): String = com.moyuer233.deepseeknative.data.DshAuth.token(this@MainActivity) ?: ""

                @android.webkit.JavascriptInterface
                fun startAuth() {
                    Thread {
                        try {
                            val url = com.moyuer233.deepseeknative.data.DshAuth.init(this@MainActivity).getString("authorizeUrl")
                            runOnUiThread {
                                authLauncher?.launch(
                                    Intent(this@MainActivity, com.moyuer233.deepseeknative.ui.OAuthActivity::class.java)
                                        .putExtra(com.moyuer233.deepseeknative.ui.OAuthActivity.EXTRA_URL, url)
                                )
                            }
                        } catch (e: Exception) {
                            runOnUiThread {
                                webViewRef?.evaluateJavascript(
                                    "window.__onNativeAuthError && window.__onNativeAuthError(" +
                                        org.json.JSONObject.quote(e.message ?: "授权初始化失败") + ")",
                                    null,
                                )
                            }
                        }
                    }.start()
                }

                @android.webkit.JavascriptInterface
                fun openTopUp() {
                    startActivity(Intent(this@MainActivity, com.moyuer233.deepseeknative.ui.TopUpActivity::class.java))
                }

                @android.webkit.JavascriptInterface
                fun openExternal(url: String) {
                    runCatching {
                        startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                }
            }, "NativeShell")
            web.webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView,
                    request: android.webkit.WebResourceRequest,
                ): android.webkit.WebResourceResponse? = assetLoader.shouldInterceptRequest(request.url)
            }
            web.webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(msg: android.webkit.ConsoleMessage?): Boolean {
                    android.util.Log.d("WebConsole", (msg?.message() ?: "") + " @" + (msg?.lineNumber() ?: 0))
                    return true
                }
            }
            setContentView(web)
            web.loadUrl("https://appassets.androidplatform.net/web/index.html")
            return
        }
        val debugScreen = intent.getStringExtra("screen")
        val importLocal = intent.getStringExtra("importLocal")
        if (importLocal != null) {
            Thread {
                try {
                    val result = com.moyuer233.deepseeknative.data.ArchiveImporter.import(this, android.net.Uri.fromFile(java.io.File(importLocal)))
                    if (result.error == null) {
                        com.moyuer233.deepseeknative.data.LocalStore.save(this, result.sessions)
                    }
                } catch (_: Exception) {}
            }.start()
        }
        val testApiKey = intent.getStringExtra("testApiKey")
        if (testApiKey != null) {
            Thread {
                com.moyuer233.deepseeknative.data.ApiClient.streamApiChat(
                    apiKey = testApiKey,
                    prompt = "say hi",
                    thinking = false,
                    onContent = { android.util.Log.d("ChatTest", "delta: " + it.take(120)) },
                    onDone = { android.util.Log.d("ChatTest", "done") },
                    onError = { android.util.Log.d("ChatTest", "error: " + it) },
                )
            }.start()
        }
        if (intent.hasExtra("testTts")) {
            Thread {
                val grant = com.moyuer233.deepseeknative.data.DshAuth.token(this)
                val bytes = com.moyuer233.deepseeknative.data.ApiClient.synthesizeTts(grant, null, "你好，这是朗读测试。", "stella")
                android.util.Log.d("TtsTest", "grant=" + (grant != null) + " bytes=" + (bytes?.size ?: -1))
                val ok = if (bytes != null) com.moyuer233.deepseeknative.data.PcmPlayer.play(bytes) else false
                android.util.Log.d("TtsTest", "play=" + ok)
            }.start()
        }
        if (intent.hasExtra("testGrant")) {
            Thread {
                val token = com.moyuer233.deepseeknative.data.DshAuth.token(this)
                android.util.Log.d("ChatTest", "grant token? " + (token != null))
                if (token != null) {
                    com.moyuer233.deepseeknative.data.ApiClient.streamGrantChat(
                        grant = token,
                        prompt = "say hi",
                        thinking = false,
                        onContent = { android.util.Log.d("ChatTest", "delta: " + it.take(120)) },
                        onDone = { android.util.Log.d("ChatTest", "done") },
                        onError = { android.util.Log.d("ChatTest", "error: " + it.take(300)) },
                    )
                }
            }.start()
        }
        com.moyuer233.deepseeknative.ui.theme.ThemeState.mode = SessionStore.themeMode(this)
        com.moyuer233.deepseeknative.ui.theme.LangState.english = SessionStore.langEnglish(this)
        val hasToken = SessionStore.token(this) != null || com.moyuer233.deepseeknative.data.DshAuth.token(this) != null || SessionStore.apiKey(this) != null
        val onboarded = SessionStore.isOnboarded(this)
        setContent {
            DeepSeekTheme {
                var screen by remember {
                    mutableStateOf(
                        when (debugScreen) {
                            "settings" -> Screen.Settings
                            "account" -> Screen.AccountManage
                            "data" -> Screen.DataManage
                            "agreement" -> Screen.Agreement
                            "devices" -> Screen.Devices
                            "minor" -> Screen.MinorMode
                            "onboarding" -> Screen.Onboarding
                            "welcome" -> Screen.Welcome
                            "phone" -> Screen.PhoneLogin
                            "password" -> Screen.PasswordLogin
                            "session" -> Screen.Session
                            "chat" -> Screen.Chat
                            else -> when {
                                !onboarded -> Screen.Onboarding
                                hasToken -> Screen.Chat
                                else -> Screen.Welcome
                            }
                        }
                    )
                }
                var sessionTitle by remember { mutableStateOf(if (debugScreen == "session") "Hello greeting" else "") }
                var selectedSession by remember { mutableStateOf<LocalSession?>(null) }
                var pendingPrompt by remember { mutableStateOf<String?>(null) }
                val context = LocalContext.current
                val oauthLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        screen = Screen.Chat
                    }
                }
                fun startGrantLogin() {
                    Thread {
                        try {
                            val url = DshAuth.init(context).getString("authorizeUrl")
                            runOnUiThread {
                                oauthLauncher.launch(
                                    Intent(context, OAuthActivity::class.java).putExtra(OAuthActivity.EXTRA_URL, url)
                                )
                            }
                        } catch (e: Exception) {
                            runOnUiThread { android.widget.Toast.makeText(context, "授权初始化失败：" + (e.message ?: ""), android.widget.Toast.LENGTH_LONG).show() }
                        }
                    }.start()
                }
                when (screen) {
                    Screen.Onboarding -> OnboardingScreen(onDone = {
                        SessionStore.setOnboarded(context)
                        screen = if (hasToken) Screen.Chat else Screen.Welcome
                    })
                    Screen.Welcome -> WelcomeScreen(
                        onPhone = { screen = Screen.PhoneLogin },
                        onPassword = { screen = Screen.PasswordLogin },
                        onGrantLogin = { startGrantLogin() },
                    )
                    Screen.PhoneLogin -> PhoneLoginScreen(
                        onBack = { screen = Screen.Welcome },
                        onLoggedIn = { screen = Screen.Chat },
                    )
                    Screen.PasswordLogin -> PasswordLoginScreen(
                        onBack = { screen = Screen.Welcome },
                        onPhoneLogin = { screen = Screen.PhoneLogin },
                        onLoggedIn = { screen = Screen.Chat },
                    )
                    Screen.Chat -> ChatHomeScreen(
                        onOpenSession = {
                            selectedSession = it
                            sessionTitle = it.title
                            screen = Screen.Session
                        },
                        onOpenSettings = { screen = Screen.Settings },
                        onStartNewChat = { prompt ->
                            val session = LocalSession(
                                id = java.util.UUID.randomUUID().toString(),
                                title = "新对话",
                                updatedAt = System.currentTimeMillis(),
                            )
                            val list = com.moyuer233.deepseeknative.data.LocalStore.load(context)
                            list.add(session)
                            com.moyuer233.deepseeknative.data.LocalStore.save(context, list)
                            selectedSession = session
                            sessionTitle = session.title
                            pendingPrompt = prompt.ifBlank { null }
                            screen = Screen.Session
                        },
                    )
                    Screen.Session -> ChatScreen(
                        title = sessionTitle,
                        initial = selectedSession,
                        initialPrompt = pendingPrompt,
                        onBack = { pendingPrompt = null; screen = Screen.Chat },
                    )
                    Screen.Settings -> SettingsScreen(
                        onBack = { screen = Screen.Chat },
                        onLogout = { screen = Screen.Welcome },
                        onOpenAccount = { screen = Screen.AccountManage },
                        onOpenData = { screen = Screen.DataManage },
                        onOpenAgreement = { screen = Screen.Agreement },
                    )
                    Screen.AccountManage -> AccountManageScreen(
                        onBack = { screen = Screen.Settings },
                        onOpenDevices = { screen = Screen.Devices },
                        onOpenMinor = { screen = Screen.MinorMode },
                    )
                    Screen.Devices -> DeviceManageScreen(onBack = { screen = Screen.AccountManage })
                    Screen.MinorMode -> MinorModeScreen(onBack = { screen = Screen.AccountManage })
                    Screen.DataManage -> DataManageScreen(onBack = { screen = Screen.Settings })
                    Screen.Agreement -> AgreementScreen(onBack = { screen = Screen.Settings })
                }
            }
        }
    }
}

package com.moyuer233.deepseeknative

import android.os.Bundle
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
        val hasToken = SessionStore.token(this) != null || com.moyuer233.deepseeknative.data.DshAuth.token(this) != null || SessionStore.apiKey(this) != null
        val onboarded = SessionStore.isOnboarded(this)
        setContent {
            DeepSeekTheme {
                var screen by remember {
                    mutableStateOf(
                        when {
                            !onboarded -> Screen.Onboarding
                            debugScreen == "session" -> Screen.Session
                            debugScreen == "chat" || hasToken -> Screen.Chat
                            else -> Screen.Welcome
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

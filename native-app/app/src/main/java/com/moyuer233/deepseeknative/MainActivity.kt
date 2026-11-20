package com.moyuer233.deepseeknative

import android.os.Bundle
import com.moyuer233.deepseeknative.data.SessionStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.moyuer233.deepseeknative.ui.OAuthActivity
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

private enum class Screen { Welcome, PhoneLogin, PasswordLogin, Chat, Session, Settings, AccountManage, DataManage, Agreement, Devices, MinorMode }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val debugScreen = intent.getStringExtra("screen")
        val hasToken = SessionStore.token(this) != null
        setContent {
            DeepSeekTheme {
                var screen by remember {
                    mutableStateOf(
                        when {
                            debugScreen == "session" -> Screen.Session
                            debugScreen == "chat" || hasToken -> Screen.Chat
                            else -> Screen.Welcome
                        }
                    )
                }
                var sessionTitle by remember { mutableStateOf(if (debugScreen == "session") "Hello greeting" else "") }
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
                            sessionTitle = it.title
                            screen = Screen.Session
                        },
                        onOpenSettings = { screen = Screen.Settings },
                    )
                    Screen.Session -> ChatScreen(
                        title = sessionTitle,
                        onBack = { screen = Screen.Chat },
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

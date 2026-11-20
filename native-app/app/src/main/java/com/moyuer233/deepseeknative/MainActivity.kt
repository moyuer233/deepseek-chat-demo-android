package com.moyuer233.deepseeknative

import android.os.Bundle
import com.moyuer233.deepseeknative.data.SessionStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.moyuer233.deepseeknative.ui.ChatHomeScreen
import com.moyuer233.deepseeknative.ui.ChatScreen
import com.moyuer233.deepseeknative.ui.PasswordLoginScreen
import com.moyuer233.deepseeknative.ui.PhoneLoginScreen
import com.moyuer233.deepseeknative.ui.WelcomeScreen
import com.moyuer233.deepseeknative.ui.theme.DeepSeekTheme

private enum class Screen { Welcome, PhoneLogin, PasswordLogin, Chat, Session }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val debugScreen = intent.getStringExtra("screen")
        val hasToken = SessionStore.token(this) != null
        setContent {
            DeepSeekTheme {
                var screen by remember { mutableStateOf(if (debugScreen == "chat" || hasToken) Screen.Chat else Screen.Welcome) }
                var sessionTitle by remember { mutableStateOf("") }
                when (screen) {
                    Screen.Welcome -> WelcomeScreen(
                        onPhone = { screen = Screen.PhoneLogin },
                        onPassword = { screen = Screen.PasswordLogin },
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
                    )
                    Screen.Session -> ChatScreen(
                        title = sessionTitle,
                        onBack = { screen = Screen.Chat },
                    )
                }
            }
        }
    }
}

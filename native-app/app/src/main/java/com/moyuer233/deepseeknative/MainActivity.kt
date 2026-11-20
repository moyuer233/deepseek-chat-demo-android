package com.moyuer233.deepseeknative

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.moyuer233.deepseeknative.ui.ChatHomeScreen
import com.moyuer233.deepseeknative.ui.PhoneLoginScreen
import com.moyuer233.deepseeknative.ui.WelcomeScreen
import com.moyuer233.deepseeknative.ui.theme.DeepSeekTheme

private enum class Screen { Welcome, PhoneLogin, Chat }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val debugScreen = intent.getStringExtra("screen")
        setContent {
            DeepSeekTheme {
                var screen by remember { mutableStateOf(if (debugScreen == "chat") Screen.Chat else Screen.Welcome) }
                when (screen) {
                    Screen.Welcome -> WelcomeScreen(
                        onPhone = { screen = Screen.PhoneLogin },
                        onPassword = { },
                    )
                    Screen.PhoneLogin -> PhoneLoginScreen(onBack = { screen = Screen.Welcome })
                    Screen.Chat -> ChatHomeScreen()
                }
            }
        }
    }
}

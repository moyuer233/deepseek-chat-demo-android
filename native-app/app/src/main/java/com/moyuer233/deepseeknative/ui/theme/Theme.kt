package com.moyuer233.deepseeknative.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val BrandBlue = Color(0xFF426EFE)
val TextPrimary = Color(0xFF16181D)
val TextSecondary = Color(0xFF6B7280)
val FieldGray = Color(0xFFF3F4F6)
val BorderGray = Color(0xFFE5E7EB)

/** 主题模式：0 跟随系统、1 浅色、2 深色。 */
object ThemeState {
    var mode by mutableStateOf(0)
}

private val Colors = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    background = Color.White,
    onBackground = TextPrimary,
    surface = Color.White,
    onSurface = TextPrimary,
)

private val DarkColors = darkColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    background = Color(0xFF000000),
    onBackground = Color(0xFFE8EAED),
    surface = Color(0xFF121318),
    onSurface = Color(0xFFE8EAED),
)

/**
 * 语义色板。页面通过 LocalAppColors 取色，切换深浅色时无需各自判断。
 * 说明：目前聊天页/首页/设置等主要页面已接入，弹窗里仍以 Material 主题为准。
 */
data class AppColors(
    val background: Color,
    val surface: Color,
    val grouped: Color,
    val field: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val bubble: Color,
)

val LightAppColors = AppColors(
    background = Color.White,
    surface = Color.White,
    grouped = Color(0xFFF5F5F7),
    field = Color(0xFFF2F3F5),
    border = Color(0xFFE5E7EB),
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    bubble = Color(0xFFEAF0FE),
)

val DarkAppColors = AppColors(
    background = Color(0xFF000000),
    surface = Color(0xFF121318),
    grouped = Color(0xFF0B0C0F),
    field = Color(0xFF1E2027),
    border = Color(0xFF2A2D35),
    textPrimary = Color(0xFFE8EAED),
    textSecondary = Color(0xFF9AA0A9),
    bubble = Color(0xFF1E2A44),
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

@Composable
fun DeepSeekTheme(content: @Composable () -> Unit) {
    val dark = when (ThemeState.mode) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }
    androidx.compose.runtime.CompositionLocalProvider(LocalAppColors provides if (dark) DarkAppColors else LightAppColors) {
        MaterialTheme(colorScheme = if (dark) DarkColors else Colors, content = content)
    }
}

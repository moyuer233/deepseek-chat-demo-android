package com.moyuer233.deepseeknative.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BrandBlue = Color(0xFF426EFE)
val TextPrimary = Color(0xFF16181D)
val TextSecondary = Color(0xFF6B7280)
val FieldGray = Color(0xFFF3F4F6)
val BorderGray = Color(0xFFE5E7EB)

private val Colors = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    background = Color.White,
    onBackground = TextPrimary,
    surface = Color.White,
    onSurface = TextPrimary,
)

@Composable
fun DeepSeekTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, content = content)
}

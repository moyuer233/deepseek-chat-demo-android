package com.moyuer233.deepseeknative.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.moyuer233.deepseeknative.ui.theme.LocalAppColors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.moyuer233.deepseeknative.ui.theme.BrandBlue
import com.moyuer233.deepseeknative.ui.theme.LocalAppColors


/** 官方条款确认弹窗（标题 Terms of Use and Privacy Policy / Disagree / Agree）。 */
@Composable
fun TermsDialog(onDisagree: () -> Unit, onAgree: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDisagree,
        containerColor = Color.White,
        title = {
            Text("Terms of Use and Privacy Policy", color = LocalAppColors.current.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        },
        text = {
            Text(
                "Thank you for using DeepSeek. Please carefully read and fully understand the Terms of Use and Privacy Policy before clicking Agree. We will use your personal information in accordance with the agreed terms to provide our services.",
                color = LocalAppColors.current.textSecondary,
                fontSize = 14.sp,
                lineHeight = 22.sp,
            )
        },
        confirmButton = {
            TextButton(onClick = onAgree) { Text("Agree", color = BrandBlue) }
        },
        dismissButton = {
            TextButton(onClick = onDisagree) { Text("Disagree", color = LocalAppColors.current.textSecondary) }
        },
    )
}

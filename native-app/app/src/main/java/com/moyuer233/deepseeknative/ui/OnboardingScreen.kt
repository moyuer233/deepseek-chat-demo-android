package com.moyuer233.deepseeknative.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import com.moyuer233.deepseeknative.R
import com.moyuer233.deepseeknative.ui.theme.BrandBlue
import com.moyuer233.deepseeknative.ui.theme.TextPrimary
import com.moyuer233.deepseeknative.ui.theme.TextSecondary

private data class OnboardPage(val title: String, val body: String)

/** 首启引导（对齐老版 4 步里的核心 3 步，可跳过）。 */
@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val pages = listOf(
        OnboardPage("欢迎使用 DeepSeek", "和官方 App 一样的外观，聊天与数据都由你自己掌控。"),
        OnboardPage("导入历史会话", "支持 deepseek_data ZIP 归档，导入后在侧边栏按时间分组展示。"),
        OnboardPage("连接聊天内核", "用 DeepSeek 账号授权或 API Key 即可开始聊天，无需验证码。"),
    )
    var index by remember { mutableStateOf(0) }
    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        if (index == 0) {
            Image(painter = painterResource(R.drawable.logo), contentDescription = null, modifier = Modifier.size(84.dp))
            Spacer(Modifier.height(12.dp))
        }
        Text(
            pages[index].title,
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            pages[index].body,
            color = TextSecondary,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 36.dp),
        )
        Spacer(Modifier.weight(1.2f))
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(bottom = 20.dp)) {
            pages.indices.forEach { i ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (i == index) 8.dp else 6.dp)
                        .background(if (i == index) BrandBlue else Color(0xFFD5D8DE), CircleShape),
                )
            }
        }
        Button(
            onClick = { if (index < pages.lastIndex) index++ else onDone() },
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(52.dp),
        ) {
            Text(if (index < pages.lastIndex) "下一步" else "开始使用", fontSize = 16.sp)
        }
        TextButton(onClick = onDone, modifier = Modifier.padding(bottom = 24.dp)) {
            Text("跳过", color = TextSecondary, fontSize = 14.sp)
        }
    }
}

package com.moyuer233.deepseeknative.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.scale
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import com.moyuer233.deepseeknative.ui.theme.LocalAppColors
import androidx.compose.ui.platform.LocalContext
import com.moyuer233.deepseeknative.data.ChatExport
import com.moyuer233.deepseeknative.data.DshAuth
import com.moyuer233.deepseeknative.data.PendingArchive
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
import com.moyuer233.deepseeknative.ui.theme.LocalAppColors


@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    var index by remember { mutableStateOf(0) }
    var importedCount by remember { mutableStateOf(0) }
    var authorized by remember { mutableStateOf(DshAuth.token(context) != null) }
    var chatImportStatus by remember { mutableStateOf("") }
    var chatImporting by remember { mutableStateOf(false) }
    val chatImportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val token = if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getStringExtra(ChatLoginActivity.EXTRA_TOKEN)
        } else null
        if (token.isNullOrEmpty()) {
            chatImporting = false
            chatImportStatus = "已取消导入"
            return@rememberLauncherForActivityResult
        }
        chatImporting = true
        Thread {
            try {
                val exported = ChatExport.exportAndDownload(token, context.cacheDir)
                PendingArchive.save(context, exported.file.absolutePath, exported.fileName, exported.bytes)
                (context as? Activity)?.runOnUiThread {
                    chatImporting = false
                    chatImportStatus = "已获取归档，进入 chat-demo 后自动导入"
                    Toast.makeText(context, "导出完成，完成后会自动导入", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                (context as? Activity)?.runOnUiThread {
                    chatImporting = false
                    chatImportStatus = "导出失败：" + (e.message ?: "")
                }
            }
        }.start()
    }

    val oauthLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        authorized = result.resultCode == Activity.RESULT_OK
        if (authorized) {
            Toast.makeText(context, "授权成功", Toast.LENGTH_SHORT).show()
        } else {
            val error = result.data?.getStringExtra(OAuthActivity.EXTRA_ERROR)
            Toast.makeText(context, "授权失败：" + (error ?: "已取消"), Toast.LENGTH_LONG).show()
        }
    }
    fun startOauth() {
        Thread {
            try {
                val url = DshAuth.init(context).getString("authorizeUrl")
                (context as? Activity)?.runOnUiThread {
                    oauthLauncher.launch(Intent(context, OAuthActivity::class.java).putExtra(OAuthActivity.EXTRA_URL, url))
                }
            } catch (e: Exception) {
                (context as? Activity)?.runOnUiThread {
                    Toast.makeText(context, "授权初始化失败：" + (e.message ?: ""), Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }
    val pages = listOf(
        Triple("欢迎使用 DeepSeek", "和官方 App 一样的外观，聊天与数据都由你自己掌控。", 0),
        Triple("导入历史会话", "从 chat.deepseek.com 登录并导出全部会话；进入 chat-demo 后自动导入。", 1),
        Triple("连接聊天内核", "用 DeepSeek 账号授权或 API Key 即可聊天，无需验证码。", 2),
        Triple("准备就绪", "随时可以在设置里更换主题、字体和授权方式。", 3),
    )
    Column(
        modifier = Modifier.fillMaxSize().background(LocalAppColors.current.background).statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        AnimatedContent(
            targetState = index,
            transitionSpec = {
                (slideInHorizontally { it / 6 } + fadeIn()) togetherWith (slideOutHorizontally { -it / 6 } + fadeOut())
            },
            label = "onboardingPage",
        ) { page ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (page == 0) {
                    Image(painter = painterResource(R.drawable.logo), contentDescription = null, modifier = Modifier.size(84.dp))
                    Spacer(Modifier.height(12.dp))
                }
                Text(
                    pages[page].first,
                    color = LocalAppColors.current.textPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    pages[page].second,
                    color = LocalAppColors.current.textSecondary,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 36.dp),
                )
            }
        }
        Spacer(Modifier.weight(1.2f))
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(bottom = 20.dp)) {
            pages.indices.forEach { i ->
                val dotSize by animateDpAsState(if (i == index) 8.dp else 6.dp, label = "dotSize")
                val dotColor by animateColorAsState(if (i == index) BrandBlue else Color(0xFFD5D8DE), label = "dotColor")
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(dotSize)
                        .background(dotColor, CircleShape),
                )
            }
        }
        if (pages[index].third == 1) {
            OutlinedButton(
                onClick = {
                    if (!chatImporting) {
                        chatImportLauncher.launch(Intent(context, ChatLoginActivity::class.java))
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 6.dp),
            ) {
                Text(
                    when {
                        chatImporting -> "正在从 chat.deepseek.com 导出…"
                        chatImportStatus.isNotEmpty() -> chatImportStatus
                        else -> "从 chat.deepseek.com 导入会话"
                    }
                )
            }
        }
        if (pages[index].third == 2) {
            OutlinedButton(
                onClick = { startOauth() },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 6.dp),
            ) { Text(if (authorized) "已授权" else "使用 DeepSeek 账号授权") }
        }
        val primaryInteraction = remember { MutableInteractionSource() }
        val primaryPressed by primaryInteraction.collectIsPressedAsState()
        val primaryScale by animateFloatAsState(if (primaryPressed) 0.97f else 1f, label = "primaryScale")
        Button(
            onClick = { if (index < pages.lastIndex) index++ else onDone() },
            interactionSource = primaryInteraction,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(52.dp).scale(primaryScale),
        ) {
            Text(if (index < pages.lastIndex) "下一步" else "开始使用", fontSize = 16.sp)
        }
        TextButton(onClick = onDone, modifier = Modifier.padding(bottom = 24.dp)) {
            Text("跳过", color = LocalAppColors.current.textSecondary, fontSize = 14.sp)
        }
    }
}

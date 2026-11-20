package com.moyuer233.deepseeknative.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import com.moyuer233.deepseeknative.ui.TopUpActivity
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.moyuer233.deepseeknative.data.UpdateChecker
import com.moyuer233.deepseeknative.data.UpdateResult
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moyuer233.deepseeknative.ui.theme.TextPrimary
import com.moyuer233.deepseeknative.ui.theme.TextSecondary

/** 官方设置页 1:1 复刻（静态版，逐项功能后续接）。 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenAccount: () -> Unit = {},
    onOpenData: () -> Unit = {},
    onOpenAgreement: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var checking by remember { mutableStateOf(false) }
    var updateResult by remember { mutableStateOf<UpdateResult?>(null) }
    var showLogout by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F7)).statusBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = CircleShape, color = Color(0xFFEDEEF0), modifier = Modifier.size(36.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "返回", tint = TextPrimary)
                }
            }
            Text(
                "设置",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Spacer(Modifier.width(36.dp))
        }
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            SectionLabel("账户")
            SettingsCard {
                SettingsRow("账号管理", Icons.Filled.PersonOutline, onClick = onOpenAccount)
                Divider()
                SettingsRow("数据管理", Icons.Filled.Storage, onClick = onOpenData)
                Divider()
                SettingsRow("充值", Icons.Filled.Paid, onClick = {
                    context.startActivity(Intent(context, TopUpActivity::class.java))
                })
            }
            SectionLabel("应用")
            SettingsCard {
                SettingsRow("语言", Icons.Filled.Language, value = "中文（简体中文）")
                Divider()
                SettingsRow("外观", Icons.Filled.DarkMode, value = "系统")
                Divider()
                SettingsRow("字体大小", Icons.Filled.FormatSize)
                Divider()
                SettingsRow("个性化", Icons.Filled.Palette)
            }
            SectionLabel("语音")
            SettingsCard {
                SettingsRow("朗读音色", Icons.Filled.VolumeUp)
            }
            SectionLabel("关于")
            SettingsCard {
                SettingsRow(
                    if (checking) "检查更新中…" else "检查更新",
                    Icons.Filled.Update,
                    value = com.moyuer233.deepseeknative.BuildConfig.VERSION_NAME,
                    onClick = {
                        if (!checking) {
                            checking = true
                            scope.launch {
                                val result = withContext(Dispatchers.IO) { UpdateChecker.check() }
                                checking = false
                                updateResult = result
                            }
                        }
                    },
                )
                Divider()
                SettingsRow("服务协议", Icons.Filled.Description, onClick = onOpenAgreement)
            }
            Spacer(Modifier.height(16.dp))
            SettingsCard {
                SettingsRow("帮助与反馈", Icons.Filled.HelpOutline)
            }
            Spacer(Modifier.height(16.dp))
            SettingsCard {
                SettingsRow("退出登录", Icons.Filled.Logout, onClick = { showLogout = true })
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "模型名称：Deepseek Chat\n备案号：Beijing-DeepseekChat-202404280016",
                color = Color(0xFF9AA0A9),
                fontSize = 12.sp,
                lineHeight = 18.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            )
        }
    }

    if (showLogout) {
        AlertDialog(
            onDismissRequest = { showLogout = false },
            title = { Text("退出登录") },
            text = { Text("退出后需要重新登录才能继续使用。") },
            confirmButton = {
                TextButton(onClick = {
                    showLogout = false
                    com.moyuer233.deepseeknative.data.SessionStore.clear(context)
                    onLogout()
                }) { Text("退出登录") }
            },
            dismissButton = { TextButton(onClick = { showLogout = false }) { Text("取消") } },
        )
    }

    updateResult?.let { result ->
        val hasNew = result.latestVersion != null && result.latestVersion != result.currentVersion
        AlertDialog(
            onDismissRequest = { updateResult = null },
            title = { Text("检查更新") },
            text = {
                Text(
                    when {
                        result.error != null -> result.error
                        hasNew -> "发现新版本 v" + result.latestVersion + "（当前 " + result.currentVersion + "）"
                        else -> "当前已是最新版本 " + result.currentVersion
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = { updateResult = null }) { Text(if (hasNew) "取消" else "好") }
            },
            dismissButton = if (hasNew) {
                {
                    TextButton(onClick = {
                        result.releaseUrl?.let { url ->
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                        updateResult = null
                    }) { Text("去下载") }
                }
            } else null,
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        color = Color(0xFF8A9099),
        fontSize = 13.sp,
        modifier = Modifier.padding(start = 28.dp, top = 18.dp, bottom = 8.dp),
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        Column { content() }
    }
}

@Composable
private fun SettingsRow(title: String, icon: ImageVector, value: String? = null, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(title, color = TextPrimary, fontSize = 16.sp)
        Spacer(Modifier.weight(1f))
        if (value != null) {
            Text(value, color = TextSecondary, fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFFC7CBD1), modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun Divider() {
    androidx.compose.material3.HorizontalDivider(
        color = Color(0xFFF0F1F3),
        modifier = Modifier.fillMaxWidth().padding(start = 54.dp),
    )
}

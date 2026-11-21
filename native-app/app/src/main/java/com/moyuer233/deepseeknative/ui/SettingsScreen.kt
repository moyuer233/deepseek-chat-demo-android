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
import com.moyuer233.deepseeknative.data.SessionStore
import com.moyuer233.deepseeknative.data.UpdateChecker
import com.moyuer233.deepseeknative.data.UpdateResult
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moyuer233.deepseeknative.ui.theme.LocalAppColors
import com.moyuer233.deepseeknative.ui.theme.ThemeState
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
    val c = LocalAppColors.current
    val scope = rememberCoroutineScope()
    var checking by remember { mutableStateOf(false) }
    var updateResult by remember { mutableStateOf<UpdateResult?>(null) }
    var showLogout by remember { mutableStateOf(false) }
    var showFontDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showVoiceDialog by remember { mutableStateOf(false) }
    var voices by remember { mutableStateOf<List<String>>(emptyList()) }
    val ttsHolder = remember { mutableStateOf<android.speech.tts.TextToSpeech?>(null) }
    androidx.compose.runtime.LaunchedEffect(showVoiceDialog) {
        if (showVoiceDialog && ttsHolder.value == null) {
            val engine = android.speech.tts.TextToSpeech(context) { }
            ttsHolder.value = engine
            voices = engine.voices?.filter { it.locale.language == "zh" }?.map { it.name }?.sorted().orEmpty()
        }
    }
    Column(modifier = Modifier.fillMaxSize().background(c.grouped).statusBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = CircleShape, color = c.field, modifier = Modifier.size(36.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "返回", tint = c.textPrimary)
                }
            }
            Text(
                "设置",
                color = c.textPrimary,
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
                SettingsRow(
                    "外观",
                    Icons.Filled.DarkMode,
                    value = listOf("系统", "浅色", "深色").getOrElse(ThemeState.mode) { "系统" },
                    onClick = { showThemeDialog = true },
                )
                Divider()
                SettingsRow(
                    "字体大小",
                    Icons.Filled.FormatSize,
                    value = listOf("小", "标准", "大", "超大").getOrElse(SessionStore.fontStep(context)) { "标准" },
                    onClick = { showFontDialog = true },
                )
                Divider()
                SettingsRow("个性化", Icons.Filled.Palette)
            }
            SectionLabel("语音")
            SettingsCard {
                SettingsRow(
                    "朗读音色",
                    Icons.Filled.VolumeUp,
                    value = when (SessionStore.ttsVoice(context)) {
                        "tide" -> "tide（男声）"
                        "stella" -> "stella（女声）"
                        else -> "stella（女声）"
                    },
                    onClick = { showVoiceDialog = true },
                )
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
                color = LocalAppColors.current.textSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            )
        }
    }

    if (showVoiceDialog) {
        AlertDialog(
            onDismissRequest = { showVoiceDialog = false },
            title = { Text("朗读音色") },
            text = {
                Column {
                    listOf("stella" to "stella（女声）", "tide" to "tide（男声）").forEach { (id, label) ->
                        val selected = (SessionStore.ttsVoice(context) ?: "stella") == id
                        TextButton(onClick = {
                            SessionStore.saveTtsVoice(context, id)
                            showVoiceDialog = false
                        }) { Text((if (selected) "✓ " else "   ") + label, fontSize = 14.sp) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showVoiceDialog = false }) { Text("取消") } },
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("外观") },
            text = {
                Column {
                    listOf("系统", "浅色", "深色").forEachIndexed { index, label ->
                        val selected = ThemeState.mode == index
                        TextButton(onClick = {
                            ThemeState.mode = index
                            SessionStore.saveThemeMode(context, index)
                            showThemeDialog = false
                        }) { Text((if (selected) "✓ " else "   ") + label) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showThemeDialog = false }) { Text("取消") } },
        )
    }

    if (showFontDialog) {
        AlertDialog(
            onDismissRequest = { showFontDialog = false },
            title = { Text("字体大小") },
            text = {
                Column {
                    listOf("小", "标准", "大", "超大").forEachIndexed { index, label ->
                        val selected = SessionStore.fontStep(context) == index
                        TextButton(onClick = {
                            SessionStore.saveFontStep(context, index)
                            showFontDialog = false
                        }) {
                            Text(if (selected) "✓ " + label else "   " + label)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showFontDialog = false }) { Text("取消") } },
        )
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
    val c = LocalAppColors.current
    Text(
        text,
        color = c.textSecondary,
        fontSize = 13.sp,
        modifier = Modifier.padding(start = 28.dp, top = 18.dp, bottom = 8.dp),
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    val c = LocalAppColors.current
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = c.surface,
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
        val c = LocalAppColors.current
    Icon(icon, contentDescription = null, tint = c.textPrimary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        val c2 = LocalAppColors.current
        Text(title, color = c2.textPrimary, fontSize = 16.sp)
        Spacer(Modifier.weight(1f))
        if (value != null) {
            val c3 = LocalAppColors.current
            Text(value, color = c3.textSecondary, fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = LocalAppColors.current.border, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun Divider() {
    androidx.compose.material3.HorizontalDivider(
        color = LocalAppColors.current.border,
        modifier = Modifier.fillMaxWidth().padding(start = 54.dp),
    )
}

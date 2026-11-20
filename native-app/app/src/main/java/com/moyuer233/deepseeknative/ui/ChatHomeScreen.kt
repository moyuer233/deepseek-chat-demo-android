package com.moyuer233.deepseeknative.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moyuer233.deepseeknative.R
import com.moyuer233.deepseeknative.ui.theme.BrandBlue
import com.moyuer233.deepseeknative.ui.theme.TextPrimary
import com.moyuer233.deepseeknative.ui.theme.TextSecondary

/** 官方聊天首页（空态）1:1 复刻。 */
@Composable
fun ChatHomeScreen(
    onOpenSession: (com.moyuer233.deepseeknative.data.ChatSession) -> Unit = {},
    onOpenSettings: () -> Unit = {},
) {
    var drawerOpen by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { drawerOpen = true }) {
                    Icon(Icons.Filled.Menu, contentDescription = "菜单", tint = TextPrimary)
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.Call, contentDescription = "通话", tint = TextPrimary, modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.VolumeOff, contentDescription = "静音", tint = TextPrimary, modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.Add, contentDescription = "新建", tint = TextPrimary, modifier = Modifier.size(26.dp))
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Spacer(Modifier.weight(1f))
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                )
                Spacer(Modifier.height(16.dp))
                Text("你好，我能帮什么忙吗？", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.weight(1.2f))
            }
            InputCard(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
            Spacer(Modifier.height(8.dp))
            Spacer(Modifier.navigationBarsPadding())
        }
        if (drawerOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x33000000))
                    .clickable { drawerOpen = false },
            )
            AnimatedVisibility(
                visible = true,
                enter = slideInHorizontally(initialOffsetX = { -it }),
                exit = slideOutHorizontally(targetOffsetX = { -it }),
            ) {
                DrawerContent(
                    modifier = Modifier.fillMaxHeight().fillMaxWidth(0.72f),
                    onOpenSession = onOpenSession,
                    onOpenSettings = onOpenSettings,
                )
            }
        }
    }
}

@Composable
private fun InputCard(modifier: Modifier = Modifier) {
    var think by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf(false) }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF7F8FA),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text("发消息或按住说话", color = Color(0xFF9AA0A9), fontSize = 16.sp)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ToggleChip("深度思考", think, Icons.Filled.GraphicEq) { think = !think }
                Spacer(Modifier.width(8.dp))
                ToggleChip("智能搜索", search, Icons.Filled.Public) { search = !search }
                Spacer(Modifier.weight(1f))
                Icon(Icons.Filled.Add, contentDescription = "上传", tint = TextPrimary, modifier = Modifier.size(26.dp))
                Spacer(Modifier.width(14.dp))
                Icon(Icons.Filled.GraphicEq, contentDescription = "语音", tint = TextPrimary, modifier = Modifier.size(26.dp))
            }
        }
    }
}

@Composable
private fun ToggleChip(text: String, active: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    val bg = if (active) Color(0xFFE8EEFF) else Color.White
    val fg = if (active) BrandBlue else TextPrimary
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE3E6EA)),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(text, color = fg, fontSize = 14.sp)
        }
    }
}

/** 侧栏：搜索框、空态、账号行。 */
@Composable
private fun DrawerContent(
    modifier: Modifier = Modifier,
    onOpenSession: (com.moyuer233.deepseeknative.data.ChatSession) -> Unit = {},
    onOpenSettings: () -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var sessions by remember { mutableStateOf<List<com.moyuer233.deepseeknative.data.ChatSession>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        val token = com.moyuer233.deepseeknative.data.SessionStore.token(context)
        if (token != null) {
            val result = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                com.moyuer233.deepseeknative.data.ApiClient.fetchSessions(token)
            }
            result.getOrNull()?.let { list ->
                sessions = list.mapNotNull { item ->
                    val id = item.optString("id", "")
                    if (id.isEmpty()) null else com.moyuer233.deepseeknative.data.ChatSession(
                        id = id,
                        title = item.optString("title", "新对话"),
                        updatedAt = item.optLong("updated_at", 0L),
                    )
                }
            }
        }
        loading = false
    }
    Surface(modifier = modifier, color = Color.White) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Spacer(Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFF2F3F5),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .height(44.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp)) {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = Color(0xFF9AA0A9), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("搜索对话内容...", color = Color(0xFF9AA0A9), fontSize = 15.sp)
                }
            }
            if (sessions.isNotEmpty()) {
                androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    items(sessions.size) { index ->
                        val session = sessions[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenSession(session) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                session.title,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            } else {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(if (loading) "加载中…" else "暂无对话", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                Text("你与 DeepSeek 的对话\n将展示在这里", color = TextSecondary, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 20.sp)
                Spacer(Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = BrandBlue,
                    modifier = Modifier.clickable { }.height(48.dp),
                ) {
                    Box(modifier = Modifier.padding(horizontal = 22.dp), contentAlignment = Alignment.Center) {
                        Text("开启新对话", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(shape = CircleShape, color = Color(0xFFE6E8EC), modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = Color(0xFF8A9099), modifier = Modifier.padding(6.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text("193****64", color = TextPrimary, fontSize = 15.sp)
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Filled.MoreHoriz,
                    contentDescription = "设置",
                    tint = TextSecondary,
                    modifier = Modifier.size(22.dp).clickable { onOpenSettings() },
                )
            }
        }
    }
}

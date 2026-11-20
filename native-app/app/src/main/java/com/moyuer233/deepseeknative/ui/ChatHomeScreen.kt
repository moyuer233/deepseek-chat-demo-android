package com.moyuer233.deepseeknative.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.moyuer233.deepseeknative.data.LocalStore
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
    onOpenSession: (com.moyuer233.deepseeknative.data.LocalSession) -> Unit = {},
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
                val greeting = remember {
                    listOf(
                        "你好，我能帮什么忙吗？",
                        "嗨！今天想聊些什么？",
                        "今天有什么可以帮到你？",
                    ).random()
                }
                Text(greeting, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Medium)
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
@OptIn(ExperimentalFoundationApi::class)
private fun DrawerContent(
    modifier: Modifier = Modifier,
    onOpenSession: (com.moyuer233.deepseeknative.data.LocalSession) -> Unit = {},
    onOpenSettings: () -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var sessions by remember { mutableStateOf<List<com.moyuer233.deepseeknative.data.LocalSession>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var query by remember { mutableStateOf("") }
    var menuSession by remember { mutableStateOf<com.moyuer233.deepseeknative.data.LocalSession?>(null) }
    var renameText by remember { mutableStateOf("") }
    var renaming by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        sessions = com.moyuer233.deepseeknative.data.LocalStore.load(context).sortedWith(compareByDescending<com.moyuer233.deepseeknative.data.LocalSession> { it.pinned }.thenByDescending { it.updatedAt })
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
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 15.sp),
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            if (query.isEmpty()) {
                                Text("搜索对话内容...", color = Color(0xFF9AA0A9), fontSize = 15.sp)
                            }
                            inner()
                        },
                    )
                }
            }
            val filtered = remember(sessions, query) {
                if (query.isBlank()) sessions else sessions.filter { s ->
                    s.title.contains(query, ignoreCase = true) || s.messages.any { it.content.contains(query, ignoreCase = true) }
                }
            }
            val groups = remember(filtered) { groupSessions(filtered) }
            if (filtered.isNotEmpty()) {
                androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    groups.forEach { (label, list) ->
                        item {
                            Text(
                                label,
                                color = Color(0xFF8A9099),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 6.dp),
                            )
                        }
                        items(list.size) { index ->
                            val session = list[index]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = { onOpenSession(session) },
                                        onLongClick = { menuSession = session; renameText = session.title },
                                    )
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
                }
            } else {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    when {
                        loading -> "加载中…"
                        query.isNotBlank() -> "无匹配结果"
                        else -> "暂无对话"
                    },
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(8.dp))
                Text("你与 DeepSeek 的对话\n将展示在这里", color = TextSecondary, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 20.sp)
                Spacer(Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = BrandBlue,
                    modifier = Modifier.clickable {
                        val session = com.moyuer233.deepseeknative.data.LocalSession(
                            id = java.util.UUID.randomUUID().toString(),
                            title = "新对话",
                            updatedAt = System.currentTimeMillis(),
                        )
                        val list = com.moyuer233.deepseeknative.data.LocalStore.load(context)
                        list.add(session)
                        com.moyuer233.deepseeknative.data.LocalStore.save(context, list)
                        sessions = list.sortedByDescending { it.updatedAt }
                        onOpenSession(session)
                    }.height(48.dp),
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
                Text(
                    com.moyuer233.deepseeknative.data.SessionStore.maskedMobile(context) ?: "未登录",
                    color = TextPrimary,
                    fontSize = 15.sp,
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Filled.MoreHoriz,
                    contentDescription = "设置",
                    tint = TextSecondary,
                    modifier = Modifier.size(22.dp).clickable { onOpenSettings() },
                )
            }
            val target = menuSession
            if (target != null && !renaming) {
                AlertDialog(
                    onDismissRequest = { menuSession = null },
                    title = { Text(target.title) },
                    text = {
                        Column {
                            TextButton(onClick = {
                                val list = com.moyuer233.deepseeknative.data.LocalStore.load(context)
                                val index = list.indexOfFirst { it.id == target.id }
                                if (index >= 0) {
                                    list[index].pinned = !list[index].pinned
                                    com.moyuer233.deepseeknative.data.LocalStore.save(context, list)
                                    sessions = list.sortedWith(compareByDescending<com.moyuer233.deepseeknative.data.LocalSession> { it.pinned }.thenByDescending { it.updatedAt })
                                }
                                menuSession = null
                            }) { Text(if (target.pinned) "取消置顶" else "置顶") }
                            TextButton(onClick = { renaming = true }) { Text("重命名") }
                            TextButton(onClick = {
                                val list = com.moyuer233.deepseeknative.data.LocalStore.load(context)
                                list.removeAll { it.id == target.id }
                                com.moyuer233.deepseeknative.data.LocalStore.save(context, list)
                                sessions = list.sortedByDescending { it.updatedAt }
                                menuSession = null
                            }) { Text("删除", color = Color(0xFFD93025)) }
                        }
                    },
                    confirmButton = { TextButton(onClick = { menuSession = null }) { Text("取消") } },
                )
            }
            if (target != null && renaming) {
                AlertDialog(
                    onDismissRequest = { renaming = false; menuSession = null },
                    title = { Text("重命名对话") },
                    text = {
                        OutlinedTextField(value = renameText, onValueChange = { renameText = it }, singleLine = true)
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            val list = com.moyuer233.deepseeknative.data.LocalStore.load(context)
                            val index = list.indexOfFirst { it.id == target.id }
                            if (index >= 0 && renameText.isNotBlank()) {
                                list[index].title = renameText.trim()
                                com.moyuer233.deepseeknative.data.LocalStore.save(context, list)
                                sessions = list.sortedByDescending { it.updatedAt }
                            }
                            renaming = false
                            menuSession = null
                        }) { Text("保存") }
                    },
                    dismissButton = { TextButton(onClick = { renaming = false; menuSession = null }) { Text("取消") } },
                )
            }
        }
    }
}


/** 按最近更新时间分组（对齐官方 Today/Yesterday/7 Days/30 Days）。 */
private fun groupSessions(sessions: List<com.moyuer233.deepseeknative.data.LocalSession>): List<Pair<String, List<com.moyuer233.deepseeknative.data.LocalSession>>> {
    val now = System.currentTimeMillis()
    val day = 24L * 60 * 60 * 1000
    val buckets = linkedMapOf(
        "Today" to mutableListOf<com.moyuer233.deepseeknative.data.LocalSession>(),
        "Yesterday" to mutableListOf(),
        "7 Days" to mutableListOf(),
        "30 Days" to mutableListOf(),
        "Earlier" to mutableListOf(),
    )
    for (s in sessions.sortedWith(compareByDescending<com.moyuer233.deepseeknative.data.LocalSession> { it.pinned }.thenByDescending { it.updatedAt })) {
        if (s.pinned) { buckets.getOrPut("Pinned") { mutableListOf() }.add(s); continue }
        val age = now - s.updatedAt
        when {
            age < day -> buckets["Today"]!!.add(s)
            age < 2 * day -> buckets["Yesterday"]!!.add(s)
            age < 7 * day -> buckets["7 Days"]!!.add(s)
            age < 30 * day -> buckets["30 Days"]!!.add(s)
            else -> buckets["Earlier"]!!.add(s)
        }
    }
    return buckets.filter { it.value.isNotEmpty() }.map { it.key to it.value }
}

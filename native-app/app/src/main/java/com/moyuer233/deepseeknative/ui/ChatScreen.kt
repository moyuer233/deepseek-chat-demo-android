package com.moyuer233.deepseeknative.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moyuer233.deepseeknative.data.ApiClient
import com.moyuer233.deepseeknative.data.DshAuth
import com.moyuer233.deepseeknative.data.LocalMessage
import com.moyuer233.deepseeknative.data.LocalSession
import com.moyuer233.deepseeknative.data.LocalStore
import com.moyuer233.deepseeknative.data.SessionStore
import com.moyuer233.deepseeknative.ui.theme.TextPrimary
import com.moyuer233.deepseeknative.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import java.util.Locale

private data class ChatMessage(
    val role: String, // user / assistant
    var content: String = "",
    var thinking: String = "",
    var done: Boolean = false,
)

/** 会话页：真实发送 + 流式回复（chat.deepseek.com，含聊天 PoW）。 */
@Composable
fun ChatScreen(
    title: String,
    sessionId: String? = null,
    initial: LocalSession? = null,
    onBack: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    var ttsReady by remember { mutableStateOf(false) }
    val tts = remember {
        TextToSpeech(context) { status -> ttsReady = status == TextToSpeech.SUCCESS }
    }
    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }
    val voiceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrEmpty()) input = spoken
        }
    }
    var currentSessionId by remember { mutableStateOf(initial?.id ?: sessionId) }
    val messages = remember {
        mutableStateListOf<ChatMessage>().apply {
            initial?.messages?.forEach { add(ChatMessage(it.role, it.content, it.thinking, true)) }
        }
    }
    fun saveLocals() {
        val id = currentSessionId ?: return
        val list = LocalStore.load(context)
        val msgs = messages.map { LocalMessage(it.role, it.content, it.thinking) }.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index >= 0) {
            list[index].messages.clear()
            list[index].messages.addAll(msgs)
            list[index].updatedAt = System.currentTimeMillis()
            if (list[index].title.isBlank()) list[index].title = title.ifBlank { "新对话" }
        } else {
            list.add(LocalSession(id, title.ifBlank { "新对话" }, System.currentTimeMillis(), msgs))
        }
        LocalStore.save(context, list)
    }
    var streaming by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showKeyDialog by remember { mutableStateOf(false) }
    var keyInput by remember { mutableStateOf("") }
    var thinkingEnabled by remember { mutableStateOf(true) }
    var searchEnabled by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    fun send() {
        val text = input.trim()
        if (text.isEmpty() || streaming) return
        val grant = DshAuth.token(context)
        val chatToken = SessionStore.token(context)
        val apiKey = SessionStore.apiKey(context)
        if (grant == null && chatToken == null && apiKey == null) {
            showKeyDialog = true
            return
        }
        input = ""
        error = null
        messages.add(ChatMessage(role = "user", content = text, done = true))
        messages.add(ChatMessage(role = "assistant", content = "", thinking = "", done = false))
        saveLocals()
        streaming = true
        scope.launch {
            when {
                grant != null -> withContext(Dispatchers.IO) {
                    ApiClient.streamGrantChat(
                        grant = grant,
                        prompt = text,
                        thinking = thinkingEnabled,
                        onContent = { content -> scope.launch { messages[messages.size - 1] = messages.last().copy(content = content) } },
                        onDone = { scope.launch { messages[messages.size - 1] = messages.last().copy(done = true); saveLocals() } },
                        onError = { msg -> scope.launch { messages[messages.size - 1] = messages.last().copy(done = true); error = msg; saveLocals() } },
                    )
                }
                apiKey != null -> withContext(Dispatchers.IO) {
                    ApiClient.streamApiChat(
                        apiKey = apiKey,
                        prompt = text,
                        thinking = true,
                        onContent = { content -> scope.launch { messages[messages.size - 1] = messages.last().copy(content = content) } },
                        onDone = { scope.launch { messages[messages.size - 1] = messages.last().copy(done = true) } },
                        onError = { msg -> scope.launch { messages[messages.size - 1] = messages.last().copy(done = true); error = msg } },
                    )
                }
                else -> {
                    val sid = currentSessionId ?: withContext(Dispatchers.IO) {
                        ApiClient.createSession(chatToken!!).getOrNull()
                    }.also { if (it != null) currentSessionId = it }
                    if (sid == null) {
                        messages[messages.size - 1] = messages.last().copy(done = true)
                        error = "创建会话失败，请稍后重试"
                    } else {
                        withContext(Dispatchers.IO) {
                            ApiClient.streamCompletion(
                                token = chatToken!!,
                                sessionId = sid,
                                prompt = text,
                                thinkingEnabled = thinkingEnabled,
                                searchEnabled = searchEnabled,
                                onDelta = { content, thinking ->
                                    scope.launch { messages[messages.size - 1] = messages.last().copy(content = content, thinking = thinking) }
                                },
                                onDone = { scope.launch { messages[messages.size - 1] = messages.last().copy(done = true) } },
                                onError = { msg -> scope.launch { messages[messages.size - 1] = messages.last().copy(done = true); error = msg } },
                            )
                        }
                    }
                }
            }
            streaming = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White).statusBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "返回", tint = TextPrimary)
            }
            Text(
                title,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
            )
            IconButton(onClick = { }) {
                Icon(Icons.Filled.MoreHoriz, contentDescription = "更多", tint = TextPrimary)
            }
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        ) {
            items(messages) { message ->
                if (message.role == "user") {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.End) {
                        Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFEAF0FE), modifier = Modifier.widthIn(max = 300.dp)) {
                            Text(message.content, color = TextPrimary, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        if (message.thinking.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { }) {
                                Text("已思考", color = Color(0xFF9AA0A9), fontSize = 14.sp)
                                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF9AA0A9), modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                        if (message.content.isEmpty() && !message.done) {
                            Text("正在思考…", color = TextSecondary, fontSize = 15.sp)
                        } else {
                            Text(message.content, color = TextPrimary, fontSize = 16.sp, lineHeight = 24.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MessageAction(Icons.Filled.ContentCopy) { clipboard.setText(AnnotatedString(message.content)) }
                            MessageAction(Icons.Filled.ThumbUp)
                            MessageAction(Icons.Filled.ThumbDown)
                            MessageAction(Icons.Filled.VolumeUp) {
                                if (ttsReady && message.content.isNotEmpty()) {
                                    tts.language = Locale.SIMPLIFIED_CHINESE
                                    tts.speak(message.content, TextToSpeech.QUEUE_FLUSH, null, "read_aloud")
                                }
                            }
                            MessageAction(Icons.Filled.Share) {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, message.content)
                                }
                                context.startActivity(Intent.createChooser(intent, "分享"))
                            }
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Filled.Refresh, contentDescription = "重新生成", tint = Color(0xFF9AA0A9), modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
        if (error != null) {
            Text(
                error!!,
                color = Color(0xFFD93025),
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = RoundedCornerShape(24.dp), color = Color(0xFFF7F8FA), modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    BasicTextField(
                        value = input,
                        onValueChange = { input = it },
                        textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 16.sp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            if (input.isEmpty()) {
                                Text("发消息或按住说话", color = Color(0xFF9AA0A9), fontSize = 15.sp)
                            }
                            inner()
                        },
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(if (thinkingEnabled) Color(0xFFEAF0FE) else Color.Transparent, RoundedCornerShape(14.dp))
                                .clickable { thinkingEnabled = !thinkingEnabled }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Icon(Icons.Filled.GraphicEq, contentDescription = null, tint = if (thinkingEnabled) Color(0xFF426EFE) else TextPrimary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("深度思考", color = if (thinkingEnabled) Color(0xFF426EFE) else TextPrimary, fontSize = 13.sp)
                        }
                        Spacer(Modifier.width(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(if (searchEnabled) Color(0xFFEAF0FE) else Color.Transparent, RoundedCornerShape(14.dp))
                                .clickable { searchEnabled = !searchEnabled }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Icon(Icons.Filled.Public, contentDescription = null, tint = if (searchEnabled) Color(0xFF426EFE) else TextPrimary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("智能搜索", color = if (searchEnabled) Color(0xFF426EFE) else TextPrimary, fontSize = 13.sp)
                        }
                        Spacer(Modifier.weight(1f))
                        if (input.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (streaming) Color(0xFF9AA0A9) else Color(0xFF426EFE),
                                modifier = Modifier.size(32.dp).clickable {
                                    if (streaming) {
                                        ApiClient.stopStream()
                                    } else {
                                        send()
                                    }
                                },
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        if (streaming) Icons.Filled.Stop else Icons.Filled.ArrowUpward,
                                        contentDescription = if (streaming) "停止" else "发送",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        } else {
                            Icon(Icons.Filled.Add, contentDescription = "附件", tint = TextPrimary, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(10.dp))
                            Icon(
                                Icons.Filled.GraphicEq,
                                contentDescription = "语音",
                                tint = TextPrimary,
                                modifier = Modifier.size(22.dp).clickable {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "请说话")
                                    }
                                    runCatching { voiceLauncher.launch(intent) }
                                },
                            )
                        }
                    }
                }
            }
        }
        Text(
            "回答由 AI 生成，仅供参考",
            color = Color(0xFFB0B5BD),
            fontSize = 11.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
        )
        Spacer(Modifier.navigationBarsPadding())
    }

    if (showKeyDialog) {
        AlertDialog(
            onDismissRequest = { showKeyDialog = false },
            title = { Text("使用 API Key") },
            text = {
                Column {
                    Text("未登录。可以填入 DeepSeek API Key 直接聊天（sk- 开头）。", color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        placeholder = { Text("sk-...") },
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val key = keyInput.trim()
                    if (key.isNotEmpty()) {
                        SessionStore.saveApiKey(context, key)
                        showKeyDialog = false
                        send()
                    }
                }) { Text("保存并发送") }
            },
            dismissButton = { TextButton(onClick = { showKeyDialog = false }) { Text("取消") } },
        )
    }
}

@Composable
private fun MessageAction(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit = {}) {
    Icon(
        icon,
        contentDescription = null,
        tint = Color(0xFF9AA0A9),
        modifier = Modifier.size(20.dp).clickable(onClick = onClick),
    )
    Spacer(Modifier.width(20.dp))
}

package com.moyuer233.deepseeknative.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Description
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
import androidx.compose.runtime.mutableStateMapOf
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
import com.moyuer233.deepseeknative.data.PcmPlayer
import com.moyuer233.deepseeknative.data.SessionStore
import com.moyuer233.deepseeknative.ui.theme.BrandBlue
import com.moyuer233.deepseeknative.ui.theme.TextPrimary
import com.moyuer233.deepseeknative.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import java.io.File
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import java.util.Locale

/** 显示/落库前清掉服务端安全标签。 */
private fun cleanSafety(raw: String): String {
    var text = raw.replace(Regex("<ds_safety>[\\s\\S]*?</ds_safety>"), "")
    val open = text.indexOf("<ds_safety>")
    if (open >= 0) text = text.substring(0, open)
    text = text.replace(Regex("</?ds_safety>"), "")
    text = text.replace(Regex("\\bSafe\\s*$"), "")
    return text.trimEnd()
}

private data class PendingAttachment(val path: String, val name: String, val mime: String, val isImage: Boolean)

private data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val dbId: Int = 0,
    val parentId: Int? = null,
    val role: String, // user / assistant
    var content: String = "",
    var thinking: String = "",
    var imagePath: String = "",
    var filePath: String = "",
    var fileName: String = "",
    var fileMime: String = "",
    var liked: Int = 0,
    var done: Boolean = false,
)

/** 会话页：真实发送 + 流式回复（chat.deepseek.com，含聊天 PoW）。 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
fun ChatScreen(
    title: String,
    sessionId: String? = null,
    initial: LocalSession? = null,
    onBack: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fontScale = remember { listOf(0.85f, 1f, 1.15f, 1.3f).getOrElse(SessionStore.fontStep(context)) { 1f } }
    var input by remember { mutableStateOf("") }
    var ttsReady by remember { mutableStateOf(false) }
    val tts = remember {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
            val saved = SessionStore.ttsVoice(context)
            if (ttsReady && !saved.isNullOrEmpty()) {
                engine?.voices?.firstOrNull { it.name == saved }?.let { voice -> engine?.voice = voice }
            }
        }
        engine
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
    var session by remember { mutableStateOf(initial) }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    fun rebuildFromTree() {
        val tree = session ?: return
        messages.clear()
        tree.linearPath().forEach { m ->
            messages.add(
                ChatMessage(
                    id = m.id.toString(),
                    dbId = m.id,
                    parentId = m.parentId,
                    role = m.role,
                    content = m.content,
                    thinking = m.thinking,
                    imagePath = m.imagePath,
                    filePath = m.filePath,
                    fileName = m.fileName,
                    fileMime = m.fileMime,
                    liked = m.liked,
                    done = true,
                )
            )
        }
    }
    androidx.compose.runtime.LaunchedEffect(Unit) { rebuildFromTree() }
    fun ensureSession(): LocalSession {
        session?.let { return it }
        val created = LocalSession(
            id = currentSessionId ?: java.util.UUID.randomUUID().toString(),
            title = title.ifBlank { "新对话" },
            updatedAt = System.currentTimeMillis(),
        )
        session = created
        currentSessionId = created.id
        return created
    }

    fun persistSession() {
        val tree = session ?: return
        tree.updatedAt = System.currentTimeMillis()
        val list = LocalStore.load(context)
        val index = list.indexOfFirst { it.id == tree.id }
        if (index >= 0) list[index] = tree else list.add(tree)
        LocalStore.save(context, list)
    }

    fun syncAssistant(dbId: Int, content: String? = null, thinking: String? = null, done: Boolean? = null) {
        session?.messages?.find { it.id == dbId }?.let { node ->
            if (content != null) node.content = cleanSafety(content)
            if (thinking != null) node.thinking = cleanSafety(thinking)
        }
        val displayIndex = messages.indexOfFirst { it.dbId == dbId }
        if (displayIndex >= 0) {
            messages[displayIndex] = messages[displayIndex].copy(
                content = content ?: messages[displayIndex].content,
                thinking = thinking ?: messages[displayIndex].thinking,
                done = done ?: messages[displayIndex].done,
            )
        }
    }
    var streaming by remember { mutableStateOf(false) }
    var sendSeq by remember { mutableStateOf(0) }
    var editingIndex by remember { mutableStateOf(-1) }
    var attachment by remember { mutableStateOf<PendingAttachment?>(null) }
    var quoted by remember { mutableStateOf<String?>(null) }
    var menuTarget by remember { mutableStateOf<ChatMessage?>(null) }
    var selectMode by remember { mutableStateOf(false) }
    val expandedThinking = remember { mutableStateMapOf<String, Boolean>() }
    val searchResults = remember { mutableStateMapOf<String, Pair<Int, String>>() }
    var searchDialogMessage by remember { mutableStateOf<ChatMessage?>(null) }
    val selectedIds = remember { mutableStateListOf<String>() }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val dir = File(context.filesDir, "images").apply { mkdirs() }
            val file = File(dir, System.currentTimeMillis().toString() + ".png")
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
                attachment = PendingAttachment(file.absolutePath, "image.png", "image/png", true)
            }
        }
    }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val dir = File(context.filesDir, "files").apply { mkdirs() }
            val name = runCatching {
                context.contentResolver.query(uri, null, null, null, null)?.use { c ->
                    val idx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (c.moveToFirst() && idx >= 0) c.getString(idx) else null
                }
            }.getOrNull() ?: ("file-" + System.currentTimeMillis())
            val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
            val file = File(dir, System.currentTimeMillis().toString() + "-" + name)
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
                attachment = PendingAttachment(file.absolutePath, name, mime, false)
            }
        }
    }
    var showAttachDialog by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var showKeyDialog by remember { mutableStateOf(false) }
    var keyInput by remember { mutableStateOf("") }
    var thinkingEnabled by remember { mutableStateOf(true) }
    var searchEnabled by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    androidx.compose.runtime.LaunchedEffect(messages.size, messages.lastOrNull()?.content?.length) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun toggleLike(id: String, value: Int) {
        val index = messages.indexOfFirst { it.id == id }
        if (index >= 0) {
            val current = messages[index].liked
            val liked = if (current == value) 0 else value
            messages[index] = messages[index].copy(liked = liked)
            session?.messages?.find { it.id == messages[index].dbId }?.liked = liked
            persistSession()
        }
    }

    fun continueLast() {
        if (streaming) return
        val targetIndex = messages.indexOfLast { it.role == "assistant" }
        if (targetIndex < 0) return
        val grant = DshAuth.token(context)
        val apiKey = SessionStore.apiKey(context)
        if (grant == null && apiKey == null) {
            error = "继续生成需要账号授权或 API Key"
            return
        }
        val id = messages[targetIndex].id
        val base = cleanSafety(messages[targetIndex].content)
        if (base.isEmpty()) return
        val prompt = "请紧接着下面这段内容继续写，不要重复已有内容：\n\n" + base.takeLast(1500)
        streaming = true
        sendSeq += 1
        val mySeq = sendSeq
        scope.launch {
            val onDelta: (String) -> Unit = { extra ->
                scope.launch {
                    if (mySeq == sendSeq) {
                        val index = messages.indexOfFirst { it.id == id }
                        if (index >= 0) messages[index] = messages[index].copy(content = base + cleanSafety(extra))
                    }
                }
            }
            withContext(Dispatchers.IO) {
                if (grant != null) {
                    ApiClient.streamGrantChat(grant = grant, prompt = prompt, thinking = thinkingEnabled, attachments = emptyList(), searchEnabled = false, onContent = onDelta,
                        onDone = { scope.launch { if (mySeq == sendSeq) { val i = messages.indexOfFirst { it.id == id }; if (i >= 0) messages[i] = messages[i].copy(done = true); val db = messages.getOrNull(i)?.dbId ?: 0; syncAssistant(db, content = messages.getOrNull(i)?.content); persistSession() } } },
                        onError = { msg -> scope.launch { if (mySeq == sendSeq) { val i = messages.indexOfFirst { it.id == id }; if (i >= 0) messages[i] = messages[i].copy(done = true); error = msg; persistSession() } } })
                } else if (apiKey != null) {
                    ApiClient.streamApiChat(apiKey, prompt, thinkingEnabled, onDelta,
                        onDone = { scope.launch { if (mySeq == sendSeq) { val i = messages.indexOfFirst { it.id == id }; if (i >= 0) messages[i] = messages[i].copy(done = true); val db = messages.getOrNull(i)?.dbId ?: 0; if (db != 0) syncAssistant(db, content = messages.getOrNull(i)?.content); persistSession() } } },
                        onError = { msg -> scope.launch { if (mySeq == sendSeq) { val i = messages.indexOfFirst { it.id == id }; if (i >= 0) messages[i] = messages[i].copy(done = true); error = msg; persistSession() } } })
                }
            }
            streaming = false
        }
    }

    fun send() {
        val text = input.trim()
        if ((text.isEmpty() && attachment == null) || streaming) return
        val grant = DshAuth.token(context)
        val chatToken = SessionStore.token(context)
        val apiKey = SessionStore.apiKey(context)
        if (grant == null && chatToken == null && apiKey == null) {
            showKeyDialog = true
            return
        }
        val pendingAttachment = attachment
        val pendingQuote = quoted
        quoted = null
        val outgoing = if (pendingQuote != null) "> " + pendingQuote + "\n\n" + text else text
        input = ""
        attachment = null
        error = null
        val tree = ensureSession()
        val userId = tree.nextId()
        tree.messages.add(
            LocalMessage(
                id = userId,
                parentId = tree.currentMessageId,
                role = "user",
                content = outgoing,
                imagePath = if (pendingAttachment?.isImage == true) pendingAttachment.path else "",
                filePath = if (pendingAttachment?.isImage == false) pendingAttachment.path else "",
                fileName = if (pendingAttachment?.isImage == false) pendingAttachment.name else "",
                fileMime = if (pendingAttachment?.isImage == false) pendingAttachment.mime else "",
            )
        )
        val assistantDbId = tree.nextId()
        tree.messages.add(LocalMessage(id = assistantDbId, parentId = userId, role = "assistant", content = ""))
        tree.currentMessageId = assistantDbId
        persistSession()
        rebuildFromTree()
        streaming = true
        sendSeq += 1
        val mySeq = sendSeq
        val assistantId = assistantDbId.toString()
        fun updateAssistant(block: (ChatMessage) -> ChatMessage) {
            val index = messages.indexOfFirst { it.id == assistantId }
            if (index >= 0) messages[index] = block(messages[index])
        }
        scope.launch {
            when {
                grant != null -> withContext(Dispatchers.IO) {
                    var attachments = emptyList<Pair<String, Boolean>>()
                    if (pendingAttachment != null) {
                        val bytes = runCatching { java.io.File(pendingAttachment.path).readBytes() }.getOrNull()
                        val fileId = bytes?.let { ApiClient.uploadFile(
                            grant,
                            it,
                            pendingAttachment.name,
                            pendingAttachment.mime,
                            if (pendingAttachment.isImage) null else "rich_layout_recognition",
                        ) }
                        if (fileId == null) {
                            scope.launch { updateAssistant { it.copy(done = true) }; error = "附件上传失败，请稍后重试" }
                            return@withContext
                        }
                        attachments = listOf(fileId to pendingAttachment.isImage)
                    }
                    ApiClient.streamGrantChat(
                        grant = grant,
                        prompt = outgoing.ifEmpty { "请描述这张图片" },
                        thinking = thinkingEnabled,
                        attachments = attachments,
                        searchEnabled = searchEnabled,
                        onContent = { content -> scope.launch { if (mySeq == sendSeq) updateAssistant { it.copy(content = content) } } },
                        onDone = { scope.launch { if (mySeq == sendSeq) { updateAssistant { it.copy(done = true) }; messages.firstOrNull { it.id == assistantId }?.let { m -> syncAssistant(assistantDbId, m.content, m.thinking) }; persistSession() } } },
                        onError = { msg -> scope.launch { if (mySeq == sendSeq) { updateAssistant { it.copy(done = true) }; error = msg; persistSession() } } },
                        onThinking = { t -> scope.launch { if (mySeq == sendSeq) updateAssistant { it.copy(thinking = t) } } },
                        onSearch = { count, text -> scope.launch { if (mySeq == sendSeq) searchResults[assistantId] = count to text } },
                    )
                }
                apiKey != null -> withContext(Dispatchers.IO) {
                    ApiClient.streamApiChat(
                        apiKey = apiKey,
                        prompt = outgoing,
                        thinking = true,
                        onContent = { content -> scope.launch { if (mySeq == sendSeq) updateAssistant { it.copy(content = content) } } },
                        onDone = { scope.launch { updateAssistant { it.copy(done = true) }; messages.firstOrNull { it.id == assistantId }?.let { m -> syncAssistant(assistantDbId, m.content, m.thinking) }; persistSession() } },
                        onError = { msg -> scope.launch { updateAssistant { it.copy(done = true) }; error = msg; persistSession() } },
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
                                prompt = outgoing,
                                thinkingEnabled = thinkingEnabled,
                                searchEnabled = searchEnabled,
                                onDelta = { content, thinking ->
                                    scope.launch { updateAssistant { it.copy(content = content, thinking = thinking) } }
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

    fun regenerate() {
        if (streaming) return
        val tree = session ?: return
        val current = tree.messages.firstOrNull { it.id == tree.currentMessageId } ?: return
        if (current.role != "assistant") return
        val parentId = current.parentId ?: return
        var cursor = parentId
        var prompt = ""
        while (true) {
            val node = tree.messages.firstOrNull { it.id == cursor } ?: break
            if (node.role == "user") { prompt = cleanSafety(node.content); break }
            cursor = node.parentId ?: break
        }
        if (prompt.isEmpty()) return
        val grant = DshAuth.token(context)
        val apiKey = SessionStore.apiKey(context)
        if (grant == null && apiKey == null) { error = "重新生成需要账号授权或 API Key"; return }
        val newId = tree.nextId()
        tree.messages.add(LocalMessage(id = newId, parentId = parentId, role = "assistant", content = ""))
        tree.currentMessageId = newId
        persistSession()
        rebuildFromTree()
        streaming = true
        sendSeq += 1
        val mySeq = sendSeq
        val assistantId = newId.toString()
        scope.launch {
            withContext(Dispatchers.IO) {
                val onContent: (String) -> Unit = { content ->
                    scope.launch {
                        if (mySeq == sendSeq) {
                            val i = messages.indexOfFirst { it.id == assistantId }
                            if (i >= 0) messages[i] = messages[i].copy(content = content)
                        }
                    }
                }
                val onThinking: (String) -> Unit = { t ->
                    scope.launch {
                        if (mySeq == sendSeq) {
                            val i = messages.indexOfFirst { it.id == assistantId }
                            if (i >= 0) messages[i] = messages[i].copy(thinking = t)
                        }
                    }
                }
                val onDone: () -> Unit = {
                    scope.launch {
                        if (mySeq == sendSeq) {
                            val i = messages.indexOfFirst { it.id == assistantId }
                            if (i >= 0) messages[i] = messages[i].copy(done = true)
                            messages.firstOrNull { it.id == assistantId }?.let { m -> syncAssistant(newId, m.content, m.thinking) }
                            persistSession()
                        }
                    }
                }
                val onError: (String) -> Unit = { msg ->
                    scope.launch { if (mySeq == sendSeq) { error = msg; streaming = false } }
                }
                if (grant != null) {
                    ApiClient.streamGrantChat(grant = grant, prompt = prompt, thinking = thinkingEnabled, attachments = emptyList(), searchEnabled = searchEnabled, onContent = onContent, onDone = onDone, onError = onError, onThinking = onThinking)
                } else if (apiKey != null) {
                    ApiClient.streamApiChat(apiKey = apiKey, prompt = prompt, thinking = thinkingEnabled, onContent = onContent, onDone = onDone, onError = onError, onThinking = onThinking)
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
            if (selectMode) {
                Text(
                    "已选 " + selectedIds.size + " 项",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                )
                TextButton(onClick = { selectMode = false; selectedIds.clear() }) { Text("取消") }
            } else {
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
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        ) {
            itemsIndexed(messages) { index, message ->
                if (selectMode) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        androidx.compose.material3.Checkbox(
                            checked = selectedIds.contains(message.id),
                            onCheckedChange = { if (!selectedIds.remove(message.id)) selectedIds.add(message.id) },
                        )
                    }
                }
                if (message.role == "user") {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.End) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFEAF0FE),
                            modifier = Modifier
                                .widthIn(max = 300.dp)
                                .combinedClickable(
                                    onClick = {
                                        if (selectMode) {
                                            if (!selectedIds.remove(message.id)) selectedIds.add(message.id)
                                        } else {
                                            editingIndex = index
                                            editText = message.content
                                        }
                                    },
                                    onLongClick = { if (!selectMode) menuTarget = message },
                                ),
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                if (message.imagePath.isNotEmpty()) {
                                    val bitmap = remember(message.imagePath) { android.graphics.BitmapFactory.decodeFile(message.imagePath)?.asImageBitmap() }
                                    if (bitmap != null) {
                                        Image(bitmap, contentDescription = null, modifier = Modifier.widthIn(max = 240.dp).heightIn(max = 240.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Fit)
                                        Spacer(Modifier.height(6.dp))
                                    }
                                }
                                if (message.filePath.isNotEmpty()) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
                                        Icon(Icons.Filled.Description, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(message.fileName.ifEmpty { "附件" }, color = TextPrimary, fontSize = 13.sp)
                                    }
                                }
                                if (message.content.isNotEmpty()) Text(message.content, color = TextPrimary, fontSize = (16 * fontScale).sp)
                            }
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        if (message.thinking.isNotEmpty()) {
                            val expanded = expandedThinking[message.id] == true
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { expandedThinking[message.id] = !expanded },
                            ) {
                                Text("已思考", color = Color(0xFF9AA0A9), fontSize = 14.sp)
                                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF9AA0A9), modifier = Modifier.size(16.dp))
                            }
                            if (expanded) {
                                Text(
                                    cleanSafety(message.thinking),
                                    color = Color(0xFF8A9099),
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp,
                                    modifier = Modifier.padding(vertical = 6.dp),
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                        val searchInfo = searchResults[message.id]
                        if (searchInfo != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { searchDialogMessage = message }.padding(bottom = 6.dp),
                            ) {
                                Icon(Icons.Filled.Public, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("已搜索 " + searchInfo.first + " 个网页", color = TextSecondary, fontSize = 13.sp)
                            }
                        }
                        if (message.content.isEmpty() && !message.done) {
                            Text("正在思考…", color = TextSecondary, fontSize = 15.sp)
                        } else if (message.content.isEmpty() && message.done) {
                            Text("已停止生成", color = Color(0xFF9AA0A9), fontSize = 15.sp)
                        } else {
                            Column(modifier = Modifier.combinedClickable(
                                onClick = { if (selectMode) { if (!selectedIds.remove(message.id)) selectedIds.add(message.id) } },
                                onLongClick = { if (!selectMode) menuTarget = message },
                            )) {
                                MarkdownText(cleanSafety(message.content), scale = fontScale)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MessageAction(Icons.Filled.ContentCopy) { clipboard.setText(AnnotatedString(cleanSafety(message.content))) }
                            MessageAction(Icons.Filled.ThumbUp, tint = if (message.liked == 1) BrandBlue else Color(0xFF9AA0A9)) { toggleLike(message.id, 1) }
                            MessageAction(Icons.Filled.ThumbDown, tint = if (message.liked == -1) BrandBlue else Color(0xFF9AA0A9)) { toggleLike(message.id, -1) }
                            MessageAction(Icons.Filled.VolumeUp) {
                                val speakText = cleanSafety(message.content)
                                if (speakText.isEmpty()) return@MessageAction
                                val grant = DshAuth.token(context)
                                val key = SessionStore.apiKey(context)
                                val voice = SessionStore.ttsVoice(context) ?: "stella"
                                if (grant != null || key != null) {
                                    scope.launch {
                                        val pcm = withContext(Dispatchers.IO) { ApiClient.synthesizeTts(grant, key, speakText, voice) }
                                        if (pcm != null) PcmPlayer.play(pcm) else error = "朗读失败，请稍后重试"
                                    }
                                } else if (ttsReady) {
                                    tts.language = Locale.SIMPLIFIED_CHINESE
                                    tts.speak(speakText, TextToSpeech.QUEUE_FLUSH, null, "read_aloud")
                                }
                            }
                            MessageAction(Icons.Filled.Share) {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, cleanSafety(message.content))
                                }
                                context.startActivity(Intent.createChooser(intent, "分享"))
                            }
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Filled.Refresh, contentDescription = "重新生成", tint = Color(0xFF9AA0A9), modifier = Modifier.size(20.dp).clickable { regenerate() })
                        }
                        if (message.done && index == messages.lastIndex && message.content.isNotEmpty() && !streaming) {
                            TextButton(onClick = { continueLast() }) { Text("继续生成", fontSize = 13.sp) }
                        }
                        val tree = session
                        if (tree != null && message.role == "assistant" && message.dbId != 0) {
                            val siblings = tree.messages.filter { it.parentId == message.parentId }.sortedBy { it.id }
                            if (siblings.size > 1) {
                                val pos = siblings.indexOfFirst { it.id == message.dbId } + 1
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "‹",
                                        color = TextSecondary,
                                        fontSize = 20.sp,
                                        modifier = Modifier.clickable {
                                            val idx = (pos - 2 + siblings.size) % siblings.size
                                            tree.currentMessageId = siblings[idx].id
                                            rebuildFromTree()
                                        }.padding(horizontal = 8.dp),
                                    )
                                    Text(pos.toString() + "/" + siblings.size, color = TextSecondary, fontSize = 12.sp)
                                    Text(
                                        "›",
                                        color = TextSecondary,
                                        fontSize = 20.sp,
                                        modifier = Modifier.clickable {
                                            val idx = pos % siblings.size
                                            tree.currentMessageId = siblings[idx].id
                                            rebuildFromTree()
                                        }.padding(horizontal = 8.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        if (selectMode) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val selectedMessages = messages.filter { selectedIds.contains(it.id) }
                TextButton(onClick = {
                    clipboard.setText(AnnotatedString(selectedMessages.joinToString("\n\n") { cleanSafety(it.content) }))
                }) { Text("复制") }
                TextButton(onClick = {
                    val text = selectedMessages.joinToString("\n\n") { cleanSafety(it.content) }
                    val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }
                    context.startActivity(Intent.createChooser(intent, "分享"))
                }) { Text("分享") }
                TextButton(onClick = {
                    val tree = session
                    val removedDbIds = messages.filter { selectedIds.contains(it.id) }.map { it.dbId }.toSet()
                    if (tree != null) {
                        val toRemove = mutableSetOf<Int>()
                        fun collect(id: Int) {
                            if (!toRemove.add(id)) return
                            tree.messages.filter { it.parentId == id }.forEach { collect(it.id) }
                        }
                        removedDbIds.forEach { collect(it) }
                        tree.messages.removeAll { toRemove.contains(it.id) }
                        if (tree.currentMessageId != null && toRemove.contains(tree.currentMessageId)) {
                            tree.currentMessageId = tree.messages.maxByOrNull { it.id }?.id
                        }
                        persistSession()
                    }
                    rebuildFromTree()
                    selectedIds.clear()
                    selectMode = false
                }) { Text("删除", color = Color(0xFFD93025)) }
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
                    quoted?.let { q ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("引用：" + q.replace("\n", " ").take(60), color = TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Text("×", color = TextSecondary, fontSize = 16.sp, modifier = Modifier.clickable { quoted = null }.padding(horizontal = 6.dp))
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                    attachment?.let { att ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (att.isImage) {
                                val bitmap = remember(att.path) { android.graphics.BitmapFactory.decodeFile(att.path)?.asImageBitmap() }
                                if (bitmap != null) {
                                    Image(bitmap, contentDescription = null, modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                                }
                            } else {
                                Icon(Icons.Filled.Description, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(24.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(if (att.isImage) "图片" else att.name, color = TextSecondary, fontSize = 12.sp, maxLines = 1)
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                    BasicTextField(
                        value = input,
                        onValueChange = { input = it },
                        textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = (16 * fontScale).sp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { send() }),
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
                        if (streaming || input.isNotEmpty()) {
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
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = "附件",
                                tint = TextPrimary,
                                modifier = Modifier.size(22.dp).clickable { showAttachDialog = true },
                            )
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

    if (showAttachDialog) {
        AlertDialog(
            onDismissRequest = { showAttachDialog = false },
            title = { Text("添加附件") },
            text = {
                Column {
                    TextButton(onClick = { showAttachDialog = false; imagePicker.launch("image/*") }) { Text("图片") }
                    TextButton(onClick = { showAttachDialog = false; filePicker.launch(arrayOf("*/*")) }) { Text("文件（PDF/文档等）") }
                }
            },
            confirmButton = { TextButton(onClick = { showAttachDialog = false }) { Text("取消") } },
        )
    }

    searchDialogMessage?.let { target ->
        val info = searchResults[target.id]
        AlertDialog(
            onDismissRequest = { searchDialogMessage = null },
            title = { Text("搜索来源") },
            text = {
                Column {
                    val lines = info?.second?.split("\n")?.filter { it.contains("\t") }.orEmpty()
                    lines.forEach { line ->
                        val title = line.substringBefore("\t")
                        val url = line.substringAfter("\t")
                        TextButton(onClick = {
                            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                            searchDialogMessage = null
                        }) { Text(title.take(40), fontSize = 13.sp) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { searchDialogMessage = null }) { Text("关闭") } },
        )
    }

    menuTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { menuTarget = null },
            title = { Text("消息操作") },
            text = {
                Column {
                    TextButton(onClick = { quoted = cleanSafety(target.content).take(200); menuTarget = null }) { Text("引用") }
                    TextButton(onClick = { selectMode = true; selectedIds.clear(); selectedIds.add(target.id); menuTarget = null }) { Text("多选") }
                    TextButton(onClick = { clipboard.setText(AnnotatedString(cleanSafety(target.content))); menuTarget = null }) { Text("复制") }
                }
            },
            confirmButton = { TextButton(onClick = { menuTarget = null }) { Text("取消") } },
        )
    }

    if (editingIndex >= 0) {
        AlertDialog(
            onDismissRequest = { editingIndex = -1 },
            title = { Text("编辑重发") },
            text = {
                OutlinedTextField(value = editText, onValueChange = { editText = it }, minLines = 3)
            },
            confirmButton = {
                TextButton(onClick = {
                    val prompt = editText.trim()
                    if (prompt.isNotEmpty()) {
                        val targetDbId = messages.getOrNull(editingIndex)?.dbId ?: 0
                        val tree = session
                        val node = tree?.messages?.firstOrNull { it.id == targetDbId }
                        if (tree != null && node != null) {
                            tree.currentMessageId = node.parentId
                            persistSession()
                            rebuildFromTree()
                        }
                        editingIndex = -1
                        input = prompt
                        send()
                    }
                }) { Text("重发") }
            },
            dismissButton = { TextButton(onClick = { editingIndex = -1 }) { Text("取消") } },
        )
    }
}

@Composable
private fun MessageAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color = Color(0xFF9AA0A9),
    onClick: () -> Unit = {},
) {
    Icon(
        icon,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(20.dp).clickable(onClick = onClick),
    )
    Spacer(Modifier.width(20.dp))
}

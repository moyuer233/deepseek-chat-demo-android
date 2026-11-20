package com.moyuer233.deepseeknative.data

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.util.zip.ZipInputStream

/** 导入 chat-demo 格式归档（deepseek_data-*.zip，export_format_version=3）。 */
object ArchiveImporter {
    data class Result(val sessions: List<LocalSession>, val error: String? = null)

    fun import(context: Context, uri: Uri): Result {
        return try {
            val sessions = mutableListOf<LocalSession>()
            var manifestSeen = false
            context.contentResolver.openInputStream(uri)?.use { input ->
                ZipInputStream(input.buffered()).use { zip ->
                    while (true) {
                        val entry = zip.nextEntry ?: break
                        val name = entry.name
                        if (name.endsWith("/")) continue
                        if (name == "manifest.json") {
                            val text = zip.readBytes().toString(Charsets.UTF_8)
                            val version = JSONObject(text).optInt("export_format_version", -1)
                            if (version != 3) {
                                return Result(emptyList(), "不支持的归档版本：" + version)
                            }
                            manifestSeen = true
                        } else if (name == "chat_session_index.json") {
                            val text = zip.readBytes().toString(Charsets.UTF_8)
                            JSONArray(text) // 索引存在即可，会话以 chat_sessions/*.json 为准
                        } else if (name.startsWith("chat_sessions/") && name.endsWith(".json")) {
                            val text = zip.readBytes().toString(Charsets.UTF_8)
                            parseSession(text)?.let { sessions.add(it) }
                        }
                    }
                }
            }
            if (!manifestSeen) Result(emptyList(), "不是有效的归档（缺少 manifest.json）")
            else Result(sessions)
        } catch (e: Exception) {
            Result(emptyList(), e.message ?: "导入失败")
        }
    }

    private fun parseSession(text: String): LocalSession? = try {
        val o = JSONObject(text)
        val id = o.optString("id")
        if (id.isEmpty()) null else {
            val title = o.optString("title", "").ifEmpty { "新对话" }
            val updatedAt = o.optLong("updated_at", 0L)
            val nodes = mutableMapOf<Int, JSONObject>()
            val arr = o.optJSONArray("messages") ?: JSONArray()
            for (i in 0 until arr.length()) {
                val m = arr.getJSONObject(i)
                nodes[m.optInt("id")] = m
            }
            // 从 current_message_id 沿 parent_id 回溯出主链
            val chain = mutableListOf<JSONObject>()
            var cursor = o.optInt("current_message_id", 0)
            val guard = mutableSetOf<Int>()
            while (cursor != 0 && nodes.containsKey(cursor) && guard.add(cursor)) {
                val node = nodes[cursor]!!
                chain.add(node)
                cursor = if (node.isNull("parent_id")) 0 else node.optInt("parent_id", 0)
            }
            chain.reverse()
            val messages = mutableListOf<LocalMessage>()
            for (node in chain) {
                val role = node.optString("role")
                val fragments = node.optJSONArray("fragments") ?: continue
                var content = ""
                var thinking = ""
                for (j in 0 until fragments.length()) {
                    val f = fragments.getJSONObject(j)
                    when (f.optString("type")) {
                        "REQUEST", "RESPONSE", "TEMPLATE_RESPONSE" -> content += f.optString("content")
                        "THINK" -> thinking += f.optString("content")
                    }
                }
                if (content.isNotEmpty() || thinking.isNotEmpty()) {
                    messages.add(LocalMessage(if (role == "USER") "user" else "assistant", content, thinking))
                }
            }
            LocalSession(id = id, title = title, updatedAt = updatedAt, messages = messages)
        }
    } catch (e: Exception) {
        null
    }
}

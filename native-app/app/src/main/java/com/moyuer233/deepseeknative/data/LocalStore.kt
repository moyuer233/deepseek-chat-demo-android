package com.moyuer233.deepseeknative.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class LocalMessage(
    var id: Int = 0,
    var parentId: Int? = null,
    val role: String,
    var content: String,
    var thinking: String = "",
    var imagePath: String = "",
    var liked: Int = 0,
)

data class LocalSession(
    val id: String,
    var title: String,
    var updatedAt: Long,
    var pinned: Boolean = false,
    var currentMessageId: Int? = null,
    val messages: MutableList<LocalMessage> = mutableListOf(),
) {
    /** 从当前消息回溯出主链（根→当前）。 */
    fun linearPath(): List<LocalMessage> {
        val byId = messages.associateBy { it.id }
        val chain = mutableListOf<LocalMessage>()
        var cursor = currentMessageId ?: messages.maxByOrNull { it.id }?.id
        val guard = mutableSetOf<Int>()
        while (cursor != null && byId.containsKey(cursor) && guard.add(cursor)) {
            val node = byId[cursor]!!
            chain.add(node)
            cursor = node.parentId
        }
        chain.reverse()
        return chain
    }

    fun siblings(message: LocalMessage): List<LocalMessage> =
        messages.filter { it.parentId == message.parentId }.sortedBy { it.id }

    fun nextId(): Int = (messages.maxOfOrNull { it.id } ?: 0) + 1
}

/** 自己的会话存储：filesDir/sessions.json（UI 与内核解耦，不依赖官方）。 */
object LocalStore {
    private fun file(context: Context) = File(context.filesDir, "sessions.json")

    fun load(context: Context): MutableList<LocalSession> {
        val f = file(context)
        if (!f.exists()) return mutableListOf()
        return try {
            val arr = JSONArray(f.readText())
            val out = mutableListOf<LocalSession>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val msgs = mutableListOf<LocalMessage>()
                val marr = o.optJSONArray("messages") ?: JSONArray()
                for (j in 0 until marr.length()) {
                    val m = marr.getJSONObject(j)
                    val msgId = m.optInt("id", 0)
                    val parent = if (m.has("parentId") && !m.isNull("parentId")) m.optInt("parentId") else null
                    msgs.add(
                        LocalMessage(
                            id = if (msgId != 0) msgId else j + 1,
                            parentId = parent,
                            role = m.optString("role"),
                            content = m.optString("content"),
                            thinking = m.optString("thinking"),
                            imagePath = m.optString("imagePath"),
                            liked = m.optInt("liked", 0),
                        )
                    )
                }
                // 旧线性数据迁移：补 parentId
                if (msgs.isNotEmpty() && msgs.all { it.parentId == null && it.id > 0 }) {
                    var prev: Int? = null
                    for (m in msgs.sortedBy { it.id }) { m.parentId = prev; prev = m.id }
                }
                val current = if (o.has("currentMessageId") && !o.isNull("currentMessageId")) o.optInt("currentMessageId") else msgs.maxByOrNull { it.id }?.id
                out.add(LocalSession(o.optString("id"), o.optString("title"), o.optLong("updatedAt"), o.optBoolean("pinned", false), current, msgs))
            }
            out
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    fun save(context: Context, sessions: List<LocalSession>) {
        val arr = JSONArray()
        for (s in sessions) {
            val marr = JSONArray()
            for (m in s.messages) {
                marr.put(
                    JSONObject()
                        .put("id", m.id)
                        .put("parentId", m.parentId ?: JSONObject.NULL)
                        .put("role", m.role)
                        .put("content", m.content)
                        .put("thinking", m.thinking)
                        .put("imagePath", m.imagePath)
                        .put("liked", m.liked)
                )
            }
            arr.put(
                JSONObject()
                    .put("id", s.id)
                    .put("title", s.title)
                    .put("updatedAt", s.updatedAt)
                    .put("pinned", s.pinned)
                    .put("currentMessageId", s.currentMessageId ?: JSONObject.NULL)
                    .put("messages", marr)
            )
        }
        file(context).writeText(arr.toString())
    }

    fun clear(context: Context) {
        file(context).delete()
    }
}

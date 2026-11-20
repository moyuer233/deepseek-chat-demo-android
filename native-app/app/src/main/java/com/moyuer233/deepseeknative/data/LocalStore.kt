package com.moyuer233.deepseeknative.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class LocalMessage(
    val role: String,
    var content: String,
    var thinking: String = "",
    var imagePath: String = "",
)

data class LocalSession(
    val id: String,
    var title: String,
    var updatedAt: Long,
    val messages: MutableList<LocalMessage> = mutableListOf(),
)

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
                    msgs.add(LocalMessage(m.optString("role"), m.optString("content"), m.optString("thinking"), m.optString("imagePath")))
                }
                out.add(LocalSession(o.optString("id"), o.optString("title"), o.optLong("updatedAt"), msgs))
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
                marr.put(JSONObject().put("role", m.role).put("content", m.content).put("thinking", m.thinking).put("imagePath", m.imagePath))
            }
            arr.put(JSONObject().put("id", s.id).put("title", s.title).put("updatedAt", s.updatedAt).put("messages", marr))
        }
        file(context).writeText(arr.toString())
    }

    fun clear(context: Context) {
        file(context).delete()
    }
}

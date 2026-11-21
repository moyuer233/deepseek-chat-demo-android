package com.moyuer233.deepseeknative.data

import android.content.Context
import org.json.JSONObject

/** 引导里从 chat.deepseek.com 导出的归档：存起来，进入 chat-demo 后交给 Web 端导入。 */
object PendingArchive {
    private const val PREFS = "pending-archive"

    fun save(context: Context, path: String, name: String, bytes: Long) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("path", path)
            .putString("name", name)
            .putLong("bytes", bytes)
            .apply()
    }

    /** 读取但不清空；导入成功后再调用 clear，失败可重试。 */
    fun peek(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val path = prefs.getString("path", null) ?: return ""
        val name = prefs.getString("name", "deepseek_data.zip") ?: "deepseek_data.zip"
        val bytes = prefs.getLong("bytes", 0)
        return JSONObject()
            .put("path", path)
            .put("name", name)
            .put("bytes", bytes)
            .toString()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }
}

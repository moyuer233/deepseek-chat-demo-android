package com.moyuer233.deepseeknative.data

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** 导出自己的归档（deepseek_data-日期.zip，format_version=3），走系统分享。 */
object ArchiveExporter {
    fun export(context: Context): File {
        val sessions = LocalStore.load(context)
        val dir = File(context.cacheDir, "export").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val file = File(dir, "deepseek_data-" + stamp + ".zip")
        ZipOutputStream(file.outputStream().buffered()).use { zip ->
            zip.putNextEntry(ZipEntry("manifest.json"))
            zip.write(JSONObject().put("export_format_version", 3).toString().toByteArray())
            zip.closeEntry()
            val index = JSONArray()
            for (s in sessions) {
                val messages = JSONArray()
                for ((i, m) in s.messages.withIndex()) {
                    val id = if (m.id != 0) m.id else i + 1
                    val parentId: Int? = m.parentId
                    val fragments = JSONArray()
                    when {
                        m.role == "user" -> fragments.put(JSONObject().put("type", "REQUEST").put("content", m.content))
                        else -> {
                            if (m.thinking.isNotEmpty()) fragments.put(JSONObject().put("type", "THINK").put("content", m.thinking).put("elapsed_secs", 0))
                            fragments.put(JSONObject().put("type", "RESPONSE").put("content", m.content))
                        }
                    }
                    messages.put(
                        JSONObject()
                            .put("id", id)
                            .put("parent_id", parentId ?: JSONObject.NULL)
                            .put("role", if (m.role == "user") "USER" else "ASSISTANT")
                            .put("status", "FINISHED")
                            .put("thinking_enabled", false)
                            .put("search_enabled", false)
                            .put("accumulated_token_usage", 0)
                            .put("inserted_at", "2026-01-01T00:00:00Z")
                            .put("fragments", fragments)
                    )
                }
                val sessionJson = JSONObject()
                    .put("id", s.id)
                    .put("title", s.title)
                    .put("pinned", s.pinned)
                    .put("current_message_id", s.currentMessageId ?: s.messages.maxOfOrNull { it.id } ?: 0)
                    .put("inserted_at", "2026-01-01T00:00:00Z")
                    .put("updated_at", "2026-01-01T00:00:00Z")
                    .put("messages", messages)
                zip.putNextEntry(ZipEntry("chat_sessions/" + s.id + ".json"))
                zip.write(sessionJson.toString().toByteArray())
                zip.closeEntry()
                index.put(
                    JSONObject()
                        .put("id", s.id)
                        .put("title", s.title)
                        .put("pinned", s.pinned)
                        .put("message_count", s.messages.size)
                        .put("inserted_at", "2026-01-01T00:00:00Z")
                        .put("updated_at", "2026-01-01T00:00:00Z")
                )
            }
            zip.putNextEntry(ZipEntry("chat_session_index.json"))
            zip.write(index.toString().toByteArray())
            zip.closeEntry()
        }
        return file
    }

    fun share(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "分享历史归档"))
    }
}

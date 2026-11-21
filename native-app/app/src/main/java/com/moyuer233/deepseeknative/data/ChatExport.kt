package com.moyuer233.deepseeknative.data

import com.moyuer233.deepseeknative.NativeCopy
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.TimeZone

/** 用 chat.deepseek.com 的 userToken 触发官方导出、轮询并下载 ZIP。 */
object ChatExport {
    private const val CHAT_ORIGIN = "https://chat.deepseek.com"
    /** 导出任务轮询间隔 */
    private const val POLL_INTERVAL_MS = 5000L
    /** 等待官方导出完成的上限 */
    private const val EXPORT_TIMEOUT_MS = 15 * 60 * 1000L

    data class Result(val file: File, val fileName: String, val bytes: Long)

    fun exportAndDownload(chatToken: String, cacheDir: File): Result {
        val dir = File(cacheDir, "export").apply { mkdirs() }
        val target = File(dir, "deepseek_data-" + System.currentTimeMillis() + ".zip")
        try {
            request(CHAT_ORIGIN + "/api/v0/export_all", chatToken)
            val url = pollDownloadUrl(chatToken)
            val bytes = download(url, target, chatToken)
            return Result(target, "deepseek_data.zip", bytes)
        } catch (e: Exception) {
            // 失败时不留半个 ZIP，避免 cache/export 越积越大
            target.delete()
            throw e
        }
    }

    /**
     * 轮询导出任务直到拿到下载地址。
     * EMPTY / FAILED 立即报错，不空等满 15 分钟。
     */
    private fun pollDownloadUrl(chatToken: String): String {
        val deadline = System.currentTimeMillis() + EXPORT_TIMEOUT_MS
        while (System.currentTimeMillis() < deadline) {
            val text = request(CHAT_ORIGIN + "/api/v0/download_export_history", chatToken)
            val json = JSONObject(text)
            val biz = json.optJSONObject("data")?.optJSONObject("biz_data")
            val status = biz?.optString("status", "").orEmpty()
            if (status == "FINISHED") {
                var url = biz?.optString("history_download_url", "").orEmpty()
                if (url.isEmpty()) {
                    url = biz?.optString("legacy_history_download_url", "").orEmpty()
                }
                if (url.isNotEmpty()) {
                    return url
                }
            } else if (status == "EMPTY") {
                throw IllegalStateException(NativeCopy.chatExportEmpty)
            } else if (status == "FAILED") {
                throw IllegalStateException(NativeCopy.chatExportFailed)
            }
            Thread.sleep(POLL_INTERVAL_MS)
        }
        throw IllegalStateException(NativeCopy.chatExportTimeout)
    }

    private fun request(url: String, token: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 20000
        conn.readTimeout = 60000
        conn.setRequestProperty("Accept", "application/json")
        conn.setRequestProperty("Authorization", "Bearer " + token)
        conn.setRequestProperty("x-client-bundle-id", "com.deepseek.chat")
        conn.setRequestProperty("x-client-platform", "web")
        conn.setRequestProperty("x-client-version", "2.3.0")
        conn.setRequestProperty("x-client-locale", Locale.getDefault().toLanguageTag())
        conn.setRequestProperty("x-client-timezone-offset", (TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 1000).toString())
        val status = conn.responseCode
        val stream: InputStream? = if (status in 200..299) conn.inputStream else conn.errorStream
        val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        conn.disconnect()
        if (status !in 200..299) throw IllegalStateException("HTTP " + status + " " + text.take(200))
        return text
    }

    private fun download(url: String, target: File, token: String): Long {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 20000
        conn.readTimeout = 120000
        conn.instanceFollowRedirects = true
        val parsed = java.net.URI(url)
        if ("https".equals(parsed.scheme, true) && "chat.deepseek.com".equals(parsed.host, true)) {
            conn.setRequestProperty("Authorization", "Bearer " + token)
        }
        val status = conn.responseCode
        if (status !in 200..299) throw IllegalStateException(NativeCopy.downloadFailedHttp + status)
        var total = 0L
        conn.inputStream.use { input ->
            FileOutputStream(target).use { out ->
                val buffer = ByteArray(64 * 1024)
                while (true) {
                    val read = input.read(buffer)
                    if (read == -1) break
                    out.write(buffer, 0, read)
                    total += read
                }
            }
        }
        conn.disconnect()
        return total
    }
}

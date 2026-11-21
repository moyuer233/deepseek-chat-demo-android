package com.moyuer233.deepseeknative.data

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

    data class Result(val file: File, val fileName: String, val bytes: Long)

    fun exportAndDownload(chatToken: String, cacheDir: File): Result {
        request(CHAT_ORIGIN + "/api/v0/export_all", chatToken)
        var downloadUrl: String? = null
        val deadline = System.currentTimeMillis() + 15 * 60 * 1000L
        while (System.currentTimeMillis() < deadline) {
            val text = request(CHAT_ORIGIN + "/api/v0/download_export_history", chatToken)
            val json = JSONObject(text)
            val data = json.optJSONObject("data")
            val biz = data?.optJSONObject("biz_data")
            val status = biz?.optString("status", "").orEmpty()
            if (status == "FINISHED") {
                var url = biz?.optString("history_download_url", "").orEmpty()
                if (url.isEmpty()) url = biz?.optString("legacy_history_download_url", "").orEmpty()
                if (url.isNotEmpty()) {
                    downloadUrl = url
                    break
                }
            }
            Thread.sleep(5000)
        }
        val url = downloadUrl ?: throw IllegalStateException("导出超时，请稍后重试")
        val dir = File(cacheDir, "export").apply { mkdirs() }
        val target = File(dir, "deepseek_data-" + System.currentTimeMillis() + ".zip")
        val bytes = download(url, target, chatToken)
        return Result(target, "deepseek_data.zip", bytes)
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
        if (status !in 200..299) throw IllegalStateException("下载失败 HTTP " + status)
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

package com.moyuer233.deepseeknative.data

import com.moyuer233.deepseeknative.BuildConfig
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateResult(
    val currentVersion: String,
    val latestVersion: String?,
    val error: String?,
    val releaseUrl: String?,
)

/** 用 GitHub Releases 做本地版本检查（native 工程自己的发版仓库）。 */
object UpdateChecker {
    private const val API = "https://api.github.com/repos/moyuer233/deepseek-chat-demo-android/releases/latest"

    fun check(): UpdateResult {
        val current = BuildConfig.VERSION_NAME
        return try {
            val conn = (URL(API).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000
                setRequestProperty("accept", "application/vnd.github+json")
                setRequestProperty("user-agent", "DeepSeekNative/" + current)
            }
            val code = conn.responseCode
            val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            conn.disconnect()
            if (code !in 200..299) {
                return UpdateResult(current, null, "检查失败（HTTP " + code + "）", null)
            }
            val json = JSONObject(text)
            val tag = json.optString("tag_name", "").trimStart('v', 'V')
            val url = json.optString("html_url", "")
            if (tag.isEmpty()) UpdateResult(current, null, "检查失败（无版本信息）", null)
            else UpdateResult(current, tag, null, url)
        } catch (e: Exception) {
            UpdateResult(current, null, "检查失败：" + (e.message ?: "网络错误"), null)
        }
    }
}

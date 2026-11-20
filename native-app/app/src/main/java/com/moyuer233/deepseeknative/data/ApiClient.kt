package com.moyuer233.deepseeknative.data

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

data class LoginResult(val ok: Boolean, val token: String?, val message: String?)

/** chat.deepseek.com 的最小 JSON 客户端；登录与会话接口共用。 */
object ApiClient {
    private const val BASE = "https://chat.deepseek.com"

    fun post(path: String, body: JSONObject, token: String? = null): JSONObject {
        val conn = (URL(BASE + path).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15000
            readTimeout = 30000
            doOutput = true
            setRequestProperty("content-type", "application/json")
            setRequestProperty("origin", BASE)
            setRequestProperty("referer", "$BASE/")
            setRequestProperty("user-agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36")
            if (token != null) setRequestProperty("authorization", "Bearer $token")
        }
        conn.outputStream.use { it.write(body.toString().toByteArray()) }
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val text = stream?.bufferedReader()?.use(BufferedReader::readText).orEmpty()
        conn.disconnect()
        if (code !in 200..299) return JSONObject().put("http_code", code).put("raw", text)
        return JSONObject(text)
    }

    private fun deviceId(): String = UUID.randomUUID().toString()

    fun loginWithPassword(mobile: String, password: String): LoginResult {
        val body = JSONObject()
            .put("mobile", mobile)
            .put("password", password)
            .put("device_id", deviceId())
            .put("os", "android")
        return try {
            val json = post("/api/v0/users/login", body)
            val data = json.optJSONObject("data")
            val biz = data?.optJSONObject("biz_data")
            val bizCode = data?.optInt("biz_code", -1) ?: -1
            if (bizCode == 0 && biz != null) {
                LoginResult(true, biz.optString("token", null), null)
            } else {
                LoginResult(false, null, data?.optString("biz_msg") ?: json.optString("raw", "登录失败"))
            }
        } catch (e: Exception) {
            LoginResult(false, null, e.message)
        }
    }

    fun sendSmsCode(mobile: String): Result<String> = try {
        val body = JSONObject().put("mobile", mobile).put("area_code", "+86").put("scenario", "login")
        val json = post("/api/v0/users/create_sms_verification_code", body)
        val data = json.optJSONObject("data")
        if (data?.optInt("biz_code", -1) == 0) Result.success("sent")
        else Result.failure(IllegalStateException(data?.optString("biz_msg") ?: json.optString("msg", "发送失败")))
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun fetchSessions(token: String): Result<List<JSONObject>> = try {
        val json = post("/api/v0/chat_session/fetch_page", JSONObject().put("after_id", 0).put("count", 50), token)
        val list = json.optJSONObject("data")?.optJSONObject("biz_data")?.optJSONArray("chat_sessions")
        val sessions = mutableListOf<JSONObject>()
        if (list != null) for (i in 0 until list.length()) sessions.add(list.getJSONObject(i))
        Result.success(sessions)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

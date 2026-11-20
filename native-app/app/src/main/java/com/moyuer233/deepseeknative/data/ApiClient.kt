package com.moyuer233.deepseeknative.data

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

data class LoginResult(val ok: Boolean, val token: String?, val message: String?)

/** chat.deepseek.com 的最小 JSON 客户端；登录与会话接口共用。 */
object ApiClient {
    private const val BASE = "https://chat.deepseek.com"
    private const val UA = "Mozilla/5.0 (Linux; Android 13; Pixel 6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"

    private fun post(path: String, body: JSONObject, token: String? = null, headers: Map<String, String> = emptyMap()): JSONObject {
        val conn = (URL(BASE + path).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15000
            readTimeout = 30000
            doOutput = true
            setRequestProperty("content-type", "application/json")
            setRequestProperty("origin", BASE)
            setRequestProperty("referer", "$BASE/")
            setRequestProperty("user-agent", UA)
            if (token != null) setRequestProperty("authorization", "Bearer $token")
            headers.forEach { (k, v) -> setRequestProperty(k, v) }
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

    private data class GuestChallenge(val salt: String, val challenge: String, val difficulty: Int, val targetPath: String, val expireAt: Long)

    private fun fetchChallenge(targetPath: String): GuestChallenge? {
        val json = post("/api/v0/users/create_guest_challenge", JSONObject().put("target_path", targetPath))
        val gc = json.optJSONObject("data")?.optJSONObject("biz_data")?.optJSONObject("guest_challenge") ?: return null
        return GuestChallenge(
            salt = gc.optString("salt"),
            challenge = gc.optString("challenge"),
            difficulty = gc.optInt("difficulty", 0),
            targetPath = gc.optString("target_path", targetPath),
            expireAt = gc.optLong("expire_at", 0),
        )
    }

    private fun guestPowHeader(targetPath: String): String? {
        val gc = fetchChallenge(targetPath) ?: return null
        val prefix = gc.salt + "_" + gc.expireAt + "_"
        val answer = PowSolver.solve(prefix, gc.challenge, gc.difficulty)
        if (answer < 0) return null
        return PowSolver.guestHeader(gc.salt, answer)
    }

    fun loginWithPassword(mobile: String, password: String): LoginResult {
        val body = JSONObject()
            .put("mobile", mobile)
            .put("password", password)
            .put("device_id", deviceId())
            .put("os", "android")
        return try {
            val json = post("/api/v0/users/login", body)
            parseLogin(json)
        } catch (e: Exception) {
            LoginResult(false, null, e.message)
        }
    }

    fun sendSmsCode(mobile: String, areaCode: String = "+86"): Result<String> = try {
        val target = "/api/v0/users/create_sms_verification_code"
        val pow = guestPowHeader(target) ?: return Result.failure(IllegalStateException("PoW 求解失败"))
        val body = JSONObject()
            .put("mobile_number", mobile)
            .put("area_code", areaCode)
            .put("scenario", "login")
            .put("device_id", deviceId())
            .put("locale", "zh_CN")
        val json = post(target, body, headers = mapOf("x-ds-guest-pow-response" to pow))
        val biz = json.optJSONObject("data")
        val bizMsg = biz?.optString("biz_msg") ?: ""
        if (biz?.optInt("biz_code", -1) == 0) Result.success("sent")
        else Result.failure(IllegalStateException(describeBizError(bizMsg)))
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun loginBySms(mobile: String, code: String, areaCode: String = "+86"): LoginResult = try {
        val target = "/api/v0/users/login_by_mobile_sms"
        val pow = guestPowHeader(target) ?: return LoginResult(false, null, "PoW 求解失败")
        val body = JSONObject()
            .put("mobile_number", mobile)
            .put("area_code", areaCode)
            .put("sms_verification_code", code)
            .put("device_id", deviceId())
            .put("os", "android")
        val json = post(target, body, headers = mapOf("x-ds-guest-pow-response" to pow))
        parseLogin(json)
    } catch (e: Exception) {
        LoginResult(false, null, e.message)
    }

    private fun parseLogin(json: JSONObject): LoginResult {
        val data = json.optJSONObject("data")
        val biz = data?.optJSONObject("biz_data")
        val bizCode = data?.optInt("biz_code", -1) ?: -1
        if (bizCode == 0 && biz != null) {
            val token = biz.optString("token", "")
            if (token.isNotEmpty()) return LoginResult(true, token, null)
            return LoginResult(false, null, "登录响应缺少 token")
        }
        return LoginResult(false, null, describeBizError(data?.optString("biz_msg") ?: json.optString("msg", "登录失败")))
    }

    private fun describeBizError(code: String?): String = when (code) {
        null, "" -> "未知错误"
        "PASSWORD_OR_USER_NAME_IS_WRONG" -> "Password is invalid. Please try again."
        "SMS_EXPIRED" -> "Verification code expired. Please request a new one."
        "SMS_CODE_ERROR" -> "Incorrect verification code."
        "RECAPTCHA_VERIFY_FAILED" -> "需要完成人机验证后才能发送验证码"
        "MOBILE_NUMBER_REQUIRED" -> "Mobile number is required."
        else -> code
    }

    fun createSession(token: String): Result<String> = try {
        val json = post("/api/v0/chat_session/create", JSONObject(), token)
        val id = json.optJSONObject("data")?.optJSONObject("biz_data")?.optJSONObject("chat_session")?.optString("id", "")
        if (id.isNullOrEmpty()) Result.failure(IllegalStateException(describeBizError(json.optJSONObject("data")?.optString("biz_msg"))))
        else Result.success(id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun powHeaderForChat(token: String, targetPath: String): String? {
        val json = post("/api/v0/chat/create_pow_challenge", JSONObject().put("target_path", targetPath), token)
        val data = json.optJSONObject("data") ?: return null
        if (data.optInt("biz_code", -1) != 0) return null
        val c = data.optJSONObject("biz_data")?.optJSONObject("challenge") ?: return null
        val salt = c.optString("salt")
        val challenge = c.optString("challenge")
        val difficulty = c.optInt("difficulty", 0)
        val expireAt = c.optLong("expire_at", 0)
        val algorithm = c.optString("algorithm", "DeepSeekHashV1")
        val signature = c.optString("signature", "")
        val targetPathResp = c.optString("target_path", targetPath)
        val answer = PowSolver.solve(salt + "_" + expireAt + "_", challenge, difficulty)
        if (answer < 0) return null
        val payload = JSONObject()
            .put("algorithm", algorithm)
            .put("challenge", challenge)
            .put("salt", salt)
            .put("answer", answer)
            .put("signature", signature)
            .put("target_path", targetPathResp)
        return android.util.Base64.encodeToString(payload.toString().toByteArray(), android.util.Base64.NO_WRAP)
    }

    @Volatile
    private var streamConn: HttpURLConnection? = null
    @Volatile
    private var stopRequested = false

    fun stopStream() {
        stopRequested = true
        try { streamConn?.disconnect() } catch (_: Exception) {}
    }

    /** 流式补全：把累计正文（和思考）通过回调抛出，阻塞直到流结束。 */
    fun streamCompletion(
        token: String,
        sessionId: String,
        prompt: String,
        thinkingEnabled: Boolean,
        searchEnabled: Boolean,
        onDelta: (content: String, thinking: String) -> Unit,
        onDone: () -> Unit,
        onError: (String) -> Unit,
    ) {
        try {
            val pow = powHeaderForChat(token, "/api/v0/chat/completion")
            if (pow == null) { onError("PoW 求解失败"); return }
            val conn = (URL(BASE + "/api/v0/chat/completion").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15000
                readTimeout = 120000
                doOutput = true
                setRequestProperty("content-type", "application/json")
                setRequestProperty("accept", "text/event-stream")
                setRequestProperty("origin", BASE)
                setRequestProperty("referer", "$BASE/")
                setRequestProperty("user-agent", UA)
                setRequestProperty("authorization", "Bearer $token")
                setRequestProperty("x-ds-pow-response", pow)
            }
            val body = JSONObject()
                .put("chat_session_id", sessionId)
                .put("parent_message_id", JSONObject.NULL)
                .put("model_type", "")
                .put("prompt", prompt)
                .put("ref_file_ids", org.json.JSONArray())
                .put("thinking_enabled", thinkingEnabled)
                .put("search_enabled", searchEnabled)
                .put("source", "chat")
                .put("action", JSONObject.NULL)
                .put("preempt", false)
            conn.outputStream.use { it.write(body.toString().toByteArray()) }
            streamConn = conn
            val code = conn.responseCode
            if (code !in 200..299) {
                val err = conn.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                onError("请求失败（HTTP " + code + "）：" + err.take(200))
                return
            }
            var content = ""
            var thinking = ""
            conn.inputStream.bufferedReader().useLines { lines ->
                for (line in lines) {
                    if (!line.startsWith("data:")) continue
                    val payload = line.removePrefix("data:").trim()
                    if (payload.isEmpty() || payload == "[DONE]") continue
                    val obj = runCatching { JSONObject(payload) }.getOrNull() ?: continue
                    val resp = obj.optJSONObject("v")?.optJSONObject("response") ?: continue
                    val newContent = resp.optString("content", "")
                    val newThinking = resp.optString("thinking_content", "")
                    if (newContent.isNotEmpty()) {
                        content = mergeStream(content, newContent)
                        onDelta(content, thinking)
                    }
                    if (newThinking.isNotEmpty()) {
                        thinking = mergeStream(thinking, newThinking)
                        onDelta(content, thinking)
                    }
                }
            }
            onDone()
        } catch (e: Exception) {
            onError(e.message ?: "网络错误")
        } finally {
            streamConn = null
        }
    }

    /** 服务端可能给全量或增量文本：以「新值是否以上一次开头」判断并合并。 */
    private fun mergeStream(prev: String, next: String): String =
        if (next.startsWith(prev)) next else prev + next

    /** 去掉服务端安全分类标签（<ds_safety>...</ds_safety>）及其后的 Safe 标记。 */
    private fun sanitizeStreamText(raw: String): String {
        var text = raw.replace(Regex("<ds_safety>[\\s\\S]*?</ds_safety>"), "")
        val open = text.indexOf("<ds_safety>")
        if (open >= 0) text = text.substring(0, open)
        text = text.replace(Regex("</?ds_safety>"), "")
        text = text.replace(Regex("\\bSafe\\s*$"), "")
        return text.trimEnd()
    }

    private fun sseStream(
        url: String,
        headers: Map<String, String>,
        body: JSONObject,
        onContent: (String) -> Unit,
        onDone: () -> Unit,
        onError: (String) -> Unit,
    ) {
        stopRequested = false
        try {
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15000
                readTimeout = 120000
                doOutput = true
                setRequestProperty("content-type", "application/json")
                setRequestProperty("accept", "text/event-stream")
                headers.forEach { (k, v) -> setRequestProperty(k, v) }
            }
            conn.outputStream.use { it.write(body.toString().toByteArray()) }
            streamConn = conn
            val code = conn.responseCode
            if (code !in 200..299) {
                val err = conn.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                onError("HTTP " + code + "：" + err.take(300))
                return
            }
            var content = ""
            conn.inputStream.bufferedReader().useLines { lines ->
                for (line in lines) {
                    if (!line.startsWith("data:")) continue
                    val payload = line.removePrefix("data:").trim()
                    if (payload.isEmpty() || payload == "[DONE]") continue
                    android.util.Log.d("ChatSSE", payload.take(400))
                    val obj = runCatching { JSONObject(payload) }.getOrNull() ?: continue
                    // OpenAI 风格
                    val delta = obj.optJSONArray("choices")?.optJSONObject(0)?.optJSONObject("delta")?.optString("content", "")
                    // Anthropic 风格
                    val anth = if (obj.optString("type") == "content_block_delta")
                        obj.optJSONObject("delta")?.optString("text", "") else ""
                    val piece = delta?.takeIf { it.isNotEmpty() } ?: anth
                    if (!piece.isNullOrEmpty()) {
                        content += piece
                        onContent(sanitizeStreamText(content))
                    }
                }
            }
            onDone()
        } catch (e: Exception) {
            if (stopRequested) onDone() else onError(e.message ?: "网络错误")
        } finally {
            streamConn = null
            stopRequested = false
        }
    }

    /** 用 DeepSeek API Key 聊天（api.deepseek.com，OpenAI 兼容）。 */
    fun streamApiChat(
        apiKey: String,
        prompt: String,
        thinking: Boolean,
        onContent: (String) -> Unit,
        onDone: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val body = JSONObject()
            .put("model", if (thinking) "deepseek-reasoner" else "deepseek-chat")
            .put("stream", true)
            .put("messages", org.json.JSONArray().put(JSONObject().put("role", "user").put("content", prompt)))
        sseStream(
            url = "https://api.deepseek.com/chat/completions",
            headers = mapOf("authorization" to "Bearer " + apiKey),
            body = body,
            onContent = onContent,
            onDone = onDone,
            onError = onError,
        )
    }

    /** 用 DSH grant 聊天（api.deepseek.com anthropic 兼容）。 */
    fun streamGrantChat(
        grant: String,
        prompt: String,
        thinking: Boolean,
        onContent: (String) -> Unit,
        onDone: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val body = JSONObject()
            .put("model", if (thinking) "deepseek-reasoner" else "deepseek-chat")
            .put("max_tokens", 4096)
            .put("stream", true)
            .put("messages", org.json.JSONArray().put(JSONObject().put("role", "user").put("content", prompt)))
        sseStream(
            url = "https://api.deepseek.com/anthropic/v1/messages",
            headers = mapOf("x-dsh-auth-token" to grant, "anthropic-version" to "2023-06-01"),
            body = body,
            onContent = onContent,
            onDone = onDone,
            onError = onError,
        )
    }

    fun fetchSessions(token: String): Result<List<JSONObject>> = try {
        val json = post("/api/v0/chat_session/fetch_page", JSONObject().put("after_id", 0).put("count", 50), token)
        val list: JSONArray? = json.optJSONObject("data")?.optJSONObject("biz_data")?.optJSONArray("chat_sessions")
        val sessions = mutableListOf<JSONObject>()
        if (list != null) for (i in 0 until list.length()) sessions.add(list.getJSONObject(i))
        Result.success(sessions)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

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

    fun sendSmsCode(
        mobile: String,
        areaCode: String = "+86",
        shumeiRid: String? = null,
        shumeiRegion: String = "CN",
    ): Result<String> = try {
        val target = "/api/v0/users/create_sms_verification_code"
        val pow = guestPowHeader(target) ?: return Result.failure(IllegalStateException("PoW 求解失败"))
        val body = JSONObject()
            .put("mobile_number", mobile)
            .put("area_code", areaCode)
            .put("scenario", "login")
            .put("device_id", deviceId())
            .put("locale", "zh_CN")
        if (shumeiRid != null) {
            body.put("shumei_verification", JSONObject().put("region", shumeiRegion).put("rid", shumeiRid))
        }
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

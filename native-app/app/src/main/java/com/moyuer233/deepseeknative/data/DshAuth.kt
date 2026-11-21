package com.moyuer233.deepseeknative.data

import android.content.Context
import android.util.Base64
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

/** DSH 账号授权：auth_init → 浏览器授权 → auth_exchange → account grant。 */
object DshAuth {
    const val PLATFORM = "https://platform.deepseek.com"
    const val REDIRECT_URI = "http://127.0.0.1:19876/oauth/callback"
    private const val PREFS = "dsh-auth"
    private const val KEY_TOKEN = "token"
    private const val KEY_VERIFIER = "verifier"
    private const val KEY_STATE = "state"
    private const val KEY_DEVICE = "device_id"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun token(context: Context): String? =
        SecurePrefs.get(context, PREFS, KEY_TOKEN)?.takeIf { it.isNotEmpty() }

    fun clear(context: Context) {
        prefs(context).edit().remove(KEY_TOKEN).apply()
    }

    private fun base64Url(bytes: ByteArray): String =
        Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)

    private fun randomUrlSafe(bytes: Int): String {
        val buffer = ByteArray(bytes)
        SecureRandom().nextBytes(buffer)
        return base64Url(buffer)
    }

    private fun challenge(verifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return base64Url(digest.digest(verifier.toByteArray(StandardCharsets.US_ASCII)))
    }

    private fun clientHeaders(): Map<String, String> = mapOf(
        "content-type" to "application/json",
        "accept" to "application/json, text/plain, */*",
        "origin" to PLATFORM,
        "referer" to PLATFORM + "/dsh/authorize",
        "user-agent" to "Mozilla/5.0 (Linux; Android 16) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36",
        "x-client-bundle-id" to "",
        "x-client-platform" to "web",
        "x-client-version" to "0.1.0",
        "x-client-locale" to "zh_CN",
        "x-client-timezone-offset" to (java.util.TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 1000).toString(),
    )

    private fun post(url: String, body: JSONObject): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20000
            readTimeout = 30000
            doOutput = true
            clientHeaders().forEach { (k, v) -> setRequestProperty(k, v) }
        }
        conn.outputStream.use { it.write(body.toString().toByteArray()) }
        val code = conn.responseCode
        val text = (if (code >= 400) conn.errorStream else conn.inputStream)
            ?.bufferedReader()?.use { it.readText() }.orEmpty()
        conn.disconnect()
        return code.toString() + "\n" + text
    }

    /** 第一步：拿 authorize_url；verifier/state 留到 exchange。 */
    fun init(context: Context): JSONObject {
        val verifier = randomUrlSafe(32)
        val state = randomUrlSafe(16)
        val body = JSONObject()
            .put("code_challenge", challenge(verifier))
            .put("code_challenge_method", "S256")
            .put("state", state)
            .put("redirect_uri", REDIRECT_URI)
            .put("locale", "zh_CN")
            .put("login_source", "desktop")
        val raw = post(PLATFORM + "/auth-api/v0/dsh/auth_init", body)
        val payload = raw.substringAfter('\n')
        val json = JSONObject(payload)
        val bizData = json.optJSONObject("data")?.optJSONObject("biz_data")
            ?: throw IllegalStateException("auth_init: " + payload)
        prefs(context).edit().putString(KEY_VERIFIER, verifier).putString(KEY_STATE, state).apply()
        return JSONObject()
            .put("authorizeUrl", bizData.getString("authorize_url"))
            .put("authorizeId", bizData.getString("authorize_id"))
            .put("expiresIn", bizData.optInt("expires_in", 600))
    }

    /** 第三步：用回调 code 换 token。 */
    fun exchange(context: Context, code: String, state: String?): String {
        val store = prefs(context)
        val verifier = store.getString(KEY_VERIFIER, "") ?: ""
        val expectedState = store.getString(KEY_STATE, "") ?: ""
        if (verifier.isEmpty() || expectedState.isEmpty()) throw IllegalStateException("no pending authorization")
        if (state == null || expectedState != state) throw IllegalStateException("state mismatch")
        var deviceId = store.getString(KEY_DEVICE, "") ?: ""
        if (deviceId.isEmpty()) {
            deviceId = UUID.randomUUID().toString()
            store.edit().putString(KEY_DEVICE, deviceId).apply()
        }
        val body = JSONObject()
            .put("code", code)
            .put("code_verifier", verifier)
            .put("redirect_uri", REDIRECT_URI)
            .put("device_id", deviceId)
            .put("device_model", "android-arm64")
            .put("os_version", "Android " + android.os.Build.VERSION.RELEASE)
        val raw = post(PLATFORM + "/auth-api/v0/dsh/auth_exchange", body)
        val payload = raw.substringAfter('\n')
        val json = JSONObject(payload)
        val bizData = json.optJSONObject("data")?.optJSONObject("biz_data")
            ?: throw IllegalStateException("auth_exchange: " + payload.take(200))
        if (!bizData.has("token")) throw IllegalStateException("auth_exchange: " + payload.take(200))
        val token = bizData.getString("token")
        runCatching { SecurePrefs.put(context, PREFS, KEY_TOKEN, token) }
        store.edit().remove(KEY_VERIFIER).remove(KEY_STATE).apply()
        return token
    }
}

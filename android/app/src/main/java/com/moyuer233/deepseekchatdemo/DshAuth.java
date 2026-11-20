package com.moyuer233.deepseekchatdemo;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** DSH 式 Platform 授权：auth_init → 浏览器授权 → auth_exchange → account grant。 */
final class DshAuth {

    static final String PLATFORM = "https://platform.deepseek.com";
    static final String API = "https://api.deepseek.com";
    static final String REDIRECT_URI = "http://127.0.0.1:19876/oauth/callback";
    private static final String PREFS = "dsh-auth";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_VERIFIER = "verifier";
    private static final String KEY_STATE = "state";
    private static final String KEY_DEVICE = "device_id";

    private DshAuth() {}

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static String token(Context context) {
        return prefs(context).getString(KEY_TOKEN, "");
    }

    /** API 24 起可用；避免 java.util.Base64（Android 上 since 26）。 */
    private static String base64Url(byte[] bytes) {
        return android.util.Base64.encodeToString(bytes,
                android.util.Base64.URL_SAFE | android.util.Base64.NO_WRAP | android.util.Base64.NO_PADDING);
    }

    private static String randomUrlSafe(int bytes) {
        byte[] buffer = new byte[bytes];
        new SecureRandom().nextBytes(buffer);
        return base64Url(buffer);
    }

    private static String challenge(String verifier) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return base64Url(digest.digest(verifier.getBytes(StandardCharsets.US_ASCII)));
    }

    private static Map<String, String> clientHeaders(Context context) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("content-type", "application/json");
        headers.put("accept", "application/json, text/plain, */*");
        headers.put("origin", PLATFORM);
        headers.put("referer", PLATFORM + "/dsh/authorize");
        headers.put("user-agent",
                "Mozilla/5.0 (Linux; Android 16) AppleWebKit/537.36 (KHTML, like Gecko) "
                        + "Chrome/131.0.0.0 Mobile Safari/537.36");
        headers.put("x-client-bundle-id", "");
        headers.put("x-client-platform", "web");
        headers.put("x-client-version", "0.1.0");
        headers.put("x-client-locale", "zh_CN");
        headers.put("x-client-timezone-offset",
                String.valueOf(java.util.TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 1000));
        return headers;
    }

    private static String request(String method, String url, Map<String, String> headers, String body)
            throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(20000);
        connection.setReadTimeout(30000);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
        if (body != null) {
            connection.setDoOutput(true);
            try (OutputStream out = connection.getOutputStream()) {
                out.write(body.getBytes(StandardCharsets.UTF_8));
            }
        }
        int status = connection.getResponseCode();
        InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
        StringBuilder text = new StringBuilder();
        if (stream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    text.append(line);
                }
            }
        }
        connection.disconnect();
        return status + "\n" + text;
    }

    /** 第一步：拿 authorize_url；verifier/state 留到 exchange。 */
    static JSONObject init(Context context) throws Exception {
        String verifier = randomUrlSafe(32);
        String state = randomUrlSafe(16);
        JSONObject body = new JSONObject();
        body.put("code_challenge", challenge(verifier));
        body.put("code_challenge_method", "S256");
        body.put("state", state);
        body.put("redirect_uri", REDIRECT_URI);
        body.put("locale", "zh_CN");
        body.put("login_source", "desktop");
        String raw = request("POST", PLATFORM + "/auth-api/v0/dsh/auth_init", clientHeaders(context), body.toString());
        String payload = raw.substring(raw.indexOf('\n') + 1);
        JSONObject json = new JSONObject(payload);
        JSONObject bizData = json.optJSONObject("data") == null ? null : json.getJSONObject("data").optJSONObject("biz_data");
        if (json.optInt("code", -1) != 0 || bizData == null) {
            throw new IllegalStateException("auth_init: " + payload);
        }
        prefs(context).edit().putString(KEY_VERIFIER, verifier).putString(KEY_STATE, state).apply();
        JSONObject result = new JSONObject();
        result.put("authorizeUrl", bizData.getString("authorize_url"));
        result.put("authorizeId", bizData.getString("authorize_id"));
        result.put("expiresIn", bizData.optInt("expires_in", 600));
        return result;
    }

    /** 第三步：用回调 code 换 token。 */
    static JSONObject exchange(Context context, String code, String state) throws Exception {
        SharedPreferences store = prefs(context);
        String verifier = store.getString(KEY_VERIFIER, "");
        String expectedState = store.getString(KEY_STATE, "");
        if (verifier.isEmpty() || expectedState.isEmpty()) {
            throw new IllegalStateException("no pending authorization");
        }
        // 防授权码注入：回调 state 必须与本次 init 生成的一致（RFC 6749 §10.12）。
        if (state == null || !expectedState.equals(state)) {
            throw new IllegalStateException("state mismatch");
        }
        String deviceId = store.getString(KEY_DEVICE, "");
        if (deviceId.isEmpty()) {
            deviceId = UUID.randomUUID().toString();
            store.edit().putString(KEY_DEVICE, deviceId).apply();
        }
        JSONObject body = new JSONObject();
        body.put("code", code);
        body.put("code_verifier", verifier);
        body.put("redirect_uri", REDIRECT_URI);
        body.put("device_id", deviceId);
        body.put("device_model", "android-arm64");
        body.put("os_version", "Android " + android.os.Build.VERSION.RELEASE);
        String raw = request("POST", PLATFORM + "/auth-api/v0/dsh/auth_exchange", clientHeaders(context), body.toString());
        String payload = raw.substring(raw.indexOf('\n') + 1);
        JSONObject json = new JSONObject(payload);
        JSONObject bizData = json.optJSONObject("data") == null ? null : json.getJSONObject("data").optJSONObject("biz_data");
        if (json.optInt("code", -1) != 0 || bizData == null || !bizData.has("token")) {
            throw new IllegalStateException("auth_exchange: " + payload);
        }
        String token = bizData.getString("token");
        store.edit().putString(KEY_TOKEN, token).remove(KEY_VERIFIER).remove(KEY_STATE).apply();
        JSONObject result = new JSONObject();
        result.put("token", token);
        result.put("authorizedUrl", bizData.optString("authorized_url", ""));
        return result;
    }

    /** 跑三个探针：模型列表、建 Key、chat 域 Cookie（SSO 线索）。 */
    static JSONArray probes(Context context) {
        JSONArray results = new JSONArray();
        String token = token(context);
        results.put(probe("models", "GET", API + "/models", token, null));
        JSONObject chat = new JSONObject();
        try {
            chat.put("model", "deepseek-flash");
            chat.put("max_tokens", 1);
            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", "hi");
            messages.put(message);
            chat.put("messages", messages);
        } catch (Exception ignored) {
        }
        results.put(probe("messages", "POST", API + "/anthropic/v1/messages", token, chat.toString()));
        JSONObject create = new JSONObject();
        try {
            create.put("action", "create");
            create.put("name", "DeepSeek Chat Demo Probe");
            create.put("redacted_key", JSONObject.NULL);
            create.put("created_at", JSONObject.NULL);
            create.put("tracking_id", JSONObject.NULL);
            create.put("is_exp", false);
        } catch (Exception ignored) {
        }
        results.put(probe("get_keys", "GET", PLATFORM + "/api/v0/users/get_api_keys", token, null));
        results.put(probeWithAuth("create_key_bearer", "POST", PLATFORM + "/api/v0/users/edit_api_keys",
                "authorization", "Bearer " + token, create.toString()));
        results.put(probe("create_key_authapi", "POST", PLATFORM + "/auth-api/v0/users/edit_api_keys", token,
                create.toString()));
        results.put(probe("chat_export", "GET", "https://chat.deepseek.com/api/v0/export_all", token, null));
        results.put(probe("create_key", "POST", PLATFORM + "/api/v0/users/edit_api_keys", token,
                create.toString()));
        return results;
    }

    private static JSONObject probe(String name, String method, String url, String token, String body) {
        return probeWithAuth(name, method, url, "x-dsh-auth-token", token, body);
    }

    private static JSONObject probeWithAuth(String name, String method, String url, String headerName,
            String headerValue, String body) {
        JSONObject result = new JSONObject();
        try {
            Map<String, String> headers = new LinkedHashMap<>();
            headers.put("content-type", "application/json");
            headers.put("accept", "application/json, text/plain, */*");
            headers.put("origin", PLATFORM);
            headers.put("referer", PLATFORM + "/");
            headers.put("user-agent",
                    "Mozilla/5.0 (Linux; Android 16) AppleWebKit/537.36 (KHTML, like Gecko) "
                            + "Chrome/131.0.0.0 Mobile Safari/537.36");
            headers.put(headerName, headerValue);
            String raw = request(method, url, headers, body);
            int newline = raw.indexOf('\n');
            result.put("name", name);
            result.put("status", Integer.parseInt(raw.substring(0, newline)));
            String payload = raw.substring(newline + 1);
            result.put("body", payload.length() > 600 ? payload.substring(0, 600) + "…" : payload);
        } catch (Exception e) {
            try {
                result.put("name", name);
                result.put("status", -1);
                result.put("body", String.valueOf(e.getMessage()));
            } catch (Exception ignored) {
            }
        }
        return result;
    }
}

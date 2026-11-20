package com.moyuer233.deepseekchatdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResult;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** DeepSeek 账号：WebView 登录拿 token、导出会话包、为 App 建 API Key、打开外部链接。 */
@CapacitorPlugin(name = "DeepSeekAccount")
public class DeepSeekAccountPlugin extends Plugin {

    private static final String CHAT_ORIGIN = "https://chat.deepseek.com";
    private static final String PLATFORM_ORIGIN = "https://platform.deepseek.com";
    private static final String PREFS = "deepseek-account";
    private static final String KEY_DEVICE_ID = "device-id";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // ---------- 登录 ----------

    @PluginMethod
    public void loginChat(PluginCall call) {
        openLogin(call, CHAT_ORIGIN + "/", "登录 DeepSeek", "loginChatResult");
    }

    @PluginMethod
    public void loginPlatform(PluginCall call) {
        openLogin(call, PLATFORM_ORIGIN + "/", "登录 DeepSeek 开放平台", "loginPlatformResult");
    }

    private void openLogin(PluginCall call, String url, String title, String callbackName) {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.putExtra(LoginActivity.EXTRA_URL, url);
        intent.putExtra(LoginActivity.EXTRA_TITLE, title);
        startActivityForResult(call, intent, callbackName);
    }

    @ActivityCallback
    private void loginChatResult(PluginCall call, ActivityResult result) {
        finishLogin(call, result);
    }

    @ActivityCallback
    private void loginPlatformResult(PluginCall call, ActivityResult result) {
        finishLogin(call, result);
    }

    private void finishLogin(PluginCall call, ActivityResult result) {
        if (call == null) {
            return;
        }
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            String token = result.getData().getStringExtra(LoginActivity.EXTRA_TOKEN);
            if (token != null && !token.isEmpty()) {
                JSObject ret = new JSObject();
                ret.put("token", token);
                call.resolve(ret);
                return;
            }
        }
        call.reject("登录已取消");
    }

    // ---------- 建 API Key ----------

    @PluginMethod
    public void createApiKey(PluginCall call) {
        String token = call.getString("token");
        String name = call.getString("name", "DeepseekChatApp");
        if (token == null || token.isEmpty()) {
            call.reject("缺少 platform 登录凭证");
            return;
        }
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("action", "create");
                body.put("name", name);
                body.put("redacted_key", JSONObject.NULL);
                body.put("created_at", JSONObject.NULL);
                body.put("tracking_id", JSONObject.NULL);
                body.put("is_exp", false);
                String text = request(PLATFORM_ORIGIN + "/api/v0/users/edit_api_keys", "POST", token, body.toString(), true);
                JSONObject json = new JSONObject(text);
                JSONObject data = json.optJSONObject("data");
                JSONObject biz = data == null ? null : data.optJSONObject("biz_data");
                JSONObject key = biz == null ? null : biz.optJSONObject("api_key");
                String apiKey = key == null ? null : key.optString("sensitive_id", "");
                if (apiKey == null || apiKey.isEmpty()) {
                    String msg = data == null ? json.optString("msg") : data.optString("biz_msg", json.optString("msg"));
                    call.reject("创建 API Key 失败：" + msg);
                    return;
                }
                JSObject ret = new JSObject();
                ret.put("apiKey", apiKey);
                call.resolve(ret);
            } catch (Exception e) {
                call.reject("创建 API Key 失败：" + e.getMessage());
            }
        });
    }

    // ---------- 导出并下载会话包 ----------

    @PluginMethod
    public void fetchExportArchive(PluginCall call) {
        String token = call.getString("token");
        if (token == null || token.isEmpty()) {
            call.reject("缺少 chat 登录凭证");
            return;
        }
        executor.execute(() -> {
            try {
                request(CHAT_ORIGIN + "/api/v0/export_all", "GET", token, null, false);
                String downloadUrl = null;
                long deadline = System.currentTimeMillis() + 15 * 60 * 1000L;
                while (System.currentTimeMillis() < deadline) {
                    String text = request(CHAT_ORIGIN + "/api/v0/download_export_history", "GET", token, null, false);
                    JSONObject json = new JSONObject(text);
                    JSONObject data = json.optJSONObject("data");
                    int bizCode = data == null ? -1 : data.optInt("biz_code", -1);
                    if (bizCode == 1) {
                        // NO_TASK：导出刚触发还没入队，稍后再看
                    } else if (bizCode == 2) {
                        call.reject("导出任务已过期，请重试");
                        return;
                    } else if (data != null) {
                        JSONObject biz = data.optJSONObject("biz_data");
                        String status = biz == null ? "" : biz.optString("status", "");
                        if ("FINISHED".equals(status)) {
                            String url = biz.optString("history_download_url", "");
                            if (url.isEmpty()) {
                                url = biz.optString("legacy_history_download_url", "");
                            }
                            if (!url.isEmpty()) {
                                downloadUrl = url;
                                break;
                            }
                        }
                    }
                    Thread.sleep(5000);
                }
                if (downloadUrl == null) {
                    call.reject("导出超时，请稍后重试");
                    return;
                }
                File dir = new File(getContext().getCacheDir(), "export");
                if (!dir.exists() && !dir.mkdirs()) {
                    call.reject("无法创建缓存目录");
                    return;
                }
                File target = new File(dir, "deepseek_data-" + System.currentTimeMillis() + ".zip");
                long bytes = download(downloadUrl, target, token);
                JSObject ret = new JSObject();
                ret.put("path", target.getAbsolutePath());
                ret.put("bytes", bytes);
                ret.put("fileName", "deepseek_data.zip");
                call.resolve(ret);
            } catch (Exception e) {
                call.reject("导出失败：" + e.getMessage());
            }
        });
    }

    // ---------- 导出文件：分块写缓存，再交给系统分享 ----------

    /** 只允许安全字符，避免 JS 通过文件名做路径穿越。 */
    private static String safeFileName(String name) {
        String base = (name == null ? "export" : name).replaceAll("[^A-Za-z0-9._-]", "_");
        if (base.isEmpty() || ".".equals(base) || "..".equals(base)) {
            base = "export";
        }
        return base;
    }

    private File cacheSubDir(String name) throws IOException {
        File dir = new File(getContext().getCacheDir(), name);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("无法创建缓存目录");
        }
        return dir;
    }

    /** 校验 JS 传来的路径确实位于指定缓存目录内（防路径穿越）。 */
    private File requireInDir(File dir, String path) throws IOException {
        File file = new File(path).getCanonicalFile();
        String base = dir.getCanonicalPath() + File.separator;
        if (!file.getPath().startsWith(base)) {
            throw new IOException("非法文件路径");
        }
        return file;
    }

    @PluginMethod
    public void beginExportFile(PluginCall call) {
        String name = call.getString("name", "export.zip");
        try {
            File dir = cacheSubDir("share");
            File target = requireInDir(dir, new File(dir, safeFileName(name)).getAbsolutePath());
            try (FileOutputStream ignored = new FileOutputStream(target, false)) {
                // 截断旧文件
            }
            JSObject ret = new JSObject();
            ret.put("path", target.getAbsolutePath());
            call.resolve(ret);
        } catch (Exception e) {
            call.reject("创建文件失败：" + e.getMessage());
        }
    }

    @PluginMethod
    public void appendExportChunk(PluginCall call) {
        String path = call.getString("path");
        String base64 = call.getString("base64");
        if (path == null || base64 == null) {
            call.reject("缺少参数");
            return;
        }
        executor.execute(() -> {
            try {
                byte[] bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
                File target = requireInDir(cacheSubDir("share"), path);
                try (FileOutputStream out = new FileOutputStream(target, true)) {
                    out.write(bytes);
                }
                JSObject ret = new JSObject();
                ret.put("bytes", bytes.length);
                call.resolve(ret);
            } catch (Exception e) {
                call.reject("写入失败：" + e.getMessage());
            }
        });
    }

    @PluginMethod
    public void shareFile(PluginCall call) {
        String path = call.getString("path");
        String mime = call.getString("mime", "application/octet-stream");
        String title = call.getString("title", "分享");
        if (path == null) {
            call.reject("缺少文件路径");
            return;
        }
        try {
            File file = requireInDir(cacheSubDir("share"), path);
            android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(
                    getContext(),
                    getContext().getPackageName() + ".fileprovider",
                    file);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(mime);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent chooser = Intent.createChooser(intent, title);
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(chooser);
            call.resolve();
        } catch (Exception e) {
            call.reject("分享失败：" + e.getMessage());
        }
    }

    // ---------- 充值页（桌面 UA + 二维码解码） ----------

    // ---------- 原生选图（相册快捷选择，贴近官方 App） ----------

    @PluginMethod
    public void pickImages(PluginCall call) {
        try {
            Intent intent;
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
                intent.putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, 9);
                intent.setType("image/*");
            } else {
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            }
            startActivityForResult(call, intent, "pickImagesResult");
        } catch (Exception e) {
            call.reject("打开相册失败：" + e.getMessage());
        }
    }

    @ActivityCallback
    private void pickImagesResult(PluginCall call, ActivityResult result) {
        if (call == null) {
            return;
        }
        if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) {
            call.reject("已取消选择", "PICK_CANCELLED");
            return;
        }
        Intent data = result.getData();
        List<Uri> uris = new ArrayList<>();
        if (data.getClipData() != null) {
            for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                uris.add(data.getClipData().getItemAt(i).getUri());
            }
        } else if (data.getData() != null) {
            uris.add(data.getData());
        }
        executor.execute(() -> {
            try {
                JSONArray files = new JSONArray();
                for (Uri uri : uris) {
                    JSONObject item = readImageAsJson(uri);
                    if (item != null) {
                        files.put(item);
                    }
                }
                JSObject ret = new JSObject();
                ret.put("files", files);
                call.resolve(ret);
            } catch (Exception e) {
                call.reject("读取图片失败：" + e.getMessage());
            }
        });
    }

    private JSONObject readImageAsJson(Uri uri) {
        try (InputStream in = getContext().getContentResolver().openInputStream(uri)) {
            if (in == null) {
                return null;
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int read;
            while ((read = in.read(chunk)) != -1) {
                buffer.write(chunk, 0, read);
            }
            byte[] bytes = buffer.toByteArray();
            if (bytes.length > 20 * 1024 * 1024) {
                return null;
            }
            String mime = getContext().getContentResolver().getType(uri);
            if (mime == null) {
                mime = "image/jpeg";
            }
            String name = "image_" + System.currentTimeMillis() + "." + (mime.contains("png") ? "png" : "jpg");
            JSONObject item = new JSONObject();
            item.put("name", name);
            item.put("mime", mime);
            item.put("base64", android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP));
            return item;
        } catch (Exception e) {
            return null;
        }
    }

    @PluginMethod
    public void openTopUp(PluginCall call) {
        try {
            Intent intent = new Intent(getContext(), TopUpActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
            call.resolve();
        } catch (Exception e) {
            call.reject("打开充值页失败：" + e.getMessage());
        }
    }

    // ---------- 外部链接 ----------

    @PluginMethod
    public void openExternal(PluginCall call) {
        String url = call.getString("url");
        if (url == null || url.isEmpty()) {
            call.reject("缺少链接");
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
            call.resolve();
        } catch (Exception e) {
            call.reject("打开链接失败：" + e.getMessage());
        }
    }

    // ---------- HTTP ----------

    private String request(String url, String method, String token, String body, boolean platform) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(20000);
        conn.setReadTimeout(60000);
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        if (platform) {
            conn.setRequestProperty("x-client-bundle-id", "com.deepseek.chat");
            conn.setRequestProperty("x-client-platform", "web");
            conn.setRequestProperty("x-client-version", "1.0.1");
            conn.setRequestProperty("x-client-locale", localeHeader());
            conn.setRequestProperty("x-client-timezone-offset", String.valueOf(timezoneOffsetSeconds()));
            conn.setRequestProperty("x-device-id", deviceId());
            conn.setRequestProperty("x-device-model", "");
        } else {
            conn.setRequestProperty("x-client-bundle-id", "com.deepseek.chat");
            conn.setRequestProperty("x-client-platform", "web");
            conn.setRequestProperty("x-client-version", "2.3.0");
            conn.setRequestProperty("x-client-locale", localeHeader());
            conn.setRequestProperty("x-client-timezone-offset", String.valueOf(timezoneOffsetSeconds()));
        }
        if (body != null) {
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            byte[] payload = body.getBytes(StandardCharsets.UTF_8);
            conn.setFixedLengthStreamingMode(payload.length);
            try (java.io.OutputStream out = conn.getOutputStream()) {
                out.write(payload);
            }
        }
        int status = conn.getResponseCode();
        InputStream stream = status >= 200 && status < 300 ? conn.getInputStream() : conn.getErrorStream();
        String text = readAll(stream);
        conn.disconnect();
        if (status < 200 || status >= 300) {
            throw new Exception("HTTP " + status + " " + text);
        }
        return text;
    }

    private long download(String url, File target, String token) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(20000);
        conn.setReadTimeout(120000);
        conn.setInstanceFollowRedirects(true);
        java.net.URI parsed = new java.net.URI(url);
        if ("https".equalsIgnoreCase(parsed.getScheme())
                && "chat.deepseek.com".equalsIgnoreCase(parsed.getHost())) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }
        int status = conn.getResponseCode();
        if (status < 200 || status >= 300) {
            throw new Exception("下载失败 HTTP " + status);
        }
        long total = 0;
        try (InputStream in = conn.getInputStream(); FileOutputStream out = new FileOutputStream(target)) {
            byte[] buffer = new byte[64 * 1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
                total += read;
            }
        } finally {
            conn.disconnect();
        }
        return total;
    }

    private static String readAll(InputStream stream) throws Exception {
        if (stream == null) {
            return "";
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[16 * 1024];
        int read;
        while ((read = stream.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return out.toString(StandardCharsets.UTF_8.name());
    }

    private String localeHeader() {
        return Locale.getDefault().getLanguage().toLowerCase(Locale.ROOT).startsWith("zh") ? "zh_CN" : "en_US";
    }

    private int timezoneOffsetSeconds() {
        return TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 1000;
    }

    private String deviceId() {
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String id = prefs.getString(KEY_DEVICE_ID, null);
        if (id == null) {
            id = UUID.randomUUID().toString();
            prefs.edit().putString(KEY_DEVICE_ID, id).apply();
        }
        return id;
    }

    // ---------- DSH 式账号授权（实验） ----------

    @PluginMethod
    public void platformAuthLogin(PluginCall call) {
        try {
            JSONObject init = DshAuth.init(getContext());
            Intent intent = new Intent(getContext(), OAuthActivity.class);
            intent.putExtra(OAuthActivity.EXTRA_URL, init.getString("authorizeUrl"));
            startActivityForResult(call, intent, "platformAuthResult");
        } catch (Exception e) {
            call.reject("auth_init 失败：" + e.getMessage());
        }
    }

    @ActivityCallback
    private void platformAuthResult(PluginCall call, ActivityResult result) {
        if (call == null) {
            return;
        }
        if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) {
            call.reject("已取消授权", "AUTH_CANCELLED");
            return;
        }
        String code = result.getData().getStringExtra(OAuthActivity.EXTRA_CODE);
        String state = result.getData().getStringExtra(OAuthActivity.EXTRA_STATE);
        if (code == null || code.isEmpty()) {
            call.reject("没有拿到授权码");
            return;
        }
        executor.execute(() -> {
            try {
                JSONObject exchange = DshAuth.exchange(getContext(), code, state);
                JSObject ret = new JSObject();
                ret.put("token", exchange.getString("token"));
                // 登录路径不跑探针（避免有副作用的写请求与额外等待）
                ret.put("probes", "[]");
                call.resolve(ret);
            } catch (Exception e) {
                call.reject("auth_exchange 失败：" + e.getMessage());
            }
        });
    }

    @PluginMethod
    public void runAccountProbes(PluginCall call) {
        executor.execute(() -> {
            try {
                String token = DshAuth.token(getContext());
                if (token.isEmpty()) {
                    call.reject("还没有授权 token");
                    return;
                }
                JSObject ret = new JSObject();
                ret.put("probes", DshAuth.probes(getContext()).toString());
                String cookie = android.webkit.CookieManager.getInstance()
                        .getCookie("https://chat.deepseek.com");
                ret.put("chatCookie", cookie == null ? "" : cookie);
                call.resolve(ret);
            } catch (Exception e) {
                call.reject("探针失败：" + e.getMessage());
            }
        });
    }

    @PluginMethod
    public void platformAuthToken(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("token", DshAuth.token(getContext()));
        call.resolve(ret);
    }
    @Override
    protected void handleOnDestroy() {
        executor.shutdownNow();
        super.handleOnDestroy();
    }
}

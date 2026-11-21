package com.moyuer233.deepseekchatdemo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 充值页：用桌面 UA 打开 platform 的 top_up，页面会按 PC 流程渲染微信二维码；
 * 注入 jsQR 解出二维码内容，再用 Intent 拉起微信。
 */
public class TopUpActivity extends AppCompatActivity {

    private static final String TOP_UP_URL = "https://platform.deepseek.com/top_up";
    private static final String DESKTOP_UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";

    private WebView webView;
    private String jsQrSource = "";
    private String hookSource = "";
    private String lastSavedQr = "";
    private String scanSource = "";
    private final Handler main = new Handler(Looper.getMainLooper());

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        jsQrSource = readAsset("jsqr.js");
        hookSource = readAsset("topup-hook.js");
        scanSource = readAsset("topup-scan.js");

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        TextView bar = new TextView(this);
        bar.setText("充值");
        bar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        bar.setTextColor(Color.parseColor("#111111"));
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(16), 0, dp(16), 0);
        bar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));
        bar.setOnClickListener(v -> finish());
        root.addView(bar);

        webView = new WebView(this);
        webView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setUserAgentString(DESKTOP_UA);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        CookieManager cookies = CookieManager.getInstance();
        cookies.setAcceptCookie(true);
        cookies.setAcceptThirdPartyCookies(webView, true);
        webView.addJavascriptInterface(new Bridge(), "DSTopUp");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                if (!hookSource.isEmpty()) {
                    view.evaluateJavascript(hookSource, null);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (!hookSource.isEmpty()) {
                    view.evaluateJavascript(hookSource, null);
                }
                injectScanner(view);
            }

            @Override
            public void onReceivedError(WebView view, android.webkit.WebResourceRequest request,
                    android.webkit.WebResourceError error) {
                Toast.makeText(TopUpActivity.this, "充值页加载失败，请检查网络", Toast.LENGTH_LONG).show();
            }
        });
        root.addView(webView);

        setContentView(root);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), root);
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(0, bars.top, 0, bars.bottom);
            return insets;
        });
        webView.loadUrl(TOP_UP_URL);
    }

    private void injectScanner(WebView view) {
        if (jsQrSource.isEmpty() || scanSource.isEmpty()) {
            return;
        }
        view.evaluateJavascript(jsQrSource, null);
        view.evaluateJavascript(scanSource, null);
    }

    private String readAsset(String name) {
        try (InputStream in = getAssets().open(name)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return out.toString("UTF-8");
        } catch (Exception e) {
            return "";
        }
    }

    private void handleQr(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        boolean isWeixin = text.startsWith("weixin://");
        String shown = text.length() > 240 ? text.substring(0, 240) + "…" : text;
        if (isWeixin) {
            new AlertDialog.Builder(this)
                    .setTitle("检测到微信支付")
                    .setMessage(shown)
                    .setPositiveButton("打开微信", (dialog, which) -> openUrl(text))
                    .setNegativeButton("复制链接", (dialog, which) -> copyText(text))
                    .setNeutralButton("取消", null)
                    .show();
            return;
        }
        // 非 weixin 链接（银联二维码）：等 onQrImage 保存图片后再提示，避免弹两个框
        android.util.Log.i("DSQR", "skip text-only dialog");
    }

    private void openUrl(String text) {
        // 只放行微信支付与 https，防止远程页面借二维码内容拉起任意组件
        if (!text.startsWith("weixin://wxpay") && !text.startsWith("https://")) {
            copyText(text);
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(text));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "打开失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void copyText(String text) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("pay", text));
        Toast.makeText(this, "已复制", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }


    private String saveQrImage(String dataUrl) {
        try {
            String base64 = dataUrl.substring(dataUrl.indexOf(',') + 1);
            byte[] bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                String name = "deepseek_qr_" + System.currentTimeMillis() + ".png";
                values.put(MediaStore.Images.Media.DISPLAY_NAME, name);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/DeepSeekChatDemo");
                values.put(MediaStore.Images.Media.IS_PENDING, 1);
                android.net.Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri == null) {
                    return null;
                }
                try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                    if (out == null) {
                        return null;
                    }
                    out.write(bytes);
                }
                values.clear();
                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                getContentResolver().update(uri, values, null, null);
                return "Pictures/DeepSeekChatDemo/" + name;
            }
            java.io.File dir = new java.io.File(getExternalFilesDir(null), "qr");
            if (!dir.exists() && !dir.mkdirs()) {
                return null;
            }
            java.io.File file = new java.io.File(dir, "deepseek_qr_" + System.currentTimeMillis() + ".png");
            try (OutputStream out = new java.io.FileOutputStream(file)) {
                out.write(bytes);
            }
            return file.getAbsolutePath();
        } catch (Exception e) {
            android.util.Log.w("DSQR", "saveQrImage failed", e);
            return null;
        }
    }

    private void openWeChatScanner() {
        ScanAssistService.requestScan();
        // weixin://scanqrcode 会被 QQ 抢走，直接按包名拉起微信主界面更可靠
        Intent launch = getPackageManager().getLaunchIntentForPackage("com.tencent.mm");
        if (launch != null) {
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launch);
            Toast.makeText(this, "已打开微信：点右上角 + → 扫一扫 → 相册", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            Intent fallback = new Intent(Intent.ACTION_VIEW, Uri.parse("weixin://"));
            fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(fallback);
        } catch (Exception ignored) {
            Toast.makeText(this, "没有找到微信", Toast.LENGTH_LONG).show();
        }
    }

    private class Bridge {
        private boolean trusted() {
            try {
                String host = Uri.parse(webView == null ? "" : webView.getUrl()).getHost();
                return host != null && ("deepseek.com".equals(host) || host.endsWith(".deepseek.com"));
            } catch (Exception e) {
                return false;
            }
        }

        @JavascriptInterface
        public void onQrImage(String dataUrl, String text) {
            if (!trusted()) {
                return;
            }
            main.post(() -> {
                if (isFinishing() || isDestroyed() || dataUrl == null || text == null) {
                    return;
                }
                if (text.equals(lastSavedQr)) {
                    return;
                }
                lastSavedQr = text;
                String saved = saveQrImage(dataUrl);
                if (saved == null) {
                    return;
                }
                new AlertDialog.Builder(TopUpActivity.this)
                        .setTitle("二维码已识别")
                        .setMessage("已保存到相册（" + saved + "）。\n\n点「打开微信」后会进入扫一扫，点右下角「相册」选这张图即可付款。")
                        .setPositiveButton("打开微信", (dialog, which) -> openWeChatScanner())
                        .setNegativeButton("取消", null)
                        .show();
            });
        }

        @JavascriptInterface
        public void diag(String text) {
            if (!trusted()) {
                return;
            }
            android.util.Log.i("DSQR", String.valueOf(text));
        }

        @JavascriptInterface
        public void onQr(String text) {
            if (!trusted()) {
                return;
            }
            android.util.Log.i("DSQR", "onQr " + String.valueOf(text).substring(0, Math.min(40, String.valueOf(text).length())));
            main.post(() -> {
                if (isFinishing() || isDestroyed() || webView == null) {
                    return;
                }
                String current = webView.getUrl();
                String host = current == null ? null : Uri.parse(current).getHost();
                if (host == null || !(host.equals("platform.deepseek.com") || host.endsWith(".deepseek.com"))) {
                    return;
                }
                handleQr(text);
            });
        }
    }
}

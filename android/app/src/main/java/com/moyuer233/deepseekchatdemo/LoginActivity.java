package com.moyuer233.deepseekchatdemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/** 独立的 WebView 登录页：用户自己完成登录，本 Activity 只负责把 userToken 交回去。 */
public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_URL = "url";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_TOKEN = "token";

    private static final String READ_TOKEN_JS =
            "(function(){try{var raw=localStorage.getItem('userToken');if(!raw)return '';"
            + "var o=JSON.parse(raw);if(!o)return '';"
            + "return String(o.token||o.access_token||o.value||'');}catch(e){return '';}})()";

    private WebView webView;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean finished = false;

    private final Runnable poll = new Runnable() {
        @Override
        public void run() {
            if (finished) {
                return;
            }
            if (webView != null) {
                webView.evaluateJavascript(READ_TOKEN_JS, value -> {
                    if (finished) {
                        return;
                    }
                    String token = unquote(value);
                    if (!token.isEmpty()) {
                        deliver(token);
                    }
                });
            }
            handler.postDelayed(this, 700);
        }
    };

    /** evaluateJavascript 返回的是 JSON 字面量：字符串带引号，空值是 "" 或 null。 */
    private static String unquote(@Nullable String raw) {
        if (raw == null) {
            return "";
        }
        String value = raw.trim();
        if (value.isEmpty() || "null".equals(value)) {
            return "";
        }
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
            value = value.replace("\\\"", "\"").replace("\\\\", "\\");
        }
        return value;
    }

    private void deliver(String token) {
        if (finished) {
            return;
        }
        finished = true;
        handler.removeCallbacks(poll);
        setResult(Activity.RESULT_OK, new Intent().putExtra(EXTRA_TOKEN, token));
        finish();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String url = getIntent().getStringExtra(EXTRA_URL);
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        if (url == null || url.isEmpty()) {
            setResult(Activity.RESULT_CANCELED);
            finish();
            return;
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        TextView bar = new TextView(this);
        bar.setText(title == null ? "登录" : title);
        bar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        bar.setTextColor(Color.parseColor("#111111"));
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(16), 0, dp(16), 0);
        bar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));
        bar.setOnClickListener(v -> cancel());
        root.addView(bar);

        webView = new WebView(this);
        webView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setUserAgentString(
                "Mozilla/5.0 (Linux; Android 14; Pixel 6) AppleWebKit/537.36 "
                        + "(KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36");
        CookieManager cookies = CookieManager.getInstance();
        cookies.setAcceptCookie(true);
        cookies.setAcceptThirdPartyCookies(webView, true);
        webView.setWebViewClient(new WebViewClient());
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
        webView.loadUrl(url);
        handler.postDelayed(poll, 800);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return;
        }
        cancel();
    }

    private void cancel() {
        if (finished) {
            return;
        }
        finished = true;
        handler.removeCallbacks(poll);
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(poll);
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}

package com.moyuer233.deepseekchatdemo;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/** 扫码辅助：在我们拉起微信后，自动点「+」→「扫一扫」，尽量贴近官方体验。 */
public class ScanAssistService extends AccessibilityService {

    private static final long REQUEST_TTL_MS = 20_000;
    private static final int MAX_RETRIES = 6;
    private static final String WECHAT_PACKAGE = "com.tencent.mm";
    private static volatile long requestedAt = 0;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private int stage = 0;
    private int retries = 0;
    private static final String[] ALBUM_LABELS = {"相册", "从相册选取", "Album"};

    static void requestScan() {
        requestedAt = System.currentTimeMillis();
    }

    static boolean isRequested() {
        return System.currentTimeMillis() - requestedAt < REQUEST_TTL_MS;
    }

    private boolean weChatForeground() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null || root.getPackageName() == null) {
            return false;
        }
        return WECHAT_PACKAGE.contentEquals(root.getPackageName());
    }

    private void reset() {
        handler.removeCallbacksAndMessages(null);
        requestedAt = 0;
        stage = 0;
        retries = 0;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!isRequested()) {
            if (stage != 0) {
                reset();
            }
            return;
        }
        if (!weChatForeground()) {
            return;
        }
        if (event.getPackageName() == null || !WECHAT_PACKAGE.contentEquals(event.getPackageName())) {
            return;
        }
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                && event.getEventType() != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            return;
        }
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            return;
        }
        if (stage == 0) {
            stage = 1;
        }
        if (stage == 1) {
            if (clickFirst(root, "更多功能", "更多", "添加", "＋", "+")) {
                stage = 2;
                retries = 0;
                handler.postDelayed(this::tryClickScan, 800);
            }
        } else if (stage == 3) {
            if (clickFirst(root, ALBUM_LABELS)) {
                reset();
            }
        }
    }

    private void tryClickScan() {
        if (!isRequested() || !weChatForeground()) {
            reset();
            return;
        }
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root != null && clickFirst(root, "扫一扫", "Scan")) {
            stage = 3;
            retries = 0;
            handler.postDelayed(this::tryClickAlbum, 1200);
            return;
        }
        retry(this::tryClickScan);
    }

    private void tryClickAlbum() {
        if (!isRequested() || !weChatForeground()) {
            reset();
            return;
        }
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root != null && clickFirst(root, ALBUM_LABELS)) {
            reset();
            return;
        }
        retry(this::tryClickAlbum);
    }

    /** 有上限的重试；超过上限就放弃，避免在任意前台页面无限点击。 */
    private void retry(Runnable action) {
        if (retries++ >= MAX_RETRIES) {
            reset();
            return;
        }
        handler.postDelayed(action, 1200);
    }

    private boolean clickFirst(AccessibilityNodeInfo root, String... labels) {
        for (String label : labels) {
            AccessibilityNodeInfo node = find(root, label, 0);
            if (node != null) {
                AccessibilityNodeInfo clickable = node;
                while (clickable != null && !clickable.isClickable()) {
                    clickable = clickable.getParent();
                }
                if (clickable != null) {
                    clickable.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    return true;
                }
            }
        }
        return false;
    }

    private AccessibilityNodeInfo find(AccessibilityNodeInfo node, String text, int depth) {
        if (node == null || depth > 25) {
            return null;
        }
        CharSequence desc = node.getContentDescription();
        CharSequence label = node.getText();
        if ((desc != null && desc.toString().contains(text)) || (label != null && label.toString().contains(text))) {
            return node;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo result = find(node.getChild(i), text, depth + 1);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public void onInterrupt() {
        reset();
    }

    @Override
    public void onDestroy() {
        reset();
        super.onDestroy();
    }
}

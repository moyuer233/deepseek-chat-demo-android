package com.moyuer233.deepseekchatdemo;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/** 扫码辅助：在我们拉起微信后，自动点「+」→「扫一扫」，尽量贴近官方体验。 */
public class ScanAssistService extends AccessibilityService {

    private static final long REQUEST_TTL_MS = 20_000;
    private static volatile long requestedAt = 0;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private int stage = 0;
    private static final String[] ALBUM_LABELS = {"相册", "从相册选取", "Album"};


    static void requestScan() {
        requestedAt = System.currentTimeMillis();
    }

    static boolean isRequested() {
        return System.currentTimeMillis() - requestedAt < REQUEST_TTL_MS;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!isRequested() || stage >= 2) {
            return;
        }
        if (event.getPackageName() == null || !"com.tencent.mm".contentEquals(event.getPackageName())) {
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
        stage++;
        if (stage == 1) {
            // 微信首页右上角「+」：不同版本 content-desc 不同，逐个试探
            if (clickFirst(root, "更多功能", "更多", "添加", "＋", "+")) {
                handler.postDelayed(this::tryClickScan, 800);
            }
        } else if (stage == 4) {
            // 扫一扫已打开：直接点「相册」，落到选择照片
            if (clickFirst(root, ALBUM_LABELS)) {
                requestedAt = 0;
                stage = 0;
            }
        }
    }

    private void tryClickScan() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root != null && clickFirst(root, "扫一扫", "Scan")) {
            stage = 4;
            handler.postDelayed(this::tryClickAlbum, 1200);
        }
    }

    private void tryClickAlbum() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root != null && clickFirst(root, ALBUM_LABELS)) {
            requestedAt = 0;
            stage = 0;
        } else {
            // 扫描界面可能还在初始化，稍后重试一次
            handler.postDelayed(this::tryClickAlbum, 1200);
        }
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
    public void onInterrupt() {}
}

package com.moyuer233.deepseeknative

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * 扫码辅助：拉起微信后自动点「+」→「扫一扫」→「相册」，省掉手动几步。
 * 只在 [requestScan] 后的 20 秒内动作，其余时间完全不碰微信界面。
 */
class ScanAssistService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var running = false

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (running || !isRequested()) {
            return
        }
        if (event.packageName?.toString() != WECHAT) {
            return
        }
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            return
        }
        running = true
        clickPlus(0)
    }

    /** 微信首页右上角「+」：不同版本文案不同，逐个试探；页面没渲染完就重试。 */
    private fun clickPlus(attempt: Int) {
        if (!isRequested() || attempt > 4) {
            complete()
            return
        }
        if (clickFirst(PLUS_LABELS)) {
            handler.postDelayed({ clickScan(0) }, 800)
        } else {
            handler.postDelayed({ clickPlus(attempt + 1) }, 500)
        }
    }

    /** 「+」菜单里的「扫一扫」。 */
    private fun clickScan(attempt: Int) {
        if (!isRequested() || attempt > 5) {
            complete()
            return
        }
        if (clickFirst(SCAN_LABELS)) {
            handler.postDelayed({ clickAlbum(0) }, 1000)
        } else {
            handler.postDelayed({ clickScan(attempt + 1) }, 600)
        }
    }

    /** 扫一扫页面右下角「相册」，落到选照片。 */
    private fun clickAlbum(attempt: Int) {
        if (!isRequested() || attempt > 6) {
            complete()
            return
        }
        if (clickFirst(ALBUM_LABELS)) {
            complete()
            return
        }
        handler.postDelayed({ clickAlbum(attempt + 1) }, 700)
    }

    private fun complete() {
        requestedAt = 0
        running = false
    }

    private fun clickFirst(labels: Array<String>): Boolean {
        val root = rootInActiveWindow ?: return false
        for (label in labels) {
            val node = find(root, label, 0) ?: continue
            var clickable: AccessibilityNodeInfo? = node
            while (clickable != null && !clickable.isClickable) {
                clickable = clickable.parent
            }
            if (clickable != null) {
                clickable.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return true
            }
        }
        return false
    }

    private fun find(node: AccessibilityNodeInfo?, text: String, depth: Int): AccessibilityNodeInfo? {
        if (node == null || depth > 25) {
            return null
        }
        val desc = node.contentDescription
        val label = node.text
        if ((desc != null && desc.contains(text)) || (label != null && label.contains(text))) {
            return node
        }
        for (i in 0 until node.childCount) {
            val result = find(node.getChild(i), text, depth + 1)
            if (result != null) {
                return result
            }
        }
        return null
    }

    override fun onInterrupt() {}

    companion object {
        private const val WECHAT = "com.tencent.mm"
        private const val REQUEST_TTL_MS = 20_000L
        private val PLUS_LABELS = arrayOf("更多功能", "更多", "添加", "＋", "+")
        private val SCAN_LABELS = arrayOf("扫一扫", "Scan")
        private val ALBUM_LABELS = arrayOf("相册", "从相册选取", "Album")

        @Volatile
        private var requestedAt = 0L

        /** 拉起微信前调用：让服务在接下来 20 秒内自动点扫一扫 → 相册。 */
        fun requestScan() {
            requestedAt = System.currentTimeMillis()
        }

        private fun isRequested() = System.currentTimeMillis() - requestedAt < REQUEST_TTL_MS
    }
}

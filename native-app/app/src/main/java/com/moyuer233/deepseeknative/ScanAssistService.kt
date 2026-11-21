package com.moyuer233.deepseeknative

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * 扫码辅助：拉起微信/支付宝后自动点「+ → 扫一扫 → 相册」，省掉手动几步。
 * 只在 [requestScan] 后的 20 秒内、且前台是微信/支付宝时动作，其余时间不碰任何界面。
 */
class ScanAssistService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var running = false

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (running || !isRequested()) {
            return
        }
        val pkg = event.packageName?.toString() ?: return
        if (pkg != WECHAT && pkg != ALIPAY) {
            return
        }
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            return
        }
        running = true
        runCatching {
            // 微信：先点右上角「+」；支付宝首页直接有「扫一扫」
            if (pkg == WECHAT) clickPlus(0) else clickScan(0)
        }.onFailure {
            complete()
        }
    }

    /** 微信首页右上角「+」；页面没渲染完就重试，最多约 5 秒。 */
    private fun clickPlus(attempt: Int) {
        if (!isRequested() || attempt > 8) {
            complete()
            return
        }
        if (safeClick(PLUS_LABELS)) {
            handler.postDelayed({ clickScan(0) }, 800)
        } else {
            handler.postDelayed({ clickPlus(attempt + 1) }, 600)
        }
    }

    /** 「+」菜单 / 支付宝首页里的「扫一扫」。 */
    private fun clickScan(attempt: Int) {
        if (!isRequested() || attempt > 8) {
            complete()
            return
        }
        if (safeClick(SCAN_LABELS)) {
            handler.postDelayed({ clickAlbum(0) }, 1000)
        } else {
            handler.postDelayed({ clickScan(attempt + 1) }, 600)
        }
    }

    /** 扫一扫页面里的「相册」，落到选照片。 */
    private fun clickAlbum(attempt: Int) {
        if (!isRequested() || attempt > 10) {
            complete()
            return
        }
        if (safeClick(ALBUM_LABELS)) {
            complete()
            return
        }
        handler.postDelayed({ clickAlbum(attempt + 1) }, 700)
    }

    private fun complete() {
        handler.removeCallbacksAndMessages(null)
        requestedAt = 0
        running = false
    }

    /** 点击失败（stale node 等）只当作没找到，不向上抛。 */
    private fun safeClick(labels: Array<String>): Boolean = runCatching {
        val root = rootInActiveWindow ?: return@runCatching false
        for (label in labels) {
            val node = find(root, label, 0) ?: continue
            var clickable: AccessibilityNodeInfo? = node
            var depth = 0
            while (clickable != null && !clickable.isClickable && depth < 3) {
                clickable = clickable.parent
                depth++
            }
            if (clickable != null &&
                clickable.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            ) {
                return@runCatching true
            }
        }
        false
    }.getOrDefault(false)

    private fun find(node: AccessibilityNodeInfo?, label: String, depth: Int): AccessibilityNodeInfo? {
        if (node == null || depth > 25) {
            return null
        }
        // 精确匹配文案，避免「查看更多」「+86」这类子串误点
        val desc = node.contentDescription?.toString()?.trim()
        val text = node.text?.toString()?.trim()
        if (desc == label || text == label) {
            return node
        }
        for (i in 0 until node.childCount) {
            val result = find(node.getChild(i), label, depth + 1)
            if (result != null) {
                return result
            }
        }
        return null
    }

    override fun onInterrupt() {
        complete()
    }

    override fun onDestroy() {
        complete()
        super.onDestroy()
    }

    companion object {
        private const val WECHAT = "com.tencent.mm"
        private const val ALIPAY = "com.eg.android.AlipayGphone"
        private const val REQUEST_TTL_MS = 20_000L
        private val PLUS_LABELS = arrayOf("更多功能", "更多选项")
        private val SCAN_LABELS = arrayOf("扫一扫", "Scan")
        private val ALBUM_LABELS = arrayOf("相册", "从相册选取", "Album")

        @Volatile
        private var requestedAt = 0L

        /** 拉起微信/支付宝前调用：让服务在接下来 20 秒内自动点扫一扫 → 相册。 */
        fun requestScan() {
            requestedAt = System.currentTimeMillis()
        }

        private fun isRequested() = System.currentTimeMillis() - requestedAt < REQUEST_TTL_MS
    }
}

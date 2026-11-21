package com.moyuer233.deepseeknative

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.webkit.WebView
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * 支持左缘横滑开/关侧边栏的 WebView：
 * 拖动时把进度（0..1）通过 window.__siderDrag 传给网页，松手调 __siderSwipeEnd。
 * DOWN 不吞事件（避免左缘按钮点不动），只有真正横向拖动后才接管。
 */
@SuppressLint("ViewConstructor")
class SwipeWebView(context: Context) : WebView(context) {
    var siderOpen: Boolean = false
    private val edgeWidth = 16 * resources.displayMetrics.density
    private val touchSlop = 8 * resources.displayMetrics.density
    private var candidate = false
    private var dragging = false
    private var downX = 0f
    private var downY = 0f
    private var lastX = 0f
    private var lastTime = 0L
    private var lastSend = 0L
    private var velocity = 0f
    private var progress = 0f

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x
                downY = ev.y
                lastX = ev.x
                lastTime = ev.eventTime
                lastSend = 0L
                velocity = 0f
                dragging = false
                progress = if (siderOpen) 1f else 0f
                candidate = if (siderOpen) ev.x < width * 0.85f else ev.x <= edgeWidth
            }
            MotionEvent.ACTION_MOVE -> {
                if (candidate) {
                    val dx = ev.x - downX
                    val dy = ev.y - downY
                    if (!dragging && abs(dx) > touchSlop && abs(dx) > abs(dy) * 1.2f) {
                        dragging = true
                        // 已经交给网页 DOWN，开始原生手势时补发 CANCEL，避免网页触摸状态残留
                        val cancel = MotionEvent.obtain(ev)
                        cancel.action = MotionEvent.ACTION_CANCEL
                        super.dispatchTouchEvent(cancel)
                        cancel.recycle()
                    }
                    if (dragging) {
                        val now = ev.eventTime
                        if (now > lastTime) {
                            velocity = (ev.x - lastX) / (now - lastTime) * 1000f
                        }
                        lastX = ev.x
                        lastTime = now
                        if (!siderOpen) {
                            progress = clamp(dx / (width * 0.82f))
                        } else {
                            progress = clamp(1f + dx / (width * 0.82f))
                        }
                        if (now - lastSend >= 16) {
                            lastSend = now
                            evaluateJavascript("window.__siderDrag && window.__siderDrag($progress)", null)
                        }
                        return true
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (dragging) {
                    if (ev.actionMasked == MotionEvent.ACTION_CANCEL) {
                        evaluateJavascript("window.__siderClose && window.__siderClose()", null)
                    } else {
                        evaluateJavascript("window.__siderSwipeEnd && window.__siderSwipeEnd($progress, $velocity)", null)
                    }
                    candidate = false
                    dragging = false
                    return true
                }
                candidate = false
                dragging = false
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun clamp(v: Float): Float = max(0f, min(1f, v))
}

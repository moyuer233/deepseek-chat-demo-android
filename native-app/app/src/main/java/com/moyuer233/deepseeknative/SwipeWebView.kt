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
 */
@SuppressLint("ViewConstructor")
class SwipeWebView(context: Context) : WebView(context) {
    var siderOpen: Boolean = false
    private val edgeWidth = 24 * resources.displayMetrics.density
    private val touchSlop = 8 * resources.displayMetrics.density
    private var tracking = false
    private var downX = 0f
    private var downY = 0f
    private var lastX = 0f
    private var lastTime = 0L
    private var velocity = 0f
    private var progress = 0f

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x
                downY = ev.y
                lastX = ev.x
                lastTime = ev.eventTime
                velocity = 0f
                progress = if (siderOpen) 1f else 0f
                tracking = if (siderOpen) {
                    ev.x < width * 0.85f
                } else {
                    ev.x <= edgeWidth
                }
                if (!siderOpen && tracking) {
                    // 收起状态下的左缘手势由原生独占，避免网页把这串手势当成文本长按
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (tracking) {
                    val dx = ev.x - downX
                    val dy = ev.y - downY
                    if (abs(dx) > touchSlop && abs(dx) > abs(dy) * 1.2f) {
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
                        evaluateJavascript("window.__siderDrag && window.__siderDrag($progress)", null)
                        return true
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (tracking && ev.actionMasked == MotionEvent.ACTION_UP && progress > 0.001f && progress < 0.999f) {
                    evaluateJavascript("window.__siderSwipeEnd && window.__siderSwipeEnd($progress, $velocity)", null)
                    tracking = false
                    return true
                }
                tracking = false
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun clamp(v: Float): Float = max(0f, min(1f, v))
}

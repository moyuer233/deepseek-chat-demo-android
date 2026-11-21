package com.moyuer233.deepseeknative.data

import android.content.Context

/**
 * 本机 UI 状态（是否看过首启引导）。
 * 账号凭证由 DshAuth（Android Keystore 加密）单独存放，这里不再重复保存。
 */
object SessionStore {
    private const val PREFS = "session"
    private const val KEY_ONBOARDED = "onboarded"

    fun isOnboarded(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_ONBOARDED, false)

    fun setOnboarded(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_ONBOARDED, true).apply()
    }
}

package com.moyuer233.deepseeknative.data

import android.content.Context

/** 登录 token 持久化（和官方一样只存在本机）。 */
object SessionStore {
    private const val PREFS = "session"
    private const val KEY_TOKEN = "token"

    fun token(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_TOKEN, null)

    fun saveToken(context: Context, token: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_TOKEN, token).apply()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY_TOKEN).apply()
    }
}

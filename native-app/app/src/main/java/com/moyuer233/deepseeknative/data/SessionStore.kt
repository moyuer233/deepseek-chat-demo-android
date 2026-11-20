package com.moyuer233.deepseeknative.data

import android.content.Context

/** 登录 token 持久化（和官方一样只存在本机）。 */
object SessionStore {
    private const val PREFS = "session"
    private const val KEY_TOKEN = "token"
    private const val KEY_MOBILE = "mobile"
    private const val KEY_API_KEY = "api_key"
    private const val KEY_ONBOARDED = "onboarded"
    private const val KEY_FONT_STEP = "font_step"

    fun token(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_TOKEN, null)

    fun saveToken(context: Context, token: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_TOKEN, token).apply()
    }

    fun mobile(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_MOBILE, null)

    fun saveMobile(context: Context, mobile: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_MOBILE, mobile).apply()
    }

    /** 官方展示口径：11 位手机号 → 193******64。 */
    fun maskedMobile(context: Context): String? {
        val mobile = mobile(context) ?: return null
        return if (mobile.length == 11) mobile.take(3) + "******" + mobile.takeLast(2) else mobile
    }

    fun apiKey(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_API_KEY, null)?.takeIf { it.isNotEmpty() }

    fun saveApiKey(context: Context, key: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_API_KEY, key).apply()
    }

    fun fontStep(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_FONT_STEP, 1)

    fun saveFontStep(context: Context, step: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_FONT_STEP, step).apply()
    }

    fun isOnboarded(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_ONBOARDED, false)

    fun setOnboarded(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_ONBOARDED, true).apply()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY_TOKEN).remove(KEY_MOBILE).remove(KEY_API_KEY).apply()
    }
}

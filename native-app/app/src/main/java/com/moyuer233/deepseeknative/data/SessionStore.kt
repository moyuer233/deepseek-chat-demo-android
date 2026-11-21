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
    private const val KEY_TTS_VOICE = "tts_voice"
    private const val KEY_THINK = "think_enabled"
    private const val KEY_SEARCH = "search_enabled"
    private const val KEY_THEME = "theme_mode"
    private const val KEY_LANG_EN = "lang_english"

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

    fun ttsVoice(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_TTS_VOICE, null)

    fun saveTtsVoice(context: Context, name: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_TTS_VOICE, name).apply()
    }

    fun thinkEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_THINK, true)

    fun searchEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_SEARCH, false)

    fun saveToggles(context: Context, think: Boolean, search: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_THINK, think).putBoolean(KEY_SEARCH, search).apply()
    }

    fun langEnglish(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_LANG_EN, false)

    fun saveLangEnglish(context: Context, english: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_LANG_EN, english).apply()
    }

    fun themeMode(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_THEME, 0)

    fun saveThemeMode(context: Context, mode: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_THEME, mode).apply()
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

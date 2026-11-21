package com.moyuer233.deepseeknative.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** 界面语言：false 中文、true English。 */
object LangState {
    var english by mutableStateOf(false)
}

/** 双语字面量：界面文案统一走这里，切换语言时重组即生效。 */
fun tr(zh: String, en: String): String = if (LangState.english) en else zh

package com.moyuer233.deepseeknative.data

/** 会话列表条目（只保留首页需要的字段）。 */
data class ChatSession(
    val id: String,
    val title: String,
    val updatedAt: Long,
)

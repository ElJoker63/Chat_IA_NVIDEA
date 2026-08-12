package com.bdavidgm.glm_chat.data

import androidx.annotation.Keep

enum class MessageRole(val apiValue: String) {
    USER("user"),
    ASSISTANT("assistant"),
    SYSTEM("system"),
}

@Keep
data class ChatMessage(
    val id: String,
    val role: MessageRole,
    val content: String,
    val isStreaming: Boolean = false,
    val imageBase64: String? = null,
    val imageType: String? = null,
)

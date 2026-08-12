package com.bdavidgm.glm_chat.data

import androidx.annotation.Keep

/**
 * Configuración de la API cargada desde un JSON del usuario (sin valores en el código fuente).
 */
@Keep
data class ApiConfig(
    val baseUrl: String,
    val chatPath: String,
    val apiKey: String,
    val model: String,
    val temperature: Double,
    val topP: Double,
    val maxTokens: Int,
    val seed: Int,
    val stream: Boolean,
    val showParticles: Boolean = false,
) {
    fun chatCompletionsUrl(): String {
        val base = baseUrl.trimEnd('/')
        val path = if (chatPath.startsWith("/")) chatPath else "/$chatPath"
        return "$base$path"
    }

    fun maskedApiKey(): String {
        val key = apiKey.trim()
        if (key.length <= 8) return "••••••••"
        return "${key.take(6)}…${key.takeLast(4)}"
    }
}

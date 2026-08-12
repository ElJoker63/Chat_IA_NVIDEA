package com.bdavidgm.glm_chat.data

import org.json.JSONObject

object ApiConfigParser {

    /**
     * Lee y valida el JSON de configuración de la API.
     * Campos esperados: base_url, chat_path, api_key, model, temperature, top_p,
     * max_tokens, seed, stream.
     */
    fun parse(jsonText: String): ApiConfig {
        val json = try {
            JSONObject(jsonText.trim())
        } catch (e: Exception) {
            throw IllegalArgumentException("El archivo no es un JSON válido: ${e.message}")
        }

        val baseUrl = json.requiredString("base_url")
        val chatPath = json.requiredString("chat_path")
        val apiKey = json.requiredString("api_key")
        val model = json.requiredString("model")

        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            throw IllegalArgumentException("base_url debe empezar por http:// o https://")
        }
        if (apiKey.isBlank()) {
            throw IllegalArgumentException("api_key no puede estar vacía")
        }
        if (model.isBlank()) {
            throw IllegalArgumentException("model no puede estar vacío")
        }

        return ApiConfig(
            baseUrl = baseUrl.trimEnd('/'),
            chatPath = chatPath.ifBlank { "/chat/completions" },
            apiKey = apiKey.trim(),
            model = model.trim(),
            temperature = json.requiredDouble("temperature"),
            topP = json.requiredDouble("top_p"),
            maxTokens = json.requiredInt("max_tokens"),
            seed = json.requiredInt("seed"),
            stream = json.requiredBoolean("stream"),
            showParticles = json.optBoolean("show_particles", false),
        )
    }

    fun toJson(config: ApiConfig): String =
        JSONObject()
            .put("base_url", config.baseUrl)
            .put("chat_path", config.chatPath)
            .put("api_key", config.apiKey)
            .put("model", config.model)
            .put("temperature", config.temperature)
            .put("top_p", config.topP)
            .put("max_tokens", config.maxTokens)
            .put("seed", config.seed)
            .put("stream", config.stream)
            .put("show_particles", config.showParticles)
            .toString(2)

    private fun JSONObject.requiredString(key: String): String {
        if (!has(key) || isNull(key)) {
            throw IllegalArgumentException("Falta el campo obligatorio \"$key\"")
        }
        return getString(key)
    }

    private fun JSONObject.requiredDouble(key: String): Double {
        if (!has(key) || isNull(key)) {
            throw IllegalArgumentException("Falta el campo obligatorio \"$key\"")
        }
        return getDouble(key)
    }

    private fun JSONObject.requiredInt(key: String): Int {
        if (!has(key) || isNull(key)) {
            throw IllegalArgumentException("Falta el campo obligatorio \"$key\"")
        }
        return getInt(key)
    }

    private fun JSONObject.requiredBoolean(key: String): Boolean {
        if (!has(key) || isNull(key)) {
            throw IllegalArgumentException("Falta el campo obligatorio \"$key\"")
        }
        return getBoolean(key)
    }
}

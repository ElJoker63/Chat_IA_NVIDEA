package com.bdavidgm.glm_chat.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

/**
 * Cliente de chat streaming compatible con la API de chat completions.
 * Todos los parámetros de conexión provienen de [ApiConfig] (JSON del usuario).
 */
class NvidiaChatClient(
    private val client: OkHttpClient = defaultClient(),
) {
    fun streamChat(config: ApiConfig, messages: List<ChatMessage>): Flow<String> = flow {
        val body = buildRequestBody(config, messages)
        val request = Request.Builder()
            .url(config.chatCompletionsUrl())
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .addHeader("Accept", "text/event-stream")
            .addHeader("Content-Type", "application/json")
            .post(body.toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val call = client.newCall(request)
        try {
            val response = call.execute()
            response.use { resp ->
                if (!resp.isSuccessful) {
                    val errorBody = resp.body?.string().orEmpty()
                    throw IllegalStateException(
                        "API error ${resp.code}: ${errorBody.ifBlank { resp.message }}",
                    )
                }

                val responseBody = resp.body
                    ?: throw IllegalStateException("Respuesta vacía del servidor")

                if (!config.stream) {
                    val full = parseFullContent(responseBody.string())
                        ?: throw IllegalStateException("Respuesta sin contenido")
                    emit(full)
                    return@use
                }

                BufferedReader(InputStreamReader(responseBody.byteStream(), Charsets.UTF_8)).use { reader ->
                    while (currentCoroutineContext().isActive) {
                        val line = reader.readLine() ?: break
                        if (line.isBlank() || line.startsWith(":")) continue
                        if (!line.startsWith("data:")) continue
                        val data = line.removePrefix("data:").trim()
                        if (data == "[DONE]") break
                        val token = parseContentDelta(data) ?: continue
                        emit(token)
                        currentCoroutineContext().ensureActive()
                    }
                }
            }
        } finally {
            call.cancel()
        }
    }.flowOn(Dispatchers.IO)

    suspend fun fetchModels(config: ApiConfig): List<String> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${config.baseUrl.trimEnd('/')}/models")
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .get()
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use emptyList<String>()
                val body = response.body?.string() ?: return@use emptyList<String>()
                val json = JSONObject(body)
                val data = json.optJSONArray("data") ?: return@use emptyList<String>()
                val models = mutableListOf<String>()
                for (i in 0 until data.length()) {
                    val modelObj = data.getJSONObject(i)
                    models.add(modelObj.getString("id"))
                }
                models.sorted()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun buildRequestBody(config: ApiConfig, messages: List<ChatMessage>): String {
        val messagesArray = JSONArray()
        messages.forEach { message ->
            val messageJson = JSONObject().put("role", message.role.apiValue)
            
            if (message.imageBase64 != null && message.imageType != null) {
                val contentArray = JSONArray()
                // Texto
                contentArray.put(JSONObject().apply {
                    put("type", "text")
                    put("text", message.content)
                })
                // Imagen
                contentArray.put(JSONObject().apply {
                    put("type", "image_url")
                    put("image_url", JSONObject().apply {
                        put("url", "data:${message.imageType};base64,${message.imageBase64}")
                    })
                })
                messageJson.put("content", contentArray)
            } else {
                messageJson.put("content", message.content)
            }
            
            messagesArray.put(messageJson)
        }
        return JSONObject()
            .put("model", config.model)
            .put("messages", messagesArray)
            .put("temperature", config.temperature)
            .put("top_p", config.topP)
            .put("max_tokens", config.maxTokens)
            .put("seed", config.seed)
            .put("stream", config.stream)
            .toString()
    }

    private fun parseContentDelta(data: String): String? {
        return try {
            val json = JSONObject(data)
            val choices = json.optJSONArray("choices") ?: return null
            if (choices.length() == 0) return null
            val delta = choices.getJSONObject(0).optJSONObject("delta") ?: return null
            if (!delta.has("content") || delta.isNull("content")) return null
            delta.getString("content")
        } catch (_: Exception) {
            null
        }
    }

    private fun parseFullContent(body: String): String? {
        return try {
            val json = JSONObject(body)
            val choices = json.optJSONArray("choices") ?: return null
            if (choices.length() == 0) return null
            val message = choices.getJSONObject(0).optJSONObject("message") ?: return null
            if (!message.has("content") || message.isNull("content")) return null
            message.getString("content")
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        private fun defaultClient(): OkHttpClient =
            OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
    }
}

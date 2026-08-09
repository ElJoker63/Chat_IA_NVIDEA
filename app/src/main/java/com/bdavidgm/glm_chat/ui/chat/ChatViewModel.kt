package com.bdavidgm.glm_chat.ui.chat

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bdavidgm.glm_chat.data.ApiConfig
import com.bdavidgm.glm_chat.data.ApiConfigStore
import com.bdavidgm.glm_chat.data.ChatMessage
import com.bdavidgm.glm_chat.data.MessageRole
import com.bdavidgm.glm_chat.data.NvidiaChatClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class ChatUiState(
    val config: ApiConfig? = null,
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val isGenerating: Boolean = false,
    val isImporting: Boolean = false,
    val error: String? = null,
    val info: String? = null,
)

class ChatViewModel(
    application: Application,
    private val configStore: ApiConfigStore = ApiConfigStore(application),
    private val chatClient: NvidiaChatClient = NvidiaChatClient(),
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ChatUiState(config = configStore.load()))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var streamJob: Job? = null

    fun onInputChange(value: String) {
        _uiState.update { it.copy(input = value, error = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearInfo() {
        _uiState.update { it.copy(info = null) }
    }

    fun clearChat() {
        streamJob?.cancel()
        _uiState.update {
            it.copy(
                messages = emptyList(),
                input = "",
                isGenerating = false,
            )
        }
    }

    fun importConfig(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, error = null, info = null) }
            try {
                val config = withContext(Dispatchers.IO) {
                    configStore.importFromUri(uri)
                }
                streamJob?.cancel()
                _uiState.update {
                    it.copy(
                        config = config,
                        messages = emptyList(),
                        input = "",
                        isGenerating = false,
                        isImporting = false,
                        info = "Configuración cargada: ${config.model}",
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        error = e.message ?: "No se pudo importar el JSON",
                    )
                }
            }
        }
    }

    fun clearConfig() {
        streamJob?.cancel()
        configStore.clear()
        _uiState.value = ChatUiState(info = "Configuración eliminada. Selecciona un JSON de nuevo.")
    }

    fun sendMessage() {
        val text = _uiState.value.input.trim()
        val config = _uiState.value.config
        if (text.isEmpty() || _uiState.value.isGenerating) return
        if (config == null) {
            _uiState.update {
                it.copy(error = "Primero selecciona el archivo JSON de configuración de la API")
            }
            return
        }

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = MessageRole.USER,
            content = text,
        )
        val assistantId = UUID.randomUUID().toString()
        val assistantPlaceholder = ChatMessage(
            id = assistantId,
            role = MessageRole.ASSISTANT,
            content = "",
            isStreaming = true,
        )

        val historyForApi = _uiState.value.messages + userMessage

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage + assistantPlaceholder,
                input = "",
                isGenerating = true,
                error = null,
            )
        }

        streamJob = viewModelScope.launch {
            try {
                chatClient.streamChat(config, historyForApi).collect { token ->
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages.map { message ->
                                if (message.id == assistantId) {
                                    message.copy(content = message.content + token)
                                } else {
                                    message
                                }
                            },
                        )
                    }
                }
                finishAssistant(assistantId, error = null)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                finishAssistant(
                    assistantId,
                    error = e.message ?: "No se pudo completar la respuesta",
                )
            }
        }
    }

    private fun finishAssistant(assistantId: String, error: String?) {
        _uiState.update { state ->
            val updated = state.messages.map { message ->
                if (message.id == assistantId) {
                    message.copy(
                        isStreaming = false,
                        content = message.content.ifBlank {
                            if (error != null) "" else "(sin respuesta)"
                        },
                    )
                } else {
                    message
                }
            }.filterNot { it.id == assistantId && it.content.isBlank() && error != null }

            state.copy(
                messages = updated,
                isGenerating = false,
                error = error,
            )
        }
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                        return ChatViewModel(application) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
                }
            }
    }
}

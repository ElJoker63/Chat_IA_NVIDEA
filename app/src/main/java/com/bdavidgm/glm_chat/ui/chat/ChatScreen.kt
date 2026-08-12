package com.bdavidgm.glm_chat.ui.chat

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bdavidgm.glm_chat.data.ApiConfig
import com.bdavidgm.glm_chat.data.ChatMessage
import com.bdavidgm.glm_chat.data.MessageRole
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = run {
        val app = LocalContext.current.applicationContext as Application
        viewModel(factory = ChatViewModel.factory(app))
    },
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    var menuExpanded by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showConfigDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.messages.size, state.messages.lastOrNull()?.content) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    LaunchedEffect(state.error) {
        val error = state.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(error)
        viewModel.clearError()
    }

    LaunchedEffect(state.info) {
        val info = state.info ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(info)
        viewModel.clearInfo()
    }

    if (showHelpDialog) {
        HelpDialog(onDismiss = { showHelpDialog = false })
    }

    if (showConfigDialog && state.config != null) {
        ConfigurationDialog(
            config = state.config!!,
            availableModels = state.availableModels,
            isFetchingModels = state.isFetchingModels,
            onDismiss = { showConfigDialog = false },
            onSave = {
                viewModel.updateConfig(it)
                showConfigDialog = false
            },
            onLoadModels = viewModel::loadAvailableModels,
        )
    }

    if (state.config == null && !state.isImporting) {
        ApiKeySetupDialog(onConfirm = viewModel::setupDefaultConfig)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        // El Scaffold no aplica insets extra al contenido; el compositor
        // se pega solo al IME / barra de navegación en bottomBar.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "NVIDEA LLM API Chat",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = state.config?.model ?: "Sin configuración de API",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Más opciones",
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Configuración") },
                            onClick = {
                                menuExpanded = false
                                showConfigDialog = true
                            },
                            enabled = state.config != null && !state.isGenerating,
                        )
                        DropdownMenuItem(
                            text = { Text("Quitar configuración") },
                            onClick = {
                                menuExpanded = false
                                viewModel.clearConfig()
                            },
                            enabled = state.config != null && !state.isGenerating && !state.isImporting,
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Limpiar chat") },
                            onClick = {
                                menuExpanded = false
                                viewModel.clearChat()
                            },
                            enabled = state.messages.isNotEmpty() && !state.isGenerating,
                        )
                        DropdownMenuItem(
                            text = { Text("Ayuda") },
                            onClick = {
                                menuExpanded = false
                                showHelpDialog = true
                            },
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            if (state.config != null) {
                ComposerBar(
                    value = state.input,
                    isGenerating = state.isGenerating,
                    onValueChange = viewModel::onInputChange,
                    onSend = viewModel::sendMessage,
                    modifier = Modifier.windowInsetsPadding(
                        WindowInsets.ime.union(WindowInsets.navigationBars),
                    ),
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                state.config == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (state.isImporting) CircularProgressIndicator()
                    }
                }

                state.messages.isEmpty() -> {
                    ConfiguredEmptyState(
                        config = state.config!!,
                        onOpenConfig = { showConfigDialog = true },
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(state.messages, key = { it.id }) { message ->
                            MessageBubble(message = message)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ayuda y Configuración") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = "Esta aplicación se conecta a la API de NVIDIA LLM.",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Al iniciar, solo necesitas tu API Key. Los valores por defecto están optimizados para los modelos Llama 3.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Puedes cambiar el modelo y otros parámetros (temperature, tokens, etc.) en el menú de Configuración en la parte superior derecha.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "El selector de modelos permite buscar entre todos los modelos disponibles en la API de NVIDIA.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Entendido")
            }
        },
    )
}

@Composable
private fun ApiKeySetupDialog(
    onConfirm: (String) -> Unit,
) {
    var apiKey by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { /* No se puede descartar */ },
        title = { Text("Configuración Inicial") },
        text = {
            Column {
                Text(
                    "Introduce tu API Key de NVIDIA para comenzar. La app se configurará automáticamente con valores por defecto.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("NVIDIA API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(apiKey) },
                enabled = apiKey.isNotBlank(),
            ) {
                Text("Empezar")
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigurationDialog(
    config: ApiConfig,
    availableModels: List<String>,
    isFetchingModels: Boolean,
    onDismiss: () -> Unit,
    onSave: (ApiConfig) -> Unit,
    onLoadModels: () -> Unit,
) {
    var baseUrl by remember { mutableStateOf(config.baseUrl) }
    var chatPath by remember { mutableStateOf(config.chatPath) }
    var apiKey by remember { mutableStateOf(config.apiKey) }
    var model by remember { mutableStateOf(config.model) }
    var temperature by remember { mutableStateOf(config.temperature.toString()) }
    var topP by remember { mutableStateOf(config.topP.toString()) }
    var maxTokens by remember { mutableStateOf(config.maxTokens.toString()) }
    var seed by remember { mutableStateOf(config.seed.toString()) }
    var stream by remember { mutableStateOf(config.stream) }

    var modelMenuExpanded by remember { mutableStateOf(false) }
    var modelFilter by remember { mutableStateOf("") }
    val filteredModels = remember(availableModels, modelFilter) {
        availableModels.filter { it.contains(modelFilter, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configuración de la API") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = model,
                        onValueChange = { },
                        label = { Text("Modelo") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = {
                                modelMenuExpanded = true
                                onLoadModels()
                            }) {
                                if (isFetchingModels) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                } else {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                        },
                    )
                    // Capa invisible para capturar el click en todo el campo
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable {
                                modelMenuExpanded = true
                                onLoadModels()
                            },
                    )
                    DropdownMenu(
                        expanded = modelMenuExpanded,
                        onDismissRequest = { modelMenuExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(400.dp),
                    ) {
                        OutlinedTextField(
                            value = modelFilter,
                            onValueChange = { modelFilter = it },
                            placeholder = { Text("Buscar modelo...") },
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            singleLine = true,
                        )
                        if (filteredModels.isEmpty() && !isFetchingModels) {
                            DropdownMenuItem(
                                text = { Text("No se encontraron modelos") },
                                onClick = {},
                                enabled = false,
                            )
                        }
                        filteredModels.take(50).forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m) },
                                onClick = {
                                    model = m
                                    modelMenuExpanded = false
                                },
                            )
                        }
                        if (filteredModels.size > 50) {
                            DropdownMenuItem(
                                text = { Text("... y ${filteredModels.size - 50} más") },
                                onClick = {},
                                enabled = false,
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("Base URL") },
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = temperature,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) temperature = it },
                    label = { Text("Temperature") },
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = topP,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) topP = it },
                    label = { Text("Top P") },
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = maxTokens,
                    onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) maxTokens = it },
                    label = { Text("Max Tokens") },
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = seed,
                    onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) seed = it },
                    label = { Text("Seed") },
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Habilitar Streaming")
                    Switch(checked = stream, onCheckedChange = { stream = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        config.copy(
                            baseUrl = baseUrl,
                            chatPath = chatPath,
                            apiKey = apiKey,
                            model = model,
                            temperature = temperature.toDoubleOrNull() ?: config.temperature,
                            topP = topP.toDoubleOrNull() ?: config.topP,
                            maxTokens = maxTokens.toIntOrNull() ?: config.maxTokens,
                            seed = seed.toIntOrNull() ?: config.seed,
                            stream = stream,
                        ),
                    )
                },
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
    )
}

@Composable
private fun ConfiguredEmptyState(
    config: ApiConfig,
    onOpenConfig: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            Text(
                text = config.model,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = config.chatCompletionsUrl(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "API key: ${config.maskedApiKey()}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Escribe un mensaje para chatear",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = onOpenConfig) {
                Text("Abrir Configuración")
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == MessageRole.USER
    val bubbleColor = if (isUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Card(
            modifier = Modifier.widthIn(max = 320.dp),
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp,
            ),
            colors = CardDefaults.cardColors(
                containerColor = bubbleColor,
                contentColor = contentColor,
            ),
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(
                    text = if (isUser) "Tú" else "Asistente",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor.copy(alpha = 0.7f),
                )
                Spacer(modifier = Modifier.height(4.dp))
                when {
                    // Spinner solo al inicio; mientras stream-ea texto plano evita el parpadeo
                    // del parser Markdown con markdown incompleto.
                    message.content.isEmpty() && message.isStreaming -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                    isUser || message.isStreaming -> {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    else -> {
                        Markdown(
                            content = message.content,
                            colors = markdownColor(
                                text = contentColor,
                                codeBackground = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                            ),
                            typography = markdownTypography(
                                h1 = MaterialTheme.typography.titleLarge,
                                h2 = MaterialTheme.typography.titleMedium,
                                h3 = MaterialTheme.typography.titleSmall,
                                h4 = MaterialTheme.typography.titleSmall,
                                h5 = MaterialTheme.typography.bodyLarge,
                                h6 = MaterialTheme.typography.bodyMedium,
                                text = MaterialTheme.typography.bodyLarge,
                                code = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                ),
                                quote = MaterialTheme.typography.bodyLarge,
                                paragraph = MaterialTheme.typography.bodyLarge,
                                ordered = MaterialTheme.typography.bodyLarge,
                                bullet = MaterialTheme.typography.bodyLarge,
                                list = MaterialTheme.typography.bodyLarge,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ComposerBar(
    value: String,
    isGenerating: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Escribe un mensaje…") },
            enabled = !isGenerating,
            maxLines = 5,
            shape = RoundedCornerShape(20.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
                onSend = { if (value.isNotBlank() && !isGenerating) onSend() },
            ),
        )

        val canSend = value.isNotBlank() && !isGenerating
        IconButton(
            onClick = onSend,
            enabled = canSend,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(
                    if (canSend) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                )
                .size(48.dp),
        ) {
            AnimatedVisibility(
                visible = isGenerating,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AnimatedVisibility(
                visible = !isGenerating,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar",
                    tint = if (canSend) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

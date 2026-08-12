package com.bdavidgm.glm_chat.ui.chat

import android.app.Application
import android.text.format.DateUtils
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.bdavidgm.glm_chat.data.ApiConfig
import com.bdavidgm.glm_chat.data.ChatMessage
import com.bdavidgm.glm_chat.data.MessageRole
import com.bdavidgm.glm_chat.data.local.ChatThread
import com.bdavidgm.glm_chat.R
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import kotlinx.coroutines.launch

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
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (layoutInfo.totalItemsCount == 0) {
                true
            } else {
                val lastVisibleItem = visibleItemsInfo.lastOrNull()
                lastVisibleItem != null && lastVisibleItem.index >= layoutInfo.totalItemsCount - 1
            }
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

    var menuExpanded by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showConfigDialog by remember { mutableStateOf(false) }

    // Manejo del botón atrás
    BackHandler(enabled = drawerState.isOpen || showConfigDialog || showHelpDialog) {
        when {
            showConfigDialog -> showConfigDialog = false
            showHelpDialog -> showHelpDialog = false
            drawerState.isOpen -> scope.launch { drawerState.close() }
        }
    }

    val pickFile = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.onFileSelected(uri, "Archivo adjunto")
        }
    }

    if (showHelpDialog) {
        HelpDialog(onDismiss = { showHelpDialog = false })
    }

    if (showConfigDialog && state.config != null) {
        FullScreenConfigView(
            config = state.config!!,
            availableModels = state.availableModels,
            isFetchingModels = state.isFetchingModels,
            onDismiss = { showConfigDialog = false },
            onSave = { updated ->
                viewModel.updateConfig(updated)
                showConfigDialog = false
            },
            onLoadModels = viewModel::loadAvailableModels,
        )
    } else {
        if (state.config == null && !state.isImporting) {
            ApiKeySetupDialog(onConfirm = viewModel::setupDefaultConfig)
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                SidebarContent(
                    threads = state.threads,
                    currentThreadId = state.currentThreadId,
                    searchQuery = state.chatSearchQuery,
                    onSearchQueryChange = viewModel::onChatSearchQueryChange,
                    onThreadSelected = { 
                        viewModel.selectThread(it)
                        scope.launch { drawerState.close() }
                    },
                    onDeleteThread = { viewModel.deleteThread(it) },
                    onNewChat = {
                        viewModel.createNewChat()
                        scope.launch { drawerState.close() }
                    }
                )
            }
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = state.threads.find { it.id == state.currentThreadId }?.title ?: "Nuevo Chat",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = state.config?.model ?: "NVIDIA AI",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menú")
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
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = Color.White,
                            actionIconContentColor = Color.White,
                            navigationIconContentColor = Color.White,
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
                            onAttachFile = { pickFile.launch("*/*") },
                            selectedFileName = state.selectedFileName,
                            selectedFileUri = state.selectedFileUri,
                            selectedFileBase64 = state.selectedFileBase64,
                            modifier = Modifier.windowInsetsPadding(
                                WindowInsets.ime.union(WindowInsets.navigationBars),
                            ),
                        )
                    }
                },
                snackbarHost = { SnackbarHost(snackbarHostState) },
            ) { padding ->
                LaunchedEffect(state.messages.size, state.streamingMessage?.content, padding.calculateBottomPadding()) {
                    if (isAtBottom) {
                        listState.scrollToItem(listState.layoutInfo.totalItemsCount)
                    }
                }

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

                        state.messages.isEmpty() && state.currentThreadId == null -> {
                            Box(modifier = Modifier.fillMaxSize())
                        }

                        else -> {
                            Box(modifier = Modifier.fillMaxSize()) {
                                val allMessages = remember(state.messages, state.streamingMessage) {
                                    (state.messages + listOfNotNull(state.streamingMessage)).distinctBy { it.id }
                                }

                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 16.dp),
                                ) {
                                    items(allMessages, key = { it.id }) { message ->
                                        MessageBubble(
                                            message = message,
                                            modelName = state.config?.model ?: "AI",
                                            onEdit = { viewModel.editMessage(message.id, it) }
                                        )
                                    }
                                    // Elemento de anclaje para asegurar scroll al final absoluto
                                    item(key = "bottom_anchor") {
                                        Spacer(modifier = Modifier.height(1.dp))
                                    }
                                }

                                // Botón de flecha hacia abajo
                                AnimatedVisibility(
                                    visible = !isAtBottom,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically(),
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 32.dp)
                                ) {
                                    FloatingActionButton(
                                        onClick = {
                                            scope.launch {
                                                if (listState.layoutInfo.totalItemsCount > 0) {
                                                    listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
                                                }
                                            }
                                        },
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.Black,
                                    shape = CircleShape,
                                    elevation = FloatingActionButtonDefaults.elevation(6.dp),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = "Bajar",
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
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
private fun FullScreenConfigView(
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    TextButton(onClick = {
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
                            )
                        )
                    }) {
                        Text("GUARDAR", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("API Key") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.DarkGray)
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = model,
                    onValueChange = { },
                    label = { Text("Modelo") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.DarkGray),
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
                Box(modifier = Modifier.matchParentSize().clickable {
                    modelMenuExpanded = true
                    onLoadModels()
                })
                DropdownMenu(
                    expanded = modelMenuExpanded,
                    onDismissRequest = { modelMenuExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.8f).height(400.dp),
                ) {
                    OutlinedTextField(
                        value = modelFilter,
                        onValueChange = { modelFilter = it },
                        placeholder = { Text("Buscar modelo...") },
                        modifier = Modifier.padding(8.dp).fillMaxWidth(),
                        singleLine = true,
                    )
                    filteredModels.take(50).forEach { m ->
                        DropdownMenuItem(
                            text = { Text(m) },
                            onClick = {
                                model = m
                                modelMenuExpanded = false
                            },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = { Text("Base URL") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.DarkGray)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = temperature,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) temperature = it },
                    label = { Text("Temp") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.DarkGray)
                )
                OutlinedTextField(
                    value = maxTokens,
                    onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) maxTokens = it },
                    label = { Text("Tokens") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.DarkGray)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Habilitar Streaming", color = Color.White)
                Switch(checked = stream, onCheckedChange = { stream = it })
            }
        }
    }
}

@Composable
fun NvidiaLogo(
    modifier: Modifier = Modifier,
    height: Dp = 40.dp,
    tint: Color? = null
) {
    val aspectRatio = 164f / 30f
    Image(
        painter = painterResource(id = R.drawable.ic_nvidia_logo),
        contentDescription = "NVIDIA Logo",
        colorFilter = tint?.let { ColorFilter.tint(it) },
        modifier = modifier
            .height(height)
            .width(height * aspectRatio)
    )
}

@Composable
private fun MessageBubble(
    message: ChatMessage, 
    modelName: String,
    onEdit: (String) -> Unit
) {
    val isUser = message.role == MessageRole.USER
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (isUser) Color(0xFF424242) else Color(0xFF3C5E00)
    val contentColor = Color.White
    val modelLabel = modelName.substringAfterLast('/').uppercase()
    val clipboardManager = LocalClipboardManager.current
    var isEditing by remember { mutableStateOf(false) }
    var editValue by remember { mutableStateOf(message.content) }
    var showFullImage by remember { mutableStateOf(false) }
    
    // Decodificamos el base64 a bytes para que Coil lo maneje mejor
    val imageBytes = remember(message.imageBase64) {
        message.imageBase64?.let { android.util.Base64.decode(it, android.util.Base64.DEFAULT) }
    }

    if (showFullImage && imageBytes != null) {
        AlertDialog(
            onDismissRequest = { showFullImage = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            text = {
                Box(modifier = Modifier.fillMaxSize().clickable { showFullImage = false }, contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = imageBytes,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    )
                }
            },
            confirmButton = {},
            containerColor = Color.Black.copy(alpha = 0.9f)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp),
        horizontalAlignment = alignment
    ) {
        if (!isUser) {
            Text(
                text = modelLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF76B900),
                modifier = Modifier.padding(bottom = 4.dp, start = 8.dp)
            )
        }
        
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isUser) {
                IconButton(
                    onClick = { isEditing = !isEditing },
                    modifier = Modifier.size(32.dp).padding(end = 4.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }

            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 2.dp,
                    bottomEnd = if (isUser) 2.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = bubbleColor,
                    contentColor = contentColor
                ),
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (imageBytes != null) {
                        AsyncImage(
                            model = imageBytes,
                            contentDescription = "Imagen adjunta",
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showFullImage = true }
                                .padding(bottom = 8.dp)
                        )
                    }

                    if (isEditing) {
                        Column {
                            OutlinedTextField(
                                value = editValue,
                                onValueChange = { editValue = it },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = MaterialTheme.typography.bodyLarge,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                TextButton(onClick = { isEditing = false }) { Text("Cancelar") }
                                TextButton(onClick = { 
                                    onEdit(editValue)
                                    isEditing = false 
                                }) { Text("Enviar") }
                            }
                        }
                    } else {
                        // Usamos un Box con tamaño mínimo para estabilizar la burbuja
                        Box(modifier = Modifier.widthIn(min = 20.dp)) {
                            when {
                                message.content.isEmpty() && message.isStreaming -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                }
                                message.isStreaming -> {
                                    Text(
                                        text = message.content,
                                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
                                        color = Color.White
                                    )
                                }
                                else -> {
                                    Markdown(
                                        content = message.content,
                                        colors = markdownColor(
                                            text = Color.White,
                                            codeBackground = Color.Black.copy(alpha = 0.5f),
                                        ),
                                        typography = markdownTypography(
                                            text = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
                                            code = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = FontFamily.Monospace,
                                            ),
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            if (!isUser) {
                IconButton(
                    onClick = { clipboardManager.setText(AnnotatedString(message.content)) },
                    modifier = Modifier.size(32.dp).padding(start = 4.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = Color.Gray, modifier = Modifier.size(16.dp))
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
    onAttachFile: () -> Unit,
    selectedFileName: String?,
    selectedFileUri: android.net.Uri?,
    selectedFileBase64: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        if (selectedFileUri != null) {
            Box(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                // Para la vista previa antes de enviar, usamos la URI local que es inmediata
                AsyncImage(
                    model = selectedFileUri,
                    contentDescription = null,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else if (selectedFileName != null) {
            Row(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .background(Color.DarkGray, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AttachFile, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(selectedFileName, style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val canSend = (value.isNotBlank() || selectedFileUri != null) && !isGenerating
            
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escribe un mensaje…", color = Color.Gray) },
                leadingIcon = {
                    IconButton(onClick = onAttachFile) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Adjuntar", tint = Color.Gray)
                    }
                },
                enabled = !isGenerating,
                maxLines = 5,
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = { if (canSend) onSend() },
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.DarkGray,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                    cursorColor = MaterialTheme.colorScheme.primary,
                ),
            )

            IconButton(
                onClick = onSend,
                enabled = canSend,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (canSend) MaterialTheme.colorScheme.primary else Color.DarkGray,
                    )
                    .size(40.dp),
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.Black,
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar",
                        tint = if (canSend) Color.Black else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SidebarContent(
    threads: List<ChatThread>,
    currentThreadId: String?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onThreadSelected: (String) -> Unit,
    onDeleteThread: (String) -> Unit,
    onNewChat: () -> Unit
) {
    val filteredThreads = remember(threads, searchQuery) {
        threads.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    val groupedThreads = remember(filteredThreads) {
        filteredThreads.groupBy { thread ->
            when {
                DateUtils.isToday(thread.lastMessageAt) -> "Hoy"
                DateUtils.isToday(thread.lastMessageAt + DateUtils.DAY_IN_MILLIS) -> "Ayer"
                else -> "Anteriores"
            }
        }
    }

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.background,
        drawerContentColor = Color.White,
        modifier = Modifier.width(300.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Historial",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onNewChat,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.DarkGray),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF76B900),
                    contentColor = Color.White
                )
            ) {
                Text(
                    "Nuevo Chat",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Buscar conversaciones", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.DarkGray,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            LazyColumn(modifier = Modifier.weight(1f)) {
                groupedThreads.forEach { (header, threadList) ->
                    item {
                        Text(
                            text = header,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                        )
                    }
                    items(threadList) { thread ->
                        val isSelected = thread.id == currentThreadId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color.White.copy(alpha = 0.05f) else Color.Transparent)
                                .clickable { onThreadSelected(thread.id) }
                                .padding(horizontal = 8.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF76B900))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            } else {
                                Spacer(modifier = Modifier.width(20.dp))
                            }
                            
                            Text(
                                thread.title,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (isSelected) Color.White else Color.LightGray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            IconButton(
                                onClick = { onDeleteThread(thread.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    tint = Color.Red,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Eliminada la función ConfiguredEmptyState que ya no se usa

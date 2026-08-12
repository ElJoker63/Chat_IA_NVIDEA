package com.bdavidgm.glm_chat.ui.chat.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ApiKeySetupDialog(
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

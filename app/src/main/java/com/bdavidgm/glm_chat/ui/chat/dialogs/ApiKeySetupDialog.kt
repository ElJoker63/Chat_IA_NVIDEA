package com.bdavidgm.glm_chat.ui.chat.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.bdavidgm.glm_chat.R
import androidx.compose.ui.unit.dp

@Composable
fun ApiKeySetupDialog(
    onConfirm: (String) -> Unit,
) {
    var apiKey by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { /* No se puede descartar */ },
        title = { Text(stringResource(R.string.setup_title)) },
        text = {
            Column {
                Text(
                    stringResource(R.string.setup_description),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text(stringResource(R.string.setup_api_key_label)) },
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
                Text(stringResource(R.string.setup_start))
            }
        },
    )
}

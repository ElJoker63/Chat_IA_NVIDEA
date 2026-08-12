package com.bdavidgm.glm_chat.ui.chat.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ayuda y Guía de Uso") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = "Potenciado por NVIDIA NIM API",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Esta aplicación ofrece una interfaz fluida para interactuar con los modelos más avanzados a través de NVIDIA NIM.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Características Principales:",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Soporte Multimodal: Envía imágenes y documentos PDF para análisis directo.\n" +
                           "• Historial Inteligente: Accede a tus conversaciones previas desde la barra lateral.\n" +
                           "• Configuración Flexible: Ajusta parámetros como temperatura y tokens máximos para cada chat.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Navegación:",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Puedes gestionar tus ajustes y API Key rápidamente desde la opción 'Ajustes' en la parte inferior de la barra lateral.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
    )
}

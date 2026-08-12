package com.bdavidgm.glm_chat.ui.chat.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.bdavidgm.glm_chat.R
import coil.compose.AsyncImage

@Composable
fun ComposerBar(
    value: String,
    isGenerating: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachFile: () -> Unit,
    selectedFileName: String?,
    selectedFileUri: android.net.Uri?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent) // Fondo transparente para efecto flotante
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        if (selectedFileUri != null) {
            Box(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E1E1E))
            ) {
                // Para la vista previa antes de enviar, usamos la URI local que es inmediata
                AsyncImage(
                    model = selectedFileUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else if (selectedFileName != null) {
            Row(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
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
                placeholder = { Text(stringResource(R.string.write_message), color = Color.Gray) },
                leadingIcon = {
                    IconButton(onClick = onAttachFile) {
                        Icon(Icons.Default.AttachFile, contentDescription = stringResource(R.string.attach_file), tint = Color.Gray)
                    }
                },
                enabled = !isGenerating,
                maxLines = 5,
                shape = RoundedCornerShape(28.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = { if (canSend) onSend() },
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.DarkGray.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.DarkGray.copy(alpha = 0.3f),
                    focusedContainerColor = Color(0xFF1E1E1E), // Color sólido para que parezca flotar
                    unfocusedContainerColor = Color(0xFF1E1E1E),
                    cursorColor = MaterialTheme.colorScheme.primary,
                ),
            )

            IconButton(
                onClick = onSend,
                enabled = canSend,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (canSend) MaterialTheme.colorScheme.primary else Color(0xFF1E1E1E),
                    ),
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
                        contentDescription = stringResource(R.string.send),
                        tint = if (canSend) Color.Black else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

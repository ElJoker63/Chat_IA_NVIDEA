package com.bdavidgm.glm_chat.ui.chat.components

import android.text.format.DateUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bdavidgm.glm_chat.data.local.ChatThread

@Composable
fun SidebarContent(
    threads: List<ChatThread>,
    currentThreadId: String?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onThreadSelected: (String) -> Unit,
    onDeleteThread: (String) -> Unit,
    onNewChat: () -> Unit,
    onOpenSettings: () -> Unit
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
        modifier = Modifier.width(320.dp),
        windowInsets = WindowInsets.ime.union(WindowInsets.navigationBars).union(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF76B900)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "NVIDIA LLM Chat",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
            
            OutlinedButton(
                onClick = onNewChat,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.DarkGray.copy(alpha = 0.5f)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.05f),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nuevo Chat", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))
            
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
                            text = header.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 8.dp)
                        )
                    }
                    items(threadList) { thread ->
                        val isSelected = thread.id == currentThreadId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFF76B900).copy(alpha = 0.15f) else Color.Transparent)
                                .clickable { onThreadSelected(thread.id) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = if (isSelected) Color(0xFF76B900) else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                thread.title,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (isSelected) Color.White else Color.LightGray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (isSelected) {
                                IconButton(
                                    onClick = { onDeleteThread(thread.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        tint = Color.Red.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Footer Section
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.DarkGray.copy(alpha = 0.5f))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onOpenSettings() }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text("Ajustes", style = MaterialTheme.typography.bodyLarge, color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

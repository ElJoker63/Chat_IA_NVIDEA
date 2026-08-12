package com.bdavidgm.glm_chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import com.bdavidgm.glm_chat.ui.chat.ChatScreen
import com.bdavidgm.glm_chat.ui.theme.DarkBackground
import com.bdavidgm.glm_chat.ui.theme.GLMchatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Forzamos el estilo de la barra de sistema para que siempre sea oscuro (iconos claros)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(DarkBackground.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(DarkBackground.toArgb())
        )
        
        setContent {
            GLMchatTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ChatScreen()
                }
            }
        }
    }
}

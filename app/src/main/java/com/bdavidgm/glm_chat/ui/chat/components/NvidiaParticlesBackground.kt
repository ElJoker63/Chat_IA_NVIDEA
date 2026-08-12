package com.bdavidgm.glm_chat.ui.chat.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive
import kotlin.random.Random

/**
 * Representa una partícula individual en el sistema.
 * Las propiedades se mutan in-place para evitar GC pressure.
 */
private class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val radius: Float,
    val color: Color,
    val alpha: Float
)

@Composable
fun NvidiaParticlesBackground(
    modifier: Modifier = Modifier,
    particleCount: Int = 100,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val connectionDistancePx = with(density) { 120.dp.toPx() }
    val connectionDistanceSq = connectionDistancePx * connectionDistancePx
    
    val nvidiaGreen = Color(0xFF76B900)
    val pureBlack = Color(0xFF000000)

    // Estado persistente de las partículas
    val particles = remember { mutableStateListOf<Particle>() }
    
    // Trigger para forzar el re-dibujado en cada frame
    var frameTime by remember { mutableLongStateOf(0L) }

    // Bucle de animación de alto rendimiento (60/120 FPS)
    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameNanos { time ->
                frameTime = time
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (frameTime == -1L) return@Canvas
            
            val width = size.width
            val height = size.height

            // Inicialización diferida cuando conocemos el tamaño del Canvas
            if (particles.isEmpty() && width > 0) {
                repeat(particleCount) {
                    val isGreen = Random.nextFloat() > 0.3f
                    particles.add(
                        Particle(
                            x = Random.nextFloat() * width,
                            y = Random.nextFloat() * height,
                            vx = (Random.nextFloat() - 0.5f) * 1.5f,
                            vy = (Random.nextFloat() - 0.5f) * 1.5f,
                            radius = Random.nextFloat() * 2.5f + 1f,
                            color = if (isGreen) nvidiaGreen else Color.White,
                            alpha = Random.nextFloat() * 0.5f + 0.2f
                        )
                    )
                }
            }

            // Fondo negro absoluto
            drawRect(color = pureBlack)

            // Actualización y Dibujado (Zero-allocation Loop)
            for (i in 0 until particles.size) {
                val p1 = particles[i]

                // Actualizar posición
                p1.x += p1.vx
                p1.y += p1.vy

                // Rebote en bordes
                if (p1.x < 0 || p1.x > width) p1.vx *= -1
                if (p1.y < 0 || p1.y > height) p1.vy *= -1

                // Dibujar conexiones (Neural Links)
                for (j in i + 1 until particles.size) {
                    val p2 = particles[j]
                    val dx = p1.x - p2.x
                    val dy = p1.y - p2.y
                    val distSq = dx * dx + dy * dy

                    if (distSq < connectionDistanceSq) {
                        // Opacidad basada en la distancia (más cerca = más brillante)
                        val fraction = 1f - (distSq / connectionDistanceSq)
                        val lineAlpha = fraction * 0.25f
                        
                        drawLine(
                            color = nvidiaGreen.copy(alpha = lineAlpha),
                            start = androidx.compose.ui.geometry.Offset(p1.x, p1.y),
                            end = androidx.compose.ui.geometry.Offset(p2.x, p2.y),
                            strokeWidth = 1f
                        )
                    }
                }

                // Dibujar el Nodo
                drawCircle(
                    color = p1.color.copy(alpha = p1.alpha),
                    radius = p1.radius,
                    center = androidx.compose.ui.geometry.Offset(p1.x, p1.y)
                )
            }
        }
        
        // El contenido del Sidebar se renderiza encima
        content()
    }
}

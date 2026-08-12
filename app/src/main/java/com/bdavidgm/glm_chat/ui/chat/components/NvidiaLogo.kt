package com.bdavidgm.glm_chat.ui.chat.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bdavidgm.glm_chat.R

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

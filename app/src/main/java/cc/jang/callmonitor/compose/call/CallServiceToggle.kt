package cc.jang.callmonitor.compose.call

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import cc.jang.callmonitor.Call
import cc.jang.callmonitor.R

@Preview
@Composable
fun CallServiceTogglePreview() {
    var status: Call.Server.Status by remember { mutableStateOf(Call.Server.Status.Syncing()) }
    CallServiceToggle(
        status = status,
        enabled = true,
    ) {
        status = when(status) {
            is Call.Server.Status.Syncing -> Call.Server.Status.Started()
            is Call.Server.Status.Started -> Call.Server.Status.Stopped()
            is Call.Server.Status.Stopped -> Call.Server.Status.Syncing()
        }
    }
}

@Composable
fun CallServiceToggle(
    status: Call.Server.Status,
    enabled: Boolean = status !is Call.Server.Status.Syncing,
    onToggleServerClick: (Boolean) -> Unit,
) {
    IconButton(
        onClick = {
            val turnOn = status is Call.Server.Status.Stopped
            onToggleServerClick(turnOn)
        },
        enabled = enabled,
    ) {
        val res = when (status) {
            is Call.Server.Status.Syncing -> R.drawable.sync_black_24dp
            is Call.Server.Status.Started -> R.drawable.link_black_24dp
            is Call.Server.Status.Stopped -> R.drawable.link_off_black_24dp
        }
        var angle by remember(status) { mutableFloatStateOf(0f) }
        val rotation = remember(status) { Animatable(angle) }
        LaunchedEffect(status) {
            if (status is Call.Server.Status.Syncing) {
                rotation.animateTo(
                    targetValue = angle - 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(920, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                ) {
                    angle = value
                }
            }
        }
        Image(
            painter = painterResource(res),
            contentDescription = "",
            modifier = Modifier.rotate(rotation.value),
            colorFilter = ColorFilter.tint(LocalContentColor.current)
        )
    }
}

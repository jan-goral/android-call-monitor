package cc.jang.callmonitor.ui.screen

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import cc.jang.callmonitor.Call
import cc.jang.callmonitor.R
import cc.jang.callmonitor.android.startCallService
import cc.jang.callmonitor.android.stopCallService


@Composable
fun CallServiceToggle(
   status: Call.Api.Status
) {
    val context = LocalContext.current
    CallServiceToggle(status) {
        when (status) {
            Call.Api.Status.Stopped -> context.startCallService()
            Call.Api.Status.Started -> context.stopCallService()
            Call.Api.Status.Syncing -> Unit
        }
    }
}

@Preview
@Composable
fun CallServiceTogglePreview() {
    var status by remember { mutableStateOf(Call.Api.Status.Syncing) }
    CallServiceToggle(
        status = status,
        enabled = true,
    ) {
        val values = Call.Api.Status.values()
        val next = (status.ordinal + 1) % values.size
        status = values[next]
    }
}

@Composable
fun CallServiceToggle(
    status: Call.Api.Status,
    enabled: Boolean = status != Call.Api.Status.Syncing,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
    ) {
        val res = when (status) {
            Call.Api.Status.Syncing -> R.drawable.sync_black_24dp
            Call.Api.Status.Started -> R.drawable.link_black_24dp
            Call.Api.Status.Stopped -> R.drawable.link_off_black_24dp
        }
        var angle by remember(status) { mutableFloatStateOf(0f) }
        val rotation = remember(status) { Animatable(angle) }
        LaunchedEffect(status) {
            if (status == Call.Api.Status.Syncing) {
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
            contentDescription = status.name,
            modifier = Modifier.rotate(rotation.value),
            colorFilter = ColorFilter.tint(LocalContentColor.current)
        )
    }
}

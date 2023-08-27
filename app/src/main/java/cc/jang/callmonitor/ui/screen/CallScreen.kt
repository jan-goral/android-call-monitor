package cc.jang.callmonitor.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cc.jang.callmonitor.Call
import cc.jang.callmonitor.ui.theme.CallMonitorTheme

@Composable
fun CallScreen(
    model: CallViewModel = viewModel(),
) {
    val status by model.status.collectAsState()
    val address by model.address.collectAsState()
    val items by model.calls.collectAsState()

    CallScreen(
        status = status,
        address = address,
        log = items
    )
}


@Preview
@Composable
fun CallScreenPreview() = CallMonitorTheme {
    CallScreen(
        status = Call.Api.Status.Syncing,
        address = "0.0.0.0:8080",
        log = (0..20).map {
            Call.Previous(
                number = (it * 100000).toString(),
                duration = (it * 10L),
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallScreen(
    status: Call.Api.Status,
    address: String,
    log: List<Call.Previous>,
) = Scaffold(
    topBar = {
        TopAppBar(
            title = { Text(text = "Call Monitor") },
            actions = { CallServiceToggle(status) }
        )
    },
) {
    Column(modifier = Modifier.padding(it)) {
        Text(
            text = address,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn {
            items(log) { item ->
                Column {
                    Box(
                        modifier = Modifier
                            .background(LocalContentColor.current)
                            .fillMaxWidth()
                            .height(0.5.dp)
                    )
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = item.name ?: item.number.parsePhoneNumber(),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = "${item.duration}s",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }
    }
}

package com.example.arigo.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onDeviceClick: (String) -> Unit,
    onAddDeviceClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "HOME",
                style = MaterialTheme.typography.headlineLarge
            )
            Button(onClick = { onDeviceClick("device_1") }) { Text("Open Device device_1") }
            Button(onClick = onAddDeviceClick) { Text("Add Device") }
            Button(onClick = onNotificationsClick) { Text("Notifications") }
        }
    }
}

package com.example.arigo.presentation.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arigo.core.theme.CardGreen
import com.example.arigo.core.theme.GrayText
import com.example.arigo.core.theme.TealDark
import com.example.arigo.core.theme.TealHeader
import com.example.arigo.core.theme.White
import com.example.arigo.domain.model.PairedDevice
import com.example.arigo.presentation.components.AuthFieldTextStyle
import com.example.arigo.presentation.components.AuthPillButton
import com.example.arigo.presentation.components.authTextFieldColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDevicesSheet(
    pairedDevices: List<PairedDevice>,
    onDismiss: () -> Unit,
    onAddNewDevice: () -> Unit,
    onDeviceRemove: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "My Devices",
                color = TealDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 4.dp, bottom = 16.dp)
            )

            if (pairedDevices.isEmpty()) {
                Text(
                    text = "No devices paired yet",
                    color = GrayText,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 32.dp)
                )
            } else {
                pairedDevices.forEach { device ->
                    PairedDeviceCard(
                        device = device,
                        onRemove = { onDeviceRemove(device.deviceId) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // "Buy new AirGo device" — outlined pill, placeholder action
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(30.dp))
                    .background(White)
                    .padding(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .clickable {
                            Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Buy new AirGo device",
                        color = TealHeader,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            AuthPillButton(
                text = "ADD",
                isLoading = false,
                onClick = onAddNewDevice,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(140.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PairedDeviceCard(
    device: PairedDevice,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CardGreen)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Device Name", color = GrayText, fontSize = 12.sp)
            Text(
                text = device.nickname.ifBlank { "—" },
                color = TealDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Model Number", color = GrayText, fontSize = 12.sp)
            Text(
                text = device.productId.ifBlank { device.deviceId },
                color = TealDark,
                fontSize = 14.sp
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(28.dp)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Remove device",
                tint = Color.Red,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewDeviceSheet(
    onDismiss: () -> Unit,
    onSave: (productId: String, nickname: String) -> Unit,
    isLoading: Boolean = false,
    error: String? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var productId by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Add New Device",
                color = TealDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 4.dp, bottom = 16.dp)
            )

            DeviceField(
                label = "Product ID",
                value = productId,
                placeholder = "Enter device ID (e.g., ARIGO_001)",
                onValueChange = { productId = it },
                keyboardType = KeyboardType.Ascii
            )

            Spacer(modifier = Modifier.height(16.dp))

            DeviceField(
                label = "Device Name",
                value = nickname,
                placeholder = "Give your device a nickname",
                onValueChange = { nickname = it },
                keyboardType = KeyboardType.Text
            )

            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = Color.Red,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AuthPillButton(
                text = "Save",
                isLoading = isLoading,
                onClick = { onSave(productId, nickname) },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(140.dp)
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = TealHeader,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun DeviceField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = TealHeader,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(5.dp),
            singleLine = true,
            placeholder = {
                Text(
                    text = placeholder,
                    color = GrayText,
                    fontSize = 14.sp
                )
            },
            colors = authTextFieldColors(),
            textStyle = AuthFieldTextStyle,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
    }
}


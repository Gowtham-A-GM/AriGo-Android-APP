package com.example.arigo.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.arigo.R
import com.example.arigo.core.theme.AqiBad
import com.example.arigo.core.theme.AqiGood
import com.example.arigo.core.theme.AqiHazardous
import com.example.arigo.core.theme.AqiNormal
import com.example.arigo.core.theme.CardGreen
import com.example.arigo.core.theme.GrayText
import com.example.arigo.core.theme.GreenBadge
import com.example.arigo.core.theme.LightGray
import com.example.arigo.core.theme.MintGreen
import com.example.arigo.core.theme.RacingSansOne
import com.example.arigo.core.theme.RadioCanada
import com.example.arigo.core.theme.TealBadge
import com.example.arigo.core.theme.TealDark
import com.example.arigo.core.theme.TealHeader
import com.example.arigo.core.theme.White
import com.example.arigo.domain.model.AqiStatus
import com.example.arigo.domain.model.EnvironmentInfo
import com.example.arigo.presentation.components.AuthPillButton
import java.util.Locale

private val WeatherCardBottomBg = Color(0xFFFFFFFF)
private val AlertBg = Color(0xFFD6E8F0)
private val CoBlue = Color(0xFF42A5F5)
private val WeatherTopGradientStart = Color(0xFF1B6B93) // TealHeader
private val WeatherTopGradientEnd = Color(0xFF2E9BBE)   // lighter teal

@Composable
fun HomeScreen(
    onDeviceClick: (String) -> Unit,
    onAddDeviceClick: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onNotificationsClick: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .verticalScroll(rememberScrollState())
    ) {
        HomeHeader(userName = state.userName)

        Spacer(modifier = Modifier.height(12.dp))

        WeatherCard(env = state.environmentInfo)

        Spacer(modifier = Modifier.height(20.dp))

        SectionDivider()

        Spacer(modifier = Modifier.height(12.dp))

        MyDevicesHeader(onAddClick = onAddDeviceClick)

        Spacer(modifier = Modifier.height(16.dp))

        if (state.hasDevices) {
            state.devices.forEach { device ->
                DeviceCard(
                    device = device,
                    onClick = { onDeviceClick(device.deviceId) },
                    onPowerToggle = { viewModel.togglePurifier(device.deviceId) },
                    onAutoToggle = { viewModel.toggleAutoMode(device.deviceId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        } else {
            AuthPillButton(
                text = "Add a device",
                isLoading = false,
                onClick = onAddDeviceClick,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(180.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ---- Header --------------------------------------------------------------

@Composable
private fun HomeHeader(userName: String) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp + topInset)
            .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
            .background(MintGreen)
            .padding(top = topInset)
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Text(
                text = "AriGo",
                fontSize = 24.sp,
                color = TealDark,
                fontFamily = RacingSansOne
            )
            Text(
                text = if (userName.isNotBlank()) "Hello, $userName" else "Hello!",
                fontSize = 18.sp,
                color = TealDark,
                fontFamily = RadioCanada,
                fontWeight = FontWeight.Medium
            )
        }
        Image(
            painter = painterResource(id = R.drawable.arigo_logo),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(40.dp)
        )
    }
}

// ---- Weather card --------------------------------------------------------

@Composable
private fun WeatherCard(env: EnvironmentInfo) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, LightGray, RoundedCornerShape(12.dp))
    ) {
        // Top half — teal horizontal gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(WeatherTopGradientStart, WeatherTopGradientEnd)
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formatCityTemp(env),
                        color = White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        WeatherBadge(label = "AQI", value = env.aqiStatus.label)
                        WeatherBadge(label = "Humidity", value = "${env.humidity.toInt()}%")
                    }
                }
                Image(
                    painter = painterResource(id = R.drawable.ic_sun),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Bottom half — light pink/white with four metric columns
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WeatherCardBottomBg)
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MetricColumn(
                label = "AQI",
                value = env.aqi.toString(),
                unit = "/500",
                progress = (env.aqi / 500f).coerceIn(0f, 1f),
                color = AqiGood,
                modifier = Modifier.weight(1f)
            )
            MetricColumn(
                label = "PM2.5",
                value = env.dustDensity.toInt().toString(),
                unit = "µg/m³",
                progress = (env.dustDensity / 100.0).coerceIn(0.0, 1.0).toFloat(),
                color = AqiNormal,
                modifier = Modifier.weight(1f)
            )
            MetricColumn(
                label = "CO",
                value = formatCo(env.coPpm),
                unit = "ppm",
                progress = (env.coPpm / 50.0).coerceIn(0.0, 1.0).toFloat(),
                color = CoBlue,
                modifier = Modifier.weight(1f)
            )
            MetricColumn(
                label = "Ozone",
                value = "—",
                unit = "DU",
                progress = 0f,
                color = AqiBad,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun formatCo(coPpm: Double): String {
    if (coPpm <= 0.0) return "—"
    return if (coPpm < 10.0) String.format(Locale.US, "%.1f", coPpm) else coPpm.toInt().toString()
}

@Composable
private fun WeatherBadge(label: String, value: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(White.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            color = White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MetricColumn(
    label: String,
    value: String,
    unit: String,
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = GrayText,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                color = TealDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = unit,
                color = GrayText,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        ProgressBar(progress = progress, color = color)
    }
}

@Composable
private fun ProgressBar(progress: Float, color: Color) {
    val safeProgress = progress.coerceIn(0f, 1f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(LightGray)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(safeProgress)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
    }
}

// ---- My Devices section --------------------------------------------------

@Composable
private fun SectionDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(1.dp)
            .background(LightGray)
    )
}

@Composable
private fun MyDevicesHeader(onAddClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "My Devices",
            color = TealDark,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        IconButton(onClick = onAddClick) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add device",
                tint = TealHeader
            )
        }
    }
}

// ---- Device card ---------------------------------------------------------

@Composable
private fun DeviceCard(
    device: DeviceCardData,
    onClick: () -> Unit,
    onPowerToggle: () -> Unit,
    onAutoToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, TealHeader, RoundedCornerShape(10.dp))
            .background(CardGreen)
            .clickable(onClick = onClick)
    ) {
        // Top section: fan icon + Filter Pro title + AQI / PM2.5 metrics
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Fan icon
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .border(1.dp, LightGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Air,
                    contentDescription = null,
                    tint = TealHeader,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title + metrics
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.deviceName,
                    color = TealDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DeviceMetric(
                        label = "AQI",
                        value = "${device.aqi}",
                        unit = "/500",
                        statusLabel = device.aqiStatus.label,
                        statusBg = badgeBgFor(device.aqiStatus),
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(60.dp)
                            .background(LightGray)
                    )
                    DeviceMetric(
                        label = "PM2.5",
                        value = device.pm25.toInt().toString(),
                        unit = "µg/m³",
                        statusLabel = device.pm25Status.label,
                        statusBg = badgeBgFor(device.pm25Status),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Bottom strip: power, auto, alert
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = LightGray, shape = RoundedCornerShape(0.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CircleToggleButton(
                icon = Icons.Filled.PowerSettingsNew,
                isOn = device.motorState,
                onClick = onPowerToggle
            )
            CircleToggleButton(
                icon = Icons.Filled.AutoMode,
                isOn = device.isAutoMode,
                onClick = onAutoToggle
            )
            AlertPill(
                aqi = device.aqi,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DeviceMetric(
    label: String,
    value: String,
    unit: String,
    statusLabel: String,
    statusBg: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, color = GrayText, fontSize = 12.sp)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                color = TealDark,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = unit,
                color = GrayText,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(statusBg)
                .padding(horizontal = 10.dp, vertical = 2.dp)
        ) {
            Text(
                text = statusLabel,
                color = White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CircleToggleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isOn: Boolean,
    onClick: () -> Unit
) {
    val tint = if (isOn) AqiGood else GrayText
    val borderColor = if (isOn) AqiGood else LightGray
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .border(1.dp, borderColor, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun AlertPill(aqi: Int, modifier: Modifier = Modifier) {
    val message = if (aqi > 50) "Alert ! Wear your mask !" else "No mask is required now"
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(AlertBg)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = TealDark,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ---- Helpers -------------------------------------------------------------

private fun formatCityTemp(env: EnvironmentInfo): String {
    val city = env.city.ifBlank { "—" }
    val temp = if (env.temperature == 0.0) "—" else "${env.temperature.toInt()}°C"
    return "$city, $temp"
}

private fun badgeBgFor(status: AqiStatus): Color = when (status) {
    AqiStatus.GOOD -> GreenBadge
    AqiStatus.NORMAL -> TealBadge
    AqiStatus.BAD -> AqiBad
    AqiStatus.HAZARDOUS -> AqiHazardous
}

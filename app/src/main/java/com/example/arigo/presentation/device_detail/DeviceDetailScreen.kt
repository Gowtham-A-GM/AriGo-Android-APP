package com.example.arigo.presentation.device_detail

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.arigo.R
import com.example.arigo.core.theme.AqiBad
import com.example.arigo.core.theme.AqiGood
import com.example.arigo.core.theme.AqiNormal
import com.example.arigo.core.theme.GrayText
import com.example.arigo.core.theme.LightGray
import com.example.arigo.core.theme.MintGreen
import com.example.arigo.core.theme.TealBadge
import com.example.arigo.core.theme.TealDark
import com.example.arigo.core.theme.White
import com.example.arigo.domain.model.AqiStatus

@Composable
fun DeviceDetailScreen(
    @Suppress("UNUSED_PARAMETER") deviceId: String,
    onBackClick: () -> Unit,
    onAirQualityClick: () -> Unit,
    onFilterHealthClick: () -> Unit,
    viewModel: DeviceDetailViewModel = viewModel(factory = DeviceDetailViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val palette = paletteFor(state.aqiStatus)

    val animatedBg by animateColorAsState(
        targetValue = palette.background,
        animationSpec = tween(durationMillis = 400),
        label = "aqi-bg"
    )
    val animatedAccent by animateColorAsState(
        targetValue = palette.accent,
        animationSpec = tween(durationMillis = 400),
        label = "aqi-accent"
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(White)) {
        // Top section is a fixed ~40% of viewport height so weight() spacers
        // inside it have a known available space to fill.
        val topSectionHeight = maxHeight * 0.40f

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            TopColoredSection(
                background = animatedBg,
                accent = animatedAccent,
                aqiStatus = state.aqiStatus,
                title = state.deviceName,
                statusMessage = state.statusMessage,
                onBackClick = onBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(topSectionHeight)
            )

            // Cards are pulled up to overlap the curved bottom of the colored section.
            // Offset shifts the entire subtree, so the CONTROLS card follows.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-30).dp)
            ) {
                AirQualityCard(
                    aqi = state.aqi,
                    pm25 = state.pm25,
                    pm10 = state.pm10,
                    co = state.co,
                    no2 = state.no2,
                    so2 = state.so2,
                    onClick = onAirQualityClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                ControlsSection(
                    motorState = state.motorState,
                    isAutoMode = state.isAutoMode,
                    onPowerToggle = viewModel::togglePurifier,
                    onAutoToggle = viewModel::toggleAutoMode,
                    onInfoClick = onFilterHealthClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// ---- Top section --------------------------------------------------------

@Composable
private fun TopColoredSection(
    background: Color,
    accent: Color,
    aqiStatus: AqiStatus,
    title: String,
    statusMessage: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // GOOD = outlined badge (white bg, accent text + border).
    // NORMAL/BAD/HAZARDOUS = filled badge (accent bg, white text).
    val isOutlinedBadge = aqiStatus == AqiStatus.GOOD
    val badgeBg = if (isOutlinedBadge) White else accent
    val badgeContent = if (isOutlinedBadge) accent else White
    val badgeBorder = if (isOutlinedBadge) accent else Color.Transparent

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(background)
            .padding(top = topInset)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Top: back arrow + title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                contentDescription = "Back",
                tint = TealDark,
                modifier = Modifier
                    .size(22.dp)
                    .clickable(onClick = onBackClick)
            )
            Text(
                text = title,
                color = TealDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 22.dp)
            )
        }

        // Push the badge + message group down to sit just above the curved bottom
        Spacer(modifier = Modifier.weight(1f))

        // AQI status pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(badgeBg)
                .border(1.5.dp, badgeBorder, RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "AQI ${aqiStatus.label}",
                color = badgeContent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = statusMessage,
            color = TealDark,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp) // brings effective horizontal pad to 24dp
        )

        // Breathing room above the curve so the message doesn't sit directly
        // on the rounded edge.
        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ---- Air quality card ---------------------------------------------------

@Composable
private fun AirQualityCard(
    aqi: Int,
    pm25: Double,
    pm10: String,
    co: Double,
    no2: Double,
    so2: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(White)
            .border(1.dp, LightGray, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        // Header: AIR QUALITY + chevron
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AIR QUALITY",
                color = TealDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open analytics",
                tint = TealDark,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        MetricGridRow(
            cells = listOf(
                MetricCell(
                    label = "AQI",
                    value = aqi.toString(),
                    unit = "/500",
                    progress = aqi / 500f,
                    barColor = colorForAqi(aqi)
                ),
                MetricCell(
                    label = "PM2.5",
                    value = pm25.toInt().toString(),
                    unit = "µg/m³",
                    progress = (pm25 / 100.0).toFloat(),
                    barColor = colorForPm25(pm25)
                ),
                MetricCell(
                    label = "PM10",
                    value = pm10,
                    unit = "µg/m³",
                    progress = 0f,
                    barColor = LightGray,
                    valueIsPlaceholder = pm10 == "N/A"
                )
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        MetricGridRow(
            cells = listOf(
                MetricCell(
                    label = "CO",
                    value = formatDecimal(co),
                    unit = "ppm",
                    progress = (co / 50.0).toFloat(),
                    barColor = colorForCo(co)
                ),
                MetricCell(
                    label = "NO2",
                    value = formatDecimal(no2),
                    unit = "µg/m³",
                    progress = (no2 / 200.0).toFloat(),
                    barColor = colorForNo2(no2)
                ),
                MetricCell(
                    label = "SO2",
                    value = so2,
                    unit = "µg/m³",
                    progress = 0f,
                    barColor = LightGray,
                    valueIsPlaceholder = so2 == "N/A"
                )
            )
        )
    }
}

private data class MetricCell(
    val label: String,
    val value: String,
    val unit: String,
    val progress: Float,
    val barColor: Color,
    val valueIsPlaceholder: Boolean = false
)

@Composable
private fun MetricGridRow(cells: List<MetricCell>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        cells.forEach { cell ->
            MetricCellView(cell = cell, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetricCellView(cell: MetricCell, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = cell.label, color = GrayText, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = if (cell.valueIsPlaceholder) "—" else cell.value,
                color = if (cell.valueIsPlaceholder) GrayText else TealDark,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = cell.unit,
                color = GrayText,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        if (cell.valueIsPlaceholder) {
            FlatGrayBar()
        } else {
            ProgressBar(progress = cell.progress, color = cell.barColor)
        }
    }
}

@Composable
private fun ProgressBar(progress: Float, color: Color) {
    val safe = progress.coerceIn(0f, 1f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(LightGray)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(safe)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
    }
}

@Composable
private fun FlatGrayBar(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(LightGray)
    )
}

// ---- Controls -----------------------------------------------------------

@Composable
private fun ControlsSection(
    motorState: Boolean,
    isAutoMode: Boolean,
    onPowerToggle: () -> Unit,
    onAutoToggle: () -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(White)
            .border(1.dp, LightGray, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CONTROLS",
                color = TealDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "Filter health",
                tint = TealDark,
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onInfoClick)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ControlButton(
                label = if (motorState) "POWER ON" else "POWER OFF",
                iconRes = R.drawable.ic_power,
                isOn = motorState,
                onClick = onPowerToggle,
                modifier = Modifier.weight(1f)
            )
            ControlButton(
                label = "AUTO",
                iconRes = R.drawable.ic_auto,
                isOn = isAutoMode,
                onClick = onAutoToggle,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ControlButton(
    label: String,
    @DrawableRes iconRes: Int,
    isOn: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = TealBadge // #81BFDA
    val bg = if (isOn) activeColor else Color.Transparent
    val contentColor = if (isOn) White else GrayText
    val borderColor = if (isOn) Color.Transparent else LightGray

    Row(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            colorFilter = ColorFilter.tint(contentColor),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            color = contentColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ---- Palette ------------------------------------------------------------

private data class AqiPalette(
    val background: Color,
    val accent: Color
)

private fun paletteFor(status: AqiStatus): AqiPalette = when (status) {
    AqiStatus.GOOD -> AqiPalette(
        background = MintGreen,
        accent = Color(0xFF4CAF50)
    )
    AqiStatus.NORMAL -> AqiPalette(
        background = Color(0xFFFFF9C4),
        accent = Color(0xFFFFC107)
    )
    AqiStatus.BAD -> AqiPalette(
        background = Color(0xFFFFCDD2),
        accent = Color(0xFFFF5252)
    )
    AqiStatus.HAZARDOUS -> AqiPalette(
        background = Color(0xFFE1BEE7),
        accent = Color(0xFF9C27B0)
    )
}

// ---- Per-metric color helpers ------------------------------------------

private fun colorForAqi(aqi: Int): Color = when {
    aqi <= 50 -> AqiGood
    aqi <= 100 -> AqiNormal
    else -> AqiBad
}

private fun colorForPm25(value: Double): Color = when {
    value <= 30.0 -> AqiGood
    value <= 60.0 -> AqiNormal
    else -> AqiBad
}

private fun colorForCo(value: Double): Color = when {
    value <= 10.0 -> AqiGood
    value <= 30.0 -> AqiNormal
    else -> AqiBad
}

private fun colorForNo2(value: Double): Color = when {
    value <= 40.0 -> AqiGood
    value <= 80.0 -> AqiNormal
    else -> AqiBad
}

private fun formatDecimal(value: Double): String {
    if (value <= 0.0) return "—"
    return if (value < 10.0) "%.1f".format(value) else value.toInt().toString()
}

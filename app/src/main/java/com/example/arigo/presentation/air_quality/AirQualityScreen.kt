package com.example.arigo.presentation.air_quality

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.arigo.core.theme.GrayText
import com.example.arigo.core.theme.LightGray
import com.example.arigo.core.theme.TealDark
import com.example.arigo.core.theme.TealHeader
import com.example.arigo.core.theme.White
import com.example.arigo.domain.model.AirQualityChartPoint
import com.example.arigo.domain.model.AqiStatus

private val BeforeLineColor = Color(0xFF37474F)
private val AfterLineColor = Color(0xFF4CAF50)
private val GreenZone = Color(0xFFE8F5E9)
private val YellowZone = Color(0xFFFFF8E1)
private val RedZone = Color(0xFFFFEBEE)
private val TabsTrackBg = Color(0xFFE6EEF1)

private val ChartHeight = 180.dp
private val YAxisWidth = 36.dp

@Composable
fun AirQualityScreen(
    @Suppress("UNUSED_PARAMETER") deviceId: String,
    onBackClick: () -> Unit,
    viewModel: AirQualityViewModel = viewModel(factory = AirQualityViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        TopBar(onBackClick = onBackClick)

        Spacer(modifier = Modifier.height(16.dp))

        TimeRangeTabs(
            selected = state.selectedTimeRange,
            onSelect = viewModel::onTimeRangeChange,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        when {
            state.isLoading -> LoadingState()
            state.error != null && state.chartData.isEmpty() -> EmptyState(state.error ?: "No data")
            else -> ChartList(state)
        }
    }
}

@Composable
private fun TopBar(onBackClick: () -> Unit) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topInset)
            .padding(horizontal = 20.dp, vertical = 16.dp),
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
            text = "Air Quality",
            color = TealDark,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .padding(end = 22.dp)
        )
    }
}

@Composable
private fun TimeRangeTabs(
    selected: TimeRange,
    onSelect: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(TabsTrackBg)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TimeRange.entries.forEach { range ->
            val isSelected = range == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isSelected) TealHeader else Color.Transparent)
                    .clickable { onSelect(range) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = range.label,
                    color = if (isSelected) White else TealDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ---- States --------------------------------------------------------------

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = TealHeader)
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = GrayText, fontSize = 14.sp)
    }
}

// ---- Chart sections list -------------------------------------------------

@Composable
private fun ChartList(state: AirQualityState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SectionDivider()
        ChartSection(
            label = "AQI",
            value = state.currentAqi.toString(),
            unit = "/500",
            tooltipUnit = "AQI",
            status = state.currentAqiStatus,
            data = state.chartData,
            timeRange = state.selectedTimeRange,
            beforeSelector = { it.beforeAqi.toDouble() },
            afterSelector = { it.afterAqi },
            maxValue = 100f,
            yAxisLabels = listOf(0, 30, 50, 100)
        )

        SectionDivider()
        ChartSection(
            label = "PM2.5",
            value = state.currentPm25.toInt().toString(),
            unit = "µg/m³",
            tooltipUnit = "µg/m³",
            status = state.currentPm25Status,
            data = state.chartData,
            timeRange = state.selectedTimeRange,
            beforeSelector = { it.beforeDust },
            afterSelector = { it.afterDust },
            maxValue = 150f,
            yAxisLabels = listOf(0, 35, 100, 150)
        )

        SectionDivider()
        ChartSection(
            label = "Carbon Monoxide (CO)",
            value = formatDecimal(state.currentCo),
            unit = "ppm",
            tooltipUnit = "ppm",
            status = state.currentCoStatus,
            data = state.chartData,
            timeRange = state.selectedTimeRange,
            beforeSelector = { it.beforeCo },
            afterSelector = { it.afterCo },
            maxValue = 100f,
            yAxisLabels = listOf(0, 30, 50, 100)
        )

        SectionDivider()
        Pm10PlaceholderSection()

        Spacer(modifier = Modifier.height(40.dp))
    }
}

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
private fun ChartSection(
    label: String,
    value: String,
    unit: String,
    tooltipUnit: String,
    status: AqiStatus,
    data: List<AirQualityChartPoint>,
    timeRange: TimeRange,
    beforeSelector: (AirQualityChartPoint) -> Double,
    afterSelector: (AirQualityChartPoint) -> Double,
    maxValue: Float,
    yAxisLabels: List<Int>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, color = GrayText, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        color = TealDark,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        color = GrayText,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
            StatusBadge(status = status)
        }

        Spacer(modifier = Modifier.height(20.dp))

        AirQualityChart(
            dataPoints = data,
            beforeValueSelector = beforeSelector,
            afterValueSelector = afterSelector,
            maxValue = maxValue,
            yAxisLabels = yAxisLabels,
            timeRange = timeRange,
            tooltipUnit = tooltipUnit
        )

        Spacer(modifier = Modifier.height(12.dp))

        ChartLegend()
    }
}

@Composable
private fun Pm10PlaceholderSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "PM10", color = GrayText, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "—",
                        color = GrayText,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "µg/m³",
                        color = GrayText,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ChartHeight)
                .clip(RoundedCornerShape(8.dp))
                .background(LightGray.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text("No data available", color = GrayText, fontSize = 13.sp)
        }
    }
}

@Composable
private fun StatusBadge(status: AqiStatus) {
    val bg = when (status) {
        AqiStatus.GOOD -> Color(0xFFA5D6A7)
        AqiStatus.NORMAL -> Color(0xFFFFE082)
        AqiStatus.BAD -> Color(0xFFEF9A9A)
        AqiStatus.HAZARDOUS -> Color(0xFFCE93D8)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = status.label,
            color = TealDark,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ---- Chart drawing -------------------------------------------------------

@Composable
private fun AirQualityChart(
    dataPoints: List<AirQualityChartPoint>,
    beforeValueSelector: (AirQualityChartPoint) -> Double,
    afterValueSelector: (AirQualityChartPoint) -> Double,
    maxValue: Float,
    yAxisLabels: List<Int>,
    timeRange: TimeRange,
    tooltipUnit: String
) {
    val xLabels = downsampleLabels(dataPoints, timeRange)
    // Each chart owns its own selection state. remember(dataPoints) resets on
    // tab change since groupByTimeRange returns a new list.
    var selectedIndex by remember(dataPoints) { mutableStateOf<Int?>(null) }
    val density = LocalDensity.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Chart canvas
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .height(ChartHeight)
            ) {
                val chartWidthDp = maxWidth
                val chartWidthPx = with(density) { chartWidthDp.toPx() }
                val n = dataPoints.size

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(dataPoints) {
                            detectTapGestures(
                                onTap = { offset ->
                                    if (n < 2) return@detectTapGestures
                                    val ratio = (offset.x / size.width).coerceIn(0f, 1f)
                                    val idx = (ratio * (n - 1)).roundToInt().coerceIn(0, n - 1)
                                    selectedIndex = if (selectedIndex == idx) null else idx
                                },
                                onLongPress = { offset ->
                                    if (n < 2) return@detectTapGestures
                                    val ratio = (offset.x / size.width).coerceIn(0f, 1f)
                                    val idx = (ratio * (n - 1)).roundToInt().coerceIn(0, n - 1)
                                    selectedIndex = idx
                                }
                            )
                        }
                ) {
                    drawZones()
                    drawChartLines(
                        dataPoints = dataPoints,
                        beforeSelector = beforeValueSelector,
                        afterSelector = afterValueSelector,
                        maxValue = maxValue,
                        strokeWidthPx = 2.dp.toPx()
                    )

                    val sel = selectedIndex
                    if (sel != null && n >= 2) {
                        val x = sel * size.width / (n - 1)
                        // Dashed vertical indicator line spanning the chart
                        drawLine(
                            color = GrayText,
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(
                                floatArrayOf(8.dp.toPx(), 4.dp.toPx()),
                                0f
                            )
                        )
                        // Dots on each line at the selection X
                        val beforeY = (size.height -
                            (beforeValueSelector(dataPoints[sel]) / maxValue * size.height).toFloat())
                            .coerceIn(0f, size.height)
                        val afterY = (size.height -
                            (afterValueSelector(dataPoints[sel]) / maxValue * size.height).toFloat())
                            .coerceIn(0f, size.height)
                        drawCircle(BeforeLineColor, radius = 5.dp.toPx(), center = Offset(x, beforeY))
                        drawCircle(White, radius = 2.5.dp.toPx(), center = Offset(x, beforeY))
                        drawCircle(AfterLineColor, radius = 5.dp.toPx(), center = Offset(x, afterY))
                        drawCircle(White, radius = 2.5.dp.toPx(), center = Offset(x, afterY))
                    }
                }

                // Tooltip overlay
                val sel = selectedIndex
                if (sel != null && sel < dataPoints.size) {
                    val point = dataPoints[sel]
                    val tooltipApproxWidth = 150.dp
                    val tooltipXPx = if (n >= 2) sel.toFloat() / (n - 1) * chartWidthPx else 0f
                    val tooltipXDp = with(density) { tooltipXPx.toDp() }
                    val maxLeft = (chartWidthDp - tooltipApproxWidth).coerceAtLeast(0.dp)
                    val tooltipLeft = (tooltipXDp - tooltipApproxWidth / 2).coerceIn(0.dp, maxLeft)

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = tooltipLeft, y = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(White)
                            .border(1.dp, LightGray, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = "Time: ${point.timestamp}",
                                color = TealDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Before: ${formatTooltipValue(beforeValueSelector(point))} $tooltipUnit",
                                color = BeforeLineColor,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "After: ${formatTooltipValue(afterValueSelector(point))} $tooltipUnit",
                                color = AfterLineColor,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Y-axis labels positioned at exact value heights
            BoxWithConstraints(
                modifier = Modifier
                    .width(YAxisWidth)
                    .height(ChartHeight)
            ) {
                val total = maxHeight
                yAxisLabels.forEach { labelValue ->
                    val ratio = 1f - (labelValue.toFloat() / maxValue).coerceIn(0f, 1f)
                    Text(
                        text = labelValue.toString(),
                        color = GrayText,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(y = total * ratio - 6.dp)
                            .padding(start = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // X-axis labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = YAxisWidth),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            xLabels.forEach {
                Text(it, color = GrayText, fontSize = 10.sp)
            }
        }
    }
}

private fun formatTooltipValue(v: Double): String {
    if (v.isNaN()) return "—"
    return if (v >= 10.0 || v <= 0.0) v.toInt().toString() else "%.2f".format(v)
}

/**
 * Paints the green/yellow/red zone backgrounds (bottom-up).
 * Green: 0–30% of chart height, Yellow: 30–60%, Red: 60–100%.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawZones() {
    val w = size.width
    val h = size.height
    drawRect(color = RedZone, topLeft = Offset(0f, 0f), size = Size(w, h * 0.40f))
    drawRect(color = YellowZone, topLeft = Offset(0f, h * 0.40f), size = Size(w, h * 0.30f))
    drawRect(color = GreenZone, topLeft = Offset(0f, h * 0.70f), size = Size(w, h * 0.30f))
}

/**
 * Draws the before- and after-filtration smoothed lines over the zones.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawChartLines(
    dataPoints: List<AirQualityChartPoint>,
    beforeSelector: (AirQualityChartPoint) -> Double,
    afterSelector: (AirQualityChartPoint) -> Double,
    maxValue: Float,
    strokeWidthPx: Float
) {
    if (dataPoints.size < 2) return
    val w = size.width
    val h = size.height
    val n = dataPoints.size

    val beforePts = dataPoints.mapIndexed { i, p ->
        Offset(
            x = i * w / (n - 1),
            y = (h - (beforeSelector(p) / maxValue * h).toFloat()).coerceIn(0f, h)
        )
    }
    val afterPts = dataPoints.mapIndexed { i, p ->
        Offset(
            x = i * w / (n - 1),
            y = (h - (afterSelector(p) / maxValue * h).toFloat()).coerceIn(0f, h)
        )
    }

    drawPath(
        path = smoothPath(beforePts),
        color = BeforeLineColor,
        style = Stroke(width = strokeWidthPx)
    )
    drawPath(
        path = smoothPath(afterPts),
        color = AfterLineColor,
        style = Stroke(width = strokeWidthPx)
    )
}

/**
 * Builds a smooth path through [points] using midpoint cubic Bezier control
 * points. Faster and more visually pleasant than straight lineTo.
 */
private fun smoothPath(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path
    path.moveTo(points[0].x, points[0].y)
    for (i in 0 until points.size - 1) {
        val p1 = points[i]
        val p2 = points[i + 1]
        val midX = (p1.x + p2.x) / 2f
        path.cubicTo(midX, p1.y, midX, p2.y, p2.x, p2.y)
    }
    return path
}

@Composable
private fun ChartLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem("Before Filtration", BeforeLineColor)
        LegendItem("After Filtration", AfterLineColor)
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(16.dp)
                .height(2.dp)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, color = GrayText, fontSize = 11.sp)
    }
}

// ---- Helpers -------------------------------------------------------------

/**
 * Reduces the data points to ~10 X-axis labels evenly spaced across the chart.
 * The label format depends on the active [TimeRange]:
 *   • SECONDS: "SS" (the seconds component, e.g. "22")
 *   • MINUTES: "HH:mm" (e.g. "09:34")
 *   • HOURS:   "HH" (e.g. "09")
 */
private fun downsampleLabels(
    points: List<AirQualityChartPoint>,
    timeRange: TimeRange
): List<String> {
    if (points.isEmpty()) return emptyList()
    val targetCount = 10
    val pickedPoints = if (points.size <= targetCount) {
        points
    } else {
        val step = (points.size - 1).toFloat() / (targetCount - 1).toFloat()
        (0 until targetCount).map { i ->
            val idx = (i * step).toInt().coerceIn(0, points.lastIndex)
            points[idx]
        }
    }
    return pickedPoints.map { formatXAxisLabel(it.timestamp, timeRange) }
}

private fun formatXAxisLabel(timestamp: String, timeRange: TimeRange): String {
    if (timestamp.isEmpty()) return ""
    return when (timeRange) {
        // Raw point timestamp is "HH:mm:ss" — show just the seconds digit.
        TimeRange.SECONDS -> timestamp.substringAfterLast(':', timestamp)
        // ViewModel grouped key is already "HH:mm".
        TimeRange.MINUTES -> timestamp.take(5)
        // ViewModel grouped key is already "HH".
        TimeRange.HOURS -> timestamp.take(2)
    }
}

private fun formatDecimal(value: Double): String {
    if (value <= 0.0) return "—"
    return if (value < 10.0) "%.1f".format(value) else value.toInt().toString()
}

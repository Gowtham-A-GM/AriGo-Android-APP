package com.example.arigo.presentation.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.arigo.R
import com.example.arigo.core.theme.BlackText
import com.example.arigo.core.theme.GrayText
import com.example.arigo.core.theme.LightGray
import com.example.arigo.core.theme.MintGreen
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
private val SearchFieldBg = Color(0xFFEFFAEF)

private val ChartHeight = 180.dp
private val YAxisWidth = 36.dp

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .verticalScroll(rememberScrollState())
    ) {
        HistoryHeader()

        Spacer(modifier = Modifier.height(16.dp))

        PeriodTabs(
            selected = state.selectedPeriod,
            onSelect = viewModel::onPeriodChange,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        SectionDivider()

        DateNavigationRow(
            label = state.selectedDate,
            onPrev = viewModel::onNavigateBackward,
            onNext = viewModel::onNavigateForward
        )

        SectionDivider()

        Spacer(modifier = Modifier.height(16.dp))

        CitySearchField(
            value = state.searchCity,
            onChange = viewModel::onSearchCityChange,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        SectionDivider()

        ChartSection(
            label = "AQI",
            city = state.city,
            value = state.currentAqi.toString(),
            unit = "/500",
            status = state.currentAqiStatus,
            data = state.chartData,
            beforeSelector = { it.beforeAqi.toDouble() },
            afterSelector = { it.afterAqi },
            maxValue = 100f,
            yAxisLabels = listOf(0, 30, 50, 100)
        )

        SectionDivider()

        ChartSection(
            label = "PM2.5",
            city = state.city,
            value = state.currentPm25.toInt().toString(),
            unit = "µg/m³",
            status = state.currentPm25Status,
            data = state.chartData,
            beforeSelector = { it.beforeDust },
            afterSelector = { it.afterDust },
            maxValue = 150f,
            yAxisLabels = listOf(0, 35, 100, 150)
        )

        SectionDivider()

        ChartSection(
            label = "Carbon Monoxide (CO)",
            city = state.city,
            value = formatDecimal(state.currentCo),
            unit = "ppm",
            status = state.currentCoStatus,
            data = state.chartData,
            beforeSelector = { it.beforeCo },
            afterSelector = { it.afterCo },
            maxValue = 100f,
            yAxisLabels = listOf(0, 30, 50, 100)
        )

        SectionDivider()
        Pm10PlaceholderSection(city = state.city)

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ---- Header --------------------------------------------------------------

@Composable
private fun HistoryHeader() {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
            .background(MintGreen)
            .padding(top = topInset)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Text(
            text = "HISTORY",
            color = White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 5.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.arigo_logo),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(40.dp)
        )
    }
}

// ---- Period tabs ---------------------------------------------------------

@Composable
private fun PeriodTabs(
    selected: HistoryPeriod,
    onSelect: (HistoryPeriod) -> Unit,
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
        HistoryPeriod.entries.forEach { period ->
            val isSelected = period == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isSelected) TealHeader else Color.Transparent)
                    .clickable { onSelect(period) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = period.label,
                    color = if (isSelected) White else TealDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ---- Date nav -----------------------------------------------------------

@Composable
private fun DateNavigationRow(
    label: String,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "Previous",
            tint = TealDark,
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onPrev)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            color = TealDark,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.width(12.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Next",
            tint = TealDark,
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onNext)
        )
    }
}

// ---- Search field --------------------------------------------------------

@Composable
private fun CitySearchField(
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        singleLine = true,
        placeholder = { Text("Search with city", color = GrayText, fontSize = 14.sp) },
        leadingIcon = {
            Icon(Icons.Filled.Search, contentDescription = null, tint = TealHeader)
        },
        textStyle = TextStyle(color = BlackText, fontSize = 14.sp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = TealHeader,
            unfocusedBorderColor = TealHeader,
            cursorColor = TealHeader,
            focusedContainerColor = SearchFieldBg,
            unfocusedContainerColor = SearchFieldBg,
            focusedTextColor = BlackText,
            unfocusedTextColor = BlackText
        ),
        keyboardOptions = KeyboardOptions.Default
    )
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

// ---- Chart section -------------------------------------------------------

@Composable
private fun ChartSection(
    label: String,
    city: String,
    value: String,
    unit: String,
    status: AqiStatus,
    data: List<AirQualityChartPoint>,
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
                if (city.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = city, color = GrayText, fontSize = 12.sp)
                }
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

        if (data.isEmpty()) {
            EmptyChartPlaceholder()
        } else {
            HistoryChart(
                dataPoints = data,
                beforeSelector = beforeSelector,
                afterSelector = afterSelector,
                maxValue = maxValue,
                yAxisLabels = yAxisLabels
            )
            Spacer(modifier = Modifier.height(12.dp))
            ChartLegend()
        }
    }
}

@Composable
private fun Pm10PlaceholderSection(city: String) {
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
                if (city.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = city, color = GrayText, fontSize = 12.sp)
                }
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
private fun EmptyChartPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ChartHeight)
            .clip(RoundedCornerShape(8.dp))
            .background(LightGray.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Text("No data for this period", color = GrayText, fontSize = 13.sp)
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
private fun HistoryChart(
    dataPoints: List<AirQualityChartPoint>,
    beforeSelector: (AirQualityChartPoint) -> Double,
    afterSelector: (AirQualityChartPoint) -> Double,
    maxValue: Float,
    yAxisLabels: List<Int>
) {
    val xLabels = downsampleLabels(dataPoints)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(ChartHeight)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawZones()
                    drawChartLines(
                        dataPoints = dataPoints,
                        beforeSelector = beforeSelector,
                        afterSelector = afterSelector,
                        maxValue = maxValue,
                        strokeWidthPx = 2.dp.toPx()
                    )
                }
            }

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

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawZones() {
    val w = size.width
    val h = size.height
    drawRect(color = RedZone, topLeft = Offset(0f, 0f), size = Size(w, h * 0.40f))
    drawRect(color = YellowZone, topLeft = Offset(0f, h * 0.40f), size = Size(w, h * 0.30f))
    drawRect(color = GreenZone, topLeft = Offset(0f, h * 0.70f), size = Size(w, h * 0.30f))
}

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

private fun downsampleLabels(points: List<AirQualityChartPoint>): List<String> {
    if (points.isEmpty()) return emptyList()
    val targetCount = 10
    val picked = if (points.size <= targetCount) {
        points
    } else {
        val step = (points.size - 1).toFloat() / (targetCount - 1).toFloat()
        (0 until targetCount).map { i ->
            val idx = (i * step).toInt().coerceIn(0, points.lastIndex)
            points[idx]
        }
    }
    // History stores its display label directly in `timestamp` during grouping.
    return picked.map { it.timestamp }
}

private fun formatDecimal(value: Double): String {
    if (value <= 0.0) return "—"
    return if (value < 10.0) "%.1f".format(value) else value.toInt().toString()
}

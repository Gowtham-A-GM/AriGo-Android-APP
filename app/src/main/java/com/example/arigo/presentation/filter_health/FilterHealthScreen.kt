package com.example.arigo.presentation.filter_health

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.example.arigo.core.theme.BlackText
import com.example.arigo.core.theme.LightGray
import com.example.arigo.core.theme.TealDark
import com.example.arigo.core.theme.TealHeader
import com.example.arigo.core.theme.White
import com.example.arigo.presentation.components.AuthPillButton

private val FilterImageBg = Color(0xFFF5F5F5)

@Composable
fun FilterHealthScreen(
    @Suppress("UNUSED_PARAMETER") deviceId: String,
    onBackClick: () -> Unit,
    viewModel: FilterHealthViewModel = viewModel(factory = FilterHealthViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .verticalScroll(rememberScrollState())
    ) {
        TopBar(onBackClick = onBackClick)

        Spacer(modifier = Modifier.height(8.dp))

        FilterImageCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "AriGo Filter",
            color = TealDark,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = state.filterDescription,
            color = BlackText,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        EfficiencySection(percent = state.efficiencyPercent)

        Spacer(modifier = Modifier.height(24.dp))

        AuthPillButton(
            text = "Replace new filter",
            isLoading = false,
            onClick = {
                Toast.makeText(
                    context,
                    "Contact support for filter replacement",
                    Toast.LENGTH_SHORT
                ).show()
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .height(48.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))
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
            text = "Device Health",
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
private fun FilterImageCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(FilterImageBg),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_filter),
            contentDescription = "AriGo Filter",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(180.dp)
        )
    }
}

@Composable
private fun EfficiencySection(percent: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "Efficiency of filter",
            color = TealHeader,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$percent%",
            color = TealDark,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        EfficiencyBar(percent = percent)
    }
}

@Composable
private fun EfficiencyBar(percent: Int) {
    val safe = percent.coerceIn(0, 100)
    val fillColor = colorForEfficiency(safe)
    val gradient = Brush.horizontalGradient(
        colors = listOf(fillColor, fillColor.copy(alpha = 0.75f))
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(LightGray)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(safe / 100f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(gradient)
            )
        }
    }
}

private fun colorForEfficiency(percent: Int): Color = when {
    percent > 60 -> AqiGood
    percent >= 30 -> AqiNormal
    else -> AqiBad
}

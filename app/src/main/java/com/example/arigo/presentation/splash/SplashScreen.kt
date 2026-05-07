package com.example.arigo.presentation.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arigo.R
import com.example.arigo.core.theme.RacingSansOne
import com.example.arigo.core.theme.RadioCanada
import com.example.arigo.core.theme.SplashGradientBottom
import com.example.arigo.core.theme.SplashGradientTop
import com.example.arigo.core.theme.TealDark
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

private const val SplashDurationMs = 2500L
private const val FadeInDurationMs = 1500

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = FadeInDurationMs),
        label = "splash-fade-in"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(SplashDurationMs)
        val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
        if (isLoggedIn) onNavigateToHome() else onNavigateToLogin()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SplashGradientTop, SplashGradientBottom)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-30).dp)
                .alpha(alphaAnim),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.arigo_logo),
                contentDescription = "AriGo logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.width(240.dp)
            )
            Text(
                text = "AriGo",
                color = TealDark,
                fontSize = 34.sp,
                fontFamily = RacingSansOne,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                text = "AirGaurd Pro",
                color = TealDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = RadioCanada,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Text(
            text = "Clean the air, Choose the care !",
            color = TealDark.copy(alpha = 0.8f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = RadioCanada,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp)
                .alpha(alphaAnim)
        )
    }
}

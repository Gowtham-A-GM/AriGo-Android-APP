package com.example.arigo.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arigo.core.theme.BlackText
import com.example.arigo.core.theme.TealHeader
import com.example.arigo.core.theme.White

/** Curved teal page header used by the auth screens (Login, Signup, ProfileSetup). */
@Composable
fun AuthHeader(title: String) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp + topInset)
            .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
            .background(TealHeader)
            .padding(top = topInset)
    ) {
        Text(
            text = title,
            color = White,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 25.dp)
        )
    }
}

/** Pill-shaped primary action button used by all auth screens. */
@Composable
fun AuthPillButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(30.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = TealHeader,
            contentColor = White
        ),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Text(
                text = text,
                color = White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/** Outlined pill button with brand icon + label, used for Google / Facebook style buttons. */
@Composable
fun AuthSocialButton(
    label: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.width(280.dp).height(44.dp),
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(1.dp, TealHeader),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = White,
            contentColor = TealHeader
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = TealHeader,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/** Default colors for outlined text fields on the auth screens. */
@Composable
fun authTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = TealHeader,
    unfocusedBorderColor = TealHeader,
    errorBorderColor = Color.Red,
    cursorColor = TealHeader,
    focusedTextColor = BlackText,
    unfocusedTextColor = BlackText,
    errorTextColor = BlackText,
    disabledTextColor = BlackText,
    focusedContainerColor = White,
    unfocusedContainerColor = White,
    errorContainerColor = White,
    disabledContainerColor = White
)

/** Default text style used inside auth-screen text fields. Ensures typed text is BlackText. */
val AuthFieldTextStyle = TextStyle(
    color = BlackText,
    fontSize = 16.sp
)

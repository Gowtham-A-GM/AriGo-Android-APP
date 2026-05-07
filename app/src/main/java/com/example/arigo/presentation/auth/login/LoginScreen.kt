package com.example.arigo.presentation.auth.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.arigo.R
import com.example.arigo.core.theme.BlackText
import com.example.arigo.core.theme.TealHeader
import com.example.arigo.core.theme.White
import com.example.arigo.presentation.components.AuthFieldTextStyle
import com.example.arigo.presentation.components.AuthHeader
import com.example.arigo.presentation.components.AuthPillButton
import com.example.arigo.presentation.components.AuthSocialButton
import com.example.arigo.presentation.components.authTextFieldColors

@Composable
fun LoginScreen(
    onNavigateToSignup: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.isLoginSuccess) {
        if (state.isLoginSuccess) onLoginSuccess()
    }

    LaunchedEffect(state.loginError) {
        state.loginError?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearLoginError()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(White)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            AuthHeader(title = "LOGIN")

            Spacer(modifier = Modifier.height(60.dp))

            UsernameField(
                value = state.email,
                error = state.emailError,
                onValueChange = viewModel::onEmailChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordField(
                value = state.password,
                error = state.passwordError,
                isVisible = state.isPasswordVisible,
                onValueChange = viewModel::onPasswordChange,
                onToggleVisibility = viewModel::togglePasswordVisibility,
                onForgotPassword = onNavigateToForgotPassword
            )

            Spacer(modifier = Modifier.height(24.dp))

            AuthPillButton(
                text = "Login",
                isLoading = state.isLoading,
                onClick = viewModel::login,
                modifier = Modifier.align(Alignment.CenterHorizontally).width(140.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Or",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            AuthSocialButton(
                label = "Login with Google",
                iconRes = R.drawable.ic_google,
                onClick = {
                    Toast.makeText(context, "Google Sign-In coming soon", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(140.dp))
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Don’t have an account ?",
                color = BlackText,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            AuthPillButton(
                text = "Signin",
                isLoading = false,
                onClick = onNavigateToSignup,
                modifier = Modifier.width(140.dp)
            )
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TealHeader)
            }
        }
    }
}

@Composable
private fun UsernameField(
    value: String,
    error: String?,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 25.dp)) {
        Text(
            text = "Username",
            color = TealHeader,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(5.dp),
            singleLine = true,
            isError = error != null,
            colors = authTextFieldColors(),
            textStyle = AuthFieldTextStyle,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        if (error != null) {
            Text(
                text = error,
                color = Color.Red,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun PasswordField(
    value: String,
    error: String?,
    isVisible: Boolean,
    onValueChange: (String) -> Unit,
    onToggleVisibility: () -> Unit,
    onForgotPassword: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 25.dp)) {
        Text(
            text = "Password",
            color = TealHeader,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(5.dp),
            singleLine = true,
            isError = error != null,
            colors = authTextFieldColors(),
            textStyle = AuthFieldTextStyle,
            visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                Text(
                    text = if (isVisible) "Hide" else "Show",
                    color = TealHeader,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable(onClick = onToggleVisibility)
                        .padding(end = 12.dp)
                )
            }
        )
        if (error != null) {
            Text(
                text = error,
                color = Color.Red,
                fontSize = 12.sp
            )
        }
        Text(
            text = "Forgot password",
            color = BlackText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Light,
            modifier = Modifier
                .padding(top = 4.dp)
                .clickable(onClick = onForgotPassword)
        )
    }
}

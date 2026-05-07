package com.example.arigo.presentation.auth.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.arigo.AriGoApplication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, emailError = null, loginError = null) }
    }

    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password, passwordError = null, loginError = null) }
    }

    fun togglePasswordVisibility() {
        _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun clearLoginError() {
        _state.update { it.copy(loginError = null) }
    }

    fun login() {
        val current = _state.value
        val emailError = validateEmail(current.email)
        val passwordError = validatePassword(current.password)

        if (emailError != null || passwordError != null) {
            _state.update {
                it.copy(emailError = emailError, passwordError = passwordError)
            }
            return
        }

        _state.update { it.copy(isLoading = true, loginError = null) }

        viewModelScope.launch {
            try {
                firebaseAuth
                    .signInWithEmailAndPassword(current.email.trim(), current.password)
                    .await()
                _state.update { it.copy(isLoading = false, isLoginSuccess = true) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        loginError = e.localizedMessage ?: "Login failed. Please try again."
                    )
                }
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        _state.update { it.copy(isLoading = true, loginError = null) }
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                firebaseAuth.signInWithCredential(credential).await()
                _state.update { it.copy(isLoading = false, isLoginSuccess = true) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        loginError = e.localizedMessage ?: "Google sign-in failed."
                    )
                }
            }
        }
    }

    private fun validateEmail(email: String): String? {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) return "Invalid username"
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) return "Invalid username"
        return null
    }

    private fun validatePassword(password: String): String? {
        if (password.isEmpty()) return "Invalid password"
        if (password.length < 6) return "Invalid password"
        return null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AriGoApplication
                LoginViewModel(app.container.firebaseAuth)
            }
        }
    }
}

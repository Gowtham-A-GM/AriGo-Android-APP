package com.example.arigo.presentation.auth.signup

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.arigo.AriGoApplication
import com.example.arigo.core.common.Resource
import com.example.arigo.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignupViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SignupState())
    val state: StateFlow<SignupState> = _state.asStateFlow()

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, emailError = null, signupError = null) }
    }

    fun onPasswordChange(password: String) {
        _state.update {
            it.copy(
                password = password,
                passwordError = null,
                // Re-evaluate confirm-mismatch since password changed
                confirmPasswordError = null,
                signupError = null
            )
        }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _state.update {
            it.copy(
                confirmPassword = confirmPassword,
                confirmPasswordError = null,
                signupError = null
            )
        }
    }

    fun togglePasswordVisibility() {
        _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleConfirmPasswordVisibility() {
        _state.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun clearSignupError() {
        _state.update { it.copy(signupError = null) }
    }

    fun signup() {
        val current = _state.value
        val emailError = validateEmail(current.email)
        val passwordError = validatePassword(current.password)
        val confirmError = validateConfirm(current.password, current.confirmPassword)

        if (emailError != null || passwordError != null || confirmError != null) {
            _state.update {
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmError
                )
            }
            return
        }

        _state.update { it.copy(isLoading = true, signupError = null) }

        viewModelScope.launch {
            when (val result = authRepository.signupWithEmail(current.email.trim(), current.password)) {
                is Resource.Success -> _state.update {
                    it.copy(isLoading = false, isSignupSuccess = true)
                }
                is Resource.Error -> _state.update {
                    it.copy(isLoading = false, signupError = result.message)
                }
                Resource.Loading -> Unit // suspend repos don't emit Loading
            }
        }
    }

    fun signupWithGoogle(idToken: String) {
        _state.update { it.copy(isLoading = true, signupError = null) }
        viewModelScope.launch {
            when (val result = authRepository.loginWithGoogle(idToken)) {
                is Resource.Success -> _state.update {
                    it.copy(isLoading = false, isSignupSuccess = true)
                }
                is Resource.Error -> _state.update {
                    it.copy(isLoading = false, signupError = result.message)
                }
                Resource.Loading -> Unit
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

    private fun validateConfirm(password: String, confirmPassword: String): String? {
        if (confirmPassword.isEmpty()) return "Password doesn't match"
        if (confirmPassword != password) return "Password doesn't match"
        return null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AriGoApplication
                SignupViewModel(app.container.authRepository)
            }
        }
    }
}

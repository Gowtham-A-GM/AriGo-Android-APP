package com.example.arigo.presentation.auth.profile_setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.arigo.AriGoApplication
import com.example.arigo.core.common.Resource
import com.example.arigo.domain.model.UserProfile
import com.example.arigo.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileSetupViewModel(
    private val firebaseAuth: FirebaseAuth,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileSetupState())
    val state: StateFlow<ProfileSetupState> = _state.asStateFlow()

    fun onNameChange(value: String) {
        _state.update { it.copy(name = value, saveError = null) }
    }

    fun onPhoneChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        _state.update { it.copy(phone = digitsOnly, saveError = null) }
    }

    fun onDobChange(value: String) {
        _state.update { it.copy(dateOfBirth = value, saveError = null) }
    }

    fun onGenderChange(value: String) {
        _state.update { it.copy(gender = value, saveError = null) }
    }

    fun onAddressChange(value: String) {
        _state.update { it.copy(address = value, saveError = null) }
    }

    fun onCityChange(value: String) {
        _state.update { it.copy(city = value, saveError = null) }
    }

    fun onPinCodeChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        _state.update { it.copy(pinCode = digitsOnly, saveError = null) }
    }

    fun onEmergencyPhoneChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        _state.update { it.copy(emergencyPhone = digitsOnly, saveError = null) }
    }

    fun onHealthIssuesChange(value: String) {
        _state.update { it.copy(healthIssues = value, saveError = null) }
    }

    fun onProfileImageSelected(uri: String) {
        _state.update { it.copy(profileImageUri = uri) }
    }

    fun onCountryCodeChange(code: String) {
        _state.update { it.copy(selectedCountryCode = code, saveError = null) }
    }

    fun onEmergencyCountryCodeChange(code: String) {
        _state.update { it.copy(selectedEmergencyCountryCode = code, saveError = null) }
    }

    fun clearSaveError() {
        _state.update { it.copy(saveError = null) }
    }

    fun saveProfile() {
        val current = _state.value
        if (current.name.isBlank()) {
            _state.update { it.copy(saveError = "Name is required") }
            return
        }

        val user = firebaseAuth.currentUser
        if (user == null) {
            _state.update { it.copy(saveError = "Not signed in. Please log in again.") }
            return
        }

        _state.update { it.copy(isLoading = true, saveError = null) }

        viewModelScope.launch {
            val phoneFull = formatPhone(current.selectedCountryCode, current.phone)
            val emergencyFull = formatPhone(current.selectedEmergencyCountryCode, current.emergencyPhone)

            val profile = UserProfile(
                uid = user.uid,
                name = current.name.trim(),
                email = user.email.orEmpty(),
                phone = phoneFull,
                dateOfBirth = current.dateOfBirth.trim(),
                gender = current.gender.trim(),
                address = current.address.trim(),
                city = current.city.trim(),
                pinCode = current.pinCode.trim(),
                emergencyPhone = emergencyFull,
                healthIssues = current.healthIssues.trim(),
                profileImageUrl = current.profileImageUri
            )
            when (val result = userRepository.saveUserProfile(profile)) {
                is Resource.Success -> _state.update { it.copy(isLoading = false, isSaveSuccess = true) }
                is Resource.Error -> _state.update { it.copy(isLoading = false, saveError = result.message) }
                Resource.Loading -> Unit
            }
        }
    }

    private fun formatPhone(countryCode: String, phone: String): String {
        val trimmed = phone.trim()
        return if (trimmed.isEmpty()) "" else "$countryCode $trimmed"
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AriGoApplication
                ProfileSetupViewModel(
                    firebaseAuth = app.container.firebaseAuth,
                    userRepository = app.container.userRepository
                )
            }
        }
    }
}

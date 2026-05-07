package com.example.arigo.presentation.auth.profile_setup

data class ProfileSetupState(
    val name: String = "",
    val phone: String = "",
    val dateOfBirth: String = "",
    val gender: String = "",
    val address: String = "",
    val city: String = "",
    val pinCode: String = "",
    val emergencyPhone: String = "",
    val healthIssues: String = "",
    val profileImageUri: String = "",
    val selectedCountryCode: String = "+91",
    val selectedEmergencyCountryCode: String = "+91",
    val isLoading: Boolean = false,
    val saveError: String? = null,
    val isSaveSuccess: Boolean = false
)

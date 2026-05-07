package com.example.arigo.core.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Signup : Screen("signup")
    data object ForgotPassword : Screen("forgot_password")
    data object ProfileSetup : Screen("profile_setup")
    data object Home : Screen("home")
    data object History : Screen("history")
    data object Map : Screen("map")
    data object More : Screen("more")
    data object DeviceDetail : Screen("device_detail/{deviceId}") {
        fun createRoute(deviceId: String) = "device_detail/$deviceId"
    }
    data object AirQualityAnalytics : Screen("air_quality_analytics/{deviceId}") {
        fun createRoute(deviceId: String) = "air_quality_analytics/$deviceId"
    }
    data object FilterHealth : Screen("filter_health/{deviceId}") {
        fun createRoute(deviceId: String) = "filter_health/$deviceId"
    }
    data object AddDevice : Screen("add_device")
    data object Notifications : Screen("notifications")
    data object EditProfile : Screen("edit_profile")
}

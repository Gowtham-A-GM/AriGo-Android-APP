package com.example.arigo.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.arigo.presentation.air_quality.AirQualityScreen
import com.example.arigo.presentation.auth.login.LoginScreen
import com.example.arigo.presentation.auth.profile_setup.ProfileSetupScreen
import com.example.arigo.presentation.auth.signup.SignupScreen
import com.example.arigo.presentation.device_detail.DeviceDetailScreen
import com.example.arigo.presentation.filter_health.FilterHealthScreen
import com.example.arigo.presentation.history.HistoryScreen
import com.example.arigo.presentation.home.HomeScreen
import com.example.arigo.presentation.map.MapScreen
import com.example.arigo.presentation.notifications.NotificationsScreen
import com.example.arigo.presentation.profile.ProfileScreen
import com.example.arigo.presentation.splash.SplashScreen

@Composable
fun AriGoNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToSignup = { navController.navigate(Screen.Signup.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Signup.route) {
            SignupScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onSignupSuccess = {
                    navController.navigate(Screen.ProfileSetup.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ProfileSetup.route) {
            ProfileSetupScreen(
                onProfileComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.ProfileSetup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onDeviceClick = { deviceId ->
                    navController.navigate(Screen.DeviceDetail.createRoute(deviceId))
                },
                onAddDeviceClick = { navController.navigate(Screen.AddDevice.route) },
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) }
            )
        }

        composable(Screen.History.route) { HistoryScreen() }
        composable(Screen.Map.route) { MapScreen() }

        composable(Screen.More.route) {
            ProfileScreen(
                onEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.DeviceDetail.route,
            arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""
            DeviceDetailScreen(
                deviceId = deviceId,
                onBackClick = { navController.popBackStack() },
                onAirQualityClick = {
                    navController.navigate(Screen.AirQualityAnalytics.createRoute(deviceId))
                },
                onFilterHealthClick = {
                    navController.navigate(Screen.FilterHealth.createRoute(deviceId))
                }
            )
        }

        composable(
            route = Screen.AirQualityAnalytics.route,
            arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""
            AirQualityScreen(
                deviceId = deviceId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.FilterHealth.route,
            arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""
            FilterHealthScreen(
                deviceId = deviceId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(onBackClick = { navController.popBackStack() })
        }
    }
}

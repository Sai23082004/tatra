package com.mahiya.safegas.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.mahiya.safegas.screens.*

@Composable
fun AppNavigation(navController: NavHostController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    // Screens that should not show the bottom bar (authentication screens)
    val screensWithoutBottomBar = listOf(
        Screen.Login.route,
        Screen.Signup.route
    )

    Scaffold(
        bottomBar = {
            if (currentRoute !in screensWithoutBottomBar) {
                BottomBar(navController = navController, currentRoute = currentRoute)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route, // Start with login
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(navController = navController)
            }

            composable(Screen.Signup.route) {
                SignupScreen(navController = navController)
            }

            composable(Screen.Home.route) {
                HomeScreen(navController = navController)
            }

            composable(Screen.GasLeak.route) {
                GasLeakScreen(navController = navController)
            }

            composable(Screen.GasLevel.route) {
                GasLevelScreen(navController = navController)
            }

            composable(Screen.PipeHealth.route) {
                PipeHealthScreen(navController = navController)
            }

            composable(Screen.RegulatorMonitor.route) {
                RegulatorMonitorScreen(navController = navController)
            }

            composable(Screen.Emergency.route) {
                EmergencyScreen(navController = navController)
            }

            composable(Screen.Profile.route) {
                ProfileScreen(navController = navController)
            }
        }
    }
}

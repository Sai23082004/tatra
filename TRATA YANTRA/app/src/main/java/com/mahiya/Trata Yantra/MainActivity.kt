package com.mahiya.safegas

import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mahiya.safegas.navigation.Screen
import com.mahiya.safegas.network.NetworkService
import com.mahiya.safegas.screens.*
import com.mahiya.safegas.ui.theme.SafeGasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SafeGasTheme {
                SafeGasApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeGasApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

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
            startDestination = Screen.Login.route, // Start with login screen
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



// Add this function to test backend connection
fun testBackendConnection() {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = NetworkService.api.getDashboardData("Bearer test-token")
            println("Backend connection successful: $response")
        } catch (e: Exception) {
            println("Backend connection failed: ${e.message}")
        }
    }
}


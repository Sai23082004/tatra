package com.mahiya.safegas.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Signup : Screen("signup")
    data object Home : Screen("home")
    data object GasLeak : Screen("gas_leak")
    data object GasLevel : Screen("gas_level")
    data object PipeHealth : Screen("pipe_health")
    data object RegulatorMonitor : Screen("regulator_monitor")
    data object Emergency : Screen("emergency")
    data object Profile : Screen("profile")
}

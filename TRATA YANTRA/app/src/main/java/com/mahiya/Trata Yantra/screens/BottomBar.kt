package com.mahiya.safegas.screens

import com.mahiya.safegas.network.NetworkService
import com.mahiya.safegas.network.LoginRequest
import com.mahiya.safegas.network.SignupRequest
// Add other imports as needed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mahiya.safegas.navigation.Screen

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedColor: Color = Color(0xFF2196F3)
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home.route, "Home", Icons.Filled.Home, Color(0xFF2196F3)),
    BottomNavItem(Screen.GasLeak.route, "Detect", Icons.Filled.Warning, Color(0xFFFF9800)),
    BottomNavItem(Screen.GasLevel.route, "Level", Icons.Filled.BarChart, Color(0xFF4CAF50)),
    BottomNavItem(Screen.Emergency.route, "SOS", Icons.Filled.Call, Color(0xFFF44336)),
    BottomNavItem(Screen.Profile.route, "Profile", Icons.Filled.Person, Color(0xFF9C27B0))
)

@Composable
fun BottomBar(navController: NavController, currentRoute: String?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(12.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .height(80.dp)
        ) {
            bottomNavItems.forEach { item ->
                val isSelected = currentRoute == item.route

                NavigationBarItem(
                    icon = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 40.dp else 32.dp)
                                    .background(
                                        if (isSelected) item.selectedColor.copy(alpha = 0.15f)
                                        else Color.Transparent,
                                        RoundedCornerShape(20.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    item.icon,
                                    contentDescription = item.label,
                                    tint = if (isSelected) item.selectedColor else Color.Gray,
                                    modifier = Modifier.size(if (isSelected) 24.dp else 20.dp)
                                )
                            }
                        }
                    },
                    label = {
                        Text(
                            text = item.label,
                            fontSize = if (isSelected) 12.sp else 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) item.selectedColor else Color.Gray
                        )
                    },
                    selected = isSelected,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(Screen.Home.route)
                                launchSingleTop = true
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = item.selectedColor,
                        selectedTextColor = item.selectedColor,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}

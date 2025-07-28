package com.mahiya.safegas.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.mahiya.safegas.network.NetworkService
import com.mahiya.safegas.utils.TokenManager
import com.mahiya.safegas.data.GasLeakData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GasLeakScreen(navController: NavController) {
    var isScanning by remember { mutableStateOf(false) }
    var leakDetected by remember { mutableStateOf(false) }
    var gasLevel by remember { mutableStateOf(0f) }
    var sensorCount by remember { mutableStateOf(0) }
    var lastScan by remember { mutableStateOf("Never") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFEBEE),
                        Color(0xFFFFCDD2)
                    )
                )
            )
            .padding(24.dp)
    ) {
        // Add a back button or navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.navigateUp() } // Use navController here
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFFD32F2F)
                )
            }

            Text(
                text = "Emergency Center",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F),
                modifier = Modifier.weight(1f)
            )
        }}

    // FIXED: Move functions outside LaunchedEffect
    suspend fun loadGasLeakStatus() {
        try {
            isLoading = true
            errorMessage = ""
            val token = TokenManager.getAccessToken(context)
            if (token != null) {
                val response = NetworkService.api.getGasLeakStatus("Bearer $token")
                if (response.success == 1 && response.data != null) {
                    val data = response.data
                    gasLevel = data.gas_level / 100f
                    leakDetected = data.status == "LEAK_DETECTED"
                    sensorCount = data.sensor_count
                    lastScan = "Just now"
                } else {
                    errorMessage = response.message ?: "Failed to load gas status"
                }
            } else {
                errorMessage = "Please login again"
            }
        } catch (e: Exception) {
            errorMessage = "Network error: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    suspend fun performGasScan() {
        try {
            isScanning = true
            errorMessage = ""
            val token = TokenManager.getAccessToken(context)
            if (token != null) {
                val response = NetworkService.api.triggerGasScan("Bearer $token")
                if (response.success == 1) {
                    // Simulate scanning animation
                    repeat(100) {
                        gasLevel = (it + 1) / 100f
                        delay(50)
                    }
                    // Reload status after scan
                    loadGasLeakStatus()
                } else {
                    errorMessage = response.message ?: "Scan failed"
                }
            } else {
                errorMessage = "Please login again"
            }
        } catch (e: Exception) {
            errorMessage = "Network error: ${e.message}"
        } finally {
            isScanning = false
        }
    }

    // Load initial data
    LaunchedEffect(Unit) {
        loadGasLeakStatus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        if (leakDetected && !isScanning) Color(0xFFFFEBEE) else Color(0xFFE3F2FD),
                        if (leakDetected && !isScanning) Color(0xFFFFCDD2) else Color(0xFFBBDEFB)
                    )
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Gas Leak Detection",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1976D2),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Real-time environmental monitoring",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Main Detection Circle
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = if (leakDetected && !isScanning) {
                            listOf(Color(0xFFFF5722), Color(0xFFD32F2F))
                        } else {
                            listOf(Color(0xFF4CAF50), Color(0xFF388E3C))
                        }
                    )
                )
                .scale(if (isScanning) pulseScale else 1f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (isScanning) Icons.Filled.Search
                    else if (leakDetected) Icons.Filled.Warning
                    else Icons.Filled.CheckCircle,
                    contentDescription = "Status",
                    modifier = Modifier
                        .size(64.dp)
                        .then(if (isScanning) Modifier.scale(rotationAngle / 360f + 0.8f) else Modifier),
                    tint = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when {
                        isScanning -> "SCANNING"
                        leakDetected -> "LEAK DETECTED"
                        else -> "ALL CLEAR"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Status Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatusCard(
                title = "Gas Level",
                value = "${(gasLevel * 100).toInt()}%",
                icon = Icons.Filled.BarChart,
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )

            StatusCard(
                title = "Sensors",
                value = "$sensorCount Active",
                icon = Icons.Filled.Sensors,
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Progress Indicator
        if (isScanning) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Scanning Environment...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1976D2)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = gasLevel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF2196F3),
                        trackColor = Color(0xFFE3F2FD)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${(gasLevel * 100).toInt()}% Complete",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // Error Message
        if (errorMessage.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Error,
                        contentDescription = "Error",
                        tint = Color(0xFFFF5722)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = errorMessage,
                        fontSize = 14.sp,
                        color = Color(0xFFFF5722)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action Buttons
        if (!isScanning) {
            if (leakDetected) {
                Button(
                    onClick = { /* Handle emergency alert */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF5722)
                    )
                ) {
                    Icon(Icons.Filled.Warning, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EMERGENCY ALERT",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedButton(
                onClick = {
                    coroutineScope.launch { performGasScan() }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF2196F3), Color(0xFF1976D2))
                    )
                )
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, tint = Color(0xFF2196F3))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Scan Again",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3)
                )
            }
        }
    }
}

@Composable
fun StatusCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

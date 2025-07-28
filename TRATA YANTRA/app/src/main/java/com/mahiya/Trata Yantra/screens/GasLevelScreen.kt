package com.mahiya.safegas.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.mahiya.safegas.network.NetworkService
import com.mahiya.safegas.utils.TokenManager
import com.mahiya.safegas.data.GasLevelData
import com.mahiya.safegas.data.GasReadingItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GasLevelScreen(navController: NavController) {
    var gasLevel by remember { mutableStateOf(0.75f) }
    var estimatedHours by remember { mutableStateOf(36.5f) }
    var flowRate by remember { mutableStateOf(2.5f) }
    var pressure by remember { mutableStateOf(15.0f) }
    var recentReadings by remember { mutableStateOf(listOf<GasReadingItem>()) } // FIXED: Explicit type
    var isLoading by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val animatedGasLevel by animateFloatAsState(
        targetValue = gasLevel,
        animationSpec = tween(2000, easing = EaseInOutCubic)
    )

    val infiniteTransition = rememberInfiniteTransition()
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
    suspend fun loadGasLevelData() {
        try {
            isLoading = true
            errorMessage = ""
            val token = TokenManager.getAccessToken(context)
            if (token != null) {
                val response = NetworkService.api.getGasLevelData("Bearer $token")
                if (response.success == 1 && response.data != null) {
                    val data = response.data
                    gasLevel = data.current_level / 100f
                    estimatedHours = data.estimated_hours
                    flowRate = data.flow_rate
                    pressure = data.pressure
                    recentReadings = data.recent_readings
                } else {
                    errorMessage = response.message ?: "Failed to load gas level data"
                }
            } else {
                errorMessage = "Please login again"
            }
        } catch (e: Exception) {
            errorMessage = "Network error: ${e.message}"
        } finally {
            isLoading = false
            isRefreshing = false
        }
    }

    suspend fun refreshData() {
        isRefreshing = true
        loadGasLevelData()
    }

    // Load initial data
    LaunchedEffect(Unit) {
        loadGasLevelData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F5E8),
                        Color(0xFFF1F8E9)
                    )
                )
            )
            .padding(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Gas Level Monitor",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )

                Text(
                    text = "Real-time cylinder monitoring",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = {
                    coroutineScope.launch { refreshData() }
                },
                modifier = Modifier
                    .background(
                        Color.White,
                        CircleShape
                    )
                    .rotate(if (isRefreshing) rotationAngle else 0f)
            ) {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = "Refresh",
                    tint = Color(0xFF4CAF50)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Error Message
        if (errorMessage.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFFF5722),
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Main Gas Level Gauge
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Gauge Background
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
                        val radius = size.width / 2 - 20.dp.toPx()

                        // Background Arc
                        drawArc(
                            color = Color(0xFFE0E0E0),
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round),
                            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                            topLeft = androidx.compose.ui.geometry.Offset(
                                center.x - radius,
                                center.y - radius
                            )
                        )

                        // Progress Arc
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFF4CAF50),
                                    Color(0xFF8BC34A),
                                    Color(0xFF4CAF50)
                                )
                            ),
                            startAngle = 135f,
                            sweepAngle = 270f * animatedGasLevel,
                            useCenter = false,
                            style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round),
                            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                            topLeft = androidx.compose.ui.geometry.Offset(
                                center.x - radius,
                                center.y - radius
                            )
                        )
                    }

                    // Center Content
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color(0xFF4CAF50),
                                modifier = Modifier.size(40.dp)
                            )
                        } else {
                            Text(
                                text = "${(animatedGasLevel * 100).toInt()}%",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )

                            Text(
                                text = "Gas Level",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Status Indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatusIndicator(
                        label = "Estimated Time",
                        value = "${estimatedHours.toInt()}h ${((estimatedHours % 1) * 60).toInt()}m",
                        icon = Icons.Filled.Schedule,
                        color = Color(0xFF2196F3)
                    )

                    StatusIndicator(
                        label = "Flow Rate",
                        value = "${flowRate} L/h",
                        icon = Icons.Filled.Speed,
                        color = Color(0xFF9C27B0)
                    )

                    StatusIndicator(
                        label = "Pressure",
                        value = "${pressure.toInt()} PSI",
                        icon = Icons.Filled.Compress,
                        color = Color(0xFFFF9800)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Readings
        Text(
            text = "Recent Readings",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (recentReadings.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recentReadings) { reading ->
                    ReadingCard(reading = reading)
                }
            }
        } else {
            Text(
                text = "No recent readings available",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Alert Threshold Settings
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (gasLevel < 0.2f) Color(0xFFFFEBEE) else Color(0xFFE8F5E8)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (gasLevel < 0.2f) Icons.Filled.Warning else Icons.Filled.CheckCircle,
                    contentDescription = "Status",
                    tint = if (gasLevel < 0.2f) Color(0xFFFF5722) else Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (gasLevel < 0.2f) "Low Gas Alert" else "Gas Level Normal",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (gasLevel < 0.2f) Color(0xFFFF5722) else Color(0xFF4CAF50)
                    )

                    Text(
                        text = if (gasLevel < 0.2f)
                            "Consider refilling your cylinder soon"
                        else "Your gas supply is adequate",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                if (gasLevel < 0.2f) {
                    Button(
                        onClick = { /* Handle emergency order */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5722)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Order Now",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusIndicator(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color.copy(alpha = 0.1f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            textAlign = TextAlign.Center
        )

        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ReadingCard(reading: GasReadingItem) {
    Card(
        modifier = Modifier.width(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = reading.time,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${(reading.level * 100).toInt()}%",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )

            Text(
                text = reading.status,
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}



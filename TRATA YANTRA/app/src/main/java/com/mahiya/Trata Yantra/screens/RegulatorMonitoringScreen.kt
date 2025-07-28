package com.mahiya.safegas.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import com.mahiya.safegas.data.RegulatorData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegulatorMonitorScreen(navController: NavController) {
    var regulatorData by remember { mutableStateOf<RegulatorData?>(null) } // FIXED: Explicit type
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isUpdating by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val infiniteTransition = rememberInfiniteTransition()
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing)
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
    suspend fun loadRegulatorData() {
        try {
            isLoading = true
            errorMessage = ""
            val token = TokenManager.getAccessToken(context)
            if (token != null) {
                val response = NetworkService.api.getRegulatorData("Bearer $token")
                if (response.success == 1 && response.data != null) {
                    regulatorData = response.data
                } else {
                    errorMessage = response.message ?: "Failed to load regulator data"
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

    suspend fun controlRegulator(action: String) {
        try {
            isUpdating = true
            errorMessage = ""
            val token = TokenManager.getAccessToken(context)
            if (token != null) {
                val response = NetworkService.api.controlRegulator(
                    "Bearer $token",
                    mapOf("action" to action)
                )
                if (response.success == 1 && response.data != null) {
                    regulatorData = response.data
                } else {
                    errorMessage = response.message ?: "Failed to control regulator"
                }
            }
        } catch (e: Exception) {
            errorMessage = "Network error: ${e.message}"
        } finally {
            isUpdating = false
        }
    }

    // Load regulator data
    LaunchedEffect(Unit) {
        loadRegulatorData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD),
                        Color(0xFFBBDEFB)
                    )
                )
            )
            .padding(24.dp)
    ) {
        // Header
        Text(
            text = "Regulator Control",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1976D2),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Monitor and control gas flow regulation",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

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

        // Main Control Panel
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
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color(0xFF1976D2),
                        modifier = Modifier.size(60.dp)
                    )
                    Text(
                        text = "Loading regulator data...",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                } else {
                    regulatorData?.let { regulator ->
                        // Power Status
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .background(
                                    if (regulator.is_on)
                                        Brush.radialGradient(
                                            colors = listOf(Color(0xFF4CAF50), Color(0xFF388E3C))
                                        )
                                    else
                                        Brush.radialGradient(
                                            colors = listOf(Color(0xFFFF5722), Color(0xFFD32F2F))
                                        ),
                                    shape = CircleShape
                                )
                                .rotate(if (regulator.is_on) rotationAngle else 0f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    if (regulator.is_on) Icons.Filled.Power else Icons.Filled.PowerOff,
                                    contentDescription = "Power Status",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )

                                Text(
                                    text = if (regulator.is_on) "ON" else "OFF",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Control Switch
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Regulator Power",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF333333)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Switch(
                                checked = regulator.is_on,
                                onCheckedChange = {
                                    coroutineScope.launch {
                                        controlRegulator("toggle_power")
                                    }
                                },
                                enabled = !isUpdating,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF4CAF50),
                                    checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f),
                                    uncheckedThumbColor = Color(0xFFFF5722),
                                    uncheckedTrackColor = Color(0xFFFF5722).copy(alpha = 0.5f)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Auto Mode Toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.AutoMode,
                                contentDescription = "Auto Mode",
                                tint = if (regulator.auto_mode) Color(0xFF2196F3) else Color.Gray
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Auto Regulation",
                                fontSize = 16.sp,
                                color = Color(0xFF333333)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Switch(
                                checked = regulator.auto_mode,
                                onCheckedChange = {
                                    coroutineScope.launch {
                                        controlRegulator("toggle_auto")
                                    }
                                },
                                enabled = regulator.is_on && !isUpdating,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF2196F3),
                                    checkedTrackColor = Color(0xFF2196F3).copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Monitoring Gauges
        regulatorData?.let { regulator ->
            Text(
                text = "System Metrics",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetricGauge(
                    title = "Flow Rate",
                    value = regulator.flow_rate,
                    unit = "L/h",
                    maxValue = 10f,
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )

                MetricGauge(
                    title = "Pressure",
                    value = regulator.current_pressure,
                    unit = "PSI",
                    maxValue = 30f,
                    color = Color(0xFF9C27B0),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Temperature Display
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                Color(0xFFFF9800).copy(alpha = 0.1f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Thermostat,
                            contentDescription = "Temperature",
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Temperature",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        Text(
                            text = "${regulator.temperature}°C",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Status",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Text(
                            text = when {
                                regulator.temperature < 0 -> "Cold"
                                regulator.temperature < 25 -> "Normal"
                                regulator.temperature < 40 -> "Warm"
                                else -> "Hot"
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                regulator.temperature < 0 -> Color(0xFF2196F3)
                                regulator.temperature < 25 -> Color(0xFF4CAF50)
                                regulator.temperature < 40 -> Color(0xFFFF9800)
                                else -> Color(0xFFFF5722)
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Safety Alerts
        regulatorData?.let { regulator ->
            if (!regulator.is_on) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = "Warning",
                            tint = Color(0xFFFF5722),
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Regulator is OFF. Gas flow is stopped for safety.",
                            fontSize = 14.sp,
                            color = Color(0xFFFF5722),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Update indicator
        if (isUpdating) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color(0xFF1976D2)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Updating regulator...",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun MetricGauge(
    title: String,
    value: Float,
    unit: String,
    maxValue: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedValue by animateFloatAsState(
        targetValue = value / maxValue,
        animationSpec = tween(1500, easing = EaseInOutCubic)
    )

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
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
                    val radius = size.width / 2 - 10.dp.toPx()

                    // Background Arc
                    drawArc(
                        color = color.copy(alpha = 0.2f),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        topLeft = androidx.compose.ui.geometry.Offset(
                            center.x - radius,
                            center.y - radius
                        )
                    )

                    // Progress Arc
                    drawArc(
                        color = color,
                        startAngle = 135f,
                        sweepAngle = 270f * animatedValue,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        topLeft = androidx.compose.ui.geometry.Offset(
                            center.x - radius,
                            center.y - radius
                        )
                    )
                }

                Text(
                    text = "${value}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )

            Text(
                text = unit,
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}

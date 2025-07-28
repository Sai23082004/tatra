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
import com.mahiya.safegas.data.PipelineHealthData
import com.mahiya.safegas.data.PipelineSectionItem
import com.mahiya.safegas.data.MaintenanceItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PipeHealthScreen(navController: NavController) {
    var overallHealth by remember { mutableStateOf(0.85f) }
    var pipeSections by remember { mutableStateOf(listOf<PipelineSectionItem>()) } // FIXED: Explicit type
    var maintenanceItems by remember { mutableStateOf(listOf<MaintenanceItem>()) } // FIXED: Explicit type
    var isScanning by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val animatedHealth by animateFloatAsState(
        targetValue = overallHealth,
        animationSpec = tween(2000, easing = EaseInOutCubic)
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
    suspend fun loadPipelineHealthData() {
        try {
            isLoading = true
            errorMessage = ""
            val token = TokenManager.getAccessToken(context)
            if (token != null) {
                val response = NetworkService.api.getPipelineHealth("Bearer $token")
                if (response.success == 1 && response.data != null) {
                    val data = response.data
                    overallHealth = data.overall_health / 100f
                    pipeSections = data.sections
                    maintenanceItems = data.maintenance_schedule
                } else {
                    errorMessage = response.message ?: "Failed to load pipeline health data"
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

    suspend fun performPipelineScan() {
        try {
            isScanning = true
            // Simulate scanning process
            kotlinx.coroutines.delay(3000)
            loadPipelineHealthData()
        } catch (e: Exception) {
            errorMessage = "Scan failed: ${e.message}"
        } finally {
            isScanning = false
        }
    }

    // Load initial data
    LaunchedEffect(Unit) {
        loadPipelineHealthData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF3E5F5),
                        Color(0xFFE1BEE7)
                    )
                )
            )
            .padding(24.dp)
    ) {
        // Header
        Text(
            text = "Pipe Health Monitor",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF7B1FA2),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Real-time pipeline integrity assessment",
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

        // Main Health Gauge
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
                    modifier = Modifier.size(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Health Gauge
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
                        val radius = size.width / 2 - 30.dp.toPx()

                        // Background Arc
                        drawArc(
                            color = Color(0xFFE0E0E0),
                            startAngle = 140f,
                            sweepAngle = 260f,
                            useCenter = false,
                            style = Stroke(width = 25.dp.toPx(), cap = StrokeCap.Round),
                            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                            topLeft = androidx.compose.ui.geometry.Offset(
                                center.x - radius,
                                center.y - radius
                            )
                        )

                        // Health Arc
                        val healthColor = when {
                            animatedHealth >= 0.8f -> Color(0xFF4CAF50)
                            animatedHealth >= 0.6f -> Color(0xFFFF9800)
                            else -> Color(0xFFFF5722)
                        }

                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    healthColor,
                                    healthColor.copy(alpha = 0.7f),
                                    healthColor
                                )
                            ),
                            startAngle = 140f,
                            sweepAngle = 260f * animatedHealth,
                            useCenter = false,
                            style = Stroke(width = 25.dp.toPx(), cap = StrokeCap.Round),
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
                        if (isLoading || isScanning) {
                            CircularProgressIndicator(
                                color = Color(0xFF7B1FA2),
                                modifier = Modifier.size(40.dp)
                            )
                            if (isScanning) {
                                Text(
                                    text = "Scanning...",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "${(animatedHealth * 100).toInt()}%",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    animatedHealth >= 0.8f -> Color(0xFF4CAF50)
                                    animatedHealth >= 0.6f -> Color(0xFFFF9800)
                                    else -> Color(0xFFFF5722)
                                }
                            )

                            Text(
                                text = when {
                                    animatedHealth >= 0.8f -> "Excellent"
                                    animatedHealth >= 0.6f -> "Good"
                                    else -> "Needs Attention"
                                },
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Quick Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickActionButton(
                        icon = Icons.Filled.Search,
                        label = "Scan All",
                        color = Color(0xFF2196F3),
                        enabled = !isScanning && !isLoading
                    ) {
                        coroutineScope.launch { performPipelineScan() }
                    }

                    QuickActionButton(
                        icon = Icons.Filled.History,
                        label = "History",
                        color = Color(0xFF9C27B0)
                    ) { /* Show history */ }

                    QuickActionButton(
                        icon = Icons.Filled.Report,
                        label = "Report",
                        color = Color(0xFFFF9800)
                    ) { /* Generate report */ }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section Health
        Text(
            text = "Pipe Sections",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (pipeSections.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pipeSections) { section ->
                    PipeSectionCard(section = section)
                }
            }
        } else {
            Text(
                text = "No section data available",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Maintenance Schedule
        if (maintenanceItems.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Schedule,
                            contentDescription = "Schedule",
                            tint = Color(0xFF9C27B0),
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Maintenance Schedule",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    maintenanceItems.forEach { item ->
                        MaintenanceItemCard(item = item)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Emergency Button
        if (overallHealth < 0.6f) {
            Button(
                onClick = { /* Handle emergency */ },
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
                    text = "Schedule Emergency Inspection",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(56.dp),
            containerColor = color.copy(alpha = if (enabled) 0.1f else 0.05f),
            contentColor = if (enabled) color else color.copy(alpha = 0.5f)
        ) {
            Icon(
                icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            fontSize = 12.sp,
            color = if (enabled) Color(0xFF333333) else Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PipeSectionCard(section: PipelineSectionItem) {
    val healthColor = when {
        section.health_percentage >= 80f -> Color(0xFF4CAF50)
        section.health_percentage >= 60f -> Color(0xFFFF9800)
        else -> Color(0xFFFF5722)
    }

    Card(
        modifier = Modifier.width(140.dp),
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
                        healthColor.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${section.health_percentage.toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = healthColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = section.section_name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                textAlign = TextAlign.Center
            )

            Text(
                text = section.status,
                fontSize = 12.sp,
                color = healthColor
            )

            section.last_inspection?.let {
                Text(
                    text = "Last checked",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun MaintenanceItemCard(item: MaintenanceItem) {
    val priorityColor = when (item.priority) {
        "HIGH" -> Color(0xFFFF5722)
        "MEDIUM" -> Color(0xFFFF9800)
        else -> Color(0xFF4CAF50)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    priorityColor.copy(alpha = 0.1f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.priority.first().toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = priorityColor
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )

            Text(
                text = "${item.description} • ${item.scheduled_date}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Text(
            text = item.priority,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = priorityColor,
            modifier = Modifier
                .background(
                    priorityColor.copy(alpha = 0.1f),
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

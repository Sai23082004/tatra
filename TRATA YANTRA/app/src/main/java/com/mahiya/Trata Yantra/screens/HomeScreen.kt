package com.mahiya.safegas.screens

import com.mahiya.safegas.network.NetworkService
import android.content.Context
import android.util.Log
import kotlinx.coroutines.launch
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mahiya.safegas.R
import com.mahiya.safegas.navigation.Screen
import com.mahiya.safegas.network.DashboardData
import kotlinx.coroutines.delay
import com.mahiya.safegas.utils.TokenManager

data class HomeFeature(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val route: String,
    val isActive: Boolean = true
)

data class StatusData(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val color: Color,
    val isHealthy: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val isTablet = screenWidth > 600.dp
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var dashboardData by remember { mutableStateOf<DashboardData?>(null) }
    var isLoadingData by remember { mutableStateOf(true) }

    // Load dashboard data on screen load
    LaunchedEffect(Unit) {
        loadDashboardData(context) { data ->
            dashboardData = data
            isLoadingData = false
        }
    }

    // Animation states
    var isVisible by remember { mutableStateOf(false) }

    // Trigger entrance animation
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    // Adaptive grid columns
    val gridColumns = when {
        isTablet -> 3
        screenWidth > 400.dp -> 2
        else -> 1
    }

    val statusData = listOf(
        StatusData(
            "Gas Level",
            "85%",
            Icons.Filled.LocalGasStation,
            Color(0xFF4CAF50),
            true
        ),
        StatusData(
            "Pressure",
            "Normal",
            Icons.Filled.Speed,
            Color(0xFF2196F3),
            true
        ),
        StatusData(
            "Temperature",
            "24°C",
            Icons.Filled.Thermostat,
            Color(0xFFFF9800),
            true
        ),
        StatusData(
            "Safety",
            "Secure",
            Icons.Filled.Shield,
            Color(0xFF4CAF50),
            true
        )
    )

    val features = listOf(
        HomeFeature(
            "Gas Leak Detection",
            "Real-time leak monitoring",
            Icons.Filled.Warning,
            Color(0xFFFF5722),
            Screen.GasLeak.route
        ),
        HomeFeature(
            "Gas Level Monitor",
            "Track cylinder levels",
            Icons.Filled.BarChart,
            Color(0xFF4CAF50),
            Screen.GasLevel.route
        ),
        HomeFeature(
            "Pipe Health Check",
            "Pipeline condition monitoring",
            Icons.Filled.Build,
            Color(0xFF2196F3),
            Screen.PipeHealth.route
        ),
        HomeFeature(
            "Regulator Control",
            "Flow regulation management",
            Icons.Filled.Settings,
            Color(0xFF9C27B0),
            Screen.RegulatorMonitor.route
        ),
        HomeFeature(
            "Emergency Center",
            "Quick emergency access",
            Icons.Filled.Call,
            Color(0xFFF44336),
            Screen.Emergency.route
        ),
        HomeFeature(
            "Profile Settings",
            "Account management",
            Icons.Filled.Person,
            Color(0xFF607D8B),
            Screen.Profile.route
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F9FA),
                        Color(0xFFE3F2FD),
                        Color(0xFFF3E5F5)
                    )
                )
            )
            .padding(horizontal = if (isTablet) 24.dp else 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Welcome Header with Animation
        item {
            AnimatedWelcomeHeader(
                isTablet = isTablet,
                isVisible = isVisible
            )
        }

        // Quick Status Dashboard
        item {
            AnimatedStatusDashboard(
                statusData = statusData,
                isTablet = isTablet,
                isVisible = isVisible
            )
        }

        // Features Section
        item {
            FeaturesSection(
                features = features,
                gridColumns = gridColumns,
                isTablet = isTablet,
                isVisible = isVisible,
                onFeatureClick = { route -> navController.navigate(route) }
            )
        }

        // Recent Activity Section
        item {
            RecentActivitySection(isTablet = isTablet, isVisible = isVisible)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
// Replace the loadDashboardData function in HomeScreen.kt
suspend fun loadDashboardData(context: Context, onDataLoaded: (DashboardData?) -> Unit) {
    try {
        // Use TokenManager instead of direct SharedPreferences
        val token = TokenManager.getAccessToken(context)

        if (token.isNullOrEmpty()) {
            Log.e("DashboardError", "No access token found")
            onDataLoaded(null)
            return
        }

        Log.d("DashboardDebug", "Making API call with token: ${token.take(20)}...")

        val authHeader = "Bearer $token"
        val response = NetworkService.api.getDashboardData(authHeader)

        // Handle Django's response format
        val isSuccess = when {
            response.success == 1 -> true
            response.data != null -> true
            else -> false
        }

        if (isSuccess && response.data != null) {
            Log.d("DashboardDebug", "Dashboard data loaded successfully")
            onDataLoaded(response.data)
        } else {
            Log.e("DashboardError", "Failed to load dashboard data: ${response.message}")
            onDataLoaded(null)
        }

    } catch (e: Exception) {
        Log.e("DashboardError", "Error loading dashboard data", e)
        onDataLoaded(null)
    }
}






@Composable
fun AnimatedWelcomeHeader(
    isTablet: Boolean,
    isVisible: Boolean
) {
    val slideAnimation by animateFloatAsState(
        targetValue = if (isVisible) 0f else -100f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = slideAnimation.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF1976D2),
                            Color(0xFF42A5F5)
                        )
                    )
                )
                .padding(if (isTablet) 32.dp else 24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Animated Circular Logo
                val logoScale by animateFloatAsState(
                    targetValue = if (isVisible) 1f else 0.3f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )

                Box(
                    modifier = Modifier
                        .size((if (isTablet) 80 else 64).dp)
                        .scale(logoScale)
                        .clip(CircleShape) // Circular container
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.3f),
                                    Color.White.copy(alpha = 0.1f)
                                )
                            )
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.tratayantra),
                        contentDescription = "TrataYantra",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape), // Circular image clipping
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Welcome Back!",
                    fontSize = if (isTablet) 28.sp else 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "TrataYantra Gas Monitoring System",
                    fontSize = if (isTablet) 16.sp else 14.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}


@Composable
fun AnimatedStatusDashboard(
    statusData: List<StatusData>,
    isTablet: Boolean,
    isVisible: Boolean
) {
    var animationDelay by remember { mutableStateOf(0) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(200)
            animationDelay = 1
        }
    }

    Column {
        Text(
            text = "System Status",
            fontSize = if (isTablet) 22.sp else 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(if (isTablet) 4 else 2),
            modifier = Modifier.height(
                if (isTablet) 180.dp else 160.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(statusData.size) { index ->
                val status = statusData[index]
                AnimatedStatusCard(
                    status = status,
                    isTablet = isTablet,
                    delay = index * 100L
                )
            }
        }
    }
}

@Composable
fun AnimatedStatusCard(
    status: StatusData,
    isTablet: Boolean,
    delay: Long
) {
    var isCardVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delay)
        isCardVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isCardVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    val alpha by animateFloatAsState(
        targetValue = if (isCardVisible) 1f else 0f,
        animationSpec = tween(600)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isTablet) 20.dp else 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(if (isTablet) 56.dp else 48.dp)
                    .background(
                        status.color.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    status.icon,
                    contentDescription = status.title,
                    tint = status.color,
                    modifier = Modifier.size(if (isTablet) 28.dp else 24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = status.value,
                fontSize = if (isTablet) 18.sp else 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (status.isHealthy) status.color else Color(0xFFFF5722)
            )

            Text(
                text = status.title,
                fontSize = if (isTablet) 12.sp else 10.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun FeaturesSection(
    features: List<HomeFeature>,
    gridColumns: Int,
    isTablet: Boolean,
    isVisible: Boolean,
    onFeatureClick: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quick Actions",
                fontSize = if (isTablet) 22.sp else 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )

            TextButton(
                onClick = { /* Show all features */ }
            ) {
                Text(
                    text = "View All",
                    color = Color(0xFF1976D2),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(gridColumns),
            modifier = Modifier.height(
                ((features.size + gridColumns - 1) / gridColumns * if (isTablet) 200 else 160).dp
            ),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(features.size) { index ->
                val feature = features[index]
                AnimatedFeatureCard(
                    feature = feature,
                    isTablet = isTablet,
                    delay = index * 100L,
                    onClick = { onFeatureClick(feature.route) }
                )
            }
        }
    }
}

@Composable
fun AnimatedFeatureCard(
    feature: HomeFeature,
    isTablet: Boolean,
    delay: Long,
    onClick: () -> Unit
) {
    var isCardVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delay + 400)
        isCardVisible = true
    }

    val slideUp by animateFloatAsState(
        targetValue = if (isCardVisible) 0f else 50f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isTablet) 180.dp else 140.dp)
            .offset(y = slideUp.dp)
            .clickable { onClick() }
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                feature.color.copy(alpha = 0.05f),
                                feature.color.copy(alpha = 0.1f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isTablet) 20.dp else 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isTablet) 64.dp else 56.dp)
                        .background(
                            feature.color.copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        feature.icon,
                        contentDescription = feature.title,
                        tint = feature.color,
                        modifier = Modifier.size(if (isTablet) 32.dp else 28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(if (isTablet) 16.dp else 12.dp))

                Text(
                    text = feature.title,
                    fontSize = if (isTablet) 16.sp else 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )

                if (isTablet) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = feature.subtitle,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
fun RecentActivitySection(
    isTablet: Boolean,
    isVisible: Boolean
) {
    var sectionVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(800)
            sectionVisible = true
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (sectionVisible) 1f else 0f,
        animationSpec = tween(600)
    )

    Column(
        modifier = Modifier.alpha(alpha)
    ) {
        Text(
            text = "Recent Activity",
            fontSize = if (isTablet) 22.sp else 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                ActivityItem(
                    icon = Icons.Filled.CheckCircle,
                    title = "Gas level check completed",
                    time = "2 minutes ago",
                    color = Color(0xFF4CAF50)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                ActivityItem(
                    icon = Icons.Filled.Settings,
                    title = "Regulator settings updated",
                    time = "1 hour ago",
                    color = Color(0xFF2196F3)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                ActivityItem(
                    icon = Icons.Filled.Warning,
                    title = "Pressure sensor calibrated",
                    time = "3 hours ago",
                    color = Color(0xFFFF9800)
                )
            }
        }
    }
}

@Composable
fun ActivityItem(
    icon: ImageVector,
    title: String,
    time: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
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

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A1A1A)
            )

            Text(
                text = time,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

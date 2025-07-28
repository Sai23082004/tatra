package com.mahiya.safegas.screens

import com.mahiya.safegas.network.NetworkService
import com.mahiya.safegas.network.LoginRequest
import com.mahiya.safegas.network.SignupRequest
// Add other imports as needed

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.mahiya.safegas.MainActivity
import com.mahiya.safegas.R
import com.mahiya.safegas.ui.theme.SafeGasTheme

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SafeGasTheme {
                SplashScreen {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    val logoAlphaAnimatable = remember { Animatable(0f) }
    val textAlphaAnimatable = remember { Animatable(0f) }
    val scaleAnimatable = remember { Animatable(0.3f) }

    LaunchedEffect(Unit) {
        // Phase 1: Logo animation
        launch {
            logoAlphaAnimatable.animateTo(
                1f,
                animationSpec = tween(1000, easing = EaseInOutCubic)
            )
        }
        launch {
            scaleAnimatable.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        delay(800)

        // Phase 2: Text animation
        launch {
            textAlphaAnimatable.animateTo(
                1f,
                animationSpec = tween(800, easing = EaseInOutCubic)
            )
        }

        delay(2000)

        // Phase 3: Exit animation
        launch {
            logoAlphaAnimatable.animateTo(
                0f,
                animationSpec = tween(600, easing = EaseInOutCubic)
            )
        }
        launch {
            textAlphaAnimatable.animateTo(
                0f,
                animationSpec = tween(600, easing = EaseInOutCubic)
            )
        }
        launch {
            scaleAnimatable.animateTo(
                1.2f,
                animationSpec = tween(600, easing = EaseInOutCubic)
            )
        }

        delay(700)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1976D2),
                        Color(0xFF0D47A1)
                    ),
                    radius = 1000f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Circular Logo with Animations
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .scale(scaleAnimatable.value)
                    .alpha(logoAlphaAnimatable.value)
                    .clip(CircleShape) // Circular container
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.White.copy(alpha = 0.1f)
                            )
                        )
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.tratayantra),
                    contentDescription = "TrataYantra Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape), // Circular image clipping
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Name
            Text(
                text = "TrataYantra",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.alpha(textAlphaAnimatable.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = "Gas Safety Monitoring",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.alpha(textAlphaAnimatable.value)
            )
        }
    }
}

package com.mahiya.safegas.screens

import java.net.UnknownHostException
import java.net.ConnectException
import java.net.SocketTimeoutException
import retrofit2.HttpException
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mahiya.safegas.R
import com.mahiya.safegas.navigation.Screen
import com.mahiya.safegas.network.LoginRequest
import com.mahiya.safegas.network.NetworkService
import kotlinx.coroutines.launch
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import com.mahiya.safegas.utils.TokenManager


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val configuration = LocalConfiguration.current

    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val isSmallScreen = screenHeight < 700.dp
    val isWideScreen = screenWidth > 600.dp

    val verticalPadding = if (isSmallScreen) 16.dp else 32.dp
    val horizontalPadding = if (isWideScreen) 48.dp else 24.dp
    val logoSize = if (isSmallScreen) 100.dp else 140.dp
    val titleSize = if (isSmallScreen) 28.sp else 36.sp
    val subtitleSize = if (isSmallScreen) 14.sp else 16.sp

    suspend fun performLogin() {
        if (!validateLogin(email, password)) {
            errorMessage = "Please enter valid email and password"
            return
        }

        try {
            isLoading = true
            errorMessage = ""

            Log.d("LoginDebug", "Starting login with email: $email")

            val response = NetworkService.api.login(
                LoginRequest(email = email.trim(), password = password)
            )

            Log.d("LoginDebug", "Response received:")
            Log.d("LoginDebug", "Success: ${response.success}")
            Log.d("LoginDebug", "Token: ${response.token}")
            Log.d("LoginDebug", "Access: ${response.access}")
            Log.d("LoginDebug", "Refresh: ${response.refresh}")
            Log.d("LoginDebug", "Message: ${response.message}")

            // Handle Django's JWT response format
            val isSuccess = when {
                response.access != null -> true  // Django returns access token for success
                response.message?.contains("successful", ignoreCase = true) == true -> true
                response.success == 1 -> true
                response.success == true -> true
                else -> false
            }

            // Use access token as the main authentication token
            val authToken = response.access ?: response.token
            val refreshToken = response.refresh

            Log.d("LoginDebug", "isSuccess: $isSuccess, authToken exists: ${authToken != null}")
            Log.d("LoginDebug", "refreshToken exists: ${refreshToken != null}")

            if (isSuccess && authToken != null) {
                // UPDATED: Use TokenManager for consistent token storage across all screens
                TokenManager.saveTokens(context, authToken, refreshToken ?: "")
                TokenManager.saveUserEmail(context, response.email ?: email)

                // Also save to SharedPreferences for backward compatibility (if needed)
                val sharedPref = context.getSharedPreferences("SafeGas", Context.MODE_PRIVATE)
                sharedPref.edit() {
                    putString("auth_token", authToken)           // Save access token
                    putString("refresh_token", refreshToken)     // Save refresh token
                    putString("user_email", response.email ?: email)
                    putString("user_name", response.email?.substringBefore("@") ?: email.substringBefore("@"))
                }

                Log.d("LoginDebug", "Tokens saved using TokenManager")
                Log.d("LoginDebug", "Access token: ${authToken.take(30)}...")
                Log.d("LoginDebug", "Refresh token: ${refreshToken?.take(30) ?: "null"}...")

                // Verify tokens were saved correctly
                val savedToken = TokenManager.getAccessToken(context)
                Log.d("LoginDebug", "Verification - Saved token: ${savedToken?.take(30)}...")
                Log.d("LoginDebug", "TokenManager.isLoggedIn(): ${TokenManager.isLoggedIn(context)}")

                Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()

                // Navigate to Home screen and clear login from back stack
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }

                Log.d("LoginDebug", "Navigation attempted")

            } else {
                errorMessage = response.message ?: "Login failed"
                Log.e("LoginError", "Login failed - Success: $isSuccess, Token: ${authToken != null}")
            }
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("LoginError", "HTTP Error ${e.code()}: $errorBody")

            when (e.code()) {
                400 -> errorMessage = "Invalid email or password"
                401 -> errorMessage = "Invalid credentials"
                404 -> errorMessage = "User not found"
                else -> errorMessage = "Server error: ${e.code()}"
            }
        } catch (e: java.net.UnknownHostException) {
            errorMessage = "Cannot connect to server. Please check if Django backend is running."
            Log.e("LoginError", "Network error - Unknown host: ${e.message}")
        } catch (e: java.net.ConnectException) {
            errorMessage = "Connection refused. Check server address and port."
            Log.e("LoginError", "Connection refused: ${e.message}")
        } catch (e: java.net.SocketTimeoutException) {
            errorMessage = "Connection timeout. Server might be slow or unreachable."
            Log.e("LoginError", "Timeout error: ${e.message}")
        } catch (e: Exception) {
            errorMessage = "Network error: ${e.localizedMessage}"
            Log.e("LoginError", "Generic login error", e)
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1976D2),
                        Color(0xFF1565C0),
                        Color(0xFF0D47A1)
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = horizontalPadding)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(verticalPadding))

        // Circular Logo Section
        Box(
            modifier = Modifier
                .size(logoSize)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.3f),
                            Color.White.copy(alpha = 0.1f)
                        )
                    )
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.tratayantra),
                contentDescription = "Tratayantra Logo",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(if (isSmallScreen) 16.dp else 24.dp))

        // Title Section
        Text(
            text = "TrataYantra",
            fontSize = titleSize,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Gas Safety Monitoring System",
            fontSize = subtitleSize,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(if (isSmallScreen) 24.dp else 40.dp))

        // Login Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = if (isWideScreen) 400.dp else screenWidth),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isSmallScreen) 20.dp else 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Sign In",
                    fontSize = if (isSmallScreen) 20.sp else 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    modifier = Modifier.padding(bottom = if (isSmallScreen) 16.dp else 24.dp)
                )

                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = ""
                    },
                    label = { Text("Email Address") },
                    leadingIcon = {
                        Icon(Icons.Filled.Email, contentDescription = "Email")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = ""
                    },
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(Icons.Filled.Lock, contentDescription = "Password")
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Filled.Visibility
                                else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            coroutineScope.launch { performLogin() }
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Error Message
                if (errorMessage.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Login Button
                Button(
                    onClick = {
                        coroutineScope.launch { performLogin() }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isSmallScreen) 48.dp else 56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Sign In",
                            fontSize = if (isSmallScreen) 14.sp else 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sign Up Link
                TextButton(
                    onClick = { navController.navigate(Screen.Signup.route) }
                ) {
                    Text(
                        text = "Don't have an account? Sign Up",
                        color = Color(0xFF1976D2),
                        fontSize = if (isSmallScreen) 12.sp else 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(verticalPadding))
    }
}

fun validateLogin(email: String, password: String): Boolean {
    return email.isNotBlank() && password.isNotBlank() &&
            android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

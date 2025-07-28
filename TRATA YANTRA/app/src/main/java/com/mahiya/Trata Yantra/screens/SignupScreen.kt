package com.mahiya.safegas.screens

import java.net.UnknownHostException
import java.net.ConnectException
import java.net.SocketTimeoutException
import retrofit2.HttpException
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mahiya.safegas.R
import com.mahiya.safegas.navigation.Screen
import com.mahiya.safegas.network.SignupRequest
import com.mahiya.safegas.network.NetworkService
import kotlinx.coroutines.launch
import android.widget.Toast
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var uniqueCode by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    suspend fun performSignup() {
        val validation = validateSignup(email, username, password, confirmPassword, uniqueCode)
        if (!validation.first) {
            errorMessage = validation.second
            return
        }

        try {
            isLoading = true
            errorMessage = ""
            showSuccessMessage = false  // Remove 'var' - this should be a state variable

            Log.d("SignupDebug", "Sending Django signup request:")
            Log.d("SignupDebug", "Username: '$username'")
            Log.d("SignupDebug", "Email: '$email'")
            Log.d("SignupDebug", "Password length: ${password.length}")
            Log.d("SignupDebug", "UniqueCode: '$uniqueCode'")

            val signupRequest = SignupRequest(
                username = username.trim(),
                email = email.trim(),
                password = password,
                device_unique_code = uniqueCode.trim() // Updated field name
            )

            val response = NetworkService.api.signup(signupRequest)

            Log.d("SignupDebug", "Response received:")
            Log.d("SignupDebug", "Success value: ${response.success}")
            Log.d("SignupDebug", "Message: ${response.message}")

            // Handle Django integer response (0/1) or boolean response
            val isSuccess = when {
                response.success == 1 -> true  // Django success as integer
                response.success == true -> true  // Boolean success
                response.message?.contains("success", ignoreCase = true) == true -> true
                response.message?.contains("created", ignoreCase = true) == true -> true
                else -> false
            }

            if (isSuccess) {
                showSuccessMessage = true
                Toast.makeText(context, "Account created successfully!", Toast.LENGTH_LONG).show()

                // Clear form fields
                email = ""
                username = ""
                password = ""
                confirmPassword = ""
                uniqueCode = ""

                delay(1500)

                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Signup.route) { inclusive = true }
                }
            } else {
                // Handle Django error responses with more detailed error parsing
                errorMessage = when {
                    !response.message.isNullOrEmpty() -> response.message
                    !response.error.isNullOrEmpty() -> response.error
                    else -> "Signup failed. Please try again."
                }
                Log.e("SignupError", "Django signup failed: ${response.message}")
                Log.e("SignupError", "Error field: ${response.error}")
            }
        } catch (e: HttpException) {
            try {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("SignupError", "Django HTTP ${e.code()}: $errorBody")

                // Enhanced Django error response parsing
                errorMessage = when (e.code()) {
                    400 -> {
                        // Try to parse specific Django validation errors
                        if (errorBody?.contains("password", ignoreCase = true) == true) {
                            "Password must be at least 6 characters"
                        } else if (errorBody?.contains("email", ignoreCase = true) == true) {
                            "Invalid email address or email already exists"
                        } else if (errorBody?.contains("username", ignoreCase = true) == true) {
                            "Username already exists or invalid"
                        } else if (errorBody?.contains("device_unique_code", ignoreCase = true) == true) {
                            "Device code already registered or invalid"
                        } else {
                            "Invalid input. Please check your details."
                        }
                    }
                    401 -> "Authentication failed."
                    403 -> "Access denied."
                    409 -> "User already exists with this email or device code."
                    422 -> "Validation error. Please check your input."
                    500 -> "Server error. Please try again later."
                    else -> "Server error: ${e.code()}"
                }
            } catch (ex: Exception) {
                errorMessage = "Server error: ${e.code()}"
                Log.e("SignupError", "Error parsing error response", ex)
            }
        } catch (e: UnknownHostException) {
            errorMessage = "Cannot connect to server. Please check if Django backend is running."
            Log.e("SignupError", "Network error - Unknown host: ${e.message}")
        } catch (e: ConnectException) {
            errorMessage = "Connection refused. Check server address (${NetworkService.BASE_URL}) and port."
            Log.e("SignupError", "Connection refused: ${e.message}")
        } catch (e: SocketTimeoutException) {
            errorMessage = "Connection timeout. Server might be slow or unreachable."
            Log.e("SignupError", "Timeout error: ${e.message}")
        } catch (e: Exception) {
            errorMessage = "Network error: ${e.localizedMessage}"
            Log.e("SignupError", "Generic error during Django signup", e)
        } finally {
            isLoading = false
        }
    }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF388E3C),
                        Color(0xFF4CAF50),
                        Color(0xFF81C784)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Header Section
            Card(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(50.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
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
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Join TrataYantra",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Create your account to get started",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Signup Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Username Field
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            errorMessage = ""
                        },
                        label = { Text("Full Name") },
                        leadingIcon = {
                            Icon(Icons.Filled.Person, contentDescription = "Username")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            focusedLabelColor = Color(0xFF4CAF50),
                            focusedLeadingIconColor = Color(0xFF4CAF50)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            focusedLabelColor = Color(0xFF4CAF50),
                            focusedLeadingIconColor = Color(0xFF4CAF50)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Unique Device Code Field
                    OutlinedTextField(
                        value = uniqueCode,
                        onValueChange = {
                            uniqueCode = it
                            errorMessage = ""
                        },
                        label = { Text("Device Unique Code") },
                        leadingIcon = {
                            Icon(Icons.Filled.QrCode, contentDescription = "Device Code")
                        },
                        placeholder = { Text("Enter hardware device code") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            focusedLabelColor = Color(0xFF4CAF50),
                            focusedLeadingIconColor = Color(0xFF4CAF50)
                        )
                    )

                    // Info card for device code
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Info,
                                contentDescription = "Info",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "This unique code is provided with your SafeGas hardware device",
                                fontSize = 12.sp,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            focusedLabelColor = Color(0xFF4CAF50),
                            focusedLeadingIconColor = Color(0xFF4CAF50)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Confirm Password Field
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = ""
                        },
                        label = { Text("Confirm Password") },
                        leadingIcon = {
                            Icon(Icons.Filled.Lock, contentDescription = "Confirm Password")
                        },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    if (confirmPasswordVisible) Icons.Filled.Visibility
                                    else Icons.Filled.VisibilityOff,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            focusedLabelColor = Color(0xFF4CAF50),
                            focusedLeadingIconColor = Color(0xFF4CAF50)
                        )
                    )

                    if (errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Sign Up Button
                    Button(
                        onClick = {
                            coroutineScope.launch { performSignup() }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "Create Account",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Login Link
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Already have an account? ",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Sign In",
                            color = Color(0xFF4CAF50),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                navController.navigate(Screen.Login.route)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun validateSignup(
    email: String,
    username: String,
    password: String,
    confirmPassword: String,
    uniqueCode: String
): Pair<Boolean, String> {
    return when {
        email.isEmpty() || username.isEmpty() || password.isEmpty() ||
                confirmPassword.isEmpty() || uniqueCode.isEmpty() ->
            Pair(false, "Please fill all fields")
        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
            Pair(false, "Please enter a valid email address")
        username.length < 3 ->
            Pair(false, "Name must be at least 3 characters")
        password.length < 6 ->
            Pair(false, "Password must be at least 6 characters")
        password != confirmPassword ->
            Pair(false, "Passwords do not match")
        uniqueCode.length < 8 ->
            Pair(false, "Device code must be at least 8 characters")
        else -> Pair(true, "")
    }
}

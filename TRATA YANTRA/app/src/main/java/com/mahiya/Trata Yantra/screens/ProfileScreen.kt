package com.mahiya.safegas.screens

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import com.mahiya.safegas.network.NetworkService
import com.mahiya.safegas.utils.TokenManager
import com.mahiya.safegas.utils.ImagePickerUtil
import com.mahiya.safegas.navigation.Screen
import com.mahiya.safegas.data.ProfileData

data class ProfileSetting(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    var profileData by remember { mutableStateOf<ProfileData?>(null) } // FIXED: Explicit type
    var showEditDialog by remember { mutableStateOf(false) }
    var showNotificationSettings by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // FIXED: Declare all functions BEFORE they are used
    suspend fun loadUserProfile() {
        try {
            isLoading = true
            errorMessage = ""
            val token = TokenManager.getAccessToken(context)
            if (token != null) {
                val response = NetworkService.api.getUserProfile("Bearer $token")
                if (response.success == 1 && response.data != null) {
                    profileData = response.data
                } else {
                    errorMessage = response.message ?: "Failed to load profile"
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

    suspend fun uploadProfileImage(uri: Uri) {
        try {
            isUploadingImage = true
            val token = TokenManager.getAccessToken(context)

            if (token != null) {
                val bitmap = ImagePickerUtil.uriToBitmap(context, uri)
                if (bitmap != null) {
                    val resizedBitmap = ImagePickerUtil.resizeBitmap(bitmap)
                    val base64Image = ImagePickerUtil.bitmapToBase64(resizedBitmap)

                    val imageData = mapOf(
                        "image_data" to "data:image/jpeg;base64,$base64Image"
                    )

                    val response = NetworkService.api.uploadProfileImage("Bearer $token", imageData)
                    if (response.success == 1) {
                        Toast.makeText(context, "Profile image updated successfully!", Toast.LENGTH_SHORT).show()
                        loadUserProfile() // Reload profile to get updated image URL
                    } else {
                        Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error uploading image: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("ProfileScreen", "Error uploading image", e)
        } finally {
            isUploadingImage = false
        }
    }

    suspend fun uploadBitmapAsProfileImage(bitmap: android.graphics.Bitmap) {
        try {
            isUploadingImage = true
            val token = TokenManager.getAccessToken(context)

            if (token != null) {
                val resizedBitmap = ImagePickerUtil.resizeBitmap(bitmap)
                val base64Image = ImagePickerUtil.bitmapToBase64(resizedBitmap)

                val imageData = mapOf(
                    "image_data" to "data:image/jpeg;base64,$base64Image"
                )

                val response = NetworkService.api.uploadProfileImage("Bearer $token", imageData)
                if (response.success == 1) {
                    Toast.makeText(context, "Profile image updated successfully!", Toast.LENGTH_SHORT).show()
                    loadUserProfile() // Reload profile to get updated image URL
                } else {
                    Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error uploading image: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("ProfileScreen", "Error uploading image", e)
        } finally {
            isUploadingImage = false
        }
    }

    suspend fun updateUserProfile(username: String, email: String, phone: String) {
        try {
            val token = TokenManager.getAccessToken(context)
            if (token != null) {
                val updateData = mapOf(
                    "username" to username,
                    "email" to email,
                    "phone_number" to phone
                )
                val response = NetworkService.api.updateUserProfile("Bearer $token", updateData)
                if (response.success == 1) {
                    Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    loadUserProfile() // Reload profile data
                } else {
                    errorMessage = response.message ?: "Failed to update profile"
                }
            }
        } catch (e: Exception) {
            errorMessage = "Network error: ${e.message}"
        }
    }

    fun signOut() {
        TokenManager.clearTokens(context)
        navController.navigate(Screen.Login.route) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    // NOW declare the launchers AFTER the functions are defined
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                uploadProfileImage(it) // Now this function is available
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            coroutineScope.launch {
                uploadBitmapAsProfileImage(it) // Now this function is available
            }
        }
    }

    // Load user profile
    LaunchedEffect(Unit) {
        loadUserProfile()
    }

    val settings = listOf(
        ProfileSetting(
            "Edit Profile",
            "Update your personal information",
            Icons.Filled.Edit
        ) { showEditDialog = true },
        ProfileSetting(
            "Notifications",
            "Manage alert preferences",
            Icons.Filled.Notifications
        ) { showNotificationSettings = true },
        ProfileSetting(
            "Device Settings",
            "Configure gas sensor parameters",
            Icons.Filled.Settings
        ) { /* Navigate to device settings */ },
        ProfileSetting(
            "Emergency Contacts",
            "Manage emergency contact list",
            Icons.Filled.ContactPhone
        ) { navController.navigate(Screen.Emergency.route) },
        ProfileSetting(
            "Data & Privacy",
            "Control your data preferences",
            Icons.Filled.Security
        ) { /* Navigate to privacy settings */ },
        ProfileSetting(
            "Help & Support",
            "Get help and support",
            Icons.AutoMirrored.Filled.Help
        ) { /* Navigate to help */ },
        ProfileSetting(
            "About",
            "App information and version",
            Icons.Filled.Info
        ) { /* Show about dialog */ }
    )

    // Rest of your UI code remains exactly the same...
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F9FA),
                        Color(0xFFE3F2FD)
                    )
                )
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Enhanced Profile Header Card with Image Upload
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Enhanced Profile Image with Upload Option
                    Box(
                        modifier = Modifier.size(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileData?.profile_image != null) {
                            // Show actual profile image
                            AsyncImage(
                                model = "http://10.0.2.2:8000${profileData!!.profile_image}",
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .clickable { showImagePicker = true },
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Show default profile icon
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1976D2).copy(alpha = 0.1f))
                                    .clickable { showImagePicker = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Person,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.size(50.dp),
                                    tint = Color(0xFF1976D2)
                                )
                            }
                        }

                        // Upload indicator or camera icon
                        if (isUploadingImage) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFF1976D2)
                            )
                        } else {
                            // Camera icon overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(32.dp)
                                    .background(Color(0xFF1976D2), CircleShape)
                                    .clickable { showImagePicker = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.CameraAlt,
                                    contentDescription = "Change Picture",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color(0xFF1976D2),
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        profileData?.let { profile ->
                            Text(
                                text = profile.username,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A)
                            )

                            Text(
                                text = profile.email,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )

                            profile.phone_number?.let { phone ->
                                Text(
                                    text = phone,
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }

                            Text(
                                text = "Device: ${profile.device_unique_code}",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    // Error Message
                    if (errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            color = Color(0xFFFF5722),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Keep all your existing debug tools and settings items exactly as they are...
        // [Rest of your existing code remains unchanged]

        // Debug Tools
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Debug Tools",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val token = TokenManager.getAccessToken(context)
                                val isLoggedIn = TokenManager.isLoggedIn(context)
                                val email = TokenManager.getUserEmail(context)

                                Log.d("TokenDebug", "=== TOKEN TEST ===")
                                Log.d("TokenDebug", "Token: ${token?.take(50)}...")
                                Log.d("TokenDebug", "Is Logged In: $isLoggedIn")
                                Log.d("TokenDebug", "Email: $email")

                                Toast.makeText(
                                    context,
                                    "Token: ${if (!token.isNullOrEmpty()) "EXISTS" else "MISSING"}\nCheck Logcat for details",
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Test Token")
                        }

                        Button(
                            onClick = {
                                TokenManager.clearTokens(context)
                                Toast.makeText(context, "Tokens cleared", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
                        ) {
                            Text("Clear Tokens")
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        Log.d("NetworkTest", "=== NETWORK CONNECTIVITY TEST ===")
                                        Log.d("NetworkTest", "Base URL: http://10.0.2.2:8000/")

                                        val token = TokenManager.getAccessToken(context)
                                        Log.d("NetworkTest", "Token exists: ${!token.isNullOrEmpty()}")

                                        if (token != null) {
                                            val response = NetworkService.api.getUserProfile("Bearer $token")
                                            Log.d("NetworkTest", "Profile Response: ${response.success}")

                                            Toast.makeText(context, "Network test completed - check Logcat", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "No token - login first", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Log.e("NetworkTest", "Network test failed", e)
                                        Toast.makeText(context, "Network Error: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("Test Network")
                        }
                    }
                }
            }
        }

        // Settings List
        items(settings.size) { index ->
            val setting = settings[index]
            SettingItem(setting = setting)
        }

        // Sign Out Button
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLogoutDialog = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFFF5722).copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign Out",
                            tint = Color(0xFFFF5722)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Sign Out",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFFF5722)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Image Picker Dialog
    if (showImagePicker) {
        AlertDialog(
            onDismissRequest = { showImagePicker = false },
            title = { Text("Change Profile Picture") },
            text = {
                Column {
                    Text("Choose how you want to update your profile picture")
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            showImagePicker = false
                            imagePickerLauncher.launch("image/*")
                        }
                    ) {
                        Text("Gallery")
                    }

                    Button(
                        onClick = {
                            showImagePicker = false
                            cameraLauncher.launch(null)
                        }
                    ) {
                        Text("Camera")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showImagePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // All other existing dialogs remain the same...
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Sign Out",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Are you sure you want to sign out of your account?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        signOut()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF5722)
                    )
                ) {
                    Text(
                        text = "Sign Out",
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text(
                        text = "Cancel",
                        color = Color(0xFF1976D2)
                    )
                }
            }
        )
    }

    if (showEditDialog && profileData != null) {
        EditProfileDialog(
            profileData = profileData!!,
            onDismiss = { showEditDialog = false },
            onSave = { username, email, phone ->
                coroutineScope.launch {
                    updateUserProfile(username, email, phone)
                }
                showEditDialog = false
            }
        )
    }

    if (showNotificationSettings) {
        NotificationSettingsDialog(
            onDismiss = { showNotificationSettings = false }
        )
    }
}

// Keep all your existing @Composable functions exactly as they are
@Composable
fun SettingItem(setting: ProfileSetting) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { setting.onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF1976D2).copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    setting.icon,
                    contentDescription = setting.title,
                    tint = Color(0xFF1976D2)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = setting.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1A1A)
                )

                Text(
                    text = setting.subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "Navigate",
                tint = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    profileData: ProfileData,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var editName by remember { mutableStateOf(profileData.username) }
    var editEmail by remember { mutableStateOf(profileData.email) }
    var editPhone by remember { mutableStateOf(profileData.phone_number ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Name") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = editEmail,
                    onValueChange = { editEmail = it },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    )
                )

                OutlinedTextField(
                    value = editPhone,
                    onValueChange = { editPhone = it },
                    label = { Text("Phone") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(editName, editEmail, editPhone) }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun NotificationSettingsDialog(onDismiss: () -> Unit) {
    var gasLeakAlerts by remember { mutableStateOf(true) }
    var lowLevelAlerts by remember { mutableStateOf(true) }
    var pressureAlerts by remember { mutableStateOf(true) }
    var maintenanceReminders by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notification Settings") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Gas Leak Alerts")
                    Switch(
                        checked = gasLeakAlerts,
                        onCheckedChange = { gasLeakAlerts = it }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Low Level Warnings")
                    Switch(
                        checked = lowLevelAlerts,
                        onCheckedChange = { lowLevelAlerts = it }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pressure Alerts")
                    Switch(
                        checked = pressureAlerts,
                        onCheckedChange = { pressureAlerts = it }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Maintenance Reminders")
                    Switch(
                        checked = maintenanceReminders,
                        onCheckedChange = { maintenanceReminders = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

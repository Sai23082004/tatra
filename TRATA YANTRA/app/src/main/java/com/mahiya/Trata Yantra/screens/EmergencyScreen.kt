package com.mahiya.safegas.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.mahiya.safegas.network.NetworkService
import com.mahiya.safegas.network.EmergencyContactRequest
import com.mahiya.safegas.utils.TokenManager
import com.mahiya.safegas.data.EmergencyContactData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(navController: NavController) {
    var contacts: List<com.mahiya.safegas.network.EmergencyContactData> by remember { mutableStateOf(listOf<EmergencyContactData>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var isEmergencyMode by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()


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


    suspend fun loadEmergencyContacts() {
        try {
            isLoading = true
            errorMessage = ""

            val token = TokenManager.getAccessToken(context)
            Log.d("EmergencyDebug", "=== EMERGENCY CONTACTS DEBUG ===")
            Log.d("EmergencyDebug", "Token exists: ${!token.isNullOrEmpty()}")
            Log.d("EmergencyDebug", "Base URL: http://10.0.2.2:8000/")

            if (token != null) {
                Log.d("EmergencyDebug", "Making API call to: emergency/contacts/")
                Log.d("EmergencyDebug", "Authorization: Bearer ${token.take(20)}...")

                val response = NetworkService.api.getEmergencyContacts("Bearer $token")

                Log.d("EmergencyDebug", "Response received:")
                Log.d("EmergencyDebug", "Success: ${response.success}")
                Log.d("EmergencyDebug", "Message: ${response.message}")
                Log.d("EmergencyDebug", "Data: ${response.data?.size ?: 0} contacts")

                if (response.success == 1 && response.data != null) {
                    contacts = response.data
                    Log.d("EmergencyDebug", "✅ Loaded ${contacts.size} contacts successfully")
                } else {
                    errorMessage = response.message ?: "Failed to load contacts"
                    Log.e("EmergencyDebug", "❌ API call failed: $errorMessage")
                }
            } else {
                errorMessage = "Please login again"
                Log.e("EmergencyDebug", "❌ No token found - user needs to login")
            }
        } catch (e: Exception) {
            errorMessage = "Network error: ${e.message}"
            Log.e("EmergencyDebug", "❌ Exception occurred: ${e.message}", e)
        } finally {
            isLoading = false
        }
    }



    suspend fun addEmergencyContact(name: String, phone: String, relation: String) {
        try {
            val token = TokenManager.getAccessToken(context)
            if (token != null) {
                val contactData = EmergencyContactRequest(
                    name = name,
                    phone_number = phone,
                    relationship = relation,
                    is_primary = contacts.isEmpty() // First contact is primary
                )
                val response = NetworkService.api.addEmergencyContact("Bearer $token", contactData)
                if (response.success == 1) {
                    loadEmergencyContacts() // Reload contacts
                } else {
                    errorMessage = response.message ?: "Failed to add contact"
                }
            }
        } catch (e: Exception) {
            errorMessage = "Network error: ${e.message}"
        }
    }

    suspend fun deleteEmergencyContact(contactId: Int) {
        try {
            val token = TokenManager.getAccessToken(context)
            if (token != null) {
                val response = NetworkService.api.deleteEmergencyContact(contactId, "Bearer $token")
                if (response.success == 1) {
                    loadEmergencyContacts() // Reload contacts
                } else {
                    errorMessage = response.message ?: "Failed to delete contact"
                }
            }
        } catch (e: Exception) {
            errorMessage = "Network error: ${e.message}"
        }
    }

    suspend fun triggerEmergencySOS() {
        try {
            isEmergencyMode = true
            val token = TokenManager.getAccessToken(context)
            if (token != null) {
                val response = NetworkService.api.triggerEmergencySOS("Bearer $token")
                if (response.success == 1) {
                    // Show success message
                } else {
                    errorMessage = response.message ?: "Failed to trigger SOS"
                }
            }
        } catch (e: Exception) {
            errorMessage = "Network error: ${e.message}"
        } finally {
            isEmergencyMode = false
        }
    }

    // Load contacts from backend
    LaunchedEffect(Unit) {
        loadEmergencyContacts()
    }

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
        // Header
        Text(
            text = "Emergency Center",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD32F2F),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Quick access for emergency situations",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // SOS Button
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
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFF1744),
                                    Color(0xFFD32F2F)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch { triggerEmergencySOS() }
                        },
                        modifier = Modifier.fillMaxSize(),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        enabled = !isEmergencyMode
                    ) {
                        if (isEmergencyMode) {
                            CircularProgressIndicator(color = Color.White)
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "SOS",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Icon(
                                    Icons.Filled.Call,
                                    contentDescription = "Emergency Call",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Emergency SOS",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )

                Text(
                    text = "Press to call all emergency contacts",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Emergency Contacts Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Emergency Contacts",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.size(48.dp),
                containerColor = Color(0xFFD32F2F),
                shape = CircleShape
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add Contact",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Contacts List
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(contacts) { contact ->
                    EmergencyContactCard(
                        contact = contact,
                        onCall = { /* Handle call */ },
                        onDelete = {
                            coroutineScope.launch {
                                deleteEmergencyContact(contact.id)
                            }
                        }
                    )
                }
            }
        }

        // Add Contact Dialog
        if (showAddDialog) {
            AddContactDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, phone, relation ->
                    coroutineScope.launch {
                        addEmergencyContact(name, phone, relation)
                    }
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun EmergencyContactCard(
    contact: EmergencyContactData,
    onCall: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    .size(48.dp)
                    .background(
                        if (contact.is_primary) Color(0xFFFF1744).copy(alpha = 0.1f)
                        else Color(0xFF2196F3).copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (contact.relationship == "Emergency Service") Icons.Filled.LocalFireDepartment
                    else if (contact.relationship == "Service Provider") Icons.Filled.Build
                    else Icons.Filled.Person,
                    contentDescription = "Contact Type",
                    tint = if (contact.is_primary) Color(0xFFFF1744) else Color(0xFF2196F3),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = contact.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )

                    if (contact.is_primary) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PRIMARY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier
                                .background(
                                    Color(0xFFFF1744),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = contact.phone_number,
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Text(
                    text = contact.relationship,
                    fontSize = 12.sp,
                    color = Color(0xFF2196F3)
                )
            }

            // Action Buttons
            IconButton(
                onClick = onCall
            ) {
                Icon(
                    Icons.Filled.Call,
                    contentDescription = "Call",
                    tint = Color(0xFF4CAF50)
                )
            }

            if (!contact.is_primary) {
                IconButton(
                    onClick = onDelete
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFFF5722)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf("Family") }
    var expanded by remember { mutableStateOf(false) }

    val relations = listOf("Family", "Friend", "Doctor", "Neighbor", "Emergency Service", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Emergency Contact",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    leadingIcon = {
                        Icon(Icons.Filled.Person, contentDescription = "Name")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    leadingIcon = {
                        Icon(Icons.Filled.Phone, contentDescription = "Phone")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = relation,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Relationship") },
                        leadingIcon = {
                            Icon(Icons.Filled.Group, contentDescription = "Relation")
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        relations.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    relation = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onAdd(name, phone, relation)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F)
                )
            ) {
                Text("Add Contact")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

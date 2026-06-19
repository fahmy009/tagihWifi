package com.example.tagihinternet.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.fragment.app.FragmentActivity
import androidx.core.content.ContextCompat
import androidx.biometric.BiometricPrompt
import com.example.tagihinternet.data.entity.Role
import com.example.tagihinternet.data.entity.User
import com.example.tagihinternet.ui.ViewModelFactory
import com.example.tagihinternet.ui.components.DeveloperFooter
import com.example.tagihinternet.utils.ActivityHelper
import com.example.tagihinternet.utils.PreferenceHelper

@Composable
fun UserManagementScreen(factory: ViewModelFactory, currentUserRole: Role) {
    val viewModel: UserViewModel = viewModel(factory = factory)
    val users by viewModel.allUsers.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredUsers = remember(users, currentUserRole) {
        when (currentUserRole) {
            Role.SUPERADMIN -> users.filter { it.role != Role.SUPERADMIN }
            Role.ADMIN -> users.filter { it.role == Role.USER }
            else -> emptyList()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text("Daftar Petugas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Text("+ USER", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(filteredUsers) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp), 
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column {
                            Text(user.username, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Text(user.userCode, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = if (user.role == Role.ADMIN) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        user.role.name, 
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        IconButton(onClick = { viewModel.deleteUser(user) }) {
                            Text("✕", color = Color.Red.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        DeveloperFooter()
    }

    if (showAddDialog) {
        AddUserDialog(
            currentUserRole = currentUserRole,
            onDismiss = { showAddDialog = false },
            onConfirm = { username, password, role ->
                viewModel.insertUser(User(username = username, password = password, role = role))
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddUserDialog(currentUserRole: Role, onDismiss: () -> Unit, onConfirm: (String, String, Role) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(Role.USER) }

    val availableRoles = if (currentUserRole == Role.SUPERADMIN) listOf(Role.ADMIN, Role.USER) else listOf(Role.USER)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("User Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Role Petugas:", style = MaterialTheme.typography.labelMedium)
                availableRoles.forEach { r ->
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(selected = role == r, onClick = { role = r })
                        Text(r.name, fontSize = 14.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(username, password, role) }, enabled = username.isNotBlank() && password.isNotBlank(), shape = RoundedCornerShape(8.dp)) { Text("SIMPAN") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("BATAL") }
        }
    )
}

@Composable
fun EditProfileDialog(user: User, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var username by remember { mutableStateOf(user.username) }
    var password by remember { mutableStateOf(user.password) }
    val context = LocalContext.current
    val activity = remember(context) { ActivityHelper.findFragmentActivity(context) }
    var isBiometricRegistered by remember { mutableStateOf(PreferenceHelper.isBiometricEnabled(context)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ganti Profil", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("ID: ${user.userCode}", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username Baru") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password Baru") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Keamanan Lokal", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        if (activity != null) {
                            val biometricManager = androidx.biometric.BiometricManager.from(context)
                            val canAuth = biometricManager.canAuthenticate(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG)
                            
                            if (canAuth != androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS) {
                                android.widget.Toast.makeText(context, "Sensor tidak tersedia atau sidik jari belum didaftarkan di sistem HP", android.widget.Toast.LENGTH_LONG).show()
                                return@Button
                            }

                            val executor = ContextCompat.getMainExecutor(context)
                            val biometricPrompt = BiometricPrompt(activity, executor,
                                object : BiometricPrompt.AuthenticationCallback() {
                                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                        super.onAuthenticationSucceeded(result)
                                        PreferenceHelper.setBiometricEnabled(context, true)
                                        isBiometricRegistered = true
                                        android.widget.Toast.makeText(context, "Sidik Jari Berhasil Didaftarkan!", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                })

                            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                .setTitle("Daftarkan Sidik Jari")
                                .setSubtitle("Konfirmasi sidik jari Anda untuk login instan")
                                .setNegativeButtonText("Batal")
                                .build()

                            biometricPrompt.authenticate(promptInfo)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = if (isBiometricRegistered) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary) else ButtonDefaults.buttonColors()
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isBiometricRegistered) "SIDIK JARI TERDAFTAR" else "DAFTARKAN SIDIK JARI")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(username, password) }, enabled = username.isNotBlank() && password.isNotBlank(), shape = RoundedCornerShape(8.dp)) { Text("SIMPAN PERUBAHAN") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("BATAL") }
        }
    )
}

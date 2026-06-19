package com.example.tagihinternet.ui.login

import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.core.content.ContextCompat
import com.example.tagihinternet.R
import com.example.tagihinternet.ui.components.DeveloperFooter
import com.example.tagihinternet.ui.components.LoadingOverlay
import com.example.tagihinternet.utils.ActivityHelper
import com.example.tagihinternet.utils.NetworkHelper
import com.example.tagihinternet.utils.PreferenceHelper

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var showUrlDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val activity = remember(context) { ActivityHelper.findFragmentActivity(context) }
    var isOnline by remember { mutableStateOf(NetworkHelper.isNetworkAvailable(context)) }
    val isLoading by viewModel.isLoading.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Persistent network monitoring via Toast-like overlay effect
    var lastOnlineStatus by remember { mutableStateOf(isOnline) }
    LaunchedEffect(Unit) {
        while (true) {
            val currentStatus = NetworkHelper.isNetworkAvailable(context)
            if (currentStatus != lastOnlineStatus) {
                if (currentStatus) {
                    Toast.makeText(context, "Terhubung (Online)", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Koneksi Terputus (Offline)", Toast.LENGTH_LONG).show()
                }
                lastOnlineStatus = currentStatus
                isOnline = currentStatus
            }
            kotlinx.coroutines.delay(3000)
        }
    }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            onLoginSuccess()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loginResult.collect { success ->
            if (success) {
                onLoginSuccess()
            } else {
                showError = true
            }
        }
    }

    fun launchBiometric() {
        if (activity == null) return
        
        val biometricManager = BiometricManager.from(context)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> { }
            else -> {
                Toast.makeText(context, "Sensor sidik jari tidak tersedia atau belum diatur di HP ini", Toast.LENGTH_LONG).show()
                return
            }
        }

        if (!PreferenceHelper.isBiometricEnabled(context)) {
            Toast.makeText(context, "Daftarkan sidik jari di menu profil terlebih dahulu", Toast.LENGTH_LONG).show()
            return
        }
        
        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val savedUser = PreferenceHelper.getSession(context)
                    if (savedUser != null) {
                        viewModel.login(savedUser.username, savedUser.password)
                    } else {
                        Toast.makeText(context, "Sesi tidak ditemukan, silakan login manual sekali", Toast.LENGTH_SHORT).show()
                    }
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Login Sidik Jari")
            .setSubtitle("Gunakan sidik jari Anda untuk masuk")
            .setNegativeButtonText("Batal")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    LoadingOverlay(isLoading = isLoading)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .aspectRatio(1f)
                    .padding(8.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Tagih Internet", 
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible)
                                Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff

                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    
                    if (showError) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Login Gagal. Cek kredensial atau koneksi.", 
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { 
                                isOnline = NetworkHelper.isNetworkAvailable(context)
                                if (isOnline) {
                                    viewModel.login(username, password)
                                } else {
                                    Toast.makeText(context, "Anda sedang Offline", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .weight(0.8f)
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("MASUK", fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        FilledTonalButton(
                            onClick = { launchBiometric() },
                            modifier = Modifier
                                .weight(0.2f)
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Fingerprint, contentDescription = "Fingerprint")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = { showUrlDialog = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pengaturan Database", fontSize = 12.sp)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            DeveloperFooter()
        }
    }

    if (showUrlDialog) {
        UrlSettingDialog(
            onDismiss = { showUrlDialog = false },
            onConfirm = { newUrl ->
                PreferenceHelper.setGasUrl(context, newUrl)
                showUrlDialog = false
            }
        )
    }
}

@Composable
fun UrlSettingDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    val context = LocalContext.current
    var url by remember { mutableStateOf(PreferenceHelper.getGasUrl(context)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pengaturan URL GAS", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Masukkan URL Web App Google Apps Script Anda:", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    placeholder = { Text("https://script.google.com/...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(url) }) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

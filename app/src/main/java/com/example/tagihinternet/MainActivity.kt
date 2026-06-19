package com.example.tagihinternet

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.*
import com.example.tagihinternet.ui.ViewModelFactory
import com.example.tagihinternet.ui.dashboard.DashboardScreen
import com.example.tagihinternet.ui.login.AuthViewModel
import com.example.tagihinternet.ui.login.LoginScreen
import com.example.tagihinternet.ui.theme.TagihInternetTheme
import com.example.tagihinternet.utils.BillingWorker
import com.example.tagihinternet.utils.UpdateManager
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private val app by lazy { application as BillingApplication }
    private val factory by lazy { ViewModelFactory(app.authRepository, app.billingRepository, app.userRepository) }
    private val authViewModel: AuthViewModel by viewModels { factory }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request Notification Permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setupBackgroundWork()

        setContent {
            TagihInternetTheme {
                val navController = rememberNavController()
                val currentUser by authViewModel.currentUser.collectAsState()

                // Check for updates from GitHub (Ganti version code sesuai build.gradle)
                UpdateManager.CheckUpdate(currentVersionCode = 1)

                // Global redirect logic
                LaunchedEffect(currentUser) {
                    if (currentUser == null) {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }

                NavHost(navController = navController, startDestination = if (currentUser == null) "login" else "dashboard") {
                    composable("login") {
                        LoginScreen(viewModel = authViewModel) {
                            navController.navigate("dashboard") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    }
                    composable("dashboard") {
                        DashboardScreen(factory = factory, onLogout = {
                            authViewModel.logout()
                        })
                    }
                }
            }
        }
    }

    private fun setupBackgroundWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val repeatingRequest = PeriodicWorkRequestBuilder<BillingWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "BillingSync",
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest
        )
    }
}

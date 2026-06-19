package com.example.tagihinternet.ui.dashboard

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tagihinternet.data.entity.Role
import com.example.tagihinternet.ui.ViewModelFactory
import com.example.tagihinternet.ui.bill.BillManagementScreen
import com.example.tagihinternet.ui.user.EditProfileDialog
import com.example.tagihinternet.ui.user.UserManagementScreen
import com.example.tagihinternet.ui.user.UserViewModel
import com.example.tagihinternet.ui.login.AuthViewModel
import com.example.tagihinternet.ui.bill.BillViewModel
import com.example.tagihinternet.ui.customer.CustomerManagementScreen
import com.example.tagihinternet.ui.components.LoadingOverlay
import com.example.tagihinternet.utils.NetworkHelper
import kotlinx.coroutines.delay

sealed class NavItem(val route: String, val icon: ImageVector, val label: String) {
    object History : NavItem("history", Icons.Default.History, "Riwayat")
    object Customers : NavItem("customers", Icons.Default.People, "Pelanggan")
    object Users : NavItem("users", Icons.Default.Badge, "Petugas")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    factory: ViewModelFactory,
    onLogout: () -> Unit
) {
    val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
    val userViewModel: UserViewModel = viewModel(factory = factory)
    val billViewModel: BillViewModel = viewModel(factory = factory)
    val authViewModel: AuthViewModel = viewModel(factory = factory)
    
    val currentUser by dashboardViewModel.currentUser.collectAsState()
    val isUserLoading by userViewModel.isLoading.collectAsState()
    val isBillLoading by billViewModel.isLoading.collectAsState()
    val isAuthLoading by authViewModel.isLoading.collectAsState()
    
    var currentTab by remember { mutableStateOf(NavItem.History.route) }
    var showEditProfile by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    var isOnline by remember { mutableStateOf(NetworkHelper.isNetworkAvailable(context)) }

    LaunchedEffect(Unit) {
        while (true) {
            val currentStatus = NetworkHelper.isNetworkAvailable(context)
            if (currentStatus != isOnline) {
                isOnline = currentStatus
            }
            delay(5000)
        }
    }

    LoadingOverlay(isLoading = isUserLoading || isBillLoading || isAuthLoading)

    val role = currentUser?.role

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        when (currentTab) {
                            NavItem.History.route -> if (role == Role.USER) "Input Tagihan" else "Riwayat Tagihan"
                            NavItem.Customers.route -> "Data Pelanggan"
                            NavItem.Users.route -> "Data Petugas"
                            else -> "Dashboard"
                        },
                        fontWeight = FontWeight.Bold
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { 
                        billViewModel.refresh()
                        userViewModel.refresh()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showEditProfile = true }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                }
            )
        },
        bottomBar = {
            if (role == Role.ADMIN || role == Role.SUPERADMIN) {
                NavigationBar {
                    val items = listOf(NavItem.History, NavItem.Customers, NavItem.Users)
                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentTab == item.route,
                            onClick = { currentTab = item.route }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
        ) {
            when (currentTab) {
                NavItem.Users.route -> UserManagementScreen(factory = factory, currentUserRole = role ?: Role.USER)
                NavItem.Customers.route -> CustomerManagementScreen(factory = factory)
                else -> BillManagementScreen(factory = factory, role = role ?: Role.USER, currentUser = currentUser)
            }
        }
    }

    if (showEditProfile && currentUser != null) {
        EditProfileDialog(
            user = currentUser!!,
            onDismiss = { showEditProfile = false },
            onConfirm = { newUsername, newPassword ->
                val updatedUser = currentUser!!.copy(username = newUsername, password = newPassword)
                userViewModel.updateUser(updatedUser)
                authViewModel.updateProfile(updatedUser)
                showEditProfile = false
                Toast.makeText(context, "Profil Berhasil Diperbarui", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

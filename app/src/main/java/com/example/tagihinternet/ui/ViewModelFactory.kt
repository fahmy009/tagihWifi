package com.example.tagihinternet.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tagihinternet.data.AuthRepository
import com.example.tagihinternet.data.BillingRepository
import com.example.tagihinternet.data.UserRepository
import com.example.tagihinternet.ui.login.AuthViewModel
import com.example.tagihinternet.ui.dashboard.DashboardViewModel
import com.example.tagihinternet.ui.bill.BillViewModel
import com.example.tagihinternet.ui.user.UserViewModel

class ViewModelFactory(
    private val authRepository: AuthRepository,
    private val billingRepository: BillingRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(authRepository) as T
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> DashboardViewModel(authRepository) as T
            modelClass.isAssignableFrom(BillViewModel::class.java) -> BillViewModel(billingRepository) as T
            modelClass.isAssignableFrom(UserViewModel::class.java) -> UserViewModel(userRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

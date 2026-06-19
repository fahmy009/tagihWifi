package com.example.tagihinternet.ui.dashboard

import androidx.lifecycle.ViewModel
import com.example.tagihinternet.data.AuthRepository

class DashboardViewModel(private val authRepository: AuthRepository) : ViewModel() {
    val currentUser = authRepository.currentUser
}

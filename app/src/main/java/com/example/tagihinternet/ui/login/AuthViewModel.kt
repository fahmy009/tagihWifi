package com.example.tagihinternet.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tagihinternet.data.AuthRepository
import com.example.tagihinternet.data.entity.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _loginResult = MutableSharedFlow<Boolean>()
    val loginResult = _loginResult.asSharedFlow()

    val currentUser = authRepository.currentUser

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = authRepository.login(username, password)
            _isLoading.value = false
            _loginResult.emit(success)
        }
    }

    fun logout() {
        authRepository.logout()
    }

    fun updateProfile(user: User) {
        authRepository.updateCurrentUser(user)
    }
}

package com.example.tagihinternet.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tagihinternet.data.UserRepository
import com.example.tagihinternet.data.entity.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val allUsers = userRepository.allUsers

    init {
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        _isLoading.value = true
        userRepository.refreshUsers()
        _isLoading.value = false
    }

    fun insertUser(user: User) = viewModelScope.launch {
        _isLoading.value = true
        userRepository.insertUser(user)
        _isLoading.value = false
    }
    fun updateUser(user: User) = viewModelScope.launch {
        _isLoading.value = true
        userRepository.updateUser(user)
        _isLoading.value = false
    }
    fun deleteUser(user: User) = viewModelScope.launch {
        _isLoading.value = true
        userRepository.deleteUser(user)
        _isLoading.value = false
    }
}

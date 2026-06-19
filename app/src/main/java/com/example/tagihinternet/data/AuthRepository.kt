package com.example.tagihinternet.data

import android.content.Context
import com.example.tagihinternet.data.entity.User
import com.example.tagihinternet.utils.PreferenceHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthRepository(private val context: Context) {
    private val _currentUser = MutableStateFlow<User?>(
        if (PreferenceHelper.isLoggedIn(context)) PreferenceHelper.getSession(context) else null
    )
    val currentUser: StateFlow<User?> = _currentUser

    suspend fun login(username: String, password: String): Boolean {
        return try {
            val apiService = RetrofitClient.getGasService(context)
            val users = apiService.getUsers()
            val user = users.find { it.username == username }
            if (user != null && user.password == password) {
                _currentUser.value = user
                PreferenceHelper.saveSession(context, user)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun logout() {
        _currentUser.value = null
        PreferenceHelper.clearSession(context)
    }

    fun updateCurrentUser(user: User) {
        _currentUser.value = user
        PreferenceHelper.saveSession(context, user)
    }
}

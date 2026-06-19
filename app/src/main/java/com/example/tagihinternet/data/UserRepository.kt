package com.example.tagihinternet.data

import android.content.Context
import com.example.tagihinternet.data.entity.User
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserRepository(private val context: Context) {
    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers
    private val gson = Gson()

    private fun getService() = RetrofitClient.getGasService(context)

    suspend fun refreshUsers() {
        try {
            val users = getService().getUsers()
            _allUsers.value = users
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getUserByUsername(username: String): User? {
        return try {
            getService().getUsers().find { it.username == username }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun insertUser(user: User) {
        try {
            getService().addData(table = "Users", jsonData = gson.toJson(user))
            refreshUsers()
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun updateUser(user: User) {
        try {
            getService().updateData(table = "Users", jsonData = gson.toJson(user))
            refreshUsers()
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun deleteUser(user: User) {
        try {
            getService().deleteData(table = "Users", id = user.id)
            refreshUsers()
        } catch (e: Exception) { e.printStackTrace() }
    }
}

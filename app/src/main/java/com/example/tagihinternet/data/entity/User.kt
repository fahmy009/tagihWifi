package com.example.tagihinternet.data.entity

data class User(
    val id: Long = 0,
    val userCode: String = "",
    val username: String,
    val password: String,
    val role: Role
)

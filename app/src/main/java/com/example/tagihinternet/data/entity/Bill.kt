package com.example.tagihinternet.data.entity

data class Bill(
    val id: Long = 0,
    val customerName: String,
    val customerLocation: String? = "",
    val amount: Double,
    val date: Long,
    val createdByUserId: Long,
    val createdByUsername: String
)

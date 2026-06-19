package com.example.tagihinternet.data

import com.example.tagihinternet.data.entity.Bill
import com.example.tagihinternet.data.entity.Customer
import com.example.tagihinternet.data.entity.User
import retrofit2.http.GET
import retrofit2.http.Query

interface GasApiService {
    @GET("exec")
    suspend fun getUsers(@Query("action") action: String = "getUsers"): List<User>

    @GET("exec")
    suspend fun getCustomers(@Query("action") action: String = "getCustomers"): List<Customer>

    @GET("exec")
    suspend fun getBills(@Query("action") action: String = "getBills"): List<Bill>

    @GET("exec")
    suspend fun initialize(@Query("action") action: String = "init"): Map<String, Any>

    // --- GET-based CRUD (More stable for GAS) ---
    
    @GET("exec")
    suspend fun addData(
        @Query("action") action: String = "add",
        @Query("table") table: String,
        @Query("data") jsonData: String // Send object as JSON string in query
    ): Map<String, Any>

    @GET("exec")
    suspend fun updateData(
        @Query("action") action: String = "update",
        @Query("table") table: String,
        @Query("data") jsonData: String
    ): Map<String, Any>

    @GET("exec")
    suspend fun deleteData(
        @Query("action") action: String = "delete",
        @Query("table") table: String,
        @Query("id") id: Long
    ): Map<String, Any>

    @GET("exec")
    suspend fun deleteByPeriod(
        @Query("action") action: String = "deleteByPeriod",
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Map<String, Any>
}

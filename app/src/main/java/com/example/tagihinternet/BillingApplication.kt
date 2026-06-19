package com.example.tagihinternet

import android.app.Application
import com.example.tagihinternet.data.AuthRepository
import com.example.tagihinternet.data.BillingRepository
import com.example.tagihinternet.data.UserRepository

class BillingApplication : Application() {
    val userRepository by lazy { UserRepository(this) }
    val billingRepository by lazy { BillingRepository(this) }
    val authRepository by lazy { AuthRepository(this) }

    override fun onCreate() {
        super.onCreate()
    }
}

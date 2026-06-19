package com.example.tagihinternet.data

import android.content.Context
import com.example.tagihinternet.utils.PreferenceHelper
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var instance: Retrofit? = null
    private var currentUrl: String? = null

    fun getGasService(context: Context): GasApiService {
        val url = PreferenceHelper.getGasUrl(context)
        
        if (instance == null || url != currentUrl) {
            currentUrl = url
            val baseUrl = if (url.isNotEmpty() && url.endsWith("exec")) {
                url.substring(0, url.lastIndexOf("exec"))
            } else if (url.isNotEmpty() && !url.endsWith("/")) {
                "$url/"
            } else {
                "https://script.google.com/macros/s/placeholder/"
            }

            instance = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        
        return instance!!.create(GasApiService::class.java)
    }
}

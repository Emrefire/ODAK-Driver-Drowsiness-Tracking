package com.example.driverdrowsinessapp.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // 1. Kendi IP adresini ve C# tarafındaki HTTP portunu (Örn: 5213) mutlaka yaz!
    // 2. Sona mutlaka "/" ekle.
    private const val BASE_URL = "http://10.158.204.135:5213/api/MobileApi/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
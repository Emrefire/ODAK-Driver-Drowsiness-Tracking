package com.example.driverdrowsinessapp.data

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("login") fun login(@Body request: LoginRequest): Call<LoginResponse>
    @POST("register") fun register(@Body request: RegisterRequest): Call<RegisterResponse>
    @POST("save-session") fun saveSession(@Body session: DrivingSession): Call<ApiResponse>

    // C# tarafındaki [HttpPost("live-update")] ismiyle birebir aynı yaptık!
    @POST("live-update")
    fun sendLiveUpdate(@Body metrics: DriverMetrics): Call<Void>

    @GET("history/{userId}") fun getHistory(@Path("userId") userId: Int): Call<List<DrivingSession>>
}
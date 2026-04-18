package com.example.driverdrowsinessapp.data

import com.google.gson.annotations.SerializedName

// --- 1. GİRİŞ (LOGIN) MODELLERİ ---
data class LoginRequest(
    @SerializedName("Email") val email: String,       // E harfi büyük
    @SerializedName("Password") val password: String  // P harfi büyük
)

data class RegisterRequest(
    @SerializedName("FullName") val fullName: String,
    @SerializedName("Email") val email: String,
    @SerializedName("Password") val password: String,
    @SerializedName("ConfirmPassword") val confirmPassword: String
)
// Kayıt cevabı için
data class RegisterResponse(
    val success: Boolean,
    val message: String?
)
data class LoginResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("userId") val userId: Int?,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("message") val message: String?
)

// --- 2. SÜRÜŞ VERİSİ MODELİ (Save & History İçin) ---
data class DrivingSession(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("userId") val userId: Int,
    @SerializedName("startTime") val startTime: String? = null,
    @SerializedName("endTime") val endTime: String? = null,
    @SerializedName("averageFatigue") val averageFatigue: Int,
    @SerializedName("sleepDuration") val sleepDuration: Int,
    @SerializedName("yawnCount") val yawnCount: Int,
    @SerializedName("score") val score: Int
)

// --- 3. CANLI YAYIN (SIGNALR) İÇİN METRİK MODELİ ---
data class DriverMetrics(
    @SerializedName("userId") val userId: Int,
    @SerializedName("earValue") val earValue: Float, // Göz açıklığı oranı
    @SerializedName("marValue") val marValue: Float, // Ağız açıklığı oranı
    @SerializedName("isDrowsy") val isDrowsy: Boolean, // Anlık uykulu mu?
    @SerializedName("isYawning") val isYawning: Boolean // Anlık esniyor mu?
)

// --- 4. GENEL YANIT MODELİ (Success/Message dönen API'ler için) ---
data class ApiResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)
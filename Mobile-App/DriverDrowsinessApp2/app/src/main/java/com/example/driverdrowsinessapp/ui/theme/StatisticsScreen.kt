package com.example.driverdrowsinessapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.driverdrowsinessapp.data.DrivingSession
import com.example.driverdrowsinessapp.data.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// --- RENKLER (Bu sayfaya özel isimlendirildi) ---
private val StatsBrandBlue = Color(0xFF2563EB)
private val StatsBrandDarkText = Color(0xFF111827)
private val StatsBrandGrayText = Color(0xFF6B7280)
private val StatsBrandLightBg = Color(0xFFF9FAFB)

@Composable
fun StatisticsScreen(
    userId: Int, // MainActivity'den gelen gerçek ID (Emre için 1, Mustafa için 2)
    onBackClick: () -> Unit
) {
    var sessions by remember { mutableStateOf<List<DrivingSession>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Sayfa açıldığında veya userId değiştiğinde çalışır
    LaunchedEffect(userId) {
        isLoading = true
        // ARTIK BURASI DİNAMİK: testUserId yerine dışarıdan gelen userId kullanılıyor
        RetrofitClient.instance.getHistory(userId).enqueue(object : Callback<List<DrivingSession>> {
            override fun onResponse(call: Call<List<DrivingSession>>, response: Response<List<DrivingSession>>) {
                isLoading = false
                if (response.isSuccessful && response.body() != null) {
                    sessions = response.body()!!
                } else {
                    errorMessage = "Veri çekilirken bir hata oluştu."
                }
            }

            override fun onFailure(call: Call<List<DrivingSession>>, t: Throwable) {
                isLoading = false
                errorMessage = "Bağlantı kurulamadı. Ngrok veya Yerel Sunucu açık mı?"
            }
        })
    }

    // İstatistik hesaplamaları (sessions değiştikçe otomatik güncellenir)
    val avgScore = if (sessions.isNotEmpty()) sessions.map { it.score }.average().toInt() else 0
    val totalYawn = if (sessions.isNotEmpty()) sessions.sumOf { it.yawnCount } else 0
    val totalSleep = if (sessions.isNotEmpty()) sessions.sumOf { it.sleepDuration } else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6))
    ) {
        // --- ÜST BAŞLIK ---
        Surface(color = Color.White, shadowElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick, modifier = Modifier.background(Color(0xFFF3F4F6), CircleShape).size(40.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = StatsBrandDarkText)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "Sürüş İstatistikleri", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = StatsBrandDarkText)
            }
        }

        // --- İÇERİK ---
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = StatsBrandBlue)
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage!!, color = StatsBrandGrayText)
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        ModernStatCard("Skor", "%$avgScore", Icons.Default.CheckCircle, Color(0xFF10B981), Color(0xFFD1FAE5), Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(12.dp))
                        ModernStatCard("Uyku", "${totalSleep}s", Icons.Default.Info, Color(0xFFF59E0B), Color(0xFFFEF3C7), Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(12.dp))
                        ModernStatCard("Esneme", "$totalYawn", Icons.Default.Warning, Color(0xFFEF4444), Color(0xFFFEE2E2), Modifier.weight(1f))
                    }
                }

                item {
                    Text(text = "Geçmiş Sürüşler", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StatsBrandDarkText, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                }

                if (sessions.isEmpty()) {
                    item {
                        Text("Kayıtlı sürüş bulunamadı.", color = StatsBrandGrayText)
                    }
                } else {
                    items(sessions) { session ->
                        ModernDrivingSessionItem(session)
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernStatCard(title: String, value: String, icon: ImageVector, iconColor: Color, bgColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(modifier = Modifier.size(36.dp).background(bgColor, CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = StatsBrandDarkText)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = title, fontSize = 12.sp, color = StatsBrandGrayText, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ModernDrivingSessionItem(session: DrivingSession) {
    val scoreColor = if (session.score >= 90) Color(0xFF10B981) else if (session.score >= 70) Color(0xFFF59E0B) else Color(0xFFEF4444)
    val scoreBg = if (session.score >= 90) Color(0xFFD1FAE5) else if (session.score >= 70) Color(0xFFFEF3C7) else Color(0xFFFEE2E2)

    val datePart = session.startTime?.substringBefore("T") ?: "Bilinmiyor"
    val timePart = try { session.startTime?.substringAfter("T")?.substring(0, 5) ?: "" } catch (e: Exception) { "" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(StatsBrandLightBg, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = StatsBrandBlue)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = datePart, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = StatsBrandDarkText)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = timePart, fontSize = 12.sp, color = StatsBrandGrayText)
                    Text(text = " • ", fontSize = 12.sp, color = StatsBrandGrayText)
                    Text(text = "${session.yawnCount} Esneme, ${session.sleepDuration}sn Uyku", fontSize = 12.sp, color = StatsBrandGrayText)
                }
            }

            Surface(color = scoreBg, shape = RoundedCornerShape(50)) {
                Text(
                    text = "${session.score}",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = scoreColor
                )
            }
        }
    }
}
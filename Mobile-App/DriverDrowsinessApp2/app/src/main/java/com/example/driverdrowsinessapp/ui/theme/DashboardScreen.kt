package com.example.driverdrowsinessapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- RENK TANIMLAMALARI ---
private val DashBrandBlue = Color(0xFF2563EB)
private val DashBrandDarkText = Color(0xFF111827)
private val DashBrandGrayText = Color(0xFF6B7280)
private val DashBrandLightBg = Color(0xFFF9FAFB)

@Composable
fun DashboardScreen(
    userName: String, // API'den gelen isim buraya gelecek
    onStartDrivingClick: () -> Unit,
    onStatsClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6))
    ) {
        // --- ÜST BAR VE PROFİL ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Hoş geldin,", fontSize = 14.sp, color = DashBrandGrayText)
                // ARTIK BURASI DİNAMİK:
                Text(text = userName, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = DashBrandDarkText)
            }

            IconButton(
                onClick = onProfileClick,
                modifier = Modifier
                    .size(48.dp)
                    .background(DashBrandLightBg, CircleShape)
                    .border(1.dp, Color(0xFFE5E7EB), CircleShape)
            ) {
                Icon(Icons.Default.Person, contentDescription = "Profil", tint = DashBrandBlue)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- ANA EYLEM: SÜRÜŞE BAŞLA BUTONU ---
        Box(modifier = Modifier.padding(horizontal = 24.dp)) {
            Button(
                onClick = onStartDrivingClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = DashBrandBlue),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Sürüşü Başlat", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = "Yapay zeka asistanını aktifleştir", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Hızlı Erişim",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DashBrandDarkText,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // --- İSTATİSTİKLER BUTONU ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clickable { onStatsClick() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(48.dp).background(Color(0xFFE0E7FF), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.List, contentDescription = null, tint = DashBrandBlue)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Sürüş İstatistiklerim", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DashBrandDarkText)
                    Text(text = "Geçmiş sürüş analizlerini incele", fontSize = 12.sp, color = DashBrandGrayText)
                }
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = DashBrandGrayText)
            }
        }
    }
}
package com.example.driverdrowsinessapp.ui.theme

import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.driverdrowsinessapp.R

// --- RENK TANIMLAMALARI ---
private val ProfBrandBlue = Color(0xFF2563EB)
private val ProfBrandDarkText = Color(0xFF111827)
private val ProfBrandGrayText = Color(0xFF6B7280)
private val ProfBrandLightBg = Color(0xFFF9FAFB)

@Composable
fun ProfileScreen(
    userName: String,   // Dinamik İsim
    userEmail: String,  // Dinamik E-posta
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfBrandLightBg)
    ) {
        // --- ÜST BAŞLIK ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = ProfBrandDarkText)
            }
            Text(
                text = "Profilim",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ProfBrandDarkText,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // --- PROFİL KARTI ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profil Resmi
            Image(
                painter = painterResource(id = R.drawable.driver_image),
                contentDescription = "Profil Resmi",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(2.dp, ProfBrandBlue, CircleShape)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ARTIK BURASI DİNAMİK:
            Text(
                text = userName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = ProfBrandDarkText
            )
            Text(
                text = userEmail,
                fontSize = 14.sp,
                color = ProfBrandGrayText
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- BİLGİ KARTLARI ---
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            ProfileOptionItem(icon = Icons.Default.Person, title = "Hesap Bilgileri", subtitle = "Ad, Soyad, Telefon")
            Spacer(modifier = Modifier.height(12.dp))
            ProfileOptionItem(icon = Icons.Default.Notifications, title = "Bildirimler", subtitle = "Açık")
            Spacer(modifier = Modifier.height(12.dp))
            ProfileOptionItem(icon = Icons.Default.Security, title = "Güvenlik", subtitle = "Şifre değiştir")
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- ÇIKIŞ YAP BUTONU ---
        Button(
            onClick = onLogoutClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .height(50.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFFDC2626))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Çıkış Yap", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProfileOptionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().clickable { /* Detay */ }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(ProfBrandLightBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = ProfBrandBlue)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.SemiBold, color = ProfBrandDarkText)
                Text(text = subtitle, fontSize = 12.sp, color = ProfBrandGrayText)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ProfBrandGrayText)
        }
    }
}
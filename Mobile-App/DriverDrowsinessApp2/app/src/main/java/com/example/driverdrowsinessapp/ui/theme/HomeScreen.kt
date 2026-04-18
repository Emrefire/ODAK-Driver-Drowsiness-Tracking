package com.example.driverdrowsinessapp.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.driverdrowsinessapp.R

private val HomeBrandBlue = Color(0xFF2563EB)
private val HomeBrandDarkText = Color(0xFF111827)
private val HomeBrandGrayText = Color(0xFF6B7280)

@Composable
fun HomeScreen(
    onLoginClick: () -> Unit,
    onStartSystemClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val isSmallScreen = screenWidth < 360.dp

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
    ) {
        // Sağ üstteki butonu parametre olarak alsa da içerde kullanmıyoruz artık
        HomeTopNavBar()
        Spacer(modifier = Modifier.height(screenHeight * 0.02f))

        HomeHeroSection(onStartSystemClick, isSmallScreen)
        Spacer(modifier = Modifier.height(screenHeight * 0.03f))

        HomeFeatureImageSection(screenWidth)
        Spacer(modifier = Modifier.height(screenHeight * 0.04f))

        HomeStatsFooter(isSmallScreen)
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun HomeTopNavBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.Start, // Sadece logo ve isim kalacağı için sola yasladık
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.driver_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(45.dp).clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text("ODAK", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = HomeBrandDarkText)
        }
        // SAĞ ÜSTTEKİ BUTON BURADAN KALDIRILDI
    }
}

@Composable
private fun HomeHeroSection(onStartSystemClick: () -> Unit, isSmallScreen: Boolean) {
    val titleSize = if (isSmallScreen) 28.sp else 36.sp
    val descSize = if (isSmallScreen) 14.sp else 16.sp
    val uriHandler = LocalUriHandler.current

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        Surface(
            color = Color.White, shape = RoundedCornerShape(50),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Red))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Yorgunluk Takibi Aktif", fontSize = 12.sp, color = HomeBrandGrayText, fontWeight = FontWeight.Medium)
            }
        }

        Text("Siz Sürün,\nODAK İzlesin.", fontSize = titleSize, lineHeight = titleSize * 1.1, fontWeight = FontWeight.ExtraBold, color = HomeBrandDarkText, modifier = Modifier.padding(bottom = 16.dp))
        Text("Gelişmiş yüz tanıma ve analiz teknolojimizle yorgunluğu saniyeler içinde tespit ediyor, sevdiklerinizi ve sizi koruyoruz.", fontSize = descSize, color = HomeBrandGrayText, lineHeight = descSize * 1.5, modifier = Modifier.padding(bottom = 24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = onStartSystemClick,
                colors = ButtonDefaults.buttonColors(containerColor = HomeBrandBlue),
                shape = RoundedCornerShape(50),
                modifier = Modifier.height(50.dp)
            ) {
                // Rengi zorunlu olarak Beyaz (Color.White) yaptık
                Text(text = "Sisteme Giriş", color = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
            }

            Spacer(modifier = Modifier.width(12.dp))

            OutlinedButton(
                onClick = {
                    uriHandler.openUri("https://www.google.com")
                },
                shape = RoundedCornerShape(50),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1D5DB)),
                modifier = Modifier.height(50.dp)
            ) {
                Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp), tint = HomeBrandDarkText)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Web Paneli", color = HomeBrandDarkText)
            }
        }
    }
}

@Composable
private fun HomeFeatureImageSection(screenWidth: androidx.compose.ui.unit.Dp) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).aspectRatio(1.3f)) {
        Image(painter = painterResource(id = R.drawable.driver_image), contentDescription = "Sürücü", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp)))
        Surface(modifier = Modifier.align(Alignment.BottomStart).padding(start = 20.dp, bottom = 20.dp).width(screenWidth * 0.55f).shadow(10.dp, RoundedCornerShape(12.dp)), color = Color.White, shape = RoundedCornerShape(12.dp)) {
        }
    }
}

@Composable
private fun HomeStatsFooter(isSmallScreen: Boolean) {
    val statNumberSize = if (isSmallScreen) 22.sp else 28.sp
    val statLabelSize = if (isSmallScreen) 8.sp else 10.sp

    Row(modifier = Modifier.fillMaxWidth().background(HomeBrandBlue).padding(vertical = 40.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
        HomeStatItem(value = "0.2sn", label = "ALGILAMA HIZI", numSize = statNumberSize, lblSize = statLabelSize)
        Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.3f)))
        HomeStatItem(value = "%98", label = "DOĞRULUK PAYI", numSize = statNumberSize, lblSize = statLabelSize)
        Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.3f)))
        HomeStatItem(value = "7/24", label = "KESİNTİSİZ TAKİP", numSize = statNumberSize, lblSize = statLabelSize)
    }
}

@Composable
private fun HomeStatItem(value: String, label: String, numSize: androidx.compose.ui.unit.TextUnit, lblSize: androidx.compose.ui.unit.TextUnit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = numSize, fontWeight = FontWeight.Bold, color = Color.White)
        Text(text = label, fontSize = lblSize, fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.8f))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(onLoginClick = {}, onStartSystemClick = {})
}
package com.example.driverdrowsinessapp.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.driverdrowsinessapp.R

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    // Ekran genişliğini al
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val isSmallScreen = screenWidth < 360.dp
    val SplashBrandBlue = Color(0xFF2563EB)
    // Dinamik Boyutlar
    val logoSize = screenWidth * 0.5f // Ekran genişliğinin yarısı kadar logo
    val titleSize = if (isSmallScreen) 32.sp else 40.sp
    val subtitleSize = if (isSmallScreen) 10.sp else 12.sp

    LaunchedEffect(true) {
        delay(2500)
        onTimeout()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBrandBlue), // Hata burada çözüldü
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Dinamik Logo
        Image(
            painter = painterResource(id = R.drawable.driver_logo),
            contentDescription = "Logo",
            modifier = Modifier
                .size(logoSize) // Dinamik boyut
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Dinamik Yazı
        Text(
            text = "ODAK",
            fontSize = titleSize,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = 4.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "GÜVENLİ SÜRÜŞ ASİSTANI",
            fontSize = subtitleSize,
            color = Color.White.copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium,
            letterSpacing = 2.sp
        )
    }
}
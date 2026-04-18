package com.example.driverdrowsinessapp.ui.theme

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.driverdrowsinessapp.data.LoginRequest
import com.example.driverdrowsinessapp.data.LoginResponse
import com.example.driverdrowsinessapp.data.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val LoginBrandBlue = Color(0xFF2563EB)
private val LoginBrandDarkText = Color(0xFF111827) // Siyah tonu
private val LoginBrandGrayText = Color(0xFF6B7280) // Gri tonu

@Composable
fun LoginScreen(
    onLoginSuccess: (name: String, email: String, userId: Int) -> Unit,
    onRegisterClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // TextField renklerini tek bir yerden yönetmek için
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = LoginBrandDarkText,    // Yazı yazarken siyah
        unfocusedTextColor = LoginBrandDarkText,  // Yazı yazmazken siyah
        focusedLabelColor = LoginBrandBlue,       // Odaklandığında label mavi
        unfocusedLabelColor = LoginBrandGrayText, // Odaklanmadığında label gri
        cursorColor = LoginBrandBlue,             // İmleç rengi
        focusedBorderColor = LoginBrandBlue,      // Kenarlık mavi
        unfocusedBorderColor = Color.LightGray    // Kenarlık gri
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = LoginBrandDarkText)
            }
        }

        Text("ODAK", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = LoginBrandBlue)
        Text("Sürücü Paneline Giriş", fontSize = 16.sp, color = LoginBrandGrayText, modifier = Modifier.padding(bottom = 32.dp))

        // --- E-POSTA ALANI ---
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-Posta Adresi") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            textStyle = TextStyle(color = LoginBrandDarkText), // İçerideki yazıyı siyaha zorla
            colors = textFieldColors
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- ŞİFRE ALANI ---
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Şifre") },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            textStyle = TextStyle(color = LoginBrandDarkText), // İçerideki yazıyı siyaha zorla
            colors = textFieldColors,
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = LoginBrandGrayText // İkon rengini griye sabitle
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- GİRİŞ YAP BUTONU ---
        Button(
            onClick = {
                if (email.isEmpty() || password.isEmpty()) return@Button
                isLoading = true
                val request = LoginRequest(email.trim(), password.trim())
                RetrofitClient.instance.login(request).enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        isLoading = false
                        val body = response.body()
                        if (response.isSuccessful && body?.success == true) {
                            onLoginSuccess(body.fullName ?: "Sürücü", email.trim(), body.userId ?: -1)
                        } else {
                            Toast.makeText(context, "Giriş Başarısız!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        isLoading = false
                        Toast.makeText(context, "Bağlantı Hatası!", Toast.LENGTH_SHORT).show()
                    }
                })
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LoginBrandBlue,
                contentColor = Color.White // Buton içindeki her şeyi (Text/Icon) Beyaza zorlar
            ),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                // Burada rengi açıkça Color.White veriyoruz
                Text("Giriş Yap", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Hesabın yok mu?", color = LoginBrandGrayText)
            TextButton(onClick = onRegisterClick) {
                Text("Kayıt Ol", color = LoginBrandBlue, fontWeight = FontWeight.Bold)
            }
        }
    }
}
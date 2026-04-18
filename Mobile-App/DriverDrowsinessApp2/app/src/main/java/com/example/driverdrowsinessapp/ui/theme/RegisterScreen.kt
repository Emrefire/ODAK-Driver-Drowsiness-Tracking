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
import com.example.driverdrowsinessapp.data.RegisterRequest
import com.example.driverdrowsinessapp.data.RegisterResponse
import com.example.driverdrowsinessapp.data.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val RegBrandBlue = Color(0xFF2563EB)
private val RegBrandDarkText = Color(0xFF111827)
private val RegBrandGrayText = Color(0xFF6B7280)

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) } // Yeni state
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // TextField renklerini sabitlemek için ortak yapı
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = RegBrandDarkText,
        unfocusedTextColor = RegBrandDarkText,
        focusedLabelColor = RegBrandBlue,
        unfocusedLabelColor = RegBrandGrayText,
        cursorColor = RegBrandBlue,
        focusedBorderColor = RegBrandBlue,
        unfocusedBorderColor = Color.LightGray
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = RegBrandDarkText, modifier = Modifier.size(28.dp))
            }
        }

        Text("ODAK", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = RegBrandBlue)
        Text("Yeni Hesap Oluştur", fontSize = 16.sp, color = RegBrandGrayText, modifier = Modifier.padding(bottom = 24.dp))

        // AD SOYAD
        OutlinedTextField(
            value = name, onValueChange = { name = it },
            label = { Text("Ad Soyad") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            enabled = !isLoading,
            textStyle = TextStyle(color = RegBrandDarkText),
            colors = textFieldColors
        )
        Spacer(modifier = Modifier.height(12.dp))

        // E-POSTA
        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("E-Posta Adresi") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            enabled = !isLoading,
            textStyle = TextStyle(color = RegBrandDarkText),
            colors = textFieldColors
        )
        Spacer(modifier = Modifier.height(12.dp))

        // ŞİFRE
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Şifre") },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            enabled = !isLoading,
            textStyle = TextStyle(color = RegBrandDarkText),
            colors = textFieldColors,
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = RegBrandGrayText
                    )
                }
            }
        )
        Spacer(modifier = Modifier.height(12.dp))

        // ŞİFRE TEKRAR (Görünürlük eklendi)
        OutlinedTextField(
            value = confirmPassword, onValueChange = { confirmPassword = it },
            label = { Text("Şifre Tekrar") },
            visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            enabled = !isLoading,
            textStyle = TextStyle(color = RegBrandDarkText),
            colors = textFieldColors,
            trailingIcon = {
                IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                    Icon(
                        imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = RegBrandGrayText
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // KAYIT OL BUTONU
        Button(
            onClick = {
                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(context, "Lütfen alanları doldurun.", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (password != confirmPassword) {
                    Toast.makeText(context, "Şifreler uyuşmuyor!", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isLoading = true
                val request = RegisterRequest(name.trim(), email.trim(), password.trim(), confirmPassword.trim())

                RetrofitClient.instance.register(request).enqueue(object : Callback<RegisterResponse> {
                    override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                        isLoading = false
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(context, "Kayıt Başarılı!", Toast.LENGTH_LONG).show()
                            onRegisterSuccess()
                        } else {
                            val errorBody = response.errorBody()?.string()
                            val apiMessage = response.body()?.message
                            val finalMsg = apiMessage ?: "Sunucu Hatası: $errorBody"
                            Toast.makeText(context, finalMsg, Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                        isLoading = false
                        Toast.makeText(context, "Bağlantı Hatası: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RegBrandBlue,
                contentColor = Color.White // Buton içindeki metni beyaza zorlar
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = "Kayıt Ol",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White // Telefon temasından bağımsız beyaz
                )
            }
        }
    }
}
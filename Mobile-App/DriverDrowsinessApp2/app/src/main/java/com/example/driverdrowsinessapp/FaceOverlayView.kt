package com.example.driverdrowsinessapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark

class FaceOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var landmarks: List<NormalizedLandmark>? = null

    // Gösterilecek Değerler
    private var currentEAR = 0f
    private var currentMAR = 0f
    private var currentPitch = 0f
    private var currentStatus = "NORMAL"

    // --- TASARIM ---

    // 1. Bilgi Paneli Arkaplanı
    private val dashboardBgPaint = Paint().apply {
        color = Color.parseColor("#99000000") // Yarı saydam Siyah
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // 2. Başlık Yazıları (Beyaz)
    private val labelPaint = Paint().apply {
        color = Color.WHITE
        textSize = 45f
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    // 3. Değer Yazıları (Turkuaz)
    private val valuePaint = Paint().apply {
        color = Color.parseColor("#00E5FF")
        textSize = 50f
        isAntiAlias = true
        style = Paint.Style.FILL
        isFakeBoldText = true
    }

    // 4. Durum Yazısı (Büyük)
    private val statusPaint = Paint().apply {
        color = Color.WHITE
        textSize = 80f
        isAntiAlias = true
        style = Paint.Style.FILL
        isFakeBoldText = true
        setShadowLayer(10f, 0f, 0f, Color.BLACK)
    }

    // Sadece Landmarks (Noktalar) için güncelleme (Çizmiyoruz ama veri akışı için kalsın)
    fun updateLandmarks(newLandmarks: List<NormalizedLandmark>) {
        landmarks = newLandmarks
        invalidate() // Ekranı yenile
    }

    // --- YENİ GÜNCELLEME FONKSİYONU ---
    fun updateDebugValues(ear: Float, mar: Float, pitch: Float, status: String) {
        currentEAR = ear
        currentMAR = mar
        currentPitch = pitch
        currentStatus = status
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Panel Boyutları
        val panelWidth = 550f
        val panelHeight = 400f // 3 değer için yeterli

        // Paneli Çiz
        canvas.drawRoundRect(20f, 50f, 20f + panelWidth, 50f + panelHeight, 30f, 30f, dashboardBgPaint)

        var currentY = 120f
        val startX = 50f
        val valueX = 350f

        // 1. EAR (Göz)
        canvas.drawText("EAR (Göz):", startX, currentY, labelPaint)
        canvas.drawText(String.format("%.2f", currentEAR), valueX, currentY, valuePaint)

        currentY += 90f
        // 2. MAR (Ağız)
        canvas.drawText("MAR (Ağız):", startX, currentY, labelPaint)
        canvas.drawText(String.format("%.2f", currentMAR), valueX, currentY, valuePaint)

        currentY += 90f
        // 3. PITCH (Kafa)
        canvas.drawText("PITCH (Baş):", startX, currentY, labelPaint)
        canvas.drawText(String.format("%.1f°", currentPitch), valueX, currentY, valuePaint)


        // --- DURUM YAZISI (ALARM DURUMU) ---
        val statusTextWidth = statusPaint.measureText(currentStatus)
        val statusX = (width - statusTextWidth) / 2f
        val statusY = height - 150f

        // Eğer UYKULU ise Kırmızı, Değilse Beyaz yapalım
        if (currentStatus.contains("UYKU")) {
            statusPaint.color = Color.RED
        } else if (currentStatus.contains("ESNEME")) {
            statusPaint.color = Color.YELLOW
        } else {
            statusPaint.color = Color.WHITE
        }

        canvas.drawText(currentStatus, statusX, statusY, statusPaint)
    }
}
package com.example.driverdrowsinessapp.face

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark

object HeadPoseUtils {
    // Python'daki mantığın basitleştirilmiş hali.
    // Burnun (1) gözlere göre dikey konumu değişirse kafa eğilmiş demektir.

    fun calculatePitch(landmarks: List<NormalizedLandmark>): Float {
        // MediaPipe Noktaları:
        // 1: Burun ucu
        // 10: Alın (Saç çizgisi ortası)
        // 152: Çene ucu

        if (landmarks.isEmpty()) return 0f

        val nose = landmarks[1]
        val chin = landmarks[152]
        val forehead = landmarks[10]

        // Yüzün toplam dikey uzunluğu
        val faceHeight = distance(forehead, chin)

        // Burun ile çene arasındaki mesafe
        val noseToChin = distance(nose, chin)

        // Oran: Eğer kafa aşağı eğilirse, burun çeneye yaklaşır (2D düzlemde).
        // Bu oranı açıya (Pitch) kabaca map ediyoruz.
        val ratio = noseToChin / faceHeight

        // Bu değerler deneysel kalibrasyon gerektirebilir ama başlangıç için:
        // Normal bakışta oran ~0.50 gibidir.
        // Aşağı bakınca oran düşer (< 0.35)
        // Yukarı bakınca oran artar (> 0.65)

        // Basit bir mapping ile dereceye çevirelim (Tahmini)
        // 0.5 -> 0 derece
        // 0.3 -> 30 derece (Aşağı)

        val estimatedPitch = (0.5f - ratio) * 150f

        return estimatedPitch
    }

    private fun distance(p1: NormalizedLandmark, p2: NormalizedLandmark): Float {
        return Math.sqrt(
            Math.pow((p1.x() - p2.x()).toDouble(), 2.0) +
                    Math.pow((p1.y() - p2.y()).toDouble(), 2.0)
        ).toFloat()
    }
}
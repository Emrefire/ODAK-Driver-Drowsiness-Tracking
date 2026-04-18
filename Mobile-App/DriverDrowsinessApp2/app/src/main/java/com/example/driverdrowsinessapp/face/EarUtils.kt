package com.example.driverdrowsinessapp.face

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.hypot

object EarUtils {

    // İki nokta arası mesafe (Öklid)
    private fun distance(p1: NormalizedLandmark, p2: NormalizedLandmark): Float {
        val dx = p1.x() - p2.x()
        val dy = p1.y() - p2.y()
        return hypot(dx, dy)
    }

    // EAR Formülü: (A + B) / (2.0 * C)
    // Gelen liste sırası: [p1, p2, p3, p4, p5, p6] (Python'daki sırayla aynı olmalı)
    fun calculateEAR(eye: List<NormalizedLandmark>): Float {
        if (eye.size != 6) return 0.0f

        // Python indeksleri: p1, p2... p6
        // MediaPipe noktaları: [33, 160, 158, 133, 153, 144]
        val p1 = eye[0] // 33 (Sol köşe)
        val p2 = eye[1] // 160 (Üst 1)
        val p3 = eye[2] // 158 (Üst 2)
        val p4 = eye[3] // 133 (Sağ köşe)
        val p5 = eye[4] // 153 (Alt 1)
        val p6 = eye[5] // 144 (Alt 2)

        // Dikey mesafeler
        val vertical1 = distance(p2, p6)
        val vertical2 = distance(p3, p5)

        // Yatay mesafe
        val horizontal = distance(p1, p4)

        if (horizontal == 0f) return 0.0f

        return (vertical1 + vertical2) / (2.0f * horizontal)
    }
}
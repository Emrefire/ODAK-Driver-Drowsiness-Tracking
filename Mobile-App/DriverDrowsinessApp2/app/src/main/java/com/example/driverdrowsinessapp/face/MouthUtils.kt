package com.example.driverdrowsinessapp.face

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.hypot

object MouthUtils {

    private fun distance(p1: NormalizedLandmark, p2: NormalizedLandmark): Float {
        val dx = p1.x() - p2.x()
        val dy = p1.y() - p2.y()
        return hypot(dx, dy)
    }

    // MAR Formülü: Dikey / Yatay
    fun calculateMAR(mouth: List<NormalizedLandmark>): Float {
        // Beklenen sıra: [TOP, BOTTOM, LEFT, RIGHT]
        // Python: 13, 14, 61, 291
        if (mouth.size != 4) return 0.0f

        val top = mouth[0]
        val bottom = mouth[1]
        val left = mouth[2]
        val right = mouth[3]

        val vertical = distance(top, bottom)
        val horizontal = distance(left, right)

        if (horizontal == 0f) return 0.0f

        return vertical / horizontal
    }
}
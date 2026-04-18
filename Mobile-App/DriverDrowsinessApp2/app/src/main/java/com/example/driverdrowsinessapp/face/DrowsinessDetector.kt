package com.example.driverdrowsinessapp.face

object DrowsinessDetector {

    private var closedFrameCount = 0

    fun isDrowsy(ear: Float): Boolean {
        if (ear < 0.23f) {
            closedFrameCount++
        } else {
            closedFrameCount = 0
        }

        return closedFrameCount >= 15
    }
}

package com.example.driverdrowsinessapp.face

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult

class FaceMeshHelper(val context: Context) {

    private var faceLandmarker: FaceLandmarker? = null

    init {
        setupFaceLandmarker()
    }

    private fun setupFaceLandmarker() {
        val baseOptionsBuilder = BaseOptions.builder()
            // Assets klasöründeki model isminin BU olduğundan emin ol!
            .setModelAssetPath("face_landmarker.task")

        val baseOptions = baseOptionsBuilder.build()

        val optionsBuilder = FaceLandmarker.FaceLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setMinFaceDetectionConfidence(0.5f) // Yüz bulma hassasiyeti
            .setMinFacePresenceConfidence(0.5f)
            .setNumFaces(1) // Sadece sürücü (1 yüz)
            .setRunningMode(RunningMode.IMAGE) // Resim modu (Video modu daha karmaşıktır, şimdilik Image kalsın)

        val options = optionsBuilder.build()

        try {
            faceLandmarker = FaceLandmarker.createFromOptions(context, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun detect(bitmap: Bitmap): FaceLandmarkerResult? {
        if (faceLandmarker == null) {
            setupFaceLandmarker()
        }

        // Bitmap'i MediaPipe formatına çevir
        val mpImage = BitmapImageBuilder(bitmap).build()

        // İşlemi yap ve sonucu döndür
        // Not: Video modunda timestamp gerekir ama IMAGE modunda gerekmez.
        return try {
            faceLandmarker?.detect(mpImage)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun close() {
        faceLandmarker?.close()
    }
}
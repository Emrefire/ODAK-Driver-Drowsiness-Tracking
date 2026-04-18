package com.example.driverdrowsinessapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

// Python kodundaki sınıf isimlerine karşılık gelen indeksler
// 0: closed_mouth, 1: open_mouth, 2: open_eyes, 3: closed_eyes
data class YoloResult(
    val classIndex: Int,
    val score: Float,
    val boundingBox: RectF
)

class YoloDetector(val context: Context) {

    private var interpreter: Interpreter? = null
    private val MODEL_NAME = "best_int8.tflite"
    private val INPUT_SIZE = 640
    // Python'daki conf=0.45 değerine yakın bir değer
    private val CONFIDENCE_THRESHOLD = 0.45f

    init {
        try {
            val modelFile = FileUtil.loadMappedFile(context, MODEL_NAME)
            val options = Interpreter.Options()
            interpreter = Interpreter(modelFile, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun detect(bitmap: Bitmap): List<YoloResult> {
        if (interpreter == null) return emptyList()

        // 1. Resmi Hazırla (640x640, Normalize 0-255 -> 0-1)
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(INPUT_SIZE, INPUT_SIZE, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0f, 255f)) // Pixel değerlerini 0-1 arasına çek
            .add(CastOp(DataType.FLOAT32))
            .build()

        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        // 2. Çıktı Buffer [1, 8, 8400]
        // 8 Satır: [cx, cy, w, h, score_class0, score_class1, score_class2, score_class3]
        val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 8, 8400), DataType.FLOAT32)

        // 3. Modeli Çalıştır
        interpreter?.run(tensorImage.buffer, outputBuffer.buffer.rewind())

        // 4. Sonuçları İşle
        val output = outputBuffer.floatArray
        val rows = 8
        val cols = 8400
        val results = ArrayList<YoloResult>()

        val scaleX = bitmap.width.toFloat() / INPUT_SIZE
        val scaleY = bitmap.height.toFloat() / INPUT_SIZE

        for (c in 0 until cols) {
            var maxScore = 0f
            var maxClass = -1

            // Skorlar 4. satırdan başlar (0,1,2,3 koordinattır)
            // class 0 (index 4), class 1 (index 5), class 2 (index 6), class 3 (index 7)
            for (r in 4 until rows) {
                val score = output[r * cols + c]
                if (score > maxScore) {
                    maxScore = score
                    maxClass = r - 4
                }
            }

            if (maxScore > CONFIDENCE_THRESHOLD) {
                val cx = output[0 * cols + c]
                val cy = output[1 * cols + c]
                val w = output[2 * cols + c]
                val h = output[3 * cols + c]

                val x1 = (cx - w / 2) * scaleX
                val y1 = (cy - h / 2) * scaleY
                val x2 = (cx + w / 2) * scaleX
                val y2 = (cy + h / 2) * scaleY

                results.add(
                    YoloResult(
                        classIndex = maxClass,
                        score = maxScore,
                        boundingBox = RectF(x1, y1, x2, y2)
                    )
                )
            }
        }

        // Basit NMS (Non-Maximum Suppression) benzeri filtreleme
        // Aynı sınıftan çok fazla kutu varsa en yüksek skorluyu alabiliriz.
        // Şimdilik ham listeyi dönüyoruz, logic MainActivity'de işlenecek.
        return results
    }

    fun close() {
        interpreter?.close()
    }
}
package com.example.driverdrowsinessapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class YuvToRgbConverter(private val context: Context) {

    fun yuvToBitmap(image: Image, rotationDegrees: Int): Bitmap {
        val nv21 = yuv420888ToNv21(image)

        val yuvImage = YuvImage(
            nv21,
            ImageFormat.NV21,
            image.width,
            image.height,
            null
        )

        val out = ByteArrayOutputStream()
        // Kaliteyi %100 yaparsak yavaşlar, %75-80 hem hızlıdır hem yeterlidir.
        yuvImage.compressToJpeg(
            Rect(0, 0, image.width, image.height),
            80,
            out
        )

        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun yuv420888ToNv21(image: Image): ByteArray {
        val width = image.width
        val height = image.height
        val ySize = width * height
        val uvSize = width * height / 2 // NV21 için UV boyutu

        val nv21 = ByteArray(ySize + uvSize)

        // --- Y Düzlemi (Luminance) ---
        val yPlane = image.planes[0]
        val yBuffer = yPlane.buffer
        val yRowStride = yPlane.rowStride
        val yPixelStride = yPlane.pixelStride

        // Eğer rowStride genişliğe eşitse direkt kopyala (Hızlı yol)
        if (yRowStride == width) {
            yBuffer.get(nv21, 0, ySize)
        } else {
            // Samsung gibi cihazlarda padding varsa satır satır kopyala
            var pos = 0
            for (row in 0 until height) {
                yBuffer.position(row * yRowStride)
                yBuffer.get(nv21, pos, width)
                pos += width
            }
        }

        // --- U ve V Düzlemleri (Chrominance) ---
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer

        val uRowStride = uPlane.rowStride
        val vRowStride = vPlane.rowStride
        val uPixelStride = uPlane.pixelStride
        val vPixelStride = vPlane.pixelStride

        val uBufferPos = uBuffer.position()
        val vBufferPos = vBuffer.position()

        // NV21 formatı: YYYY... ardından VUVU... (V önce gelir)
        var pos = ySize

        val uvHeight = height / 2
        val uvWidth = width / 2

        for (row in 0 until uvHeight) {
            for (col in 0 until uvWidth) {
                // V pixelini al
                val vIndex = (row * vRowStride) + (col * vPixelStride)
                // Buffer limit kontrolü (Crash önlemek için)
                if (vIndex < vBuffer.capacity()) {
                    nv21[pos++] = vBuffer.get(vIndex)
                }

                // U pixelini al
                val uIndex = (row * uRowStride) + (col * uPixelStride)
                if (uIndex < uBuffer.capacity()) {
                    nv21[pos++] = uBuffer.get(uIndex)
                }
            }
        }

        // Buffer pozisyonlarını resetle (CameraX tekrar kullanabilsin diye)
        yBuffer.position(0)
        uBuffer.position(uBufferPos)
        vBuffer.position(vBufferPos)

        return nv21
    }
}
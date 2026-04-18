package com.example.driverdrowsinessapp

import android.Manifest
import android.content.Context
import android.graphics.*
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.driverdrowsinessapp.face.*
import com.example.driverdrowsinessapp.ui.theme.*
import com.example.driverdrowsinessapp.data.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.Toast
// --- SABİTLER ---
const val EAR_THRESHOLD = 0.22f
const val MAR_THRESHOLD = 0.55f
const val ALERT_THRESHOLD_MS = 500L
const val EMA_ALPHA = 0.3f
const val HEAD_PITCH_DOWN_THRESHOLD = 15.0f

const val CLASS_CLOSED_MOUTH = 0
const val CLASS_OPEN_MOUTH = 1
const val CLASS_OPEN_EYES = 2
const val CLASS_CLOSED_EYES = 3

class MainActivity : ComponentActivity() {
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var faceMeshHelper: FaceMeshHelper
    private lateinit var yoloDetector: YoloDetector
    private lateinit var audioManager: AudioManager
    private lateinit var toneGen: ToneGenerator

    var currentUserId by mutableIntStateOf(-1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        faceMeshHelper = FaceMeshHelper(this)
        yoloDetector = YoloDetector(this)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 100)

        val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                setContent {
                    DriverDrowsinessAppTheme {
                        var currentScreen by remember { mutableStateOf("splash") }
                        var loggedInUserName by remember { mutableStateOf("") }
                        var loggedInUserEmail by remember { mutableStateOf("") }

                        when (currentScreen) {
                            "splash" -> SplashScreen(onTimeout = { currentScreen = "home" })
                            "home" -> HomeScreen(onLoginClick = { currentScreen = "login" }, onStartSystemClick = { currentScreen = "login" })
                            "login" -> LoginScreen(
                                onLoginSuccess = { name, email, userId ->
                                    loggedInUserName = name
                                    loggedInUserEmail = email
                                    currentUserId = userId
                                    currentScreen = "dashboard"
                                },
                                onRegisterClick = { currentScreen = "register" },
                                onBackClick = { currentScreen = "home" }
                            )
                            "register" -> RegisterScreen(onRegisterSuccess = { currentScreen = "login" }, onLoginClick = { currentScreen = "login" }, onBackClick = { currentScreen = "login" })
                            "dashboard" -> DashboardScreen(userName = loggedInUserName, onStartDrivingClick = { currentScreen = "camera" }, onStatsClick = { currentScreen = "stats" }, onProfileClick = { currentScreen = "profile" })
                            "stats" -> StatisticsScreen(userId = currentUserId, onBackClick = { currentScreen = "dashboard" })
                            "profile" -> ProfileScreen(userName = loggedInUserName, userEmail = loggedInUserEmail, onBackClick = { currentScreen = "dashboard" }, onLogoutClick = { currentScreen = "home" })
                            "camera" -> MainScreen(faceMeshHelper, yoloDetector, cameraExecutor, this, onBackClick = { currentScreen = "dashboard" })
                        }
                    }
                }
            }
        }
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    fun playLoudAlarm() {
        try {
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)
            toneGen.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 500)
        } catch (e: Exception) { e.printStackTrace() }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown(); faceMeshHelper.close(); yoloDetector.close(); toneGen.release()
    }

    fun toGrayscale(bmpOriginal: Bitmap): Bitmap {
        val width = bmpOriginal.width; val height = bmpOriginal.height
        val bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmpGrayscale); val paint = Paint()
        val cm = ColorMatrix().apply { setSaturation(0f) }
        paint.colorFilter = ColorMatrixColorFilter(cm)
        c.drawBitmap(bmpOriginal, 0f, 0f, paint)
        return bmpGrayscale
    }
}

@Composable
fun MainScreen(
    faceMeshHelper: FaceMeshHelper,
    yoloDetector: YoloDetector,
    cameraExecutor: ExecutorService,
    context: MainActivity,
    onBackClick: () -> Unit
) {
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_FRONT) }
    var overlayViewRef by remember { mutableStateOf<FaceOverlayView?>(null) }

    // --- SÜRÜŞ İSTATİSTİKLERİNİ TUTAN DEĞİŞKENLER ---
    var totalYawns by remember { mutableIntStateOf(0) }
    var totalSleepMs by remember { mutableLongStateOf(0L) }
    val sessionStartTime = remember { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date()) }

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            faceMeshHelper = faceMeshHelper,
            yoloDetector = yoloDetector,
            cameraExecutor = cameraExecutor,
            context = context,
            lensFacing = lensFacing,
            overlayView = overlayViewRef,
            onYawnDetected = { totalYawns++ }, // Esneme oldukça sayıyı artır
            onSleepTimeAdded = { ms -> totalSleepMs += ms } // Uyunan milisaniyeleri topla
        )

        AndroidView(modifier = Modifier.fillMaxSize(), factory = { ctx -> FaceOverlayView(ctx).also { overlayViewRef = it } })

        FloatingActionButton(
            onClick = { lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) CameraSelector.LENS_FACING_BACK else CameraSelector.LENS_FACING_FRONT },
            modifier = Modifier.align(Alignment.BottomEnd).padding(30.dp).size(70.dp),
            containerColor = Color.White
        ) {
            Icon(Icons.Filled.Cameraswitch, contentDescription = "Kamera", modifier = Modifier.size(35.dp), tint = Color.Black)
        }

        // --- ÇIKIŞ VE SÜRÜŞÜ KAYDETME BUTONU ---
        IconButton(
            onClick = {
                val sleepSeconds = (totalSleepMs / 1000).toInt()

                // YENİ MATEMATİK: Esneme -2 puan, Uyku saniyesi -5 puan
                val calculatedScore = maxOf(0, 100 - (totalYawns * 2) - (sleepSeconds * 5))

                // Yorgunluk: Esneme +5, Uyku saniyesi +10 (Maks 100 olabilir)
                val calculatedFatigue = minOf(100, (totalYawns * 5) + (sleepSeconds * 10))

                val session = DrivingSession(
                    userId = context.currentUserId,
                    startTime = sessionStartTime,
                    endTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date()),
                    averageFatigue = calculatedFatigue,
                    sleepDuration = sleepSeconds,
                    yawnCount = totalYawns,
                    score = calculatedScore
                )

                RetrofitClient.instance.saveSession(session).enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        if (response.isSuccessful) {
                            // Ekranda kaç esneme ve saniye uyku olduğunu da gösterelim ki test ederken anlayalım
                            Toast.makeText(context, "Kaydedildi! Esneme: $totalYawns, Uyku: ${sleepSeconds}s, Puan: $calculatedScore", Toast.LENGTH_LONG).show()
                        }
                    }
                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        Toast.makeText(context, "Kayıt Hatası: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                })
                onBackClick()
            },
            modifier = Modifier.align(Alignment.TopStart).padding(24.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Sürüşü Bitir", tint = Color.White)
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreview(
    faceMeshHelper: FaceMeshHelper,
    yoloDetector: YoloDetector,
    cameraExecutor: ExecutorService,
    context: MainActivity,
    lensFacing: Int,
    overlayView: FaceOverlayView?,
    onYawnDetected: () -> Unit,
    onSleepTimeAdded: (Long) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    key(lensFacing, overlayView) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    cameraProvider.unbindAll()
                    val preview = Preview.Builder().build().apply { setSurfaceProvider(previewView.surfaceProvider) }
                    val imageAnalysis = ImageAnalysis.Builder().setTargetResolution(Size(640, 640)).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
                    val converter = YuvToRgbConverter(ctx)

                    var earEMA: Float? = null
                    var eyeClosedStartTime: Long? = null
                    var headDownStartTime: Long? = null
                    var lastUpdateServerTime = 0L

                    // Veri toplayıcılar için sayaçlar
                    var wasYawning = false
                    var lastFrameTime = System.currentTimeMillis()

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val rotation = imageProxy.imageInfo.rotationDegrees
                            val bitmap = converter.yuvToBitmap(mediaImage, rotation)
                            val matrix = Matrix().apply { postRotate(rotation.toFloat()); if (lensFacing == CameraSelector.LENS_FACING_FRONT) postScale(-1f, 1f) }
                            val fullBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                            val currentTime = System.currentTimeMillis()

                            // Çerçeveler arası geçen süre (uyku süresi hesaplamak için)
                            val deltaTime = currentTime - lastFrameTime
                            lastFrameTime = currentTime

                            // --- YOLO ANALİZ ---
                            val yoloResults = yoloDetector.detect(context.toGrayscale(fullBitmap))
                            var yoloSeesClosedEyes = false
                            var yoloSeesOpenEyes = false
                            var yoloSeesOpenMouth = false
                            for (res in yoloResults) {
                                if (res.classIndex == CLASS_CLOSED_EYES) yoloSeesClosedEyes = true
                                if (res.classIndex == CLASS_OPEN_EYES) yoloSeesOpenEyes = true
                                if (res.classIndex == CLASS_OPEN_MOUTH) yoloSeesOpenMouth = true
                            }

                            // --- ESNEME SAYACI (Debounce Mantığı) ---
                            // Eğer şu an ağzı açıksa ve bir önceki karede kapalıysa yeni bir esneme say!
                            if (yoloSeesOpenMouth && !wasYawning) {
                                onYawnDetected()
                            }
                            wasYawning = yoloSeesOpenMouth

                            // --- FACE MESH ANALİZ ---
                            val mpResult = faceMeshHelper.detect(fullBitmap)
                            var currentEAR = 0f; var currentPitch = 0f
                            if (mpResult != null && mpResult.faceLandmarks().isNotEmpty()) {
                                val landmarks = mpResult.faceLandmarks()[0]
                                val leftEAR = EarUtils.calculateEAR(listOf(33, 160, 158, 133, 153, 144).map { landmarks[it] })
                                val rightEAR = EarUtils.calculateEAR(listOf(362, 385, 387, 263, 373, 380).map { landmarks[it] })
                                currentEAR = (leftEAR + rightEAR) / 2f
                                currentPitch = HeadPoseUtils.calculatePitch(landmarks)
                                earEMA = if (earEMA == null) currentEAR else (EMA_ALPHA * currentEAR) + ((1 - EMA_ALPHA) * earEMA!!)
                            }

                            val eyesClosed = yoloSeesClosedEyes || ((yoloSeesClosedEyes || yoloSeesOpenEyes) && earEMA != null && earEMA!! < EAR_THRESHOLD)

                            // --- UYKU SÜRESİ TOPLAYICI ---
                            if (eyesClosed) {
                                onSleepTimeAdded(deltaTime)
                            }

                            // --- ALARM MANTIĞI ---
                            var playAlarm = false
                            if (currentPitch > HEAD_PITCH_DOWN_THRESHOLD) {
                                if (headDownStartTime == null) headDownStartTime = currentTime
                                else if (currentTime - headDownStartTime!! > ALERT_THRESHOLD_MS) playAlarm = true
                            } else headDownStartTime = null

                            if (eyesClosed) {
                                if (eyeClosedStartTime == null) eyeClosedStartTime = currentTime
                                else if (currentTime - eyeClosedStartTime!! > ALERT_THRESHOLD_MS) playAlarm = true
                            } else eyeClosedStartTime = null

                            if (playAlarm) context.playLoudAlarm()

                            // --- CANLI VERİ (SIGNALR) GÖNDERİMİ ---
                            if (currentTime - lastUpdateServerTime > 1500) {
                                lastUpdateServerTime = currentTime
                                val metrics = DriverMetrics(
                                    userId = context.currentUserId,
                                    earValue = currentEAR,
                                    marValue = if (yoloSeesOpenMouth) 1.0f else 0.0f,
                                    isDrowsy = eyesClosed,
                                    isYawning = yoloSeesOpenMouth
                                )

                                RetrofitClient.instance.sendLiveUpdate(metrics).enqueue(object : Callback<Void> {
                                    override fun onResponse(call: Call<Void>, response: Response<Void>) {}
                                    override fun onFailure(call: Call<Void>, t: Throwable) {}
                                })
                            }

                            overlayView?.post {
                                if (mpResult != null && mpResult.faceLandmarks().isNotEmpty()) overlayView.updateLandmarks(mpResult.faceLandmarks()[0])
                                overlayView.updateDebugValues(earEMA ?: 0f, if (yoloSeesOpenMouth) 1f else 0f, currentPitch, if (eyesClosed) "UYKULU!" else "NORMAL")
                            }
                        }
                        imageProxy.close()
                    }
                    try { cameraProvider.bindToLifecycle(lifecycleOwner, if (lensFacing == CameraSelector.LENS_FACING_FRONT) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis) } catch (e: Exception) { Log.e("CAMERA", "Hata: ${e.message}") }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            }
        )
    }
}
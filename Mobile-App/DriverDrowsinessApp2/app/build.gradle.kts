plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.driverdrowsinessapp"
    compileSdk = 36   // ⚠️ 36 yerine 34 → MediaPipe daha stabil
    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
    defaultConfig {
        applicationId = "com.example.driverdrowsinessapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            // Kotlin'de .add kullanıyoruz veya += listOf(...)
            abiFilters.add("armeabi-v7a")
            abiFilters.add("arm64-v8a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    // 🔥🔥 BU OLMADAN MEDIAPIPE AÇILMAZ 🔥🔥
    packaging {
        pickFirst("lib/**/libmediapipe_tasks_vision_jni.so")
    }
}

dependencies {

    // ===== CameraX (STABLE) =====
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")
// Ağ işlemleri için Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // ===== MediaPipe Face Landmarker =====
    implementation("com.google.mediapipe:tasks-vision:0.10.14")

    // ===== TensorFlow Lite (ileride YOLO için) =====
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")

    // ===== Compose =====
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.camera.camera2.pipe)
    implementation(libs.androidx.compiler)
    implementation("androidx.compose.material:material-icons-extended:1.5.0")
}

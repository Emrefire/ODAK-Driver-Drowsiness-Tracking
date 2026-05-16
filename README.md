![Kotlin](https://img.shields.io/badge/Mobile-Kotlin_Native-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![ASP.NET Core](https://img.shields.io/badge/Backend-ASP.NET_Core_MVC-512BD4?style=for-the-badge&logo=dotnet&logoColor=white)
![Python](https://img.shields.io/badge/AI-Python-3776AB?style=for-the-badge&logo=python&logoColor=white)
![SignalR](https://img.shields.io/badge/RealTime-SignalR-orange?style=for-the-badge)

# AI-POWERED DRIVER DROWSINESS AND FATIGUE DETECTION SYSTEM (ODAK)

[![EN](https://img.shields.io/badge/Language-English-blue.svg)](#-english-version)

---

## 🇬🇧 English Version

This project is a real-time safety system developed to prevent driver fatigue and distraction, which are among the primary causes of traffic accidents. It was developed as part of the **Computer Engineering Graduation Project**.

By utilizing deep learning (YOLO) and computer vision (MediaPipe) techniques in a hybrid structure, the system detects the driver's face, analyzes eye/mouth movements, and triggers an audible warning in case of danger.

### 🎯 Purpose of the Project
The goal is to detect the driver's drowsiness behind the wheel within milliseconds and sound an audible alarm to prevent potential accidents. It aims to achieve high accuracy using only a standard camera, eliminating the need for expensive sensors.

### ⚙️ How It Works (Hybrid Algorithm)
The system combines **Deep Learning** and **Geometric Analysis** methods:

1. **Image Capture:** Live frames are captured from the camera stream.
2. **YOLO Detection:** The frame is first fed into the YOLO (You Only Look Once) model. The model detects the driver's face with high accuracy, even in complex scenes.
3. **Landmark Analysis (MediaPipe):** The MediaPipe Face Mesh algorithm is executed on the face region detected by YOLO, extracting 468 distinct facial landmarks.
4. **Mathematical Calculation (EAR & MAR):**
   * **EAR (Eye Aspect Ratio):** Calculates the eye openness ratio.
   * **MAR (Mouth Aspect Ratio):** Calculates the mouth openness ratio (yawning).
5. **Decision and Warning:** If the calculated ratios fall below or exceed the predefined thresholds, the system detects "Fatigue" or "Drowsiness" and triggers an audible alarm.

### 🛠️ Technologies Used
* **Python:** Core programming language.
* **YOLO (Ultralytics):** Object detection model used for face and head detection.
* **MediaPipe:** Real-time extraction of facial landmarks in milliseconds.
* **OpenCV:** Image processing, camera stream management, and drawing operations.
* **PyTorch:** Deep learning infrastructure required for the YOLO model.
* **NumPy:** Vectorial operations and EAR/MAR formula calculations.
* **Pygame / Playsound:** Triggering alarm sounds.

### 📊 Algorithm Logic and Mathematical Models
To minimize false alarms, the system detects fatigue symptoms through a weighted analysis of three different parameters.

#### 1. Eye Tracking (EAR - Eye Aspect Ratio)
6 different landmark points are used to analyze eye closure. The ratio of the vertical distance of the eye to its horizontal distance is calculated.



$$EAR = \frac{||p_2 - p_6|| + ||p_3 - p_5||}{2 \times ||p_1 - p_4||}$$

* **Logic:** While the eyes are open, this value remains stable; when closed, it rapidly approaches zero.
* **Threshold:** If the EAR value drops below the determined limit (e.g., 0.25), it is flagged as "Eye Closed".

#### 2. Yawning Detection (MAR - Mouth Aspect Ratio)
The mouth openness ratio is calculated to detect yawning, which is a key symptom of fatigue.



$$MAR = \frac{||p_{top} - p_{bottom}||}{||p_{left} - p_{right}||}$$

* **Logic:** When the mouth opens vertically (during a yawn), the vertical distance increases, and the MAR value rises.
* **Threshold:** If the MAR value exceeds a certain ratio (e.g., 0.7), the system counts this as "Yawning".

#### 3. Head Pose Estimation (Pitch)
A 3D perspective calculation is performed to detect if the driver's head drops forward (nodding off). The pitch angle (forward/backward tilt) of the head is analyzed using the surface normals provided by MediaPipe.

* **Logic:** When the driver dozes off and drops their head forward, the **Pitch** angle increases in the negative direction.
* **Threshold:** When the head angle falls below a specific degree, the system recognizes it as "Distraction/Sleeping".

### 🧠 Decision Tree
The system continuously monitors the following scenarios to trigger an alarm:

1. **SCENARIO 1 (Drowsiness):** If `EAR < Threshold` persists for `2` seconds (or a specific frame count) -> **ALARM SOUNDS.**
2. **SCENARIO 2 (Fatigue):** If `MAR > Threshold` (Yawning) is detected within a certain period for `2` seconds -> **"FATIGUE" WARNING IS ISSUED.**
3. **SCENARIO 3 (Head Drop):** If the `Pitch` angle indicates that the driver is not looking at the road (head has dropped) -> **ALARM SOUNDS.**

---
[![TR](https://img.shields.io/badge/Dil-T%C3%BCrk%C3%A7e-red.svg)](#-t%C3%BCrk%C3%A7e-versiyon)
 
# YAPAY ZEKA DESTEKLİ SÜRÜCÜ UYKU VE YORGUNLUK ALGILAMA SİSTEMİ (ODAK)

## 🇹🇷 Türkçe Versiyon

Bu proje, trafik kazalarının en büyük nedenlerinden biri olan sürücü yorgunluğunu ve dikkat dağınıklığını önlemek amacıyla geliştirilmiş gerçek zamanlı bir güvenlik sistemidir. **Bilgisayar Mühendisliği Bitirme Projesi** kapsamında geliştirilmiştir.

Derin öğrenme (YOLO) ve bilgisayarlı görme (MediaPipe) tekniklerini hibrit bir yapıda kullanarak, sürücünün yüzünü tespit eder, göz/ağız hareketlerini analiz eder ve tehlike anında sesli uyarı verir.

### 🎯 Projenin Amacı
Sürücülerin direksiyon başında uyuklama (drowsiness) durumunu milisaniyeler içinde tespit ederek, olası kazaları önlemek için sesli ikazda bulunmaktır. Pahalı sensörlere ihtiyaç duymadan, sadece standart bir kamera ile yüksek doğrulukla çalışması hedeflenmiştir.

### ⚙️ Nasıl Çalışır? (Hibrit Algoritma)
Sistem, **Derin Öğrenme** ve **Geometrik Analiz** yöntemlerini birleştirerek çalışır:

1. **Görüntü Yakalama:** Kamera akışından anlık kareler (frame) alınır.
2. **YOLO ile Tespiti:** Görüntü önce YOLO (You Only Look Once) modeline beslenir. Model, sahne karmaşık olsa bile sürücünün yüzünü yüksek doğrulukla tespit eder.
3. **Landmark Analizi (MediaPipe):** YOLO tarafından bulunan yüz bölgesinde, MediaPipe Face Mesh algoritması çalıştırılarak 468 farklı yüz noktası (landmark) çıkarılır.
4. **Matematiksel Hesaplama (EAR & MAR):**
   * **EAR (Eye Aspect Ratio):** Gözün açıklık oranını hesaplar.
   * **MAR (Mouth Aspect Ratio):** Ağzın açıklık oranını (esneme) hesaplar.
5. **Karar ve Uyarı:** Hesaplanan oranlar belirlenen eşik değerlerin (threshold) altına düşerse veya üstüne çıkarsa sistem "Yorgunluk" veya "Uyku" tespiti yapar ve sesli alarmı tetikler.

### 🛠️ Kullanılan Teknolojiler
* **Python:** Ana geliştirme dili.
* **YOLO (Ultralytics):** Yüz ve kafa tespiti için kullanılan Nesne Tespit Modeli.
* **MediaPipe:** Yüzdeki kilit noktaların (landmark) milisaniyeler içinde çıkarılması.
* **OpenCV:** Görüntü işleme, kamera akışı ve çizim işlemleri.
* **PyTorch:** YOLO modelinin çalışması için gerekli Derin Öğrenme altyapısı.
* **NumPy:** Vektörel işlemler ve EAR/MAR formül hesaplamaları.
* **Pygame / Playsound:** Alarm seslerinin tetiklenmesi.

### 📊 Algoritma Mantığı ve Matematiksel Modeller
Sistem, hatalı alarmları minimize etmek için yorgunluk belirtilerini üç farklı parametrenin ağırlıklı analiziyle tespit eder.

#### 1. Sürücü Göz Takibi (EAR - Eye Aspect Ratio)
Gözlerin kapanma durumunu analiz etmek için 6 farklı landmark noktası kullanılır. Gözün dikey uzunluğunun yatay uzunluğuna oranı hesaplanır.

$$EAR = \frac{||p_2 - p_6|| + ||p_3 - p_5||}{2 \times ||p_1 - p_4||}$$

* **Mantık:** Gözler açıkken bu değer sabit kalır, kapandığında hızla sıfıra yaklaşır.
* **Eşik Değeri:** Eğer EAR değeri belirlenen limitin (Örn: 0.25) altına düşerse "Göz Kapalı" olarak işaretlenir.

#### 2. Esneme Tespiti (MAR - Mouth Aspect Ratio)
Sürücünün yorgunluk belirtisi olan esnemeyi yakalamak için ağız açıklık oranı hesaplanır.

$$MAR = \frac{||p_{top} - p_{bottom}||}{||p_{left} - p_{right}||}$$

* **Mantık:** Ağız dikey olarak açıldığında (esneme anı) dikey mesafe artar ve MAR değeri yükselir.
* **Eşik Değeri:** MAR değeri belirli bir oranın (Örn: 0.7) üzerine çıkarsa sistem bunu "Esneme" olarak sayar.

#### 3. Baş Pozisyonu (Head Pose - Pitch)
Sürücünün başını öne düşürmesi (uyuklama hali) durumunu tespit etmek için 3 Boyutlu perspektif (3D Perspective) hesaplaması yapılır. MediaPipe'ın sağladığı yüzey normalleri kullanılarak başın öne/arkaya eğimi (Pitch) analiz edilir.

* **Mantık:** Sürücü uyuklayıp başını öne düşürdüğünde **Pitch** açısı negatif yönde artar.
* **Eşik Değeri:** Baş açısı belirli bir derecenin altına indiğinde sistem bunu "Dikkat Dağınıklığı/Uyuma" olarak algılar.

### 🧠 Karar Mekanizması (Decision Tree)
Sistem alarm vermek için şu senaryoları sürekli kontrol eder:

1. **DURUM 1 (Uyku):** `EAR < Eşik` durumu `2` saniye (frame) boyunca devam ederse -> **ALARM ÇALAR.**
2. **DURUM 2 (Yorgunluk):** Belirli bir süre içinde `2` saniye `MAR > Eşik` (Esneme) tespit edilirse -> **"YORGUNLUK" UYARISI VERİLİR.**
3. **DURUM 3 (Baş Düşmesi):** `Pitch` açısı sürücünün yola bakmadığını (başın düştüğünü) gösterirse -> **ALARM ÇALAR.**

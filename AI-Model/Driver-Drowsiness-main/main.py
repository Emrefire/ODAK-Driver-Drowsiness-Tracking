# main_mediapipe.py - MediaPipe ile yüz analizi (DÜZELTİLMİŞ VERSİYON)
from fastapi import FastAPI, File, UploadFile
from fastapi.middleware.cors import CORSMiddleware
import cv2
import numpy as np
import uvicorn
import logging
import mediapipe as mp
import math
import uuid

# Loglama ayarları
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="Driver Drowsiness API - MediaPipe")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# MediaPipe Face Mesh Kurulumu
mp_face_mesh = mp.solutions.face_mesh
mp_drawing = mp.solutions.drawing_utils
mp_drawing_styles = mp.solutions.drawing_styles

@app.get("/")
async def root():
    return {
        "message": "Driver Drowsiness API with MediaPipe",
        "status": "running",
        "technology": "MediaPipe Face Mesh (Auto-Rotation Enabled)"
    }

@app.get("/health")
async def health_check():
    return {"status": "healthy", "technology": "MediaPipe"}

def calculate_ear(eye_landmarks):
    """Eye Aspect Ratio hesapla - Göz açıklığı"""
    # Dikey mesafeler
    A = math.dist([eye_landmarks[1].x, eye_landmarks[1].y], 
                  [eye_landmarks[5].x, eye_landmarks[5].y])
    B = math.dist([eye_landmarks[2].x, eye_landmarks[2].y], 
                  [eye_landmarks[4].x, eye_landmarks[4].y])
    
    # Yatay mesafe
    C = math.dist([eye_landmarks[0].x, eye_landmarks[0].y], 
                  [eye_landmarks[3].x, eye_landmarks[3].y])
    
    # EAR formülü
    ear = (A + B) / (2.0 * C)
    return ear

def calculate_mar(mouth_landmarks):
    """Mouth Aspect Ratio hesapla - Ağız açıklığı"""
    # Dikey mesafe (üst dudak - alt dudak)
    vertical = math.dist([mouth_landmarks[2].x, mouth_landmarks[2].y],
                         [mouth_landmarks[6].x, mouth_landmarks[6].y])
    
    # Yatay mesafe (ağız genişliği)
    horizontal = math.dist([mouth_landmarks[0].x, mouth_landmarks[0].y],
                           [mouth_landmarks[4].x, mouth_landmarks[4].y])
    
    mar = vertical / horizontal
    return mar

@app.post("/predict")
async def predict(file: UploadFile = File(...)):
    """MediaPipe ile yüz analizi - Otomatik Döndürme Destekli"""
    
    try:
        # Resmi oku
        contents = await file.read()
        nparr = np.frombuffer(contents, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        
        if img is None:
            return {"success": False, "error": "Geçersiz resim formatı"}
        
        logger.info(f"📸 Resim alındı: {img.shape[1]}x{img.shape[0]}")
        
        # --- DÖNGÜ BAŞLANGICI: YÜZ ARAMA ---
        # Resmi 0, 90, 180, 270 derece döndürerek dene
        found_face = False
        face_landmarks = None
        img_rgb = None
        rotations = [None, cv2.ROTATE_90_CLOCKWISE, cv2.ROTATE_180, cv2.ROTATE_90_COUNTERCLOCKWISE]
        rotation_names = ["Orjinal", "90 Derece", "180 Derece", "270 Derece"]

        # MediaPipe örneğini başlat (Hassasiyet 0.3 yapıldı)
        with mp_face_mesh.FaceMesh(
            static_image_mode=True,
            max_num_faces=1,
            refine_landmarks=True,
            min_detection_confidence=0.3  # Düşük ışık/uzaklık için hassasiyeti düşürdük
        ) as face_mesh:

            current_img = img.copy()
            
            for i, rotation_code in enumerate(rotations):
                # Resmi döndür (İlk tur hariç)
                if rotation_code is not None:
                    current_img = cv2.rotate(img, rotation_code)
                
                # RGB'ye çevir
                img_rgb = cv2.cvtColor(current_img, cv2.COLOR_BGR2RGB)
                
                # İşle
                results = face_mesh.process(img_rgb)
                
                if results.multi_face_landmarks:
                    logger.info(f"✅ Yüz bulundu! ({rotation_names[i]})")
                    face_landmarks = results.multi_face_landmarks[0]
                    found_face = True
                    img = current_img # İşlenen doğru resmi ana resim yap
                    break # Döngüden çık
                else:
                    logger.warning(f"❌ Yüz yok ({rotation_names[i]})... Döndürülüp deneniyor.")

        # --- DÖNGÜ SONU ---

        if not found_face:
            logger.error("⛔ Hiçbir açıda yüz tespit edilemedi.")
            return {
                "success": False,
                "error": "Yüz tespit edilemedi (Tüm açılar denendi)",
                "eye_state": "unknown",
                "mouth_state": "unknown",
                "confidence": 0.0
            }
            
        # --- HESAPLAMALAR ---
            
        # Göz landmark indeksleri
        LEFT_EYE_INDICES = [33, 160, 158, 133, 153, 144]
        RIGHT_EYE_INDICES = [362, 385, 387, 263, 373, 380]
        
        left_eye = [face_landmarks.landmark[i] for i in LEFT_EYE_INDICES]
        right_eye = [face_landmarks.landmark[i] for i in RIGHT_EYE_INDICES]
        
        # Ağız landmark indeksleri
        MOUTH_INDICES = [13, 14, 78, 308, 191, 80, 81, 82]
        mouth = [face_landmarks.landmark[i] for i in MOUTH_INDICES]
        
        # EAR ve MAR Hesapla
        left_ear = calculate_ear(left_eye)
        right_ear = calculate_ear(right_eye)
        ear = (left_ear + right_ear) / 2.0 
        
        mar = calculate_mar(mouth)
        
        # Eşik Değerleri (Thresholds)
        EYE_CLOSED_THRESHOLD = 0.10  # 0.10'un altı kapalı
        MOUTH_OPEN_THRESHOLD = 0.50  # 0.50'nin üstü açık
        
        # Göz Durumu Analizi
        if ear < EYE_CLOSED_THRESHOLD:
            eye_state = "closed"
            eye_confidence = 1.0 - (ear / EYE_CLOSED_THRESHOLD)
        else:
            eye_state = "open"
            eye_confidence = ear
        
        # Ağız Durumu Analizi
        if mar > MOUTH_OPEN_THRESHOLD:
            mouth_state = "open"
            mouth_confidence = min(mar / MOUTH_OPEN_THRESHOLD, 1.0)
        else:
            mouth_state = "closed"
            mouth_confidence = 1.0 - (mar / MOUTH_OPEN_THRESHOLD)
        
        # Genel Güven Skoru
        confidence = (eye_confidence + mouth_confidence) / 2.0
        
        logger.info(f"SONUÇ: Göz={eye_state} (EAR={ear:.3f}), Ağız={mouth_state} (MAR={mar:.3f})")
        
        # Debug resmi kaydet (Çizim yaparak)
        debug_filename = f"mediapipe_{uuid.uuid4().hex[:8]}.jpg"
        
        annotated_image = img.copy()
        mp_drawing.draw_landmarks(
            image=annotated_image,
            landmark_list=face_landmarks,
            connections=mp_face_mesh.FACEMESH_TESSELATION,
            landmark_drawing_spec=None,
            connection_drawing_spec=mp_drawing_styles.get_default_face_mesh_tesselation_style())
            
        cv2.imwrite(debug_filename, annotated_image)
        logger.info(f"Debug resmi kaydedildi: {debug_filename}")
        
        return {
            "success": True,
            "eye_state": eye_state,
            "mouth_state": mouth_state,
            "confidence": float(confidence),
            "ear": float(ear),
            "mar": float(mar),
            "debug_image": debug_filename,
            "technology": "MediaPipe"
        }
    
    except Exception as e:
        logger.error(f" Hata: {str(e)}", exc_info=True)
        return {"success": False, "error": str(e)}

if __name__ == "__main__":
    print("=" * 70)
    print(" DRIVER DROWSINESS DETECTION - MEDIAPIPE (AUTO-ROTATE VERSION)")
    print("=" * 70)
    
    PORT = 8002
    print(f"\n API URL: http://localhost:{PORT}")
    print(f" Endpoint: POST /predict")
    print("\n Başlatılıyor... (Ctrl+C to stop)\n")
    
    uvicorn.run(app, host="0.0.0.0", port=PORT)
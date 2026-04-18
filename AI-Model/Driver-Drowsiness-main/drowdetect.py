import cv2
import time
from ultralytics import YOLO
from playsound import playsound
import threading
import mediapipe as mp
import numpy as np
import csv  # Raporlama ve veri toplama için eklendi
import os   # Dosya sistemi kontrolü (loglama) için eklendi

model_path = r"runs/train/driver_drowsiness/weights/best.pt"
model = YOLO(model_path)
yolo_class_names = model.names

mp_face_mesh = mp.solutions.face_mesh
face_mesh = mp_face_mesh.FaceMesh(
    max_num_faces=1,
    refine_landmarks=True,
    min_detection_confidence=0.5,
    min_tracking_confidence=0.5
)

# Kafa Pozu için 3D Model 
face_3d_model_points = np.array([
    [0.0, 0.0, 0.0],
    [0.0, -330.0, -65.0],
    [-225.0, 170.0, -135.0],
    [225.0, 170.0, -135.0],
    [-150.0, -150.0, -125.0],
    [150.0, -150.0, -125.0]
])
HEAD_POSE_INDICES = [1, 152, 263, 33, 291, 61]

cap = cv2.VideoCapture(0)

eye_closed_time = None
mouth_open_time = None
head_down_time = None
head_up_time = None

# ALERT_THRESHOLD = 2.0 
# NOT: 1.5s çok hassastı (örn. Kısa süreli baş eğmede alarm çalıyordu).
# 3.0s ise mikro-uykuları kaçırabiliyordu. 2.0s optimal bulundu.
ALERT_THRESHOLD = 2.0

# HEAD_PITCH_DOWN_THRESHOLD = 25.0
# NOT: 30.0 derece, sürücü normal şekilde yola bakarken bile tetiklendi. 25'e çekildi.
HEAD_PITCH_DOWN_THRESHOLD = 25.0
HEAD_PITCH_UP_THRESHOLD = -20.0 # Başın geriye düşmesi (daha nadir ama kritik)


# Alarm Mekanizması 
alarm_playing = False
alarm_lock = threading.Lock()

def play_alarm():
    global alarm_playing
    with alarm_lock:
        if alarm_playing:
            return
        alarm_playing = True
    try:
        playsound("alarm.wav")
    except Exception as e:
        print(f"Alarm çalınamadı: {e}")
    finally:
        with alarm_lock:
            time.sleep(1.0) 
            alarm_playing = False

#  EAR/MAR DEĞERLERİ ve İndeksler 
LEFT_EYE_IDX = [33, 160, 158, 133, 153, 144]
RIGHT_EYE_IDX = [362, 385, 387, 263, 373, 380]
MOUTH_TOP = 13
MOUTH_BOTTOM = 14
MOUTH_LEFT = 61
MOUTH_RIGHT = 291

EAR_THRESHOLD = 0.22
# NOT: EAR (Göz Açıklık Oranı) en kritik parametre.
# 0.25 -> Gözlüklü kullanıcılarda yanlış pozitif verdi.
# 0.20 -> Gözü gerçekten kapalıyken kaçırdı. 0.22 en stabil değer oldu.

MAR_THRESHOLD = 0.55 
# NOT: 0.50'de normal konuşmayı 'esneme' sanabiliyordu. 0.55'e yükseltildi.

#  Yumuşatma (EMA) 
ear_ema = None
mar_ema = None
EMA_ALPHA = 0.3
# NOT: EMA alpha = 0.4 denedim, anlık sıçramalara neden oldu.
# 0.2 ise tepkiyi çok yavaşlattı. 0.3 (yumuşatma) ideal.

# EAR/MAR Hesaplama Fonksiyonları 
def euclidean(a, b):
    return np.linalg.norm(np.array(a) - np.array(b))

def compute_ear(landmarks, frame_w, frame_h, indices):
    try:
        pts = [(landmarks[i].x * frame_w, landmarks[i].y * frame_h) for i in indices]
        A = euclidean(pts[1], pts[5])
        B = euclidean(pts[2], pts[4])
        C = euclidean(pts[0], pts[3])
        if C == 0:
            return 0.0
        ear = (A + B) / (2.0 * C)
        return ear
    except:
        return 0.0

def compute_mar(landmarks, frame_w, frame_h):
    try:
        top = (landmarks[MOUTH_TOP].x * frame_w, landmarks[MOUTH_TOP].y * frame_h)
        bottom = (landmarks[MOUTH_BOTTOM].x * frame_w, landmarks[MOUTH_BOTTOM].y * frame_h)
        left = (landmarks[MOUTH_LEFT].x * frame_w, landmarks[MOUTH_LEFT].y * frame_h)
        right = (landmarks[MOUTH_RIGHT].x * frame_w, landmarks[MOUTH_RIGHT].y * frame_h)
        vertical = euclidean(top, bottom)
        horizontal = euclidean(left, right)
        if horizontal == 0:
            return 0.0
        mar = vertical / horizontal
        return mar
    except:
        return 0.0

#  CSV Loglama Kurulumu 
LOG_FILE = "driver_drowsiness_log.csv"
log_header_yazildi = os.path.exists(LOG_FILE)

try:
    # Dosya yoksa başlıkları yaz (raporlama için)
    if not log_header_yazildi:
        with open(LOG_FILE, "w", newline="", encoding="utf-8") as f:
            writer = csv.writer(f)
            writer.writerow(["timestamp", "ear_ema", "mar_ema", "pitch", "yolo_classes", "status_text"])
    print(f"Log kaydı '{LOG_FILE}' dosyasına yapılıyor...")
except IOError as e:
    print(f"HATA: Log dosyası açılamadı! {e}")
#  CSV Loglama Bitiş 

while True:
    ret, frame = cap.read()
    if not ret:
        print("Kamera okunamadı.")
        break

    frame_height, frame_width, _ = frame.shape
    # YOLO için Gri tonlamalı görüntü (daha hızlı)
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    gray_rgb = cv2.cvtColor(gray, cv2.COLOR_GRAY2RGB)
    # MediaPipe için orijinal RGB görüntü
    rgb_frame_for_mp = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)

    # YOLO TESPİTİ
    yolo_results = model.predict(gray_rgb, conf=0.45, verbose=False)
    detected_classes = [yolo_class_names[int(cls)] for cls in yolo_results[0].boxes.cls]
    yolo_boxes = yolo_results[0].boxes

    # YOLO o an herhangi bir göz veya ağız (açık veya kapalı) görüyor mu?
    # Bu, MediaPipe'ın ne zaman çalışacağını belirlemek için bir kapı görevi görür.
    yolo_sees_eyes = "open_eyes" in detected_classes or "closed_eyes" in detected_classes
    yolo_sees_mouth = "open_mouth" in detected_classes or "closed_mouth" in detected_classes or "yawn" in detected_classes

    # MEDIAPIPE TESPİTİ
    mp_results = face_mesh.process(rgb_frame_for_mp)

    is_head_down = False
    is_head_up = False
    pitch = 0.0

    ear_left = ear_right = ear = 0.0
    mar = 0.0

    if mp_results.multi_face_landmarks:
        face_landmarks = mp_results.multi_face_landmarks[0]
        landmarks_list = face_landmarks.landmark

        # 1. Kafa Pozu (PITCH) Hesaplama
        try:
            face_2d_image_points = np.array(
                [(landmarks_list[i].x * frame_width, landmarks_list[i].y * frame_height) for i in HEAD_POSE_INDICES],
                dtype="double"
            )
            focal_length = frame_width
            center = (frame_width / 2, frame_height / 2)
            cam_matrix = np.array([[focal_length, 0, center[0]], [0, focal_length, center[1]], [0, 0, 1]], dtype="double")
            dist_coeffs = np.zeros((4, 1))

            (success, rvec, tvec) = cv2.solvePnP(face_3d_model_points, face_2d_image_points, cam_matrix, dist_coeffs, flags=cv2.SOLVEPNP_ITERATIVE)
            rmat, _ = cv2.Rodrigues(rvec)
            angles, _, _, _, _, _ = cv2.RQDecomp3x3(rmat)
            pitch = angles[0]

            # Sadece yüzü çerçevele (YOLO kutularıyla karışmasın diye opsiyonel)
            x_coords = [landmark.x * frame_width for landmark in landmarks_list]
            y_coords = [landmark.y * frame_height for landmark in landmarks_list]
            x_min, x_max = int(min(x_coords)), int(max(x_coords))
            y_min, y_max = int(min(y_coords)), int(max(y_coords))
            cv2.rectangle(frame, (x_min, y_min), (x_max, y_max), (255, 255, 255), 2)

            if pitch > HEAD_PITCH_DOWN_THRESHOLD:
                is_head_down = True
            elif pitch < HEAD_PITCH_UP_THRESHOLD:
                is_head_up = True
        except Exception:
            # FIXME: Hızlı kafa hareketlerinde veya kamera açısı çok değiştiğinde
            # solvePnP nadiren 'nan' dönebiliyor ve sistemi çökertiyor.
            # Geçici olarak try-except bloğu ile korumaya alındı.
            pass

        # 2. EAR Hesaplama
        ear_left = compute_ear(landmarks_list, frame_width, frame_height, LEFT_EYE_IDX)
        ear_right = compute_ear(landmarks_list, frame_width, frame_height, RIGHT_EYE_IDX)
        ear = (ear_left + ear_right) / 2.0

        # 3. MAR Hesaplama
        mar = compute_mar(landmarks_list, frame_width, frame_height)

        # Değerleri Yumuşatma (EMA)
        if ear_ema is None:
            ear_ema = ear
        else:
            ear_ema = EMA_ALPHA * ear + (1 - EMA_ALPHA) * ear_ema

        if mar_ema is None:
            mar_ema = mar
        else:
            mar_ema = EMA_ALPHA * mar + (1 - EMA_ALPHA) * mar_ema

        cv2.putText(frame, f"EAR: {ear_ema:.3f}", (30, 80), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (200,200,0), 2)
        cv2.putText(frame, f"MAR: {mar_ema:.3f}", (30, 110), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (200,200,0), 2)

    current_time = time.time()
    status_text = "NORMAL"
    status_color = (0, 255, 0)
    trigger_alarm = False

    # 1) Baş Düşüklüğü (MediaPipe) - En güvenilir uykululuk belirtilerinden biri
    if is_head_down:
        if head_down_time is None: head_down_time = current_time
        if current_time - head_down_time > ALERT_THRESHOLD:
            status_text = "UYKU HALI! (Bas Dusmesi)"
            status_color = (0, 0, 255)
            trigger_alarm = True
        else:
            status_text = "DIKKAT! (Bas Egik)"
            status_color = (0, 165, 255)
    else:
        head_down_time = None

    if is_head_up:
        if head_up_time is None: head_up_time = current_time
        if current_time - head_up_time > ALERT_THRESHOLD:
            status_text = "UYKU HALI! (Bas Geriye Dustu)"
            status_color = (0, 0, 255)
            trigger_alarm = True
        elif not trigger_alarm and status_text == "NORMAL":
            status_text = "DIKKAT! (Bas Geriye Yatik)"
            status_color = (0, 165, 255)
    else:
        head_up_time = None

    # 2) Ağız (HİBRİT: YOLO VEYA MAR)
    mouth_detected_by_yolo = "open_mouth" in detected_classes or "yawn" in detected_classes
    
    # HİBRİT KONTROL (Ağız):
    # Sadece MAR'a güvenirsek, sürücü elini ağzına götürdüğünde (MediaPipe şaşırır) yanlış alarm verir.
    # Sadece YOLO'ya güvenirsek, küçük esnemeleri (uzaktan) kaçırabilir.
    # ÇÖZÜM: Önce YOLO bir 'esneme' bölgesi tespit ederse (yolo_sees_mouth), 
    # ardından o bölgedeki MAR değerini teyit et. Bu, yanlış alarmları ciddi oranda azalttı.
    mouth_open_by_mar = yolo_sees_mouth and mar_ema is not None and mar_ema > MAR_THRESHOLD

    if mouth_detected_by_yolo or mouth_open_by_mar:
        if mouth_open_time is None: mouth_open_time = current_time
        if current_time - mouth_open_time > ALERT_THRESHOLD:
            status_text = "UYKU HALI! (Esneme)"
            status_color = (0, 0, 255)
            trigger_alarm = True
        elif not trigger_alarm and status_text == "NORMAL":
            status_text = "DIKKAT! (Agiz Acildi)"
            status_color = (0, 165, 255)
    else:
        mouth_open_time = None

    # 3) Göz (HİBRİT: YOLO VEYA EAR) - en yüksek öncelik
    eyes_detected_by_yolo = "closed_eyes" in detected_classes
    
    # HİBRİT KONTROL (Göz):
    # Göz tespiti için de aynı mantık geçerli. YOLO genel bölgeyi bulur (yolo_sees_eyes),
    # MediaPipe'ın EAR değeri bu tespiti doğrular (teyit alır).
    # Bu, özellikle gözlük parlamasında YOLO'nun kararsızlığını dengeler.
    eyes_closed_by_ear = yolo_sees_eyes and ear_ema is not None and ear_ema < EAR_THRESHOLD

    if eyes_detected_by_yolo or eyes_closed_by_ear:
        if eye_closed_time is None: eye_closed_time = current_time
        if current_time - eye_closed_time > ALERT_THRESHOLD:
            status_text = "UYKU HALI! (Goz Kapali)"
            status_color = (0, 0, 255)
            trigger_alarm = True
        elif not trigger_alarm:
            # Göz kapanması diğer tüm durumlardan daha öncelikli bir 'DİKKAT'tir.
            status_text = "DIKKAT! (Goz Kapandi)"
            status_color = (0, 165, 255)
    else:
        eye_closed_time = None

    #  Rapor için Veri Kaydı (CSV) 
    try:
        with open(LOG_FILE, "a", newline="", encoding="utf-8") as f:
            writer = csv.writer(f)
            # Sadece anlamlı veriler varken logla (YOLO/MP yüz bulduğunda)
            if ear_ema is not None or len(detected_classes) > 0:
                writer.writerow([current_time, f"{ear_ema:.3f}", f"{mar_ema:.3f}", f"{pitch:.2f}", 
                                   "-".join(detected_classes) if detected_classes else "None", 
                                   status_text])
    except Exception as e:
        # Programın çökmemesi için loglama hatasını es geç
        pass 
    #  Veri Kaydı Sonu 

    # Alarmı tetikle
    if trigger_alarm:
        threading.Thread(target=play_alarm, daemon=True).start()

    # YOLO Box Çizimleri
    for box in yolo_boxes:
        x1, y1, x2, y2 = map(int, box.xyxy[0])
        cls = yolo_class_names[int(box.cls)]
        conf = float(box.conf)

        if cls == "closed_eyes" or cls == "open_mouth" or cls == "yawn":
            box_color = (0, 0, 255) # Tehlike (Kırmızı)
        else:
            box_color = (0, 255, 0) # Normal (Yeşil)

        cv2.rectangle(frame, (x1, y1), (x2, y2), box_color, 2)
        cv2.putText(frame, f"{cls} {conf:.2f}", (x1, y1 - 10),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.7, box_color, 2)

    # Baş Eğimi Bilgisi
 
    cv2.putText(frame, f"Pitch: {pitch:.2f}", (30, 140), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (200,200,0), 2)


    # Ana Durum Bilgisi
    cv2.putText(frame, f"Durum: {status_text}", (30, 50),
                cv2.FONT_HERSHEY_SIMPLEX, 1.1, status_color, 3)

    cv2.imshow("Yapay Zeka Destekli Akilli Surucu Asistani", frame)

    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

print("Sistem kapatılıyor...")
cap.release()
cv2.destroyAllWindows()
face_mesh.close()
print("Log dosyası kaydedildi: ", LOG_FILE)
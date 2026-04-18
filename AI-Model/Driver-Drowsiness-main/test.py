# check_model.py
from ultralytics import YOLO
import cv2
import numpy as np

# Modeli yükle
model = YOLO(r"C:\Users\adame\Downloads\Driver-Drowsiness-main (1)\Driver-Drowsiness-main\runs\train\driver_drowsiness\weights\best.pt")

print("=" * 60)
print("🔍 MODEL ANALİZİ")
print("=" * 60)

# Model bilgileri
print(f"📊 Model classes: {model.names}")
print(f"🔢 Number of classes: {model.nc}")
print(f"📏 Input size: {model.args.get('imgsz', 640)}")

# Test için siyah bir resim oluştur
test_img = np.zeros((640, 640, 3), dtype=np.uint8)

# Modeli test et
print("\n🧪 MODEL TESTİ - Boş resim:")
results = model(test_img, verbose=False)

for i, r in enumerate(results):
    if r.boxes is not None and len(r.boxes) > 0:
        print(f"  Result {i}: {len(r.boxes)} detection")
        for box in r.boxes:
            cls = int(box.cls[0])
            conf = float(box.conf[0])
            print(f"    Class: {cls} ({model.names.get(cls, 'unknown')}), Confidence: {conf:.3f}")
    else:
        print(f"  Result {i}: No detection")

# Webcam ile test
print("\n🎥 WEBCAM TESTİ (5 saniye):")
print("Gözlerinizi kapatın, ağzınızı açın...")

cap = cv2.VideoCapture(0)
start_time = cv2.getTickCount()
duration = 5  # 5 saniye

while (cv2.getTickCount() - start_time) / cv2.getTickFrequency() < duration:
    ret, frame = cap.read()
    if not ret:
        break
    
    # Model ile tahmin
    results = model(frame, conf=0.3, verbose=False)
    
    for r in results:
        if r.boxes is not None:
            for box in r.boxes:
                cls = int(box.cls[0])
                conf = float(box.conf[0])
                class_name = model.names.get(cls, f"class_{cls}")
                print(f"  👁️  {class_name}: {conf:.3f}")
    
    cv2.imshow('Model Test - Press ESC to skip', frame)
    if cv2.waitKey(1) & 0xFF == 27:  # ESC
        break

cap.release()
cv2.destroyAllWindows()

print("\n" + "=" * 60)
print("📋 SONUÇ:")
print("=" * 60)
print("Model sadece 'open_eyes' tespit ediyorsa:")
print("1. Model yanlış eğitilmiş olabilir")
print("2. Dataset'te 'closed_eyes' ve 'open_mouth' yok")
print("3. Eğitim sırasında overfit olmuş")
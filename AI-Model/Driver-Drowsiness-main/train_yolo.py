from ultralytics import YOLO

if __name__ == "__main__":
    model = YOLO('yolov8n.pt')

    model.train(
        data=r'C:\Users\mustafaaslan\Desktop\labelimg\Drowsiness Datasets\data.yaml',
        epochs=100,       
        imgsz=640,       
        batch=16,        
        project='runs/train',
        name='driver_drowsiness',
        exist_ok=True,
        device=0,
        workers=0
    )

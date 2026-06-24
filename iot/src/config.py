# coding: utf-8
"""
Raspberry Pi 監視システムの設定値を一元管理するモジュール。
"""

# GPIO ピン設定
# 参照: IES7 Tips "1. Use of a Motion Sensor"
PIR_PIN = 4

# 参照: IES4 "PWM", IES3 "Control of Sensors and Actuators Using GPIOs"
BUZZER_PIN = 18

# USB カメラ設定
# 参照: IES5 "Capturing of Images Using a Camera"
CAMERA_DEVICE = 0
CAPTURE_RESOLUTION = (640, 480)

# YOLO モデル設定
# 参照: IES6 "Deployment of an Object Detection Model"
# Ultralytics公式のCOCO学習済みYOLOv8 Nano物体検出モデルを使用する。
# COCOには本システムで使用する person / car を含む80クラスがある。
# モデル仕様: https://docs.ultralytics.com/models/yolov8/
# 配布元: https://github.com/ultralytics/assets/releases/download/v8.4.0/yolov8n.pt
# ライセンス: https://www.ultralytics.com/license
YOLO_MODEL_PATH = "models/yolov8n.pt"
YOLO_CLASSES = ["person", "car"]
CONFIDENCE_THRESHOLD = 0.5

# 判定閾値
PERSON_DETECTION_THRESHOLD_SECONDS = 10.0
SUSPICIOUS_VEHICLE_THRESHOLD_SECONDS = 300.0
CAR_LOCATION_TOLERANCE = 50

# 定期撮影間隔（秒）
PERIODIC_CAPTURE_INTERVAL_SECONDS = 30

# Server 送信設定
# HTTP クライアントには requests を使用（講義資料外）
# 参照: https://requests.readthedocs.io/en/latest/user/quickstart/#post-a-multipart-encoded-file
SERVER_BASE_URL = "http://archlinux.tail1dcb8b.ts.net:8080"
SERVER_ENDPOINT = "/api/detections"

# 画像保存先
IMAGE_SAVE_DIR = "data"

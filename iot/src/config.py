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
PERSON_DETECTION_THRESHOLD_SECONDS = 20.0
PERSON_LOST_GRACE_SECONDS = 4.0
PIR_OBSERVATION_TIMEOUT_SECONDS = 5.0
PERSON_CAPTURE_INTERVAL_SECONDS = 2.0
PERSON_LOCATION_TOLERANCE = 150
SUSPICIOUS_VEHICLE_THRESHOLD_SECONDS = 300.0
CAR_LOCATION_TOLERANCE = 20
CAR_MISSING_CAPTURE_LIMIT = 2

# 監視間隔（秒）
MONITOR_LOOP_INTERVAL_SECONDS = 1.0
PERIODIC_CAPTURE_INTERVAL_SECONDS = 30.0

# 監視プログラムの稼働状態を表示する間隔（秒）
MONITOR_STATUS_INTERVAL_SECONDS = 5.0

# Server 送信設定
# HTTP クライアントには requests を使用（講義資料外）
# 参照: https://requests.readthedocs.io/en/latest/user/quickstart/#post-a-multipart-encoded-file
SERVER_BASE_URL = "http://archlinux.tail1dcb8b.ts.net:8080"
SERVER_ENDPOINT = "/api/detections"
IOT_HEARTBEAT_ENDPOINT = "/api/iot/heartbeat"
IOT_CONTROL_SYNC_INTERVAL_SECONDS = 3.0
SERVER_CONNECT_TIMEOUT_SECONDS = 5.0
SERVER_READ_TIMEOUT_SECONDS = 15.0

# 画像保存先
IMAGE_SAVE_DIR = "data"


def validate_config():
    """監視処理が成立しない設定値の組み合わせを起動前に検出する。"""
    errors: list[str] = []
    if MONITOR_LOOP_INTERVAL_SECONDS <= 0:
        errors.append("MONITOR_LOOP_INTERVAL_SECONDS must be greater than 0")
    if PERSON_CAPTURE_INTERVAL_SECONDS < MONITOR_LOOP_INTERVAL_SECONDS:
        errors.append(
            "PERSON_CAPTURE_INTERVAL_SECONDS must be greater than or equal to "
            "MONITOR_LOOP_INTERVAL_SECONDS"
        )
    if PERSON_LOST_GRACE_SECONDS < PERSON_CAPTURE_INTERVAL_SECONDS * 2:
        errors.append(
            "PERSON_LOST_GRACE_SECONDS must be at least twice "
            "PERSON_CAPTURE_INTERVAL_SECONDS"
        )
    if PERSON_DETECTION_THRESHOLD_SECONDS <= 0:
        errors.append("PERSON_DETECTION_THRESHOLD_SECONDS must be greater than 0")
    if PIR_OBSERVATION_TIMEOUT_SECONDS <= 0:
        errors.append("PIR_OBSERVATION_TIMEOUT_SECONDS must be greater than 0")
    if PERSON_LOCATION_TOLERANCE <= 0:
        errors.append("PERSON_LOCATION_TOLERANCE must be greater than 0")
    if PERIODIC_CAPTURE_INTERVAL_SECONDS <= 0:
        errors.append("PERIODIC_CAPTURE_INTERVAL_SECONDS must be greater than 0")
    if SUSPICIOUS_VEHICLE_THRESHOLD_SECONDS <= PERIODIC_CAPTURE_INTERVAL_SECONDS:
        errors.append(
            "SUSPICIOUS_VEHICLE_THRESHOLD_SECONDS must be greater than "
            "PERIODIC_CAPTURE_INTERVAL_SECONDS"
        )
    if CAR_LOCATION_TOLERANCE <= 0:
        errors.append("CAR_LOCATION_TOLERANCE must be greater than 0")
    if CAR_MISSING_CAPTURE_LIMIT < 1:
        errors.append("CAR_MISSING_CAPTURE_LIMIT must be at least 1")
    if not 0.0 <= CONFIDENCE_THRESHOLD <= 1.0:
        errors.append("CONFIDENCE_THRESHOLD must be between 0.0 and 1.0")
    if IOT_CONTROL_SYNC_INTERVAL_SECONDS <= 0:
        errors.append("IOT_CONTROL_SYNC_INTERVAL_SECONDS must be greater than 0")

    if errors:
        raise ValueError("Invalid IoT configuration:\n- " + "\n- ".join(errors))

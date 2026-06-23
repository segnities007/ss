# coding: utf-8
"""
統合フローテスト。

PIR → カメラ → YOLO → 判定 → ブザー → server 送信の一連の流れを確認する。
各ステップの成否をログに出力する。

参照:
- docs/plan.md Operation flow
- docs/design/iot_design.md 5.2 モジュールテスト
"""

import time

from src.buzzer import PiezoBuzzer
from src.camera import Camera
from src.car_tracker import CarTracker
from src.config import PERSON_DETECTION_THRESHOLD_SECONDS
from src.pir_sensor import PIRSensor
from src.person_tracker import PersonTracker
from src.server_client import ServerClient
from src.yolo_detector import YOLODetector


def main():
    print("=== Full flow test started ===")
    pir = PIRSensor()
    camera = Camera()
    buzzer = PiezoBuzzer()
    detector = YOLODetector()
    person_tracker = PersonTracker(threshold_seconds=0.0)  # テスト用に即判定
    car_tracker = CarTracker(threshold_seconds=0.0)
    client = ServerClient()

    try:
        # 1. PIR 確認
        print("[1/5] Checking PIR sensor...")
        motion = pir.is_motion_detected()
        print(f"  Motion detected: {motion}")

        # 2. カメラ撮影
        print("[2/5] Capturing image...")
        frame = camera.capture()
        image_path = camera.save_frame(frame, "test_full_flow.jpg")
        print(f"  Image saved to: {image_path}")

        # 3. YOLO 推論
        print("[3/5] Running YOLO...")
        detections = detector.detect(frame)
        for det in detections:
            print(f"  {det.label}: {det.confidence:.2f}")

        # 4. 判定・ブザー
        print("[4/5] Checking alerts...")
        persons = [d for d in detections if d.label == "person"]
        cars = [d for d in detections if d.label == "car"]

        if persons and person_tracker.update(True):
            print("  Long-staying person detected -> buzzer")
            buzzer.beep(count=1)

        suspicious_cars = car_tracker.update(cars)
        if suspicious_cars:
            print("  Suspicious vehicle detected -> buzzer")
            buzzer.beep(count=1)

        # 5. Server 送信
        print("[5/5] Sending to server...")
        target = persons[0] if persons else (suspicious_cars[0] if suspicious_cars else None)
        if target is not None:
            result = client.send_detection(
                detection_type="person" if target.label == "person" else "suspicious_vehicle",
                confidence=target.confidence,
                image_path=image_path,
                metadata={
                    "boundingBox": {
                        "x": target.x,
                        "y": target.y,
                        "width": target.width,
                        "height": target.height,
                    }
                },
            )
            print(f"  Server response: {result}")
        else:
            print("  No target detected, skipping server send.")

        print("=== Full flow test completed ===")

    except Exception as e:
        print(f"Error: {e}")
    finally:
        camera.release()
        buzzer.close()
        pir.close()


if __name__ == "__main__":
    main()

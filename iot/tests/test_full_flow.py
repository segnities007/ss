# coding: utf-8
"""
統合フローテスト。

PIR → カメラ → YOLO → 判定 → ブザー → server 送信の一連の流れを確認する。
各ステップの成否をログに出力する。

参照:
- IES7 Tips "Use of a Motion Sensor"
- IES5 "Capturing of Images Using a Camera"
- IES6 "Deployment of an Object Detection Model"
"""

from src.buzzer import PiezoBuzzer
from src.camera import Camera
from src.car_tracker import CarTracker
from src.monitor import build_metadata
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

        person_tracker.update_pir(True, now=0.0)
        person_result = person_tracker.update_detection(persons, now=0.0)
        if person_result.alerts:
            print("  Long-staying person detected -> buzzer")
            buzzer.beep(count=1)

        suspicious_cars = car_tracker.update(cars, now=0.0)
        if suspicious_cars:
            print("  Suspicious vehicle detected (no buzzer)")

        # 5. Server 送信
        print("[5/5] Sending to server...")
        sent_count = 0

        for alert in person_result.alerts:
            target = alert.detection
            result = client.send_detection(
                detection_type="person",
                confidence=target.confidence,
                image_path=image_path,
                metadata=build_metadata(
                    target,
                    alert.duration_seconds,
                    alert.track_id,
                ),
            )
            sent_count += 1
            print(f"  Person response: {result}")

        for alert in suspicious_cars:
            target = alert.detection
            result = client.send_detection(
                detection_type="suspicious_vehicle",
                confidence=target.confidence,
                image_path=image_path,
                metadata=build_metadata(
                    target,
                    alert.duration_seconds,
                    alert.track_id,
                ),
            )
            sent_count += 1
            print(f"  Vehicle response: {result}")

        if sent_count == 0:
            print("  No target detected, skipping server send.")

        print("=== Full flow test completed ===")

    except Exception as e:
        print(f"Error: {e}")
    finally:
        buzzer.off()
        camera.release()
        buzzer.close()
        pir.close()


if __name__ == "__main__":
    main()

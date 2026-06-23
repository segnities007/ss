# coding: utf-8
"""
全体監視ループを統合するモジュール。

PIR センサー、USB カメラ、YOLO、ブザーを連携させ、
不審者・不審車両を検知した場合に server へ通知する。

参照:
- IES7 Tips "Use of a Motion Sensor"
- IES7 Tips "Multi-Threads" (複数処理を組み合わせる考え方)
- Python datetime.now: https://docs.python.org/3/library/datetime.html#datetime.datetime.now
"""

import time
from datetime import datetime, timezone
from typing import List

from src.buzzer import PiezoBuzzer
from src.camera import Camera
from src.car_tracker import CarTracker
from src.config import PERIODIC_CAPTURE_INTERVAL_SECONDS
from src.pir_sensor import PIRSensor
from src.person_tracker import PersonTracker
from src.server_client import ServerClient
from src.yolo_detector import DetectionResult, YOLODetector


def build_metadata(result: DetectionResult, duration_seconds: float | None = None) -> dict:
    """検知結果から server 送信用メタデータを作成する。"""
    metadata = {
        "boundingBox": {
            "x": result.x,
            "y": result.y,
            "width": result.width,
            "height": result.height,
        }
    }
    if duration_seconds is not None:
        metadata["durationSeconds"] = int(duration_seconds)
    return metadata


def monitor():
    """メインの監視ループ。"""
    pir = PIRSensor()
    camera = Camera()
    buzzer = PiezoBuzzer()
    detector = YOLODetector()
    person_tracker = PersonTracker()
    car_tracker = CarTracker()
    client = ServerClient()

    last_periodic_capture = 0.0

    print(f"[{_now()}] Monitoring started")

    try:
        while True:
            now = time.time()
            is_motion = pir.is_motion_detected()
            is_periodic = (now - last_periodic_capture) >= PERIODIC_CAPTURE_INTERVAL_SECONDS

            if is_motion:
                _handle_motion(
                    camera=camera,
                    detector=detector,
                    person_tracker=person_tracker,
                    buzzer=buzzer,
                    client=client,
                )

            if is_periodic:
                last_periodic_capture = now
                _handle_periodic_capture(
                    camera=camera,
                    detector=detector,
                    car_tracker=car_tracker,
                    client=client,
                )

            time.sleep(0.5)

    except KeyboardInterrupt:
        print(f"[{_now()}] Monitoring stopped by user")
    finally:
        camera.release()
        buzzer.close()
        pir.close()


def _handle_motion(
    camera: Camera,
    detector: YOLODetector,
    person_tracker: PersonTracker,
    buzzer: PiezoBuzzer,
    client: ServerClient,
):
    """PIR 動作検知時の処理。"""
    try:
        frame = camera.capture()
        detections = detector.detect(frame)
        persons = [d for d in detections if d.label == "person"]

        if persons:
            is_long_person = person_tracker.update(True)
            if is_long_person:
                print(f"[{_now()}] Long-staying person detected")
                buzzer.beep()
                image_path = camera.save_frame(frame, f"person_{_timestamp()}.jpg")
                client.send_detection(
                    detection_type="person",
                    confidence=persons[0].confidence,
                    image_path=image_path,
                    metadata=build_metadata(persons[0]),
                )
        else:
            person_tracker.update(False)

    except Exception as e:
        print(f"[{_now()}] Error in motion handling: {e}")


def _handle_periodic_capture(
    camera: Camera,
    detector: YOLODetector,
    car_tracker: CarTracker,
    client: ServerClient,
):
    """定期撮影時の処理。"""
    try:
        frame = camera.capture()
        detections = detector.detect(frame)
        cars = [d for d in detections if d.label == "car"]
        suspicious_cars = car_tracker.update(cars)

        for car in suspicious_cars:
            print(f"[{_now()}] Suspicious vehicle detected")
            image_path = camera.save_frame(frame, f"vehicle_{_timestamp()}.jpg")
            client.send_detection(
                detection_type="suspicious_vehicle",
                confidence=car.confidence,
                image_path=image_path,
                metadata=build_metadata(car),
            )

    except Exception as e:
        print(f"[{_now()}] Error in periodic capture: {e}")


def _now() -> str:
    return datetime.now(timezone.utc).isoformat()


def _timestamp() -> str:
    return datetime.now(timezone.utc).strftime("%Y%m%d_%H%M%S_%f")


if __name__ == "__main__":
    monitor()

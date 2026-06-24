# coding: utf-8
"""
PIR、USBカメラ、YOLO、ブザー、Server送信を統合する監視ループ。

人物監視と車両監視が同時刻の場合は、1回の撮影・YOLO推論結果を共有する。
人物イベントではブザーとServer送信を1回だけ行い、車両ではブザーを鳴らさない。

参照:
- IES7 Tips "Use of a Motion Sensor"
- Python datetime.now: https://docs.python.org/3/library/datetime.html#datetime.datetime.now
- Python time.monotonic: https://docs.python.org/3/library/time.html#time.monotonic
"""

import time
from datetime import datetime, timezone

from src.buzzer import PiezoBuzzer
from src.camera import Camera
from src.car_tracker import CarTracker, VehicleAlert
from src.config import (
    MONITOR_LOOP_INTERVAL_SECONDS,
    MONITOR_STATUS_INTERVAL_SECONDS,
    PERIODIC_CAPTURE_INTERVAL_SECONDS,
    validate_config,
)
from src.detection import DetectionResult
from src.person_tracker import PersonTracker, PersonTrackingResult
from src.pir_sensor import PIRSensor
from src.server_client import ServerClient
from src.yolo_detector import YOLODetector


def build_metadata(
    result: DetectionResult,
    duration_seconds: float | None = None,
) -> dict:
    """検知結果からServer送信用メタデータを作成する。"""
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
    """監視を開始し、Ctrl+Cまで人物・車両イベントを処理する。"""
    validate_config()
    pir: PIRSensor | None = None
    camera: Camera | None = None
    buzzer: PiezoBuzzer | None = None

    try:
        pir = PIRSensor()
        camera = Camera()
        buzzer = PiezoBuzzer()
        detector = YOLODetector()
        person_tracker = PersonTracker()
        car_tracker = CarTracker()
        client = ServerClient()

        started_at = time.monotonic()
        last_periodic_capture = started_at - PERIODIC_CAPTURE_INTERVAL_SECONDS
        last_status_log = started_at
        previous_motion: bool | None = None

        print(f"[{_now()}] Monitoring started | server: {client.url}", flush=True)
        if client.health_check():
            print(f"[{_now()}] Server health check: OK", flush=True)
        else:
            print(
                f"[{_now()}] Server health check: FAILED "
                "| local monitoring will continue",
                flush=True,
            )

        while True:
            now = time.monotonic()
            try:
                is_motion = pir.is_motion_detected()
            except Exception as error:
                print(f"[{_now()}] PIR read error: {error}", flush=True)
                time.sleep(MONITOR_LOOP_INTERVAL_SECONDS)
                continue

            if is_motion != previous_motion:
                state = "detected" if is_motion else "cleared"
                print(f"[{_now()}] PIR motion {state}", flush=True)
                previous_motion = is_motion

            if person_tracker.update_pir(is_motion, now):
                print(f"[{_now()}] Person observation started", flush=True)

            person_capture_due = person_tracker.capture_due(now)
            vehicle_capture_due = (
                now - last_periodic_capture >= PERIODIC_CAPTURE_INTERVAL_SECONDS
            )

            if person_capture_due:
                person_tracker.mark_capture_attempt(now)
            if vehicle_capture_due:
                last_periodic_capture = now
                print(f"[{_now()}] Periodic vehicle check started", flush=True)

            if person_capture_due or vehicle_capture_due:
                _capture_and_process(
                    camera=camera,
                    detector=detector,
                    person_tracker=person_tracker,
                    car_tracker=car_tracker,
                    buzzer=buzzer,
                    client=client,
                    process_person=person_capture_due,
                    process_vehicle=vehicle_capture_due,
                    now=now,
                )

            if now - last_status_log >= MONITOR_STATUS_INTERVAL_SECONDS:
                next_vehicle_check = max(
                    0.0,
                    PERIODIC_CAPTURE_INTERVAL_SECONDS
                    - (now - last_periodic_capture),
                )
                pir_state = "motion" if is_motion else "no motion"
                print(
                    f"[{_now()}] Monitoring active "
                    f"| PIR: {pir_state} "
                    f"| person: {person_tracker.state.value} "
                    f"| tracked cars: {car_tracker.tracked_count} "
                    f"| next vehicle check: {next_vehicle_check:.0f}s",
                    flush=True,
                )
                last_status_log = now

            time.sleep(MONITOR_LOOP_INTERVAL_SECONDS)

    except KeyboardInterrupt:
        print(f"[{_now()}] Monitoring stopped by user", flush=True)
    finally:
        if buzzer is not None:
            try:
                buzzer.off()
            except Exception as error:
                print(f"[{_now()}] Buzzer stop error: {error}", flush=True)
            try:
                buzzer.close()
            except Exception as error:
                print(f"[{_now()}] Buzzer close error: {error}", flush=True)
        if camera is not None:
            try:
                camera.release()
            except Exception as error:
                print(f"[{_now()}] Camera release error: {error}", flush=True)
        if pir is not None:
            try:
                pir.close()
            except Exception as error:
                print(f"[{_now()}] PIR close error: {error}", flush=True)


def _capture_and_process(
    camera: Camera,
    detector: YOLODetector,
    person_tracker: PersonTracker,
    car_tracker: CarTracker,
    buzzer: PiezoBuzzer,
    client: ServerClient,
    process_person: bool,
    process_vehicle: bool,
    now: float,
):
    """画像を1回取得・推論し、必要な人物・車両処理へ結果を渡す。"""
    try:
        frame = camera.capture()
        detections = detector.detect(frame)
    except Exception as error:
        # 撮影・推論失敗は「対象なし」ではなく判定不能として扱う。
        print(f"[{_now()}] Capture or YOLO error: {error}", flush=True)
        return

    if process_person:
        persons = [detection for detection in detections if detection.label == "person"]
        result = person_tracker.update_detection(bool(persons), now)
        _handle_person_result(
            result=result,
            persons=persons,
            frame=frame,
            camera=camera,
            buzzer=buzzer,
            client=client,
        )

    if process_vehicle:
        cars = [detection for detection in detections if detection.label == "car"]
        alerts = car_tracker.update(cars, now)
        print(
            f"[{_now()}] Periodic vehicle check completed "
            f"| cars: {len(cars)} "
            f"| tracked: {car_tracker.tracked_count}",
            flush=True,
        )
        for alert in alerts:
            _notify_vehicle(alert, frame, camera, client)


def _handle_person_result(
    result: PersonTrackingResult,
    persons: list[DetectionResult],
    frame,
    camera: Camera,
    buzzer: PiezoBuzzer,
    client: ServerClient,
):
    if result.first_detected:
        print(f"[{_now()}] Person detected; continuous timer started", flush=True)

    if result.event_ended:
        print(
            f"[{_now()}] Person event ended "
            f"| duration: {result.duration_seconds:.1f}s",
            flush=True,
        )
        return

    if not result.alert_triggered or not persons:
        return

    target = max(persons, key=lambda detection: detection.confidence)
    print(
        f"[{_now()}] Long-staying person detected "
        f"| duration: {result.duration_seconds:.1f}s",
        flush=True,
    )

    try:
        buzzer.beep()
        print(f"[{_now()}] Person alert buzzer started", flush=True)
    except Exception as error:
        print(f"[{_now()}] Buzzer error: {error}", flush=True)

    _save_and_send(
        frame=frame,
        filename=f"person_{_timestamp()}.jpg",
        detection_type="person",
        target=target,
        duration_seconds=result.duration_seconds,
        camera=camera,
        client=client,
    )


def _notify_vehicle(
    alert: VehicleAlert,
    frame,
    camera: Camera,
    client: ServerClient,
):
    """不審車両をServerへ通知する。車両ではブザーを鳴らさない。"""
    print(
        f"[{_now()}] Suspicious vehicle detected "
        f"| track: {alert.track_id} "
        f"| duration: {alert.duration_seconds:.1f}s",
        flush=True,
    )
    _save_and_send(
        frame=frame,
        filename=f"vehicle_{alert.track_id}_{_timestamp()}.jpg",
        detection_type="suspicious_vehicle",
        target=alert.detection,
        duration_seconds=alert.duration_seconds,
        camera=camera,
        client=client,
    )


def _save_and_send(
    frame,
    filename: str,
    detection_type: str,
    target: DetectionResult,
    duration_seconds: float,
    camera: Camera,
    client: ServerClient,
):
    try:
        image_path = camera.save_frame(frame, filename)
    except Exception as error:
        print(f"[{_now()}] Failed to save alert image: {error}", flush=True)
        return

    try:
        response = client.send_detection(
            detection_type=detection_type,
            confidence=target.confidence,
            image_path=image_path,
            metadata=build_metadata(target, duration_seconds),
        )
        detection_id = response.get("id", "unknown")
        print(
            f"[{_now()}] Detection sent "
            f"| type: {detection_type} "
            f"| id: {detection_id}",
            flush=True,
        )
    except Exception as error:
        print(
            f"[{_now()}] Detection send failed "
            f"| image kept at {image_path} "
            f"| error: {error}",
            flush=True,
        )


def _now() -> str:
    return datetime.now(timezone.utc).isoformat()


def _timestamp() -> str:
    return datetime.now(timezone.utc).strftime("%Y%m%d_%H%M%S_%f")


if __name__ == "__main__":
    monitor()

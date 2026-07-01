# coding: utf-8
"""
carの基準位置を追跡し、長時間同じ場所にいる車両を判定する。

直前の位置ではなく追跡開始時の中心座標と比較することで、
少しずつ移動する車両を「同じ場所」と誤判定しにくくする。

参照:
- IES2 "Programming with Python" (classes / lists)
- Python dataclasses: https://docs.python.org/3/library/dataclasses.html
- Python math.hypot: https://docs.python.org/3/library/math.html#math.hypot
- Python time.monotonic: https://docs.python.org/3/library/time.html#time.monotonic
"""

import math
import time
from collections.abc import Callable
from dataclasses import dataclass

from src.config import (
    CAR_LOCATION_TOLERANCE,
    CAR_MISSING_CAPTURE_LIMIT,
    SUSPICIOUS_VEHICLE_THRESHOLD_SECONDS,
)
from src.detection import DetectionResult


@dataclass
class TrackedCar:
    track_id: int
    reference_x: float
    reference_y: float
    detection: DetectionResult
    first_seen_at: float
    last_seen_at: float
    missing_count: int = 0
    alerted: bool = False


@dataclass(frozen=True)
class VehicleAlert:
    """初めて不審車両条件を満たした追跡結果。"""

    track_id: int
    detection: DetectionResult
    duration_seconds: float


class CarTracker:
    """複数のcarを1対1で対応付けて追跡する。"""

    def __init__(
        self,
        threshold_seconds: float = SUSPICIOUS_VEHICLE_THRESHOLD_SECONDS,
        tolerance: float = CAR_LOCATION_TOLERANCE,
        missing_capture_limit: int = CAR_MISSING_CAPTURE_LIMIT,
        clock: Callable[[], float] = time.monotonic,
    ):
        self.threshold_seconds = threshold_seconds
        self.tolerance = tolerance
        self.missing_capture_limit = missing_capture_limit
        self.clock = clock
        self.tracked_cars: list[TrackedCar] = []
        self.next_track_id = 1

    def update(
        self,
        detections: list[DetectionResult],
        now: float | None = None,
    ) -> list[VehicleAlert]:
        """新しいcar検知結果で追跡を更新し、初回アラートだけ返す。"""
        current = self.clock() if now is None else now
        car_detections = [detection for detection in detections if detection.label == "car"]
        assignments = self._assign_detections(car_detections)
        matched_track_ids = {track_id for track_id, _ in assignments}
        matched_detection_indexes = {
            detection_index for _, detection_index in assignments
        }
        alerts: list[VehicleAlert] = []

        tracks_by_id = {track.track_id: track for track in self.tracked_cars}
        for track_id, detection_index in assignments:
            track = tracks_by_id[track_id]
            detection = car_detections[detection_index]
            track.detection = detection
            track.last_seen_at = current
            track.missing_count = 0
            self._append_alert_if_needed(track, current, alerts)

        remaining_tracks: list[TrackedCar] = []
        for track in self.tracked_cars:
            if track.track_id not in matched_track_ids:
                track.missing_count += 1
            if track.missing_count < self.missing_capture_limit:
                remaining_tracks.append(track)

        for index, detection in enumerate(car_detections):
            if index in matched_detection_indexes:
                continue
            center_x, center_y = detection.center
            track = TrackedCar(
                track_id=self.next_track_id,
                reference_x=center_x,
                reference_y=center_y,
                detection=detection,
                first_seen_at=current,
                last_seen_at=current,
            )
            self.next_track_id += 1
            self._append_alert_if_needed(track, current, alerts)
            remaining_tracks.append(track)

        self.tracked_cars = remaining_tracks
        return alerts

    @property
    def tracked_count(self) -> int:
        return len(self.tracked_cars)

    def reset(self):
        """追跡状態をリセットする。track IDはプロセス内で再利用しない。"""
        self.tracked_cars = []

    def _assign_detections(
        self,
        detections: list[DetectionResult],
    ) -> list[tuple[int, int]]:
        candidates: list[tuple[float, int, int]] = []
        for track in self.tracked_cars:
            for detection_index, detection in enumerate(detections):
                center_x, center_y = detection.center
                distance = math.hypot(
                    center_x - track.reference_x,
                    center_y - track.reference_y,
                )
                if distance <= self.tolerance:
                    candidates.append((distance, track.track_id, detection_index))

        assignments: list[tuple[int, int]] = []
        assigned_tracks: set[int] = set()
        assigned_detections: set[int] = set()
        for _, track_id, detection_index in sorted(candidates):
            if track_id in assigned_tracks or detection_index in assigned_detections:
                continue
            assignments.append((track_id, detection_index))
            assigned_tracks.add(track_id)
            assigned_detections.add(detection_index)
        return assignments

    def _append_alert_if_needed(
        self,
        track: TrackedCar,
        now: float,
        alerts: list[VehicleAlert],
    ):
        duration = now - track.first_seen_at
        if duration >= self.threshold_seconds and not track.alerted:
            # Server送信が失敗しても同じ追跡を再送しないよう、
            # 外部処理より先に通知済み状態へ移行する。
            track.alerted = True
            alerts.append(
                VehicleAlert(
                    track_id=track.track_id,
                    detection=track.detection,
                    duration_seconds=duration,
                )
            )

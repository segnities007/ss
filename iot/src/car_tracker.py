# coding: utf-8
"""
car の位置を追跡し、長時間同一場所に滞在している車両を判定するモジュール。

参照:
- IES2 "Programming with Python" (classes / lists / time module)
- Python dataclasses: https://docs.python.org/3/library/dataclasses.html#dataclasses.dataclass
- Python time.time: https://docs.python.org/3/library/time.html#time.time
"""

import time
from dataclasses import dataclass
from typing import List

from src.config import SUSPICIOUS_VEHICLE_THRESHOLD_SECONDS, CAR_LOCATION_TOLERANCE
from src.yolo_detector import DetectionResult


@dataclass
class TrackedCar:
    x: int
    y: int
    width: int
    height: int
    first_seen_at: float


class CarTracker:
    """car の位置を追跡し、長時間停滞車両を検出するクラス。"""

    def __init__(
        self,
        threshold_seconds: float = SUSPICIOUS_VEHICLE_THRESHOLD_SECONDS,
        tolerance: int = CAR_LOCATION_TOLERANCE,
    ):
        self.threshold_seconds = threshold_seconds
        self.tolerance = tolerance
        self.tracked_cars: List[TrackedCar] = []

    def update(self, detections: List[DetectionResult]) -> List[DetectionResult]:
        """
        新しい car 検知結果を元に追跡を更新し、不審車両と判定されたものを返す。
        """
        suspicious: List[DetectionResult] = []
        new_tracked: List[TrackedCar] = []

        for det in detections:
            if det.label != "car":
                continue

            matched = self._find_match(det)
            if matched is not None:
                # 既存の追跡を更新（位置は最新のものにする）
                elapsed = time.time() - matched.first_seen_at
                if elapsed >= self.threshold_seconds:
                    suspicious.append(det)
                new_tracked.append(TrackedCar(
                    x=det.x,
                    y=det.y,
                    width=det.width,
                    height=det.height,
                    first_seen_at=matched.first_seen_at,
                ))
            else:
                # 新規追跡
                new_tracked.append(TrackedCar(
                    x=det.x,
                    y=det.y,
                    width=det.width,
                    height=det.height,
                    first_seen_at=time.time(),
                ))

        self.tracked_cars = new_tracked
        return suspicious

    def _find_match(self, det: DetectionResult) -> TrackedCar | None:
        """許容範囲内で最も近い追跡中の car を探す。"""
        for car in self.tracked_cars:
            dx = abs(car.x - det.x)
            dy = abs(car.y - det.y)
            if dx <= self.tolerance and dy <= self.tolerance:
                return car
        return None

    def reset(self):
        """追跡状態をリセットする。"""
        self.tracked_cars = []

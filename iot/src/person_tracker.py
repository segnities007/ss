# coding: utf-8
"""
person の連続検知を追跡するモジュール。

一定時間以上 person が連続して検知された場合に「長時間滞在」と判定する。

参照:
- docs/plan.md Functional spec.
- docs/design/iot_design.md 4.6 person_tracker.py
- Python time.time: https://docs.python.org/3/library/time.html#time.time
"""

import time

from src.config import PERSON_DETECTION_THRESHOLD_SECONDS


class PersonTracker:
    """person の連続検知時間を追跡するクラス。"""

    def __init__(self, threshold_seconds: float = PERSON_DETECTION_THRESHOLD_SECONDS):
        self.threshold_seconds = threshold_seconds
        self.first_detected_at: float | None = None

    def update(self, detected: bool) -> bool:
        """
        person 検知状態を更新する。

        Returns:
            True: 閾値時間以上連続して検知されている場合
            False: それ以外
        """
        if detected:
            if self.first_detected_at is None:
                self.first_detected_at = time.time()
            elapsed = time.time() - self.first_detected_at
            return elapsed >= self.threshold_seconds
        else:
            self.first_detected_at = None
            return False

    def reset(self):
        """追跡状態をリセットする。"""
        self.first_detected_at = None

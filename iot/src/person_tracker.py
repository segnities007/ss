# coding: utf-8
"""
PIRをきっかけにpersonの連続検知イベントを追跡する。

PIRは観測開始のきっかけとして使用し、開始後はPIRがLOWでも
カメラによるperson観測を継続する。同じイベントの通知は1回だけ行う。

参照:
- IES2 "Programming with Python" (classes / functions)
- Python Enum: https://docs.python.org/3/library/enum.html
- Python dataclasses: https://docs.python.org/3/library/dataclasses.html
- Python time.monotonic: https://docs.python.org/3/library/time.html#time.monotonic
"""

import time
from collections.abc import Callable
from dataclasses import dataclass
from enum import Enum

from src.config import (
    PERSON_CAPTURE_INTERVAL_SECONDS,
    PERSON_DETECTION_THRESHOLD_SECONDS,
    PERSON_LOST_GRACE_SECONDS,
    PIR_OBSERVATION_TIMEOUT_SECONDS,
)


class PersonState(Enum):
    """人物監視イベントの状態。"""

    ARMED = "armed"
    OBSERVING = "observing"
    ALERTED = "alerted"
    WAIT_PIR_CLEAR = "wait_pir_clear"


@dataclass(frozen=True)
class PersonTrackingResult:
    """1回のperson検知更新によって発生した結果。"""

    state: PersonState
    first_detected: bool = False
    alert_triggered: bool = False
    event_ended: bool = False
    duration_seconds: float = 0.0


class PersonTracker:
    """PIR反応から始まる1つの人物イベントを管理する。"""

    def __init__(
        self,
        threshold_seconds: float = PERSON_DETECTION_THRESHOLD_SECONDS,
        lost_grace_seconds: float = PERSON_LOST_GRACE_SECONDS,
        observation_timeout_seconds: float = PIR_OBSERVATION_TIMEOUT_SECONDS,
        capture_interval_seconds: float = PERSON_CAPTURE_INTERVAL_SECONDS,
        clock: Callable[[], float] = time.monotonic,
    ):
        self.threshold_seconds = threshold_seconds
        self.lost_grace_seconds = lost_grace_seconds
        self.observation_timeout_seconds = observation_timeout_seconds
        self.capture_interval_seconds = capture_interval_seconds
        self.clock = clock
        self.state = PersonState.ARMED
        self.observation_started_at: float | None = None
        self.first_detected_at: float | None = None
        self.last_detected_at: float | None = None
        self.last_capture_at: float | None = None

    @property
    def is_observing(self) -> bool:
        """カメラによる人物観測を続ける状態か。"""
        return self.state in (PersonState.OBSERVING, PersonState.ALERTED)

    def update_pir(self, motion_detected: bool, now: float | None = None) -> bool:
        """
        PIR状態を更新する。

        Returns:
            新しい人物観測を開始した場合だけTrue。
        """
        current = self._now(now)
        started = False

        if self.state == PersonState.ARMED and motion_detected:
            self._start_observation(current)
            started = True
        elif self.state == PersonState.WAIT_PIR_CLEAR and not motion_detected:
            self._arm()

        return started

    def capture_due(self, now: float | None = None) -> bool:
        """人物観測用の次の撮影時刻に達しているか。"""
        if not self.is_observing:
            return False
        current = self._now(now)
        return (
            self.last_capture_at is None
            or current - self.last_capture_at >= self.capture_interval_seconds
        )

    def mark_capture_attempt(self, now: float | None = None):
        """撮影を試行した時刻を記録する。"""
        self.last_capture_at = self._now(now)

    def update_detection(
        self,
        detected: bool,
        now: float | None = None,
    ) -> PersonTrackingResult:
        """YOLOによるperson検知結果でイベント状態を更新する。"""
        current = self._now(now)
        if not self.is_observing:
            return PersonTrackingResult(state=self.state)

        if detected:
            first_detected = self.first_detected_at is None
            if first_detected:
                self.first_detected_at = current
            self.last_detected_at = current

            duration = current - self.first_detected_at
            if (
                self.state == PersonState.OBSERVING
                and duration >= self.threshold_seconds
            ):
                # 外部処理が失敗しても同じイベントを再通知しないよう、
                # ブザー・Server送信より先に通知済み状態へ移行する。
                self.state = PersonState.ALERTED
                return PersonTrackingResult(
                    state=self.state,
                    first_detected=first_detected,
                    alert_triggered=True,
                    duration_seconds=duration,
                )

            return PersonTrackingResult(
                state=self.state,
                first_detected=first_detected,
                duration_seconds=duration,
            )

        if self.first_detected_at is None:
            if (
                self.observation_started_at is not None
                and current - self.observation_started_at
                >= self.observation_timeout_seconds
            ):
                return self._end_event()
            return PersonTrackingResult(state=self.state)

        if (
            self.last_detected_at is not None
            and current - self.last_detected_at > self.lost_grace_seconds
        ):
            return self._end_event()

        return PersonTrackingResult(
            state=self.state,
            duration_seconds=current - self.first_detected_at,
        )

    def current_duration(self, now: float | None = None) -> float:
        """personを初めて検知してからの経過時間。"""
        if self.first_detected_at is None:
            return 0.0
        return max(0.0, self._now(now) - self.first_detected_at)

    def reset(self):
        """全状態を初期化する。"""
        self._arm()

    def _start_observation(self, now: float):
        self.state = PersonState.OBSERVING
        self.observation_started_at = now
        self.first_detected_at = None
        self.last_detected_at = None
        self.last_capture_at = None

    def _end_event(self) -> PersonTrackingResult:
        duration = 0.0
        if self.first_detected_at is not None and self.last_detected_at is not None:
            duration = max(0.0, self.last_detected_at - self.first_detected_at)
        self.state = PersonState.WAIT_PIR_CLEAR
        self.observation_started_at = None
        self.first_detected_at = None
        self.last_detected_at = None
        self.last_capture_at = None
        return PersonTrackingResult(
            state=self.state,
            event_ended=True,
            duration_seconds=duration,
        )

    def _arm(self):
        self.state = PersonState.ARMED
        self.observation_started_at = None
        self.first_detected_at = None
        self.last_detected_at = None
        self.last_capture_at = None

    def _now(self, now: float | None) -> float:
        return self.clock() if now is None else now

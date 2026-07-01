# coding: utf-8
"""
PIRをきっかけにpersonを個別追跡し、人物ごとの滞在時間を判定する。

各personへ監視プロセス内で一意なtrack IDを割り当て、連続する撮影間で
中心座標が最も近い検知を同じ人物として対応付ける。顔認証やRe-IDでは
ないため、画面を離れた後の本人確認は行わない。

参照:
- IES2 "Programming with Python" (classes / lists)
- Python Enum: https://docs.python.org/3/library/enum.html
- Python dataclasses: https://docs.python.org/3/library/dataclasses.html
- Python math.hypot: https://docs.python.org/3/library/math.html#math.hypot
- Python time.monotonic: https://docs.python.org/3/library/time.html#time.monotonic
"""

import math
import time
from collections.abc import Callable
from dataclasses import dataclass
from enum import Enum

from src.config import (
    PERSON_CAPTURE_INTERVAL_SECONDS,
    PERSON_DETECTION_THRESHOLD_SECONDS,
    PERSON_LOCATION_TOLERANCE,
    PERSON_LOST_GRACE_SECONDS,
    PIR_OBSERVATION_TIMEOUT_SECONDS,
)
from src.detection import DetectionResult


class PersonState(Enum):
    """人物監視イベントの状態。"""

    ARMED = "armed"
    OBSERVING = "observing"
    ALERTED = "alerted"
    WAIT_PIR_CLEAR = "wait_pir_clear"


@dataclass
class TrackedPerson:
    """連続する画像間で対応付けられた1人のperson。"""

    track_id: int
    detection: DetectionResult
    first_seen_at: float
    last_seen_at: float
    alerted: bool = False


@dataclass(frozen=True)
class PersonAlert:
    """1人のpersonが初めて長時間滞在条件を満たした結果。"""

    track_id: int
    detection: DetectionResult
    duration_seconds: float


@dataclass(frozen=True)
class PersonTrackingResult:
    """1回のperson追跡更新によって発生した結果。"""

    state: PersonState
    new_track_ids: tuple[int, ...] = ()
    ended_track_ids: tuple[int, ...] = ()
    alerts: tuple[PersonAlert, ...] = ()
    event_ended: bool = False


class PersonTracker:
    """PIR反応後に複数のpersonを個別のtrack IDで管理する。"""

    def __init__(
        self,
        threshold_seconds: float = PERSON_DETECTION_THRESHOLD_SECONDS,
        lost_grace_seconds: float = PERSON_LOST_GRACE_SECONDS,
        observation_timeout_seconds: float = PIR_OBSERVATION_TIMEOUT_SECONDS,
        capture_interval_seconds: float = PERSON_CAPTURE_INTERVAL_SECONDS,
        location_tolerance: float = PERSON_LOCATION_TOLERANCE,
        clock: Callable[[], float] = time.monotonic,
    ):
        self.threshold_seconds = threshold_seconds
        self.lost_grace_seconds = lost_grace_seconds
        self.observation_timeout_seconds = observation_timeout_seconds
        self.capture_interval_seconds = capture_interval_seconds
        self.location_tolerance = location_tolerance
        self.clock = clock
        self.state = PersonState.ARMED
        self.observation_started_at: float | None = None
        self.last_capture_at: float | None = None
        self.tracked_persons: list[TrackedPerson] = []
        self.next_track_id = 1
        self.had_person = False

    @property
    def is_observing(self) -> bool:
        return self.state in (PersonState.OBSERVING, PersonState.ALERTED)

    @property
    def tracked_count(self) -> int:
        return len(self.tracked_persons)

    def update_pir(self, motion_detected: bool, now: float | None = None) -> bool:
        current = self._now(now)
        if self.state == PersonState.ARMED and motion_detected:
            self._start_observation(current)
            return True
        if self.state == PersonState.WAIT_PIR_CLEAR and not motion_detected:
            self._arm()
        return False

    def capture_due(self, now: float | None = None) -> bool:
        if not self.is_observing:
            return False
        current = self._now(now)
        return (
            self.last_capture_at is None
            or current - self.last_capture_at >= self.capture_interval_seconds
        )

    def mark_capture_attempt(self, now: float | None = None):
        self.last_capture_at = self._now(now)

    def update_detection(
        self,
        detections: list[DetectionResult],
        now: float | None = None,
    ) -> PersonTrackingResult:
        """person検知を既存trackへ1対1で対応付けて状態を更新する。"""
        current = self._now(now)
        if not self.is_observing:
            return PersonTrackingResult(state=self.state)

        persons = [detection for detection in detections if detection.label == "person"]
        active_tracks: list[TrackedPerson] = []
        ended_track_ids: list[int] = []
        for track in self.tracked_persons:
            if current - track.last_seen_at > self.lost_grace_seconds:
                ended_track_ids.append(track.track_id)
            else:
                active_tracks.append(track)
        self.tracked_persons = active_tracks

        assignments = self._assign_detections(persons)
        matched_detection_indexes = {
            detection_index for _, detection_index in assignments
        }
        tracks_by_id = {
            track.track_id: track for track in self.tracked_persons
        }
        alerts: list[PersonAlert] = []

        for track_id, detection_index in assignments:
            track = tracks_by_id[track_id]
            track.detection = persons[detection_index]
            track.last_seen_at = current
            self._append_alert_if_needed(track, current, alerts)

        new_track_ids: list[int] = []
        for index, detection in enumerate(persons):
            if index in matched_detection_indexes:
                continue
            track = TrackedPerson(
                track_id=self.next_track_id,
                detection=detection,
                first_seen_at=current,
                last_seen_at=current,
            )
            self.next_track_id += 1
            self.tracked_persons.append(track)
            new_track_ids.append(track.track_id)
            self.had_person = True
            self._append_alert_if_needed(track, current, alerts)

        if alerts:
            self.state = PersonState.ALERTED

        if not self.tracked_persons:
            no_person_timed_out = (
                not self.had_person
                and self.observation_started_at is not None
                and current - self.observation_started_at
                >= self.observation_timeout_seconds
            )
            all_people_left = self.had_person and bool(ended_track_ids)
            if no_person_timed_out or all_people_left:
                return self._end_event(ended_track_ids)

        return PersonTrackingResult(
            state=self.state,
            new_track_ids=tuple(new_track_ids),
            ended_track_ids=tuple(ended_track_ids),
            alerts=tuple(alerts),
        )

    def current_duration(
        self,
        track_id: int,
        now: float | None = None,
    ) -> float:
        current = self._now(now)
        for track in self.tracked_persons:
            if track.track_id == track_id:
                return max(0.0, current - track.first_seen_at)
        return 0.0

    def reset(self):
        """観測中の人物を破棄する。次のtrack IDは再利用しない。"""
        self._arm()

    def _assign_detections(
        self,
        detections: list[DetectionResult],
    ) -> list[tuple[int, int]]:
        candidates: list[tuple[float, int, int]] = []
        for track in self.tracked_persons:
            track_x, track_y = track.detection.center
            for detection_index, detection in enumerate(detections):
                center_x, center_y = detection.center
                distance = math.hypot(center_x - track_x, center_y - track_y)
                size_tolerance = max(
                    track.detection.width,
                    track.detection.height,
                    detection.width,
                    detection.height,
                )
                allowed_distance = max(self.location_tolerance, size_tolerance)
                if distance <= allowed_distance:
                    candidates.append(
                        (distance, track.track_id, detection_index)
                    )

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
        track: TrackedPerson,
        now: float,
        alerts: list[PersonAlert],
    ):
        duration = now - track.first_seen_at
        if duration >= self.threshold_seconds and not track.alerted:
            track.alerted = True
            alerts.append(
                PersonAlert(
                    track_id=track.track_id,
                    detection=track.detection,
                    duration_seconds=duration,
                )
            )

    def _start_observation(self, now: float):
        self.state = PersonState.OBSERVING
        self.observation_started_at = now
        self.last_capture_at = None
        self.tracked_persons = []
        self.had_person = False

    def _end_event(self, ended_track_ids: list[int]) -> PersonTrackingResult:
        self.state = PersonState.WAIT_PIR_CLEAR
        self.observation_started_at = None
        self.last_capture_at = None
        self.tracked_persons = []
        self.had_person = False
        return PersonTrackingResult(
            state=self.state,
            ended_track_ids=tuple(ended_track_ids),
            event_ended=True,
        )

    def _arm(self):
        self.state = PersonState.ARMED
        self.observation_started_at = None
        self.last_capture_at = None
        self.tracked_persons = []
        self.had_person = False

    def _now(self, now: float | None) -> float:
        return self.clock() if now is None else now

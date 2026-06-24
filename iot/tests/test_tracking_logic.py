# coding: utf-8
"""
人物・車両追跡ロジックの自動テスト。

実時間を待たず、now引数で時刻を進めて閾値の境界を確認する。

参照:
- Python unittest: https://docs.python.org/3/library/unittest.html
"""

import unittest

from src.car_tracker import CarTracker
from src.config import validate_config
from src.detection import DetectionResult
from src.person_tracker import PersonState, PersonTracker


def car(x: int, y: int = 0) -> DetectionResult:
    return DetectionResult(
        label="car",
        confidence=0.9,
        x=x,
        y=y,
        width=20,
        height=20,
    )


class PersonTrackerTest(unittest.TestCase):
    def setUp(self):
        self.tracker = PersonTracker(
            threshold_seconds=60.0,
            lost_grace_seconds=4.0,
            observation_timeout_seconds=5.0,
            capture_interval_seconds=2.0,
        )

    def test_alerts_once_at_threshold(self):
        self.assertTrue(self.tracker.update_pir(True, now=0.0))
        first = self.tracker.update_detection(True, now=0.0)
        before = self.tracker.update_detection(True, now=59.9)
        at_threshold = self.tracker.update_detection(True, now=60.0)
        after = self.tracker.update_detection(True, now=70.0)

        self.assertTrue(first.first_detected)
        self.assertFalse(before.alert_triggered)
        self.assertTrue(at_threshold.alert_triggered)
        self.assertFalse(after.alert_triggered)
        self.assertEqual(self.tracker.state, PersonState.ALERTED)

    def test_one_missed_capture_is_tolerated(self):
        self.tracker.update_pir(True, now=0.0)
        self.tracker.update_detection(True, now=0.0)

        within_grace = self.tracker.update_detection(False, now=4.0)
        beyond_grace = self.tracker.update_detection(False, now=4.1)

        self.assertFalse(within_grace.event_ended)
        self.assertTrue(beyond_grace.event_ended)
        self.assertEqual(self.tracker.state, PersonState.WAIT_PIR_CLEAR)

    def test_new_event_requires_pir_clear(self):
        self.tracker.update_pir(True, now=0.0)
        self.tracker.update_detection(True, now=0.0)
        self.tracker.update_detection(False, now=4.1)

        self.assertFalse(self.tracker.update_pir(True, now=5.0))
        self.tracker.update_pir(False, now=6.0)
        self.assertEqual(self.tracker.state, PersonState.ARMED)
        self.assertTrue(self.tracker.update_pir(True, now=7.0))

    def test_observation_continues_after_pir_clears(self):
        self.tracker.update_pir(True, now=0.0)
        self.tracker.update_detection(True, now=0.0)
        self.tracker.update_pir(False, now=1.0)
        result = self.tracker.update_detection(True, now=60.0)

        self.assertTrue(result.alert_triggered)
        self.assertEqual(self.tracker.state, PersonState.ALERTED)

    def test_observation_ends_when_no_person_is_found(self):
        self.tracker.update_pir(True, now=0.0)
        result = self.tracker.update_detection(False, now=5.0)

        self.assertTrue(result.event_ended)
        self.assertEqual(self.tracker.state, PersonState.WAIT_PIR_CLEAR)

    def test_capture_interval(self):
        self.tracker.update_pir(True, now=0.0)
        self.assertTrue(self.tracker.capture_due(now=0.0))
        self.tracker.mark_capture_attempt(now=0.0)
        self.assertFalse(self.tracker.capture_due(now=1.9))
        self.assertTrue(self.tracker.capture_due(now=2.0))


class CarTrackerTest(unittest.TestCase):
    def test_alerts_once_at_threshold(self):
        tracker = CarTracker(
            threshold_seconds=300.0,
            tolerance=50.0,
            missing_capture_limit=2,
        )

        self.assertEqual(tracker.update([car(0)], now=0.0), [])
        alerts = tracker.update([car(0)], now=300.0)
        repeated = tracker.update([car(0)], now=330.0)

        self.assertEqual(len(alerts), 1)
        self.assertEqual(alerts[0].duration_seconds, 300.0)
        self.assertEqual(repeated, [])

    def test_track_ends_after_two_missing_captures(self):
        tracker = CarTracker(missing_capture_limit=2)
        tracker.update([car(0)], now=0.0)

        tracker.update([], now=30.0)
        self.assertEqual(tracker.tracked_count, 1)
        tracker.update([], now=60.0)
        self.assertEqual(tracker.tracked_count, 0)

    def test_position_is_compared_with_initial_reference(self):
        tracker = CarTracker(
            threshold_seconds=999.0,
            tolerance=50.0,
            missing_capture_limit=2,
        )
        tracker.update([car(0)], now=0.0)
        tracker.update([car(40)], now=30.0)
        tracker.update([car(80)], now=60.0)

        self.assertEqual(tracker.tracked_count, 2)
        reference_positions = {
            (track.reference_x, track.reference_y)
            for track in tracker.tracked_cars
        }
        self.assertEqual(reference_positions, {(10.0, 10.0), (90.0, 10.0)})

    def test_multiple_cars_are_assigned_one_to_one(self):
        tracker = CarTracker(tolerance=50.0)
        tracker.update([car(0), car(200)], now=0.0)
        tracker.update([car(5), car(205)], now=30.0)

        self.assertEqual(tracker.tracked_count, 2)
        self.assertEqual(
            {track.track_id for track in tracker.tracked_cars},
            {1, 2},
        )


class ConfigTest(unittest.TestCase):
    def test_default_configuration_is_valid(self):
        validate_config()


if __name__ == "__main__":
    unittest.main()

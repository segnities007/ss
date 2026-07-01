# coding: utf-8
"""監視制御のバックグラウンド同期を確認する自動テスト。"""

import time
import unittest

from src.iot_control import IoTControlPoller
from src.server_client import IoTControlCommand, IoTDeviceSettings


class FakeServerClient:
    def __init__(self, command: IoTControlCommand):
        self.command = command
        self.reported_states: list[bool] = []

    def sync_monitoring_control(
        self,
        monitoring_active: bool,
    ) -> IoTControlCommand:
        self.reported_states.append(monitoring_active)
        return self.command


class IoTControlPollerTest(unittest.TestCase):
    def test_sync_runs_in_background_and_updates_requested_state(self):
        expected = IoTControlCommand(
            monitoring_enabled=False,
            settings=IoTDeviceSettings(buzzer_enabled=False),
        )
        client = FakeServerClient(command=expected)
        poller = IoTControlPoller(client, interval_seconds=0.01)

        poller.start()
        try:
            deadline = time.monotonic() + 0.5
            while poller.command != expected and time.monotonic() < deadline:
                time.sleep(0.005)

            self.assertEqual(poller.command, expected)
            self.assertEqual(client.reported_states[0], True)
        finally:
            poller.stop()


if __name__ == "__main__":
    unittest.main()

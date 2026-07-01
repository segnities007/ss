# coding: utf-8
"""PC ServerとのIoT制御同期を確認する自動テスト。"""

import unittest
from unittest.mock import Mock, patch

from src.server_client import ServerClient


class ServerClientControlTest(unittest.TestCase):
    @patch("src.server_client.requests.post")
    def test_sync_monitoring_control_returns_requested_state(self, post: Mock):
        response = Mock()
        response.json.return_value = {
            "monitoringEnabled": False,
            "settings": {
                "buzzerEnabled": False,
                "cameraEnabled": True,
                "pirSensorEnabled": False,
            },
        }
        post.return_value = response
        client = ServerClient("http://server.example")

        command = client.sync_monitoring_control(monitoring_active=True)

        self.assertFalse(command.monitoring_enabled)
        self.assertFalse(command.settings.buzzer_enabled)
        self.assertTrue(command.settings.camera_enabled)
        self.assertFalse(command.settings.pir_sensor_enabled)
        post.assert_called_once()
        self.assertEqual(
            post.call_args.kwargs["json"],
            {"monitoringActive": True},
        )
        response.raise_for_status.assert_called_once_with()

    @patch("src.server_client.requests.post")
    def test_sync_monitoring_control_rejects_invalid_response(self, post: Mock):
        response = Mock()
        response.json.return_value = {}
        post.return_value = response
        client = ServerClient("http://server.example")

        with self.assertRaises(ValueError):
            client.sync_monitoring_control(monitoring_active=True)


if __name__ == "__main__":
    unittest.main()

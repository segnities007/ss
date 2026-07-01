# coding: utf-8
"""
PC server へ検知情報と画像を送信するモジュール。

HTTP 通信には requests を使用（講義資料外）。
講義資料では HTTP 通信について詳しく扱っていないため、簡便な requests を採用。
参照:
- requests multipart upload: https://requests.readthedocs.io/en/latest/user/quickstart/#post-a-multipart-encoded-file
- requests JSON request: https://requests.readthedocs.io/en/latest/user/quickstart/#more-complicated-post-requests
- requests timeout: https://requests.readthedocs.io/en/latest/user/quickstart/#timeouts
"""

import json
from dataclasses import dataclass
from datetime import datetime, timezone
from typing import Any, Dict, Optional

import requests

from src.config import (
    IOT_HEARTBEAT_ENDPOINT,
    SERVER_BASE_URL,
    SERVER_CONNECT_TIMEOUT_SECONDS,
    SERVER_ENDPOINT,
    SERVER_READ_TIMEOUT_SECONDS,
)


@dataclass(frozen=True)
class IoTDeviceSettings:
    """Mobile Applicationから指定するデバイスの有効状態。"""

    buzzer_enabled: bool = True
    camera_enabled: bool = True
    pir_sensor_enabled: bool = True


@dataclass(frozen=True)
class IoTControlCommand:
    """PC Serverから取得する監視制御。"""

    monitoring_enabled: bool = True
    settings: IoTDeviceSettings = IoTDeviceSettings()


class ServerClient:
    """PC server との通信を行うクラス。"""

    def __init__(self, base_url: str = SERVER_BASE_URL):
        self.base_url = base_url.rstrip("/")
        self.url = self.base_url + SERVER_ENDPOINT
        self.heartbeat_url = self.base_url + IOT_HEARTBEAT_ENDPOINT

    def send_detection(
        self,
        detection_type: str,
        confidence: float,
        image_path: str,
        metadata: Optional[Dict[str, Any]] = None,
    ) -> Dict[str, Any]:
        """検知情報と画像を server に送信する。"""
        detected_at = datetime.now(timezone.utc).isoformat()
        data = {
            "type": detection_type,
            "detectedAt": detected_at,
            "confidence": str(confidence),
        }
        if metadata is not None:
            data["metadata"] = json.dumps(metadata)

        with open(image_path, "rb") as image_file:
            files = {"image": image_file}
            response = requests.post(
                self.url,
                data=data,
                files=files,
                timeout=(
                    SERVER_CONNECT_TIMEOUT_SECONDS,
                    SERVER_READ_TIMEOUT_SECONDS,
                ),
            )
            response.raise_for_status()
            return response.json()

    def health_check(self) -> bool:
        """server のヘルスチェックを行う。"""
        try:
            response = requests.get(
                self.base_url + "/api/health",
                timeout=5,
            )
            return response.status_code == 200
        except requests.RequestException:
            return False

    def sync_monitoring_control(
        self,
        monitoring_active: bool,
    ) -> IoTControlCommand:
        """
        現在の監視状態を通知し、Mobile Applicationが指定した状態を取得する。

        Returns:
            Serverが要求する監視状態とデバイス設定。
        """
        response = requests.post(
            self.heartbeat_url,
            json={"monitoringActive": monitoring_active},
            timeout=(
                SERVER_CONNECT_TIMEOUT_SECONDS,
                SERVER_READ_TIMEOUT_SECONDS,
            ),
        )
        response.raise_for_status()
        payload = response.json()
        monitoring_enabled = payload.get("monitoringEnabled")
        if not isinstance(monitoring_enabled, bool):
            raise ValueError("Server response does not contain monitoringEnabled")

        settings = payload.get("settings", {})
        if not isinstance(settings, dict):
            raise ValueError("Server response contains invalid settings")
        buzzer_enabled = settings.get("buzzerEnabled", True)
        camera_enabled = settings.get("cameraEnabled", True)
        pir_sensor_enabled = settings.get("pirSensorEnabled", True)
        if not all(
            isinstance(value, bool)
            for value in (buzzer_enabled, camera_enabled, pir_sensor_enabled)
        ):
            raise ValueError("Server response contains invalid device settings")

        return IoTControlCommand(
            monitoring_enabled=monitoring_enabled,
            settings=IoTDeviceSettings(
                buzzer_enabled=buzzer_enabled,
                camera_enabled=camera_enabled,
                pir_sensor_enabled=pir_sensor_enabled,
            ),
        )

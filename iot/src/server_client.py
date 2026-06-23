# coding: utf-8
"""
PC server へ検知情報と画像を送信するモジュール。

HTTP 通信には requests を使用（講義資料外）。
講義資料では HTTP 通信について詳しく扱っていないため、簡便な requests を採用。
参照:
- requests multipart upload: https://requests.readthedocs.io/en/latest/user/quickstart/#post-a-multipart-encoded-file
- requests timeout: https://requests.readthedocs.io/en/latest/user/quickstart/#timeouts
"""

import json
from datetime import datetime, timezone
from typing import Any, Dict, Optional

import requests

from src.config import SERVER_BASE_URL, SERVER_ENDPOINT


class ServerClient:
    """PC server との通信を行うクラス。"""

    def __init__(self, base_url: str = SERVER_BASE_URL):
        self.url = base_url.rstrip("/") + SERVER_ENDPOINT

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
            response = requests.post(self.url, data=data, files=files, timeout=30)
            response.raise_for_status()
            return response.json()

    def health_check(self) -> bool:
        """server のヘルスチェックを行う。"""
        try:
            response = requests.get(
                self.url.rstrip("/").replace(SERVER_ENDPOINT, "/api/health"),
                timeout=5,
            )
            return response.status_code == 200
        except requests.RequestException:
            return False

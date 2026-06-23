# coding: utf-8
"""
PC server への送信テスト。

テスト画像を作成して server に送信する。

HTTP クライアントには requests を使用（講義資料外）。
参照:
- requests multipart upload: https://requests.readthedocs.io/en/latest/user/quickstart/#post-a-multipart-encoded-file
- requests timeout: https://requests.readthedocs.io/en/latest/user/quickstart/#timeouts
"""

from pathlib import Path

import cv2
import numpy as np

from src.server_client import ServerClient


def main():
    print("Testing server send...")
    client = ServerClient()

    # テスト画像を作成
    Path("data").mkdir(exist_ok=True)
    image_path = "data/test_server_send.jpg"
    image = np.full((480, 640, 3), 128, dtype=np.uint8)
    cv2.imwrite(image_path, image)

    try:
        result = client.send_detection(
            detection_type="person",
            confidence=0.95,
            image_path=image_path,
            metadata={"boundingBox": {"x": 10, "y": 10, "width": 100, "height": 200}},
        )
        print(f"Server response: {result}")
    except Exception as e:
        print(f"Failed to send: {e}")


if __name__ == "__main__":
    main()

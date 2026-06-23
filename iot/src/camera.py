# coding: utf-8
"""
USB カメラの制御モジュール。

参照:
- IES5 "Capturing of Images Using a Camera"
- OpenCV VideoCapture tutorial: https://docs.opencv.org/4.x/dd/d43/tutorial_py_video_display.html
- OpenCV image write API: https://docs.opencv.org/4.x/d4/da8/group__imgcodecs.html
"""

from pathlib import Path
from typing import Optional

import cv2
import numpy as np

from src.config import CAMERA_DEVICE, CAPTURE_RESOLUTION, IMAGE_SAVE_DIR


class Camera:
    """USB カメラから画像を取得するクラス。"""

    def __init__(self, device: int = CAMERA_DEVICE):
        self.cap = cv2.VideoCapture(device)
        if not self.cap.isOpened():
            raise RuntimeError(f"Cannot open camera device {device}")
        self.cap.set(cv2.CAP_PROP_FRAME_WIDTH, CAPTURE_RESOLUTION[0])
        self.cap.set(cv2.CAP_PROP_FRAME_HEIGHT, CAPTURE_RESOLUTION[1])

    def capture(self) -> np.ndarray:
        """カメラから 1 フレーム取得する。"""
        ret, frame = self.cap.read()
        if not ret:
            raise RuntimeError("Failed to capture image")
        return frame

    def save_frame(self, frame: np.ndarray, filename: str) -> str:
        """フレームをファイルに保存する。"""
        Path(IMAGE_SAVE_DIR).mkdir(parents=True, exist_ok=True)
        path = Path(IMAGE_SAVE_DIR) / filename
        cv2.imwrite(str(path), frame)
        return str(path)

    def release(self):
        """カメラリソースを解放する。"""
        self.cap.release()

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.release()

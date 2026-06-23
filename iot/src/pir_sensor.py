# coding: utf-8
"""
PIR モーションセンサーの読み取りモジュール。

参照:
- IES7 Tips "1. Use of a Motion Sensor"
- gpiozero.MotionSensor: https://gpiozero.readthedocs.io/en/stable/api_input.html#gpiozero.MotionSensor
"""

from gpiozero import MotionSensor
from src.config import PIR_PIN


class PIRSensor:
    """PIR モーションセンサーを扱うクラス。"""

    def __init__(self, pin: int = PIR_PIN):
        self.sensor = MotionSensor(pin)

    def is_motion_detected(self) -> bool:
        """現在動きが検知されているかを返す。"""
        return self.sensor.motion_detected

    def wait_for_motion(self, timeout: float | None = None) -> bool:
        """動きを検知するまで待つ。timeout 秒以内に検知できなければ False を返す。"""
        return self.sensor.wait_for_motion(timeout=timeout)

    def close(self):
        """GPIO リソースを解放する。"""
        self.sensor.close()

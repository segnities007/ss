# coding: utf-8
"""
圧電ブザーの制御モジュール。

参照:
- IES3 "Control of Sensors and Actuators Using GPIOs"
- IES4 "PWM"
- gpiozero.Buzzer: https://gpiozero.readthedocs.io/en/stable/api_output.html#gpiozero.Buzzer
"""

import time

from gpiozero import Buzzer

from src.config import BUZZER_PIN


class PiezoBuzzer:
    """圧電ブザーを制御するクラス。"""

    def __init__(self, pin: int = BUZZER_PIN):
        # OSOYOOの配線（ブザー＋を3.3V、－をGPIOへ接続）ではLOWで鳴る。
        self.buzzer = Buzzer(pin, active_high=False)

    def beep(self, on_time: float = 0.5, off_time: float = 0.5, count: int = 3):
        """指定回数ビープ音を鳴らす。"""
        self.buzzer.beep(on_time=on_time, off_time=off_time, n=count)

    def on(self):
        """ブザーを連続で鳴らす。"""
        self.buzzer.on()

    def off(self):
        """ブザーを止める。"""
        self.buzzer.off()

    def alert(self, duration_seconds: float = 5.0):
        """警告音を指定秒数鳴らす。"""
        self.on()
        time.sleep(duration_seconds)
        self.off()

    def close(self):
        """GPIO リソースを解放する。"""
        self.buzzer.close()

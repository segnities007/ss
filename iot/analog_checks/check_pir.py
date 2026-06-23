# coding: utf-8
"""
PIR モーションセンサーのアナログ動作確認。

手を振ったり動いたりして、PIR が反応することを確認する。

参照:
- IES7 Tips "1. Use of a Motion Sensor"
- gpiozero.MotionSensor: https://gpiozero.readthedocs.io/en/stable/api_input.html#gpiozero.MotionSensor
"""

import time

from src.pir_sensor import PIRSensor


def main():
    print("PIR sensor check started. Press Ctrl+C to stop.")
    pir = PIRSensor()
    try:
        while True:
            if pir.is_motion_detected():
                print("[MOTION DETECTED]")
            else:
                print("[NO MOTION]")
            time.sleep(0.5)
    except KeyboardInterrupt:
        print("\nStopped.")
    finally:
        pir.close()


if __name__ == "__main__":
    main()

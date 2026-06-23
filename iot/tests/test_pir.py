# coding: utf-8
"""
PIR センサー検知 → メッセージ出力のテスト。
"""

import time

from src.pir_sensor import PIRSensor


def main():
    print("Testing PIR sensor...")
    pir = PIRSensor()
    try:
        detected_count = 0
        for _ in range(20):
            if pir.is_motion_detected():
                detected_count += 1
                print(f"Motion detected! (count: {detected_count})")
            time.sleep(0.5)
        print(f"Total detections: {detected_count}")
    finally:
        pir.close()


if __name__ == "__main__":
    main()

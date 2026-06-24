# coding: utf-8
"""
圧電ブザー制御のテスト。

参照:
- IES3 "Control of Sensors and Actuators Using GPIOs"
- IES4 "PWM"
- gpiozero.Buzzer: https://gpiozero.readthedocs.io/en/stable/api_output.html#gpiozero.Buzzer
"""

from src.buzzer import PiezoBuzzer


def main():
    print("Testing buzzer. It will sound for 5 seconds.")
    buzzer = PiezoBuzzer()
    try:
        buzzer.alert(duration_seconds=5.0)
        print("Buzzer test completed.")
    finally:
        buzzer.close()


if __name__ == "__main__":
    main()

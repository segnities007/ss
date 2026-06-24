# coding: utf-8
"""
圧電ブザーのアナログ動作確認。

ブザーが数回鳴ることを確認する。

参照:
- IES3 "Control of Sensors and Actuators Using GPIOs"
- IES4 "PWM"
- gpiozero.Buzzer: https://gpiozero.readthedocs.io/en/stable/api_output.html#gpiozero.Buzzer
"""

from src.buzzer import PiezoBuzzer


def main():
    print("Buzzer check started. It will sound for 5 seconds.")
    buzzer = PiezoBuzzer()
    try:
        buzzer.alert(duration_seconds=5.0)
        print("Buzzer check completed.")
    finally:
        buzzer.close()


if __name__ == "__main__":
    main()

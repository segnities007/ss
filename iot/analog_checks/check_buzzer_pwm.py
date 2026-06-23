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
    print("Buzzer check started.")
    buzzer = PiezoBuzzer()
    try:
        buzzer.beep(on_time=0.3, off_time=0.3, count=5)
        print("Buzzer should have beeped 5 times.")
    finally:
        buzzer.close()


if __name__ == "__main__":
    main()

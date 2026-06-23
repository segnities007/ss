# coding: utf-8
"""
圧電ブザー制御のテスト。
"""

from src.buzzer import PiezoBuzzer


def main():
    print("Testing buzzer...")
    buzzer = PiezoBuzzer()
    try:
        buzzer.beep(count=3)
        print("Buzzer test completed.")
    finally:
        buzzer.close()


if __name__ == "__main__":
    main()

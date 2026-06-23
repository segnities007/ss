# coding: utf-8
"""
USB カメラ画像取得・保存のテスト。
"""

from src.camera import Camera


def main():
    print("Testing camera...")
    camera = Camera()
    try:
        frame = camera.capture()
        path = camera.save_frame(frame, "test_camera.jpg")
        print(f"Image saved to: {path}")
    finally:
        camera.release()


if __name__ == "__main__":
    main()

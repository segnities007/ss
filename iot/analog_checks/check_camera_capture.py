# coding: utf-8
"""
USB カメラのアナログ動作確認。

カメラから画像を取得し、data/check_camera.jpg に保存する。

参照:
- IES5 "Capturing of Images Using a Camera"
- OpenCV VideoCapture tutorial: https://docs.opencv.org/4.x/dd/d43/tutorial_py_video_display.html
- OpenCV image write API: https://docs.opencv.org/4.x/d4/da8/group__imgcodecs.html
"""

from src.camera import Camera


def main():
    print("Camera check started.")
    camera = Camera()
    try:
        frame = camera.capture()
        path = camera.save_frame(frame, "check_camera.jpg")
        print(f"Image saved to: {path}")
    finally:
        camera.release()


if __name__ == "__main__":
    main()

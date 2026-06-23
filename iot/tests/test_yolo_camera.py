# coding: utf-8
"""
カメラ映像に対する YOLO 推論テスト。

カメラから取得した画像に対して person / car を検知する。

参照:
- IES6 "Deployment of an Object Detection Model"
- Ultralytics Python usage: https://docs.ultralytics.com/usage/python/#predict
- OpenCV VideoCapture tutorial: https://docs.opencv.org/4.x/dd/d43/tutorial_py_video_display.html
"""

import cv2

from src.camera import Camera
from src.yolo_detector import YOLODetector


def main():
    print("Testing YOLO on camera feed...")
    camera = Camera()
    detector = YOLODetector()
    try:
        frame = camera.capture()
        detections = detector.detect(frame)
        for det in detections:
            print(f"{det.label}: {det.confidence:.2f} at ({det.x}, {det.y}, {det.width}, {det.height})")

        # 検知結果を描画して保存
        for det in detections:
            cv2.rectangle(frame, (det.x, det.y), (det.x + det.width, det.y + det.height), (0, 255, 0), 2)
        path = camera.save_frame(frame, "test_yolo_camera.jpg")
        print(f"Annotated image saved to: {path}")
    finally:
        camera.release()


if __name__ == "__main__":
    main()

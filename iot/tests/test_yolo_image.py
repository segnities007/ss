# coding: utf-8
"""
静止画に対する YOLO 推論テスト。

 models/best.pt と test.jpg を用意して実行する。

参照:
- IES6 "Deployment of an Object Detection Model"
- Ultralytics Python usage: https://docs.ultralytics.com/usage/python/#predict
- Ultralytics predict results: https://docs.ultralytics.com/modes/predict#boxes
"""

from pathlib import Path

import cv2

from src.yolo_detector import YOLODetector


def main():
    print("Testing YOLO on static image...")
    detector = YOLODetector()
    image_path = "test.jpg"
    if not Path(image_path).exists():
        print(f"Please prepare {image_path}")
        return

    frame = cv2.imread(image_path)
    detections = detector.detect(frame)
    for det in detections:
        print(f"{det.label}: {det.confidence:.2f} at ({det.x}, {det.y}, {det.width}, {det.height})")


if __name__ == "__main__":
    main()

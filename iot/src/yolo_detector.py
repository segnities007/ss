# coding: utf-8
"""
YOLO による物体検知モジュール。

参照:
- IES6 "Deployment of an Object Detection Model"
- YOLOv8 model specification: https://docs.ultralytics.com/models/yolov8/
- Ultralytics prediction API: https://docs.ultralytics.com/modes/predict/
- Ultralytics Boxes reference: https://docs.ultralytics.com/reference/engine/results/#ultralytics.engine.results.Boxes

models/yolov8n.pt はUltralytics公式配布のCOCO学習済み物体検出モデル。
COCOの80クラスから、config.pyで指定したpersonとcarだけを抽出する。
"""

from dataclasses import dataclass
from typing import List

import numpy as np

from src.config import YOLO_MODEL_PATH, YOLO_CLASSES, CONFIDENCE_THRESHOLD


@dataclass
class DetectionResult:
    """YOLO 検知結果を表すデータクラス。"""
    label: str
    confidence: float
    x: int
    y: int
    width: int
    height: int


class YOLODetector:
    """YOLO モデルを使って画像内の物体を検知するクラス。"""

    def __init__(self, model_path: str = YOLO_MODEL_PATH):
        # 遅延インポートにより、モデルファイルがなくてもモジュール読み込みは可能
        # YOLOクラスの公式使用例:
        # https://docs.ultralytics.com/models/yolov8/#yolov8-usage-examples
        from ultralytics import YOLO
        self.model = YOLO(model_path)

    def detect(self, frame: np.ndarray) -> List[DetectionResult]:
        """画像に対して推論を実行し、必要なクラスの検知結果を返す。"""
        # 推論APIとResults/Boxesの仕様:
        # https://docs.ultralytics.com/modes/predict/
        # https://docs.ultralytics.com/reference/engine/results/#ultralytics.engine.results.Boxes
        results = self.model(frame, conf=CONFIDENCE_THRESHOLD, verbose=False)
        detections: List[DetectionResult] = []

        for result in results:
            boxes = result.boxes
            if boxes is None:
                continue
            for box in boxes:
                cls_id = int(box.cls[0])
                label = result.names[cls_id]
                if label not in YOLO_CLASSES:
                    continue
                conf = float(box.conf[0])
                x1, y1, x2, y2 = box.xyxy[0].tolist()
                detections.append(DetectionResult(
                    label=label,
                    confidence=conf,
                    x=int(x1),
                    y=int(y1),
                    width=int(x2 - x1),
                    height=int(y2 - y1),
                ))

        return detections

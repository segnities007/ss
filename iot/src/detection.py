# coding: utf-8
"""
YOLOの検知結果を表すデータ構造。

参照:
- Python dataclasses: https://docs.python.org/3/library/dataclasses.html
"""

from dataclasses import dataclass


@dataclass(frozen=True)
class DetectionResult:
    """画像内で検知した1つの物体。"""

    label: str
    confidence: float
    x: int
    y: int
    width: int
    height: int

    @property
    def center(self) -> tuple[float, float]:
        """バウンディングボックスの中心座標を返す。"""
        return self.x + self.width / 2, self.y + self.height / 2

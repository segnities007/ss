# YOLO model source

`yolov8n.pt`は、Ultralytics公式配布のCOCO学習済みYOLOv8 Nano物体検出モデルです。独自学習モデルではありません。

- 用途: COCOの80クラスから`person`と`car`を検出
- モデル仕様: https://docs.ultralytics.com/models/yolov8/
- 配布元: https://github.com/ultralytics/assets/releases/download/v8.4.0/yolov8n.pt
- ライセンス: https://www.ultralytics.com/license
- SHA-256: `f59b3d833e2ff32e194b5bb8e08d211dc7c5bdf144b90d2c8412c47ccfc83b36`

再取得:

```bash
curl -L \
  https://github.com/ultralytics/assets/releases/download/v8.4.0/yolov8n.pt \
  -o models/yolov8n.pt
```

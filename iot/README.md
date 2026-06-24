# IoT (Raspberry Pi)

Raspberry Pi 上で動作する不審者・不審車両検知システムのプログラム。

## ディレクトリ構成

```
iot/
├── src/              # 本番モジュール
├── tests/            # 動作確認スクリプト
├── analog_checks/    # 各部品のアナログ動作確認
├── data/             # 撮影画像の保存先
├── models/           # YOLO モデルの保存先
└── README.md
```

## 必要な環境

- Raspberry Pi 5
- Python 3.11+
- gpiozero
- OpenCV (`python3-opencv`)
- Ultralytics (`ultralytics`)
- requests
- NumPy

## インストール

```bash
cd /home/pi/ss/iot
pip3 install gpiozero opencv-python ultralytics requests numpy
```

## 設定

`src/config.py` を編集して、server 接続先や判定閾値を調整してください。

```python
SERVER_BASE_URL = "http://192.168.1.100:8080"
PERSON_DETECTION_THRESHOLD_SECONDS = 60.0
PERSON_CAPTURE_INTERVAL_SECONDS = 2.0
PERSON_LOST_GRACE_SECONDS = 4.0
SUSPICIOUS_VEHICLE_THRESHOLD_SECONDS = 300.0
```

## YOLOモデル

Ultralytics公式のCOCO学習済み`yolov8n.pt`を使用します。COCOの80クラスには、本システムで使用する`person`と`car`が含まれます。

モデルを再取得する場合:

```bash
curl -L \
  https://github.com/ultralytics/assets/releases/download/v8.4.0/yolov8n.pt \
  -o models/yolov8n.pt
```

- モデル仕様: https://docs.ultralytics.com/models/yolov8/
- 公式配布元: https://github.com/ultralytics/assets/releases
- ライセンス: https://www.ultralytics.com/license

## アナログ動作確認

実際のハードウェアを接続した上で、各部品が単体で動作するか確認してください。

```bash
# PIR センサー
python3 -m analog_checks.check_pir

# USB カメラ
python3 -m analog_checks.check_camera_capture

# 圧電ブザー
python3 -m analog_checks.check_buzzer_pwm
```

## 統合テスト

```bash
# 人物・車両の判定ロジック（ハードウェア不要）
python3 -m unittest tests.test_tracking_logic -v

# YOLO 静止画推論
python3 -m tests.test_yolo_image

# YOLO カメラ推論
python3 -m tests.test_yolo_camera

# Server 送信
python3 -m tests.test_server_send

# 統合フロー
python3 -m tests.test_full_flow
```

## 本番監視の起動

```bash
python3 -m src.monitor
```

`Ctrl+C` で停止します。
待機中も5秒ごとに`Monitoring active`が表示され、PIR状態と次の車両確認までの秒数を確認できます。

- personは60秒以上連続して検知した場合、1イベントにつき1回だけブザーとServer通知を実行します。
- YOLOがpersonを一時的に見失っても、4秒以内なら同じイベントとして継続します。
- carは同じ場所に5分以上いる場合、1追跡につき1回だけServerへ通知します。carではブザーを鳴らしません。

## 講義資料との対応

| 機能 | 使用技術 | 講義資料 |
|------|---------|---------|
| GPIO 入出力 | `gpiozero` | IES3 |
| PWM 制御 | `gpiozero.Buzzer` | IES4 |
| USB カメラ | `cv2.VideoCapture` | IES5 |
| 画像処理 | `OpenCV` | IES5 |
| 物体検出 | `ultralytics.YOLO` | IES6 |
| PIR センサー | `gpiozero.MotionSensor` | IES7 Tips |
| スレッド | `threading` | IES7 Tips |
| スケジューラ | `cron` | IES7 Tips |

## 講義資料外の参照

- `requests` multipart upload: https://requests.readthedocs.io/en/latest/user/quickstart/#post-a-multipart-encoded-file
- `requests` timeout: https://requests.readthedocs.io/en/latest/user/quickstart/#timeouts
- `pathlib.Path.mkdir` (パス操作): https://docs.python.org/3/library/pathlib.html#pathlib.Path.mkdir
- `datetime.datetime.now` (日時処理): https://docs.python.org/3/library/datetime.html#datetime.datetime.now

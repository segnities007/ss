# Mobile Application

PC Serverに保存された検知履歴の表示と、Raspberry Pi監視の開始・停止を行うAndroid application。

## 画面

- 履歴: 検知画像、種別、日時、信頼度を表示する。
- Dashboard: Raspberry Piの接続・監視状態を表示し、監視全体、ブザー、USB camera、PIR motion sensorの有効・無効を切り替える。

画面は下部のNavigationBarから切り替える。

## Build

```bash
./gradlew assembleDebug
```

PC Serverの接続先は`RetrofitInstance.kt`の`BASE_URL`で設定する。

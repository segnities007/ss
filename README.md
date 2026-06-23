# What is this

- このプロダクトは、学校の課題のための提出物です。


## 全体構成

- client
  - モバイルアプリで使用するコードの置き場
- server
  - laptopで使用するコードの置き場
- iot
  - ラズパイ等で用いるコードの置き場

## セットアップ方法

### 1. PC Server を起動する

```bash
sudo tailscale up
cd server
./gradlew run
```

### 2. Android 実機から確認する

```text
http://...
```

以下が返れば、Android実機から PC Server への通信は成功。

```json
{"status":"ok"}
```

### 3. Raspberry Pi から確認する

```bash
curl http://...
```

以下が返れば、Raspberry Pi から PC Server への通信は成功。

```json
{"status":"ok"}
```

### 4. Tailscale の接続を切る

```bash
sudo tailscale down
```

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

このプロジェクトでは、PC Server、Android実機、Raspberry Pi を同じ Tailscale の Tailnet に接続して通信する。
LAN IP や大学内ネットワークの違いに依存しないように、Server の接続先には Tailscale の MagicDNS 名を使う。

PC名やTailnetが変わった場合は、PC側で以下を実行して Tailscale IP または DNS 名を確認する。

```bash
tailscale status --self
tailscale ip -4
```

### 1. PC Server を起動する

```bash
cd server
./gradlew run
```

起動後、PC上で以下を開き、Server が動作していることを確認する。

```text
http://...
```

### 2. Android 実機から確認する

Android に Tailscale を入れ、PCと同じTailnetに接続する。
その後、Androidのブラウザで以下を開く。

```text
http://...
```

以下が返れば、Android実機から PC Server への通信は成功。

```json
{"status":"ok"}
```

Androidアプリ側の接続先は `client/app/src/main/java/com/segnities007/client/network/RetrofitInstance.kt` の `BASE_URL` で指定する。

```kotlin
const val BASE_URL = "http://archlinux.tail1dcb8b.ts.net:8080/"
```

### 3. Raspberry Pi から確認する

Raspberry Pi に Tailscale を入れ、PCと同じTailnetに接続する。
その後、Raspberry Pi 上で以下を実行する。

```bash
curl http://archlinux.tail1dcb8b.ts.net:8080/api/health
```

以下が返れば、Raspberry Pi から PC Server への通信は成功。

```json
{"status":"ok"}
```

IoT側の接続先は `iot/src/config.py` の `SERVER_BASE_URL` で指定する。

```python
SERVER_BASE_URL = "http://archlinux.tail1dcb8b.ts.net:8080"
```

### 4. Tailscale の接続を切る

PC または Raspberry Pi で一時的に切断する場合。

```bash
sudo tailscale down
```

再接続する場合。

```bash
sudo tailscale up
```

Androidでは Tailscale アプリを開き、上部のスイッチを OFF にする。

完全にログアウトする場合。

```bash
sudo tailscale logout
```

`logout` を実行すると、次回接続時に再認証が必要になる。通常は `down` または Android アプリの OFF でよい。

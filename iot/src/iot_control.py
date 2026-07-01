# coding: utf-8
"""
PC Serverから監視制御を取得するバックグラウンド処理。

Server通信中もPIR確認と人物・車両追跡を止めないため、制御同期は
監視ループとは別のthreadで実行する。

参照: IES7 Tips "Threading"
"""

import threading

from src.config import IOT_CONTROL_SYNC_INTERVAL_SECONDS
from src.server_client import IoTControlCommand, ServerClient


class IoTControlPoller:
    """IoTの稼働状態を通知し、要求された監視状態を保持する。"""

    def __init__(
        self,
        client: ServerClient,
        interval_seconds: float = IOT_CONTROL_SYNC_INTERVAL_SECONDS,
    ):
        self.client = client
        self.interval_seconds = interval_seconds
        self._lock = threading.Lock()
        self._stop_event = threading.Event()
        self._thread: threading.Thread | None = None
        self._monitoring_active = True
        self._command = IoTControlCommand()
        self._last_error: str | None = None

    def start(self):
        if self._thread is not None:
            return
        self._thread = threading.Thread(
            target=self._run,
            name="iot-control-poller",
            daemon=True,
        )
        self._thread.start()

    def stop(self):
        self._stop_event.set()
        if self._thread is not None:
            self._thread.join(timeout=1.0)

    def set_monitoring_active(self, active: bool):
        with self._lock:
            self._monitoring_active = active

    @property
    def command(self) -> IoTControlCommand:
        with self._lock:
            return self._command

    @property
    def last_error(self) -> str | None:
        with self._lock:
            return self._last_error

    def _run(self):
        while not self._stop_event.is_set():
            with self._lock:
                active = self._monitoring_active
            try:
                command = self.client.sync_monitoring_control(active)
                with self._lock:
                    self._command = command
                    self._last_error = None
            except Exception as error:
                with self._lock:
                    self._last_error = str(error)

            self._stop_event.wait(self.interval_seconds)

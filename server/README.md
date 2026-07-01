# server

This project was created using the [Ktor Project Generator](https://start.ktor.io).

Here are some useful links to get you started:

* [Ktor Documentation](https://ktor.io/docs/home.html)
* [Ktor GitHub page](https://github.com/ktorio/ktor)
* [Ktor Slack chat](https://app.slack.com/client/T09229ZC6/C0A974TJ9). [Request an invite](https://surveys.jetbrains.com/s3/kotlin-slack-sign-up).

## Features

Detection metadata is stored in a local SQLite database. Detection images are stored in the local `uploads/` directory.

The server also relays monitoring control between the Mobile Application and Raspberry Pi.

## IoT Control API

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/iot/control` | Read requested and actual monitoring status |
| `PUT` | `/api/iot/control` | Start or stop monitoring from the Mobile Application |
| `PUT` | `/api/iot/settings` | Enable or disable the buzzer, camera, and PIR sensor |
| `POST` | `/api/iot/heartbeat` | Receive Raspberry Pi status and return requested control state |

The Raspberry Pi is considered offline when no heartbeat is received for 15 seconds. The requested state is held in memory and defaults to monitoring enabled when the server starts.

Here's a list of features included in this project:

| Name                                                                                  | Description                                                                        |
|---------------------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| [Content Negotiation](https://start.ktor.io/p/io.ktor/server-content-negotiation)     | Provides automatic content conversion according to Content-Type and Accept headers |
| [kotlinx.serialization](https://start.ktor.io/p/io.ktor/server-kotlinx-serialization) | Handles JSON serialization using kotlinx.serialization library                     |
| [Exposed](https://start.ktor.io/p/org.jetbrains/server-exposed)                       | Adds Exposed database to your application                                          |
| [Koin](https://start.ktor.io/p/io.insert-koin/server-koin)                            | Provides dependency injection                                                      |

## Building & Running

To build or run the project, use one of the following tasks:

| Task              | Description       |
|-------------------|-------------------|
| `./gradlew test`  | Run the tests     |
| `./gradlew build` | Build the project |
| `./gradlew run`   | Run the server    |

If the server starts successfully, you'll see the following output:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```

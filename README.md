# JDialer

JDialer is a free and open-source Android dialer that prioritizes privacy while delivering a modern, Material 3 UI. It includes core phone, contacts, and in-call experiences plus an optional floating bubble so you can keep the dialer accessible while multitasking. JDialer is designed to be telemetry-free and to keep sensitive data on device.

## What you can do
- **Dial and manage calls** with dedicated dialer and in-call activities optimized for phone use.
- **Keep the dialer on top** using a floating bubble foreground service for quick access.
- **Manage contacts** with dedicated detail and add/edit screens.
- **Use home screen widgets** for favorites and quick dial actions.
- **Toggle the bubble quickly** via a Quick Settings tile.
- **Call screening hook** is wired into the system so future screening logic can be added (currently pass-through).
- **Call recording service hook** is present for microphone-based recording flows.

## Privacy-first by default
JDialer is built as a local-first dialer. It does not include analytics or telemetry SDKs, and its default architecture favors on-device data storage and processing.

## Requirements
- **Android 7.0 (API 24)** or higher (minSdk = 24).
- **Telephony hardware** is required; camera, NFC, and Bluetooth are optional features.

## Permissions overview
JDialer requests the standard Android permissions needed for a full-featured dialer experience. Highlights include:
- **Call handling**: placing/answering calls, reading phone state, and call logs.
- **Contacts**: read/write contacts for dialer and contact management.
- **Floating bubble**: overlay + foreground service permissions to keep the bubble visible.
- **Optional capabilities**: microphone (recording), notifications, biometrics, storage, Bluetooth, NFC, and camera for supported flows.

## Tech stack
- **Kotlin + AndroidX** with Material 3 UI components.
- **MVVM + Clean Architecture foundations** (ViewModel, LiveData, Room, DataStore).
- **Dependency injection** with Koin and async work via coroutines + WorkManager.
- **Networking** with OkHttp + Retrofit (reserved for optional services).
- **Compose** enabled alongside view binding for modern UI screens.

## Build
```bash
./gradlew assembleDebug
```

## License
GPLv3

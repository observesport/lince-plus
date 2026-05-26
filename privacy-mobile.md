---
layout: default
title: Lince Plus iOS — Privacy Policy
permalink: /privacy-mobile/
---

# Lince Plus iOS — Privacy Policy

**Last updated:** 2026-05-26

This privacy policy applies to the **Lince Plus** iOS application ("the App") developed by Alberto Soto Fernández / ObserveSport ("we", "us"). It explains what data the App accesses, how it is used, and where it is stored.

If you have questions, contact **alberto.soto@gmail.com**.

---

## TL;DR

- The App runs entirely **on your device**.
- We **do not collect, transmit, or share** any personal data.
- We **do not use analytics, advertising, tracking, or third-party SDKs** that send data off-device.
- All recordings, sensor data, and settings stay on your iPhone unless **you** explicitly export or share them.

---

## What the App accesses

| iOS Permission | Why | What we do with it |
|---|---|---|
| **Bluetooth** (`NSBluetoothAlwaysUsageDescription`, `NSBluetoothPeripheralUsageDescription`) | To pair and stream data from Movesense BLE sensors | Used in-memory and written to local files. Never transmitted off-device. |
| **Camera** (`NSCameraUsageDescription`) | To record video synchronized with sensor data | Saved as `.mov` files in the App's private Documents folder on your device. |
| **Microphone** (`NSMicrophoneUsageDescription`) | To record audio alongside video | Stored inside the same `.mov` file as the video. |
| **Background Bluetooth** (`UIBackgroundModes: bluetooth-central`) | To keep the sensor connection alive when the screen locks during a recording | No data is transmitted; only the BLE link is maintained. |

The App **does not** access: contacts, photos library, location, calendar, reminders, health data, motion data (other than from the BLE sensor), or any other iOS data source.

---

## What data is created and stored

When you record a session, the App stores the following **locally on your device**, inside the App's private Documents directory (`Documents/lince-recordings/`):

- **Video file** (`.mov`) — the camera and microphone recording, if you selected a mode that includes video.
- **Sensor data file** (`.jsonl`) — one JSON line per sample, containing timestamps and sensor values (heart rate, accelerometer, gyroscope, IMU).
- **Recording metadata** (`.meta.json`) — recording mode, duration, sensor identifiers, and creation timestamp.

These files are sandboxed by iOS to the App and are **not accessible** to other apps unless you explicitly share or export them.

App settings (e.g. paired device identifiers, sample rate preferences) are stored in iOS `UserDefaults`, which is also sandboxed to the App.

---

## What we do NOT do

- We do **not** upload your recordings, sensor data, or settings to any server.
- We do **not** use analytics services (no Firebase, no Crashlytics, no third-party SDKs).
- We do **not** display advertisements.
- We do **not** track your usage, behavior, or device identifiers.
- We do **not** sell, rent, or share any data with third parties.
- We do **not** include any social-network SDKs.

The App contains no networking code other than what is required by iOS to communicate with the Movesense BLE sensor over Bluetooth.

---

## Third-party components

The App uses the following open-source / vendor libraries, all of which run locally on the device and do not transmit data:

- **Movesense Mobile Library** (Movesense / Suunto) — for BLE communication with Movesense sensors. No off-device transmission.
- **iOSDFULibrary**, **ZIPFoundation** — for optional sensor firmware updates and file packaging. Local operations only.

---

## Children's privacy

The App is not directed at children under 13 and does not knowingly collect any data from children.

---

## Your control over your data

Because all data stays on your device:

- **To delete a recording**: use the in-app Recordings screen and tap delete.
- **To delete all data**: uninstall the App. iOS automatically removes the App's private storage.
- **To export a recording**: use the in-app share action — the destination (Files, email, AirDrop, etc.) is entirely your choice.

We have no ability to access, recover, or delete your data remotely because we never receive it.

---

## Changes to this policy

If we change how the App handles data in a future version, we will update this page and indicate the new "Last updated" date. Material changes will be noted in the App's release notes on the App Store.

---

## Contact

Alberto Soto Fernández
Email: **alberto.soto@gmail.com**
Project: [https://observesport.github.io/lince-plus/](https://observesport.github.io/lince-plus/)
Source: [https://github.com/observesport/lince-plus](https://github.com/observesport/lince-plus)

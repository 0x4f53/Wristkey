[![Wear OS](https://img.shields.io/badge/Made%20for-WearOS-4285f4.svg?style=flat&logo=wear%20os)](https://wearos.google.com/)
[![License](https://img.shields.io/badge/License-MIT-purple)](LICENSE)
[![Maintenance](https://img.shields.io/badge/Maintained-Yes-green.svg)](https://gitlab.com/ThomasCat/wristkey/activity)
[![Kotlin](https://img.shields.io/badge/Made%20with-Kotlin-7f52ff.svg)](https://kotlinlang.org/)
[![Download APK](https://img.shields.io/badge/Preview-Click%20Here!-blue)](app/build/outputs/apk/debug/app-debug.apk)

# Wristkey

A standalone two-factor authentication application for Wear OS. This application is for the de-Googlers and custom ROM-ers out there.

## Usage

### Bitwarden import

1. Download your Bitwarden Vault in an unencrypted JSON format from the Desktop website.

2. Enable ADB Debugging by going to Settings → System → About and tapping 'Build Number' 7 times.

3. Go back and tap on Developer Settings, then scroll down and enable "ADB Debugging".

4. Connect your watch to your computer. If prompted on watch, tap 'Always allow from this computer'.

4. Open a terminal on your computer and push it to the main directory of your watch via the following command

    ```adb push <bitwarden json filename> /sdcard/```

5. Tap the settings button '⚙️', then scroll down and tap *Import from Bitwarden*.

***Note:** Make sure to give Wristkey storage permissions from the Settings app on your watch.*

### Manual entry

1. Tap the '+' button to manually add a login. The default settings are for Google Authenticator codes (SHA-1, 6 digits, time-based).

2. Scroll down and tap the tick button '✓' at the at the bottom when done.

## Troubleshooting

### Wrong TOTP codes are shown

If the wrong codes are being shown, then your watch may have the time set incorrectly. Please set the time by pairing it to a phone.

### File import not working

Make sure Wristkey has storage permissions in your watch's Settings app. Make sure the file you download from your Bitwarden account is an **Unencrypted** file in **JSON** format (Encrypted JSON and Encrypted / Unencrypted CSV files don't work).

[Grab the latest APK](https://gitlab.com/ThomasCat/wristkey/-/raw/master/app/release/app-release.apk)

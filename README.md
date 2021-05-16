[![Wear OS](https://img.shields.io/badge/Made%20for-WearOS-4285f4.svg?style=flat&logo=wear%20os)](https://wearos.google.com/)
[![License](https://img.shields.io/badge/License-MIT-purple)](LICENSE)
[![Maintenance](https://img.shields.io/badge/Maintained-Yes-green.svg)](https://gitlab.com/ThomasCat/wristkey/activity)
[![Kotlin](https://img.shields.io/badge/Made%20with-Kotlin-7f52ff.svg)](https://kotlinlang.org/)
[![Download APK](https://img.shields.io/badge/Download%20APK-Click%20Here!-blue)](app/release/app-release.apk)

# Wristkey

A standalone two-factor authentication application for Wear OS. This application is for the de-Googlers and custom ROM-ers out there.

[Get the latest release APK here](app/release/app-release.apk)

<img src = screenshots/home.png alt="screenshot">

## Usage

### Bitwarden import

<img src = screenshots/import.png alt="screenshot">

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

<img src = screenshots/add.png alt="screenshot">
<img src = screenshots/add2.png alt="screenshot">

### Generate QR Code

To transfer a code from your watch to the Authenticator app on your phone, just press and hold the 2FA code number on your watch. You can then scan the QR code that is displayed on your phone's Authenticator app.

***Tip:** Tap the QR Code to dim it for better scanning.*

<img src = screenshots/qr.png alt="screenshot">
<img src = screenshots/qr2.png alt="screenshot">

## Troubleshooting

### Wrong TOTP codes are shown

If the wrong codes are being shown, then your watch may have the time set incorrectly. Please set the time by pairing it to a phone.

### File import not working

Make sure Wristkey has storage permissions in your watch's Settings app. Make sure the file you download from your Bitwarden account is an **Unencrypted** file in **JSON** format (Encrypted JSON and Encrypted / Unencrypted CSV files don't work).

[![Wear OS](https://img.shields.io/badge/Made%20for-WearOS-4285f4.svg?style=flat&logo=wear%20os)](https://wearos.google.com/)
[![License](https://img.shields.io/badge/License-MIT-purple)](LICENSE)
[![Maintenance](https://img.shields.io/badge/Maintained-Yes-green.svg)](https://gitlab.com/ThomasCat/wristkey/activity)
[![Version](https://img.shields.io/badge/Version-1.2-orange.svg)](https://gitlab.com/ThomasCat/wristkey/-/releases)
[![Kotlin](https://img.shields.io/badge/Made%20with-Kotlin-7f52ff.svg)](https://kotlinlang.org/)
[![Download APK](https://img.shields.io/badge/Download%20APK-Click%20Here!-blue)](app/release/app-release.apk)

# Wristkey

Wristkey is a sideloadable / standalone two-factor authentication application for Wear OS. I made this app as a fun project (and because the LG G Watch (W100) I wear doesn't support internet access when paired with iOS).

[Get the latest release APK here](app/release/app-release.apk)

<img src = screenshots/home.png alt="screenshot">
<img src = screenshots/settings.png alt="screenshot">

## Importing

Wristkey supports both Bitwarden and Google Authenticator importing for ease-of-use, though the procedures differ slightly for each. For Bitwarden, a JSON file is parsed and the ```totp``` field is extracted from each login. For Google Authenticator, a QR code image file is scanned, the base64 string in it is converted from Google's protobuf format and the ```secret``` field is extracted from ```OtpParameters```.

<img src = screenshots/settings2.png alt="screenshot">

### Enable ADB Debugging

1. Enable ADB Debugging by going to Settings → System → About and tapping 'Build Number' 7 times.

2. Go back and tap on Developer Settings, then scroll down and enable "ADB Debugging".

3. Connect your watch to your computer. If prompted on watch, tap 'Always allow from this computer'.

4. Make sure Wristkey has storage permissions. On your watch, go to Settings → Apps & notifications → App info → Wristkey → Permissions and enable 'Storage'.

### Bitwarden import

<img src = screenshots/bitwardenimport.png alt="screenshot">

1. Download your Bitwarden Vault in an unencrypted JSON format from the Bitwarden Desktop website.

2. Open a terminal on your computer and place this JSON file on the main directory of your watch (/sdcard/) via the following command

    ```adb push <bitwarden json filename> /sdcard/```

3. On your watch, open Wristkey, tap the settings icon '⚙️', then scroll down and tap *Import from Bitwarden*.

4. After your accounts are imported, delete the JSON file from your watch via the following commands

    ```
    adb shell
    cd /sdcard/
    rm <bitwarden filename>.json
    exit
    ```

### Google Authenticator import

<img src = screenshots/authenticatorimport.png alt="screenshot">

1. Tap the three dots on the top right corner in the Google Authenticator app, then tap on 'Export accounts'.

2. Select the accounts you\'d like to export and tap the export button 'Export'.

3. Take a screenshot of the QR code that is displayed.

4. place this screenshot on the main folder of your watch (/sdcard/) via ADB.

5. Open a terminal on your computer and place this screenshot PNG file on the main directory of your watch (/sdcard/) via the following command

    ```adb push <screenshot filename>.png /sdcard/```

6. On your watch, open Wristkey, tap the settings icon '⚙️', then scroll down and tap *Import from Authenticator*.

4. After your accounts are imported, delete the PNG file from your watch via the following commands

    ```
    adb shell
    cd /sdcard/
    rm <screenshot filename>.png
    exit
    ```

## Manual entry

1. Tap the '+' button to manually add a login. The default settings are for Google Authenticator codes (SHA-1, 6 digits, time-based).

2. Scroll down and tap the tick button '✓' at the at the bottom when done.

<img src = screenshots/add.png alt="screenshot">
<img src = screenshots/add2.png alt="screenshot">

### Generate QR Code

To transfer a code from your watch to the Authenticator app on your phone, just press and hold the 2FA code number on your watch. You can then scan the QR code that is displayed on your watch screen.

***Tip:** Tap the QR Code to dim it for better scanning.*

<img src = screenshots/qr.png alt="screenshot">
<img src = screenshots/qr2.png alt="screenshot">

## Troubleshooting

### Wrong TOTP codes are shown

If the wrong codes are being shown, your watch may have the time set incorrectly. Please set the time by pairing it to a phone.

### File import not working

Make sure Wristkey has storage permissions in your watch's Settings app. Make sure the file you download from your Bitwarden account is an **Unencrypted** file in **JSON** format (Encrypted JSON and Encrypted / Unencrypted CSV files don't work). If importing from Authenticator, make sure the screenshot is in **PNG** format.

## Security

### Importing files

To prevent data extraction, snooping and theft, make sure you delete the JSON and PNG files from your watch's storage once you're done importing them. You can confirm this by connecting your watch via ADB and running the ```adb shell ls /sdcard/``` command.

### In-app storage

All sensitive data on Wristkey (including the secrets to generate OTPs) is encrypted with 256-bit AES-GCM encryption, with the keys coming from your watch's Keystore. No backdoor on my end.  ;)

## Acknowledgements

### Libraries

Please star and support the developers below for their hard work.

[Marcel Kliemannel - Kotlin Onetimepassword (to generate OTPs)](https://github.com/marcelkliemannel/kotlin-onetimepassword)

[Androidmads - QR Generator library (to generate QR Codes)](https://github.com/androidmads/QRGenerator)

[Zxing (to scan QR codes for Google Authenticator imports)](https://github.com/zxing/zxing)

[GSON (to parse data)](https://github.com/google/gson)

[EncryptedSharedPreferences (to store data in an encrypted format securely)](https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-main/security/crypto/src/main/java/androidx/security/crypto/EncryptedSharedPreferences.java)

[Aegis (for the ```.protobuf``` file that made Google Authenticator imports possible)](https://github.com/beemdevelopment/Aegis/blob/master/app/src/main/proto/google_auth.proto)

### License

[Copyright (c) 2021 Owais Shaikh](LICENSE)
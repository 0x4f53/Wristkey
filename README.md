[![Wear OS](https://img.shields.io/badge/Made%20for-WearOS-4285f4.svg?style=flat&logo=wear%20os)](https://wearos.google.com)
[![License](https://img.shields.io/badge/License-MIT-purple)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Made%20with-Kotlin-7f52ff.svg)](https://kotlinlang.org/)
[![Maintenance](https://img.shields.io/badge/Maintained-Yes-green.svg)](https://gitlab.com/ThomasCat/wristkey/activity)
[![Tag](https://img.shields.io/github/v/tag/4f77616973/Wristkey?label=Version)](https://gitlab.com/ThomasCat/wristkey/tags)
[![Download APK](https://img.shields.io/badge/Download%20APK-Click%20Here!-blue)](app/release/app-release.apk)

# Wristkey

An offline two-factor authentication app for Wear OS.

[Get the latest release APK here](app/release/app-release.apk)

## What is this?

Wristkey is a completely offline, open-source, sideloadable and standalone two-factor authentication application for Wear OS watches. It does not need your watch to be paired to a phone to work. It support both square and round watches, customization, backups and more!

I made this app as a fun project and because the LG G Watch W100 I use doesn't support internet access when paired with iOS.

<img src = screenshots/home.png alt="screenshot"><img src = screenshots/settings.png alt="screenshot">

## Usage

### Adding items

Wristkey supports both Bitwarden and Google Authenticator importing for ease-of-use, though the procedures differ slightly for each. For Bitwarden, a JSON file is parsed and the ```totp``` field is extracted from each login. For Google Authenticator, a QR code image file is scanned, the base64 string in it is converted from Google's protobuf format and the ```secret``` field is extracted from ```OtpParameters```.

<img src = screenshots/addscreen.png alt="screenshot">

#### Transferring data

##### Via phone

If your watch is paired to an Android phone, you can use a third-party Wear OS file manager like [Nav Explorer](https://play.google.com/store/apps/details?id=com.turndapage.navexplorer) to transfer your PNG / JSON files from your phone's storage to your watch.

##### Via ADB

1. Enable ADB Debugging on your watch by going to Settings → System → About and tapping 'Build Number' 7 times.

2. Go back and tap on Developer Settings, then scroll down and enable "ADB Debugging".

3. Make sure Wristkey has storage permissions. On your watch, go to Settings → Apps & notifications → App info → Wristkey → Permissions and enable 'Storage'.

###### ADB over USB

1. Connect your watch to your computer via USB. When prompted on watch, tap 'Always allow from this computer'.

###### ADB over Bluetooth

1. Enable Bluetooth Debugging on your watch by going to Settings → System → About and tapping 'Build Number' 7 times.

2. Now on your Android phone / tablet, open the Wear OS app and tap the top right mennu button and tap on 'Settings'.

3. Enable 'Debugging over Bluetooth'. You’ll should see

    ```
    Host: disconnected
    Target: connected
    ```

4. Connect your Android phone / tablet to your computer via USB. Then open a new terminal and run the following commands

    ```
    adb forward tcp:4444 localabstract:/adb-hub
    adb connect localhost:4444
    ```
5. On your phone, the Wear OS app should display

    ```
    Host: connected
    Target: connected
    ```

#### Google Authenticator import

<img src = screenshots/authenticatorimport.png alt="screenshot">

1. Tap the three dots on the top right corner in the Google Authenticator app, then tap on 'Export accounts'.

2. Select the accounts you\'d like to export and tap the export button 'Export'.

3. Take a picture or screenshot of the QR code that is displayed. **Make sure it is a PNG or JPG file** and that it is clear with no blurring, glare or pixelation.

4. Open a terminal on your computer and place this PNG or JPG file on the main directory of your watch (/sdcard/) via the following command

    ```
    adb push <screenshot filename>.png /sdcard/
    ```

6. On your watch, open Wristkey, scroll down and tap the add icon '+', then tap *Import from Authenticator*.

4. After your accounts are imported, delete the PNG or JPG file from your watch via the following commands

    ```
    adb shell
    cd /sdcard/
    rm <screenshot filename>.png
    exit
    ```

#### Bitwarden import

<img src = screenshots/bitwardenimport.png alt="screenshot">

1. Download your Bitwarden Vault in an unencrypted JSON format from the Bitwarden Desktop website.

2. Open a terminal on your computer and place this JSON file on the main directory of your watch (/sdcard/) via the following command

    ```
    adb push <bitwarden json filename>.json /sdcard/
    ```

3. On your watch, open Wristkey, scroll down and tap the add icon '+', then tap *Import from Bitwarden*.

4. After your accounts are imported, delete the JSON file from your watch via the following commands

    ```
    adb shell
    cd /sdcard/
    rm <bitwarden filename>.json
    exit
    ```

#### Manual entry

1.  On your watch, open Wristkey, scroll down and tap the add icon '+', then tap *Manual Entry*. The default settings are for Google Authenticator codes (SHA-1, 6 digits, time-based).

2. Scroll down and tap the tick button '✓' at the at the bottom when done.

<img src = screenshots/add.png alt="screenshot"><img src = screenshots/add2.png alt="screenshot">

### Deleting items

To delete an item, tap and hold on its name. This was made difficult on purpose so that logins aren't accidentally deleted.

### Exporting

Since watches are tiny devices that can be misplaced, backing up and exporting your secrets and storing them in a safe place is always a a good idea.

#### Single account

<img src = screenshots/qr.png alt="screenshot"><img src = screenshots/qr2.png alt="screenshot">

To transfer a code from your watch to an Authenticator app on your phone, just press and hold the 2FA code number on your watch. You can then scan the QR code that is displayed on your watch screen.

***Tip:** Tap the QR Code to dim it for better scanning.**

#### All accounts

To backup all content, open Wristkey, tap the settings icon '⚙️', then scroll down and tap *Backup all data*.

##### Via QR code

Tap 'QR code' to get a (not compatible with Authenticator) QR Code data. **This QR code cannot be scanned in any 2FA application and is purely for extraction purposes.**

##### Via file

1. Tap 'File' to get your backups in the form of a file. If 'File' is selected, the data will be placed in ```/sdcard/wristkey/```.

2. Open a terminal on your computer and extract this file via the following command

    ```
    adb pull /sdcard/wristkey/
    adb shell rm /sdcard/wristkey/
    ```

***Note:** The data is unencrypted and must be handled with care. Delete it when not in use.*

## Troubleshooting

#### Wrong TOTP codes are shown

If the wrong codes are being shown, your watch may have the time set incorrectly. Please set the time by pairing it to a phone.

#### File import not working

Make sure Wristkey has storage permissions in your watch's Settings app. If importing from JSON, make sure the file you download from your Bitwarden account is an **Unencrypted** file in **JSON** format (Encrypted JSON and Encrypted / Unencrypted CSV files don't work). If importing from Authenticator, make sure the screenshot or picture is in **PNG or JPG** format and is clear.

#### File export not working

Make sure Wristkey has storage permissions in your watch's Settings app. If already enabled, disable and enable storage permissions again.

## Security

### Importing files

To prevent data extraction, snooping and theft, make sure you delete the JSON, PNG or JPG files from your watch's storage once you're done importing them. You can confirm the existence of items by connecting your watch via ADB and running the ```adb shell ls /sdcard/``` command.

### In-app storage

All sensitive data within Wristkey (including secrets to generate OTPs) is stored encrypted [using 256-bit AES encryption](https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences), with the decryption key [stored locally](https://developer.android.com/training/articles/keystore) on your watch. No backdoor on my end.  ;)

### Privacy

Wristkey can be set to unlock after entering your watch's password / PIN / Pattern. To enable screen locking for the app, go to your watch's Settings → Personalization → Screen Lock and set a PIN / Pattern / Password. To override this setting, open Wristkey, tap the settings icon '⚙️', then scroll down and disable *Screen locking*

Wristkey doesn't use Wear OS's Ambient Mode by default to prevent bystanders from peeking at your 2FA codes. To enable Ambient Mode, open Wristkey, tap the settings icon '⚙️', then scroll down and enable *Ambient mode*.

## Changelog

A detailed changelog is available on the [tags](https://gitlab.com/ThomasCat/wristkey/-/tags) page. The latest version is  
[![Tag](https://img.shields.io/github/v/tag/4f77616973/Wristkey?label="")](https://gitlab.com/ThomasCat/wristkey/tags)

## Acknowledgements

Please star and support these developers for their hard work. All libraries, dependencies and tools used belong to their respective owners.

[Marcel Kliemannel - Kotlin Onetimepassword (to generate OTPs)](https://github.com/marcelkliemannel/kotlin-onetimepassword)

[Androidmads - QR Generator library (to generate QR Codes)](https://github.com/androidmads/QRGenerator)

[Zxing (to scan QR codes for Google Authenticator imports)](https://github.com/zxing/zxing)

[GSON (to parse data)](https://github.com/google/gson)

[EncryptedSharedPreferences (to store data in an encrypted format securely)](https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-main/security/crypto/src/main/java/androidx/security/crypto/EncryptedSharedPreferences.java)

[Aegis (for the ```.protobuf``` file that made Google Authenticator imports possible)](https://github.com/beemdevelopment/Aegis/blob/master/app/src/main/proto/google_auth.proto)

[Roland Kurmann - extract_otp_secret_keys (to extract Google Authenticator data)](https://github.com/scito/extract_otp_secret_keys)

[Chaquo Ltd - Chaquopy (to run Python to decode protobuf3 data)](https://github.com/chaquo/chaquopy)

## License

[Copyright © 2021 Owais Shaikh](LICENSE) 

(GitLab: [ThomasCat](https://gitlab.com/thomascat) | GitHub: [4f77616973](https://github.com/4f77616973) | Email: [0x4f@tuta.io](mailto://0x4f@tuta.io))

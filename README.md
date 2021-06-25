[![Wear OS](https://img.shields.io/badge/Made%20for-WearOS%20-4285f4.svg?style=flat-square&logo=wear%20os)](https://wearos.google.com)
[![Kotlin](https://img.shields.io/badge/Made%20with-Kotlin-7f52ff.svg?style=flat-square&logo=kotlin)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-MIT-purple?style=flat-square&logo=libreoffice)](LICENSE)
[![Open Source?](https://img.shields.io/badge/Open%20Source-Yes-darkgreen?style=flat-square&logo=github)](https://opensource.org/)
[![Security Policy](https://img.shields.io/badge/Security-Click%20Here-orange.svg?style=flat-square&logo=bitwarden)](https://github.com/4f77616973/Wristkey/security/policy)
[![Latest Version](https://img.shields.io/github/v/tag/4f77616973/Wristkey?label=Version&style=flat-square&logo=semver)](https://github.com/4f77616973/Wristkey/tags)
[![Download APK](https://img.shields.io/badge/Download%20APK-Click%20Here!-blue?style=flat-square&logo=dropbox)](app/release/app-release.apk)

# Wristkey

<img src = "app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png" alt = "Wristkey logo" width = "75dp">

Need 2FA codes quickly, right on your Wear watch without needing a phone? Wristkey is an open-source 2FA client for Wear OS watches that does just that! Supports Android Wear 7.1 (Nougat) and above. [Get the APK here.](app/release/app-release.apk)

<img src = screenshots/preview.png alt="mockup">

## FEATURES

- Works without being paired to phone (standalone) and without internet connection.

- Direct import from Aegis Authenticator, andOTP, Bitwarden and Google Authenticator.

- Secure app by locking via PIN, pattern and password.

- Safe data storage using 256-bit encryption.

- Time and counter mode OTPs, upto SHA-512 and 4-8 digits.

- Supports round and square devices and customization via theming.

- Backup options via QR code and JSON.

## USAGE

### Adding items

Wristkey supports importing data from multiple sources for ease-of-use, though the procedures differ slightly for each of them. For example, for Bitwarden, a JSON file is parsed and the ```totp``` field is extracted from each account. For website QR codes, the QR Code is scanned and the resulting `otpauth://` URL is parsed.

<img src = screenshots/add-round-1.png alt="add">

#### Transferring data

##### Via phone

If your watch is paired to an Android phone, you can use a third-party Wear OS file manager like [myWear File Explorer](https://play.google.com/store/apps/details?id=com.mrs.wear_file_explorer) or [Nav Explorer](https://play.google.com/store/apps/details?id=com.turndapage.navexplorer) to transfer PNG / JSON files from your phone's storage to your watch.

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

#### Google Authenticator and normal QR Code imports

1. If using a QR Code from a website, save it as a screenshot and make sure it is clearly visible with no pixelation. If importing from Google Authenticator, tap the three dots on the top right corner, then tap on 'Export accounts'. Then select the accounts you\'d like to export and tap the export button 'Export'. Take a picture or screenshot of the QR code that is displayed and **make sure it is a PNG or JPG file** and is clear with no blurring, glare or pixelation.

2. Open a terminal on your computer and place this PNG or JPG file on the main directory of your watch (/sdcard/) via the following command

    ```
    adb push <filename>.png /sdcard/
    ```

3. On your watch, open Wristkey, scroll down and tap the add icon '+', then select your import option.

4. After your accounts are imported, delete the PNG or JPG file from your watch via the following commands

    ```
    adb shell
    cd /sdcard/
    rm <filename>.png
    exit
    ```

#### Aegis Authenticator, andOTP, Bitwarden and Wristkey backup imports

1. Export your data in an unencrypted JSON format. Make sure you don't rename the file. 

2. Open a terminal on your computer and place this JSON file on the main directory of your watch (/sdcard/). If using a Wristkey backup file, do **not** place it in the /Wristkey folder. Do this via the following command

    ```
    adb push <filename> /sdcard/
    ```

3. On your watch, open Wristkey, scroll down and tap the add icon '+', then select your import option.

4. After your accounts are imported, delete the JSON file from your watch via the following commands

    ```
    adb shell
    cd /sdcard/
    rm <filename>
    exit
    ```

#### Manual entry

1.  On your watch, open Wristkey, scroll down and tap the add icon '+', then tap *Manual Entry*. The default settings are for Google Authenticator codes (SHA-1, 6 digits, time-based).

2. Scroll down and tap the tick button '✓' at the at the bottom when done.

<img src = screenshots/manual-round-1.png alt="add"><img src = screenshots/manual-round-2.png alt="add">

### Editing and Deleting items

To edit or delete an item, tap and hold on its name. This was made difficult on purpose so that accounts aren't accidentally edited or deleted. To delete an item, scroll all the way to the bottom of the edit screen and tap the trash icon.

### Exporting

<img src = screenshots/export-round.png alt="export"><img src = screenshots/qr.png alt="qrcode">

Since watches are tiny devices that can be misplaced, backing up and exporting your secrets and storing them in a safe place is always a a good idea.

#### Single account

To transfer a code from your watch to an Authenticator app on your phone, just press and hold the 2FA code number on your watch. You can then scan the QR code that is displayed on your watch screen.

***Tip:** Tap the QR Code to dim it for better scanning.*

#### All accounts

To backup all content, open Wristkey, tap the settings icon '⚙️', then scroll down and tap *Backup all data*.

##### Via QR code

Tap 'QR code' to get a (not compatible with Authenticator) QR Code data. **This QR code cannot be scanned in any 2FA application and is purely for extraction purposes.**

##### Via file

1. Tap 'File' to get your backups in the form of a file. The data will be placed in ```/sdcard/wristkey/```.

2. Open a terminal on your computer and extract this file via the following command

    ```
    adb pull /sdcard/wristkey/
    ```
3. To delete the directory, type 

    ```
    adb shell rm /sdcard/wristkey/
    ```

***Note:** The exported data is unencrypted and must be handled with care. Delete it when not in use.*

## TROUBLESHOOTING

#### Wrong TOTP codes are shown

Make sure you set your secret key, digit length and algorithm correctly. If the displayed codes are still wrong, your watch may have the time set incorrectly. Please set the time by pairing it to a phone or connecting to WiFi.

#### File import not working

Make sure Wristkey has storage permissions in your watch's Settings app. If importing from JSON, make sure the file you export is an **Unencrypted** file in **JSON** format. If importing from Authenticator, make sure the screenshot or picture is in **PNG or JPG** format and is clear. If using a Wristkey backup file, make sure it has the _.backup_ extension.

#### File export not working

Make sure Wristkey has storage permissions in your watch's Settings app. If already enabled, disable and enable storage permissions again.

## SECURITY

_Further reading: [Security Policy](https://github.com/4f77616973/Wristkey/security/policy)_

### Importing files

To prevent data extraction, snooping and theft, make sure you delete the JSON, PNG or JPG files from your watch's storage once you're done importing them. You can confirm the existence of items by connecting your watch via ADB and running the ```adb shell ls /sdcard/``` command.

### In-app storage

All sensitive data within Wristkey (including secrets to generate OTPs) is stored encrypted [using 256-bit AES encryption](https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences), with the decryption key [stored locally](https://developer.android.com/training/articles/keystore) on your watch. No backdoor on my end.  ;)

### Privacy

Wristkey can be set to unlock after entering your watch's password / PIN / pattern. To enable screen locking for the app, go to your watch's Settings → Personalization → Screen Lock and set a PIN / pattern / password. To override this setting, open Wristkey, tap the settings icon '⚙️', then scroll down and disable *Screen locking*

Wristkey doesn't use Wear OS's Ambient Mode by default to prevent bystanders from peeking at your 2FA codes. To enable Ambient Mode, open Wristkey, tap the settings icon '⚙️', then scroll down and enable *Ambient mode*.

## CHANGELOG

A detailed changelog is available on the [releases](https://github.com/4f77616973/Wristkey/releases) page. The latest release is [![Release](https://img.shields.io/github/v/tag/4f77616973/Wristkey?style=plastic&label=)](https://github.com/4f77616973/Wristkey/releases).

## CONTRIBUTION

### Contributing

I made this app for myself because the LG G Watch W100 I use doesn't support internet access when paired with iOS and Google scrapped their Authenticator app from the Wear OS Play Store. However, anyone can contribute to this project. [Click here to read the rules](CONTRIBUTING.md) if you'd like to. 

### Code of Conduct

Make sure to adhere to the [code of conduct](CODE_OF_CONDUCT.md) when interacting with others on this project.

## ACKNOWLEDGEMENTS

The [Aegis Authenticator](https://getaegis.app/), [andOTP](https://github.com/andOTP/andOTP), [Google Authenticator](https://github.com/google/google-authenticator) and [Bitwarden](https://bitwarden.com/) names, data export formats, logos and trademarks belong to their respective owners.

Please star and support these developers for their hard work. All libraries, dependencies and tools used belong to their respective owners.

[Marcel Kliemannel - Kotlin Onetimepassword (to generate OTPs)](https://github.com/marcelkliemannel/kotlin-onetimepassword)

[Androidmads - QR Generator library (to generate QR Codes)](https://github.com/androidmads/QRGenerator)

[Zxing (to scan QR codes for Google Authenticator imports)](https://github.com/zxing/zxing)

[GSON (to parse data)](https://github.com/google/gson)

[EncryptedSharedPreferences (to store data in an encrypted format securely)](https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-main/security/crypto/src/main/java/androidx/security/crypto/EncryptedSharedPreferences.java)

[Roland Kurmann - extract_otp_secret_keys (to extract Google Authenticator data)](https://github.com/scito/extract_otp_secret_keys)

[Chaquo Ltd - Chaquopy (to run Python to decode protobuf3 data)](https://github.com/chaquo/chaquopy)

## LICENSE

Multimedia licensed under [![License: CC BY-NC-SA 4.0](https://licensebuttons.net/l/by-nc-sa/4.0/80x15.png)](https://creativecommons.org/licenses/by-nc-sa/4.0/) 

[Copyright © 2021 Owais Shaikh](LICENSE)

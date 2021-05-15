[![License](https://img.shields.io/badge/License-MIT-purple)](LICENSE)
[![Maintenance](https://img.shields.io/badge/Maintained-No-red.svg)](https://github.com/4f77616973/GetPerms/graphs/commit-activity)
[![Kotlin](https://img.shields.io/badge/Made%20with-Kotlin-7f52ff.svg)](https://kotlinlang.org/)
[![Preview](https://img.shields.io/badge/Preview-Click%20Here!-blue)](/app/build/outputs/apk/debug/app-debug.apk)

# Wristkey

A standalone two-factor authentication application for Wear OS. This application is for the de-Googlers and custom ROM-ers out there.

## Usage

### Bitwarden import

1. Download your Bitwarden Vault in an unencrypted JSON format from the Desktop website.

2. Enable ADB Debugging by going to Settings → System → About and tapping 'Build Number' 7 times.

3. Go back and tap on developer settings, then scroll down and enable "ADB Debugging".

4. Connect your watch to your computer. If prompted on watch, tap 'Always allow from this computer'.

4. Open a terminal on your computer and push it to the main directory of your watch via the following command

    ```adb push <bitwarden json filename> /sdcard/```

5. Tap the settings button '⚙️', then scroll down and tap *Import from Bitwarden*.

***Note:** Make sure to give Wristkey storage permissions from the Settings app on your watch.*

### Manual entry

1. Tap the '+' button to manually add a login. The default settings are for Google Authenticator codes (SHA-1, 6 digits, time-based).

2. Scroll down and tap the tick button '✓' at the at the bottom when done.

[Grab the latest APK](https://gitlab.com/ThomasCat/wristkey/-/raw/master/app/release/app-release.apk?inline=false)
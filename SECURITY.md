## SECURITY

### Current Security Features

Wristkey uses file encryption to secure your data, rather than storing it unencrypted. This means that data is stored in a scrambled form such that only the person with the key to decrypt it can view it. This is done by using [EncryptedSharedPreferences](https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences), the encrypted version of the popular SharedPreferences library in Android.

SharedPreferences (and all its variants) stores data in a key-value pair. Your account data is stored against a randomly-generated UUID4 key, with the value being a JSONObject of your account data, including usernames, secrets and parameters.

Both the key and value stored in EncryptedSharedPreferences are symmetrically encrypted using the [Advanced Encryption Standard (AES)](https://web.archive.org/web/20210622171351/https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.197.pdf). A 256-bit private key is generated using AES-GCM from the [MasterKey](https://developer.android.com/reference/androidx/security/crypto/MasterKey) class. The key is stored in the [Android Keystore](https://developer.android.com/training/articles/keystore), and the encrypted data is stored in the `/storage/emulated/0/Android/data/` directory. Both are stored locally on your watch and do not leave your watch, be it through WiFi, Bluetooth, USB or NFC. **†**

(**†** — _unless you specifically choose to do so via an unencrypted export._)

To prevent data extraction, snooping and theft, make sure you delete the already unencrypted JSON, PNG or JPG files from your watch storage once you're done importing. 

Wristkey can also be set to unlock after entering your watch's password / PIN / pattern. To enable screen locking for the app, go to your watch's Settings → Personalization → Screen Lock and set a PIN / pattern / password.

### Supported Versions

Below are supported Wristkey versions with active support from the developer.

| Version  | Supported          |
| -------  | ------------------ |
| v1.3.1   | :white_check_mark: |
| < v1.3.1 | :x:                |

### Reporting a Vulnerability

We take all security bugs in Wristkey seriously.
Thank you for improving the security of Wristkey. We appreciate your efforts and
responsible disclosure and will make every effort to acknowledge your
contributions.

Report security bugs by emailing the lead maintainer at 0x4f@tuta.io.

The lead maintainer should acknowledge your email within 48 hours, and will send a
more detailed response indicating the next steps in handling
your report. After the initial reply to your report, the security team will
endeavor to keep you informed of the progress towards a fix and full
announcement, and may ask for additional information or guidance.

Report security bugs in third-party modules to the person or team maintaining
the module.

### Comments on this Policy

If you have suggestions on how this process could be improved, [please submit a
pull request](https://github.com/4f77616973/Wristkey/pulls).

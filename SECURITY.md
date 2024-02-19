## SECURITY

### Current Security Features

Wristkey does not send any data to a server or backend. All 2FA data is stored locally.


#### Wi-Fi transfers
For Wi-Fi transfers, Wristkey starts two [HTTP servers](https://github.com/NanoHttpd/nanohttpd) (one on each device) on random ports on your local LAN (your Wi-Fi router connection, hotspot network etc). Devices are paired by scanning a QR code displayed on the receiving device. Data transfers are encrypted using public key encryption via [libsodium's crypto_box_seal()](https://libsodium.gitbook.io/doc/public-key_cryptography/sealed_boxes), which in turn uses XSalsa20-Poly1305 and X25519 key exchange. Data is encrypted on the sending device using the public key the receiver sends, and the receiver decrypts it using their secret key. Both HTTP servers are stopped as soon as the transfer is complete. 


#### On-device encryption
Wristkey uses file encryption to secure your data at rest, rather than storing it unencrypted. This means that data is stored in a scrambled form in a way that only the party with the key to decrypt it can view it, similar to a door lock. This is done by using [EncryptedSharedPreferences](https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences), the encrypted version of the popular [SharedPreferences](https://developer.android.com/training/data-storage/shared-preferences) library in Android.

SharedPreferences (and all its variants) stores data in a key-value pair. All your data is stored in an array of [otpauth URI](https://github.com/google/google-authenticator/wiki/Key-Uri-Format)s which includes your usernames, secrets and parameters. Using otpauth URIs in an array makes rearranging them easy.

Data in EncryptedSharedPreferences is symmetrically encrypted using the [Advanced Encryption Standard (AES)](https://web.archive.org/web/20210622171351/https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.197.pdf). A 256-bit private key is generated using AES-GCM from the [MasterKey](https://developer.android.com/reference/androidx/security/crypto/MasterKey) class. The key is stored in the [Android Keystore](https://developer.android.com/training/articles/keystore), and the encrypted data is stored in the `/data/user/0/` directory. Both the key and the ciphertext are stored locally and do not leave your watch, be it through WiFi, Bluetooth, USB or NFC (_unless you specifically choose to do so via an export_).

### Recommended Security Practices

To prevent data extraction, snooping, phishing and theft, make sure you delete any unencrypted JSON, PNG or JPG export files from your device storage once you're done importing / exporting them. Not doing so could lead to a compromising situation, such as social engineering attacks, or a thief bruteforcing their way into your online accounts by stealing your watch and trying to access the sensitive data on it via ADB.

Every time you switch to a major version of Wristkey, make sure to completely uninstall and reinstall it. This helps in keeping the local SharedPreferences data format up-to-date. To make this process easy, use the _Backup all data_ option present in the app, under the settings icon '⚙️'.

Use a password / PIN / pattern screen lock to prevent unauthorized access to your codes. This can be either via a single watch screen lock, or a combined app + screen lock for an added a layer of security. Avoid using Wear OS's Ambient Mode to prevent bystanders from peeking at your 2FA codes.

### Reporting a Vulnerability

Security bugs are no laughing matter.

Thank you for improving the security of Wristkey. I appreciate your efforts and
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

If you have suggestions on how this policy could be improved, [please submit a
feature request](https://github.com/4f77616973/Wristkey/issues).

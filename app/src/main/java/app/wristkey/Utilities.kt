package app.wristkey

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Point
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64.DEFAULT
import android.util.Base64.encodeToString
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import dev.turingcomplete.kotlinonetimepassword.HmacAlgorithm
import dev.turingcomplete.kotlinonetimepassword.HmacOneTimePasswordConfig
import dev.turingcomplete.kotlinonetimepassword.HmacOneTimePasswordGenerator
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordConfig
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordGenerator
import fi.iki.elonen.NanoHTTPD
import org.bouncycastle.crypto.agreement.X25519Agreement
import org.bouncycastle.crypto.generators.X25519KeyPairGenerator
import org.bouncycastle.crypto.params.X25519KeyGenerationParameters
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters
import org.bouncycastle.crypto.params.X25519PublicKeyParameters
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import wristkey.R
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.SecureRandom
import java.security.Security
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


@RequiresApi(Build.VERSION_CODES.M)
class Utilities (context: Context) {

    val FILES_REQUEST_CODE = 69
    val CAMERA_REQUEST_CODE = 420
    val EXPORT_RESPONSE_CODE = 69420

    val JSON_MIME_TYPE = "application/json"
    val JPG_MIME_TYPE = "image/jpeg"
    val PNG_MIME_TYPE = "image/png"

    val OTPAUTH_SCAN_CODE = "OTPAUTH_SCAN_CODE"
    val AUTHENTICATOR_EXPORT_SCAN_CODE = "AUTHENTICATOR_EXPORT_SCAN_CODE"
    val QR_CODE_SCAN_REQUEST = "QR_CODE_SCAN_REQUEST"

    val INTENT_WIFI_IP = "INTENT_WIFI_IP"

    val context = context

    val QR_TIMER_DURATION = 5

    val INTENT_QR_DATA = "INTENT_QR_DATA"
    val INTENT_QR_METADATA = "INTENT_QR_METADATA"
    val INTENT_WIPE = "INTENT_WIPE"
    val INTENT_EDIT = "INTENT_EDIT"
    val INTENT_DELETE_MODE = "INTENT_DELETE_MODE"

    val SETTINGS_BACKGROUND_COLOR = "SETTINGS_BACKGROUND_COLOR"
    val SETTINGS_ACCENT_COLOR = "SETTINGS_ACCENT_COLOR"

    val SETTINGS_SEARCH_ENABLED = "SETTINGS_SEARCH_ENABLED"
    val SETTINGS_CLOCK_ENABLED = "SETTINGS_CLOCK_ENABLED"
    val SETTINGS_24H_CLOCK_ENABLED = "SETTINGS_24H_CLOCK_ENABLED"
    val SETTINGS_HAPTICS_ENABLED = "SETTINGS_HAPTICS_ENABLED"
    val SETTINGS_BEEP_ENABLED = "SETTINGS_BEEP_ENABLED"
    val CONFIG_SCREEN_ROUND = "CONFIG_SCREEN_ROUND"
    val SETTINGS_LOCK_ENABLED = "SETTINGS_LOCK_ENABLED"

    val DATA_STORE = "DATA_STORE"

    val MFA_TIME_MODE = "totp"
    val MFA_COUNTER_MODE = "hotp"

    val ALGO_SHA1 = "SHA1"
    val ALGO_SHA256 = "SHA256"
    val ALGO_SHA512 = "SHA512"

    var masterKey: MasterKey
    private val accountsFilename: String = "vault.wfs" // WristkeyFS
    var db: SharedPreferences
    private val objectMapper: ObjectMapper

    init {
        masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyGenParameterSpec(
                KeyGenParameterSpec.Builder (
                    MasterKey.DEFAULT_MASTER_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(MasterKey.DEFAULT_AES_GCM_MASTER_KEY_SIZE)
                    .setKeySize(256)
                    .setDigests(KeyProperties.DIGEST_SHA512)
                    .build()
            )
            .setRequestStrongBoxBacked(true)
            .build()

        db = EncryptedSharedPreferences.create (
            context,
            accountsFilename,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        objectMapper = ObjectMapper()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
    }

    data class MfaCode (
        val mode: String,
        val issuer: String,
        val account: String,
        val secret: String,
        val algorithm: String,
        val digits: Int,
        val period: Int,
        val lock: Boolean,
        var counter: Long,
        val label: String,
    )

    fun isIp (string: String): Boolean {
        if (string.contains("0.0.") || string.contains("10.0.")) return false
        return """^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}:\d{1,5}$""".toRegex().matches(string)
    }
    fun hasCamera(): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }
    fun generateQrCode (qrData: String, windowManager: WindowManager): Bitmap? {
        val display = windowManager.defaultDisplay
        val point = Point()
        display.getSize(point)
        val width: Int = point.x + 150
        val height: Int = point.y + 150
        val dimensions = if (width < height) width else height

        val qrEncoder = QRGEncoder(qrData, null, QRGContents.Type.TEXT, dimensions)
        return qrEncoder.bitmap
    }
    @Suppress("DEPRECATION")
    fun wiFiExists(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.type == ConnectivityManager.TYPE_WIFI
        }
    }

    fun scanQRImage(bMap: Bitmap): String {
        var contents: String
        val intArray = IntArray(bMap.width * bMap.height)
        //copy pixel data from the Bitmap into the 'intArray' array
        bMap.getPixels(intArray, 0, bMap.width, 0, 0, bMap.width, bMap.height)
        val source: LuminanceSource = RGBLuminanceSource(bMap.width, bMap.height, intArray)
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        val reader: Reader = MultiFormatReader()

        contents = try {
            val result = reader.decode(bitmap)
            result.text
        } catch (e: Exception) {
            "No data found"
        }

        return contents
    }

    fun authenticatorToWristkey (decodedQRCodeData: String): MutableList<MfaCode> {

        fun decodeAuthenticatorData (authenticatorJsonString: String): MutableList<MfaCode> {
            val logins = mutableListOf<MfaCode>()

            val items = JSONObject(authenticatorJsonString)
            for (key in items.keys()) {
                val itemData = JSONObject(items[key].toString())

                var mode: String = if (itemData["type"].toString() == "1") "hotp" else "totp"

                var issuer: String = key

                var account: String = if (itemData["username"].toString().isNotEmpty()
                    && itemData["username"].toString() != issuer)
                    itemData["username"].toString()
                else ""

                var secret: String = itemData["secret"].toString()

                logins.add(
                    MfaCode (
                        mode = mode,
                        issuer = issuer,
                        account = account,
                        secret = secret,
                        algorithm = ALGO_SHA1,
                        digits = 6,
                        period = 30,
                        lock = false,
                        counter = 0,
                        label = ""
                    )
                )
            }

            return logins
        }

        fun scanAndDecodeQrCode(decodedQRCodeData: String): String {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(context))
            }

            val pythonRuntime = Python
                .getInstance()
                .getModule("extract_otp_secret_keys")
                .callAttr("decode", decodedQRCodeData)

            val timeStamp: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())

            val logcat: Process
            val log = StringBuilder()
            try {
                logcat = Runtime.getRuntime().exec(arrayOf("logcat", "-d"))
                val br =
                    BufferedReader(InputStreamReader(logcat.inputStream), 4 * 1024)
                var line: String?
                val separator = System.getProperty("line.separator")
                while (br.readLine().also { line = it } != null) {
                    log.append(line)
                    log.append(separator)
                }
                Runtime.getRuntime().exec("clear")
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

            // pythonRuntime.close()

            return log.toString()
                .substringAfter(timeStamp)  // get most recent occurrence of data
                .substringAfter("python.stdout")
                .substringAfter("<wristkey>")
                .substringBefore("<\\wristkey>")
        }

        // put data in Python script and extract Authenticator data
        var logins: MutableList<MfaCode> = mutableListOf()
        if (decodedQRCodeData.contains("otpauth-migration://")) {
            val logExtractedString = scanAndDecodeQrCode(decodedQRCodeData)
            logins = decodeAuthenticatorData(logExtractedString)
        }

        return logins

    }

    fun wfsToHashmap(jsonObject: JSONObject): Map<String, Any> {
        val map: MutableMap<String, String> = HashMap()
        for (key in jsonObject.keys()) {
            map[key] = jsonObject[key] as String
        }
        return map
    }

    fun bitwardenToWristkey (jsonObject: JSONObject): MutableList<MfaCode> {

        val logins = mutableListOf<MfaCode>()

        val items = jsonObject["items"].toString()
        val itemsArray = JSONArray(items)

        val accounts = JSONArray(itemsArray.toString())

        val accountList = mutableListOf<JSONObject>()

        for (index in 0 until accounts.length()) {
            accountList.add(JSONObject(accounts[index].toString()))
        }

        for (account in accountList) {

            try {
                val issuer = account["name"].toString().trim()
                val totp = JSONObject(account["login"].toString())["totp"].toString().trim()
                val username = JSONObject(account["login"].toString())["username"].toString().trim()

                if (!totp.contains("null") && !totp.isNullOrBlank()) {

                    if (totp.startsWith("otpauth://")) {
                        val type: String = if (totp.substringAfter("://").substringBefore("/").contains("totp")) "totp" else "hotp"
                        val issuer: String = totp.substringAfterLast("otp/").substringBefore(":")
                        val account: String = totp.substringAfterLast(":").substringBefore("?")
                        val secret: String = totp.substringAfter("secret=").substringBefore("&")
                        val algorithm: String = if (totp.contains("algorithm")) totp.substringAfter("algorithm=").substringBefore("&") else ALGO_SHA1
                        val digits: Int = if (totp.contains("digits")) totp.substringAfter("digits=").substringBefore("&").toInt() else 6
                        val period: Int = if (totp.contains("period")) totp.substringAfter("period=").substringBefore("&").toInt() else 30
                        val lock: Boolean = if (totp.contains("lock")) totp.substringAfter("lock=").substringBefore("&").toBoolean() else false
                        val counter: Long = if (totp.contains("counter")) totp.substringAfter("counter=").substringBefore("&").toLong() else 0
                        val label: String = if (totp.contains("label")) totp.substringAfter("label=").substringBefore("&") else account
                        logins.add (
                            MfaCode(
                                mode = type,
                                issuer = issuer,
                                account = account,
                                secret = secret,
                                algorithm = algorithm,
                                digits = digits,
                                period = period,
                                lock = false,
                                counter = counter,
                                label = label
                            )
                        )

                    } else if (!totp.startsWith("otpauth://")) { // Google Authenticator
                        logins.add (
                            MfaCode(
                                mode = "totp",
                                issuer = issuer.ifBlank { "Unknown issuer" },
                                account = username.ifBlank { "" },
                                secret = totp,
                                algorithm = ALGO_SHA1,
                                digits = 6,
                                period = 30,
                                lock = false,
                                counter = 0,
                                label = ""
                            )
                        )
                    }
                }

            } catch (_: JSONException) { }

        }

        return logins

    }

    fun andOtpToWristkey (jsonArray: JSONArray): MutableList<MfaCode> {

        val logins = mutableListOf<MfaCode>()

        for (itemIndex in 0 until jsonArray.length()) {

            val login = jsonArray[itemIndex].toString()
            val secret = JSONObject(login)["secret"].toString().replace("=", "")
            val issuer = JSONObject(login)["issuer"].toString()
            val counter = try { JSONObject(login)["used_frequency"].toString().toLong() } catch (_: JSONException) { 0 }
            val algorithm = JSONObject(login)["algorithm"].toString()
            val digits = JSONObject(login)["digits"].toString().toInt()
            val period = JSONObject(login)["period"].toString().toInt()
            var type = JSONObject(login)["type"].toString().lowercase()
            if (type == "STEAM") type = "totp"
            val label = try { JSONObject(login)["label"].toString() } catch (_: JSONException) { "" }

            logins.add (
                MfaCode(
                    mode = type,
                    issuer = issuer,
                    account = label,
                    secret = secret,
                    algorithm = algorithm,
                    digits = digits,
                    period = period,
                    lock = false,
                    counter = counter,
                    label = label
                )
            )

        }

        return logins

    }

    fun searchLogins (searchTerms: String, logins: MutableList<MfaCode>): MutableList<MfaCode> {
        val results = mutableListOf<MfaCode>()
        for (_login in logins)
            if (_login
                    .toString()
                    .lowercase()
                    .replace("mfacode", "")
                    .replace("issuer", "")
                    .replace("secret", "")
                    .replace("lock", "")
                    .replace("counter", "")
                    .replace("period", "")
                    .replace("digits", "")
                    .replace("mode", "")
                    .replace("algorithm", "")
                    .replace("label", "")
                    .replace(Regex("""[^a-zA-Z\\d]"""), "")
                    .contains(searchTerms.lowercase())
            ) results.add(_login)
        return results
    }

    fun aegisToWristkey (unencryptedAegisJsonString: String): MutableList<MfaCode> {

        val logins = mutableListOf<MfaCode>()

        val db = JSONObject(unencryptedAegisJsonString)["db"].toString()
        val entries = JSONObject(db)["entries"].toString()

        val itemsArray = JSONArray(entries)

        for (itemIndex in 0 until itemsArray.length()) {
            try {

                val accountData = JSONObject(itemsArray[itemIndex].toString())
                var type = accountData["type"]
                val uuid = accountData["uuid"].toString()
                val issuer = accountData["issuer"].toString()
                var username = accountData["name"].toString()

                if (username == issuer || username == "null" || username.isNullOrEmpty()) {
                    username = ""
                }

                var totpSecret = JSONObject(accountData["info"].toString())["secret"].toString()
                val digits = JSONObject(accountData["info"].toString())["digits"].toString().toInt()
                var algorithm = JSONObject(accountData["info"].toString())["algo"].toString()
                var period = JSONObject(accountData["info"].toString())["period"].toString().toInt()
                var counter = try { JSONObject(accountData["info"].toString())["counter"].toString().toLong() } catch (_: JSONException) { 0L }

                type = if (type.equals("totp")) "totp" else "hotp"

                if (totpSecret.isNotEmpty() && totpSecret != "null") {
                    logins.add (
                        MfaCode(
                            mode = type,
                            issuer = issuer,
                            account = username,
                            secret = totpSecret,
                            algorithm = algorithm,
                            digits = digits,
                            period = period,
                            lock = false,
                            counter = counter,
                            label = ""
                        )
                    )
                }

            } catch (noData: JSONException) {
                noData.printStackTrace()
            }
        }

        return logins

    }

    fun decodeOtpAuthURL(otpAuthURL: String): MfaCode? {
        return try {
            val decodedURL = URLDecoder.decode(otpAuthURL, "UTF-8")

            val parts = decodedURL.split("?")
            if (parts.size != 2) return null

            val params = parts[1].split("&")
            val paramMap = mutableMapOf<String, String>()

            for (param in params) {
                val keyValue = param.split("=")
                if (keyValue.size == 2) paramMap[keyValue[0]] = keyValue[1]
            }

            val mode = if (parts[0].substringAfter("://").substringBefore("/") == "hotp") MFA_COUNTER_MODE else MFA_TIME_MODE
            val issuer = parts[0].substringAfter("otp/").substringBefore(":")
            val account = parts[0].substringAfter("otp/").substringAfter(":")
            val secret = paramMap["secret"]
            val algorithm = paramMap["algorithm"] ?: ALGO_SHA1
            val digits = paramMap["digits"]?.toIntOrNull() ?: 6 // Default to 6 digits
            val period = paramMap["period"]?.toIntOrNull() ?: 30 // Default to 30 seconds
            val lock = paramMap["lock"]?.toBoolean() ?: false // Default to false
            val counter = paramMap["counter"]?.toLongOrNull()
            val label = paramMap["label"]

            // Create and return the MfaCode object
            MfaCode(mode, issuer, account, secret ?: "", algorithm, digits, period, lock, counter ?: 0, label ?: "")
        } catch (e: Exception) { null }
    }

    fun encodeOtpAuthURL (mfaCodeObject: MfaCode): String {
        // TOTPs: otpauth://totp/Google%20LLC%2E:me%400x4f.in?secret=ASDFGHJKL&issuer=Google&algorithm=SHA1&digits=6&period=30&counter=0&label=Work
        val issuer: String = URLEncoder.encode(mfaCodeObject.issuer)
        val account: String = URLEncoder.encode(mfaCodeObject.account)
        val secret: String = mfaCodeObject.secret.replace(" ", "")
        val digits: String = mfaCodeObject.digits.toString()
        val period: String = mfaCodeObject.period.toString()
        val algorithm: String = mfaCodeObject.algorithm.replace(" ", "").replace("-", "").uppercase()
        val lock: String = mfaCodeObject.lock.toString()
        val counter: String = mfaCodeObject.counter.toString()
        val label: String = URLEncoder.encode(mfaCodeObject.label)

        return if (mfaCodeObject.mode.lowercase().contains(MFA_TIME_MODE)) "otpauth://${mfaCodeObject.mode}/$issuer:$account?secret=$secret&algorithm=$algorithm&digits=$digits&period=$period&lock=$lock&label=$label"
        else "otpauth://${MFA_COUNTER_MODE}/$issuer:$account?secret=$secret&algorithm=$algorithm&digits=$digits&counter=$counter&lock=$lock&label=$label"
    }

    fun deleteFromDataStore (dataToDelete: String): Boolean {
        var data  = db.all[DATA_STORE].toString()
        val dataStore = objectMapper.readValue (
            data,
            WristkeyFileSystem::class.java
        )

        val iterator: MutableIterator<String> = dataStore.otpauth.iterator()
        while (iterator.hasNext()) if (iterator.next().contains(dataToDelete)) iterator.remove()

        data = objectMapper.writeValueAsString(dataStore)
        db.edit().putString(DATA_STORE, data).apply()

        return true
    }

    fun randomString(length: Int): String {
        return (1..length).map { ('A'..'Z').random() }.joinToString("")
    }

    class WristkeyFileSystem (
        @JsonProperty("otpauth") val otpauth: MutableList<String>
    )

    fun overwriteLogin (otpAuthURL: String): Boolean {  // Overwrites an otpAuth String if it already exists
        var data = objectMapper.writeValueAsString (
            WristkeyFileSystem(
                mutableListOf()
            )
        )

        data = db.getString(DATA_STORE, data)

        val dataStore =
            objectMapper.readValue (
                data,
                WristkeyFileSystem::class.java
            )

        val iterator = dataStore.otpauth.iterator()
        while (iterator.hasNext()) {
            val login = iterator.next()
            val loginSecret = decodeOtpAuthURL(login)!!.secret.lowercase().replace(" ", "")
            val secretToWrite = decodeOtpAuthURL(otpAuthURL)!!.secret.lowercase().replace(" ", "")
            if (loginSecret.contains(secretToWrite)) iterator.remove()
        }

        dataStore.otpauth.add(otpAuthURL)
        data = objectMapper.writeValueAsString(dataStore)
        db.edit().putString(DATA_STORE, data).apply()

        return true
    }

    fun getData (): WristkeyFileSystem {
        val wfs = objectMapper.writeValueAsString (
            WristkeyFileSystem(
                mutableListOf()
            )
        )

        val data = db.getString (DATA_STORE, wfs)
        return objectMapper.readValue (
            data,
            WristkeyFileSystem::class.java
        )
    }

    fun beep () {
        try {
            val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            toneGen1.startTone(ToneGenerator.TONE_SUP_INTERCEPT, 150)
        } catch (_: Exception) { }
    }

    fun getLocalIpAddress(context: Context): String? {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo: WifiInfo? = wifiManager.connectionInfo

            if (wifiInfo != null) {
                val ipAddress = wifiInfo.ipAddress
                val ipByteArray = byteArrayOf(
                    (ipAddress and 0xFF).toByte(),
                    (ipAddress shr 8 and 0xFF).toByte(),
                    (ipAddress shr 16 and 0xFF).toByte(),
                    (ipAddress shr 24 and 0xFF).toByte()
                )

                val inetAddress = InetAddress.getByAddress(ipByteArray)
                return inetAddress.hostAddress
            }
        } catch (e: Exception) {
            Log.e("IP Address", "Error getting IP address: ${e.message}")
        }
        return null
    }

    fun generateTotp (secret: String, algorithm: String, digits: Int, period: Int): String {
        lateinit var _algorithm: HmacAlgorithm
        when (algorithm) {
            ALGO_SHA1 -> _algorithm = HmacAlgorithm.SHA1
            ALGO_SHA256 -> _algorithm = HmacAlgorithm.SHA256
            ALGO_SHA512 -> _algorithm = HmacAlgorithm.SHA512
        }

        val config = TimeBasedOneTimePasswordConfig(
            codeDigits = digits,
            hmacAlgorithm = _algorithm,
            timeStep = period.toLong(),
            timeStepUnit = TimeUnit.SECONDS
        )

        return TimeBasedOneTimePasswordGenerator(secret.toByteArray(), config).generate()
    }

    fun generateHotp (secret: String, algorithm: String, digits: Int, counter: Long): String {
        lateinit var _algorithm: HmacAlgorithm
        when (algorithm) {
            ALGO_SHA1 -> _algorithm = HmacAlgorithm.SHA1
            ALGO_SHA256 -> _algorithm = HmacAlgorithm.SHA256
            ALGO_SHA512 -> _algorithm = HmacAlgorithm.SHA512
        }

        val config = HmacOneTimePasswordConfig (codeDigits = digits, hmacAlgorithm = _algorithm)

        return HmacOneTimePasswordGenerator (secret.toByteArray(), config).generate(counter)
    }

}

class HttpServer (private val activityContext: Context, ip: String, port: Int): NanoHTTPD (ip, port) {

    class Keys (
        val publicKey: ByteArray,
        val privateKey: ByteArray
    )
    private fun generateKeyPair(): Keys {
        Security.addProvider(BouncyCastleProvider())
        val generator = X25519KeyPairGenerator()
        val keyGenParam = X25519KeyGenerationParameters(null)
        generator.init(keyGenParam)

        val keyPair = generator.generateKeyPair()

        val privateKeyBytes = (keyPair.private as X25519PrivateKeyParameters).encoded
        val publicKeyBytes = (keyPair.public as X25519PublicKeyParameters).encoded

        return Keys(publicKeyBytes, privateKeyBytes)
    }

    private fun exchangeKeys(publicKeyBytes: ByteArray, privateKeyBytes: ByteArray): ByteArray {
        val publicKey = X25519PublicKeyParameters(publicKeyBytes, 0)
        val privateKey = X25519PrivateKeyParameters(privateKeyBytes, 0)

        val agreement = X25519Agreement()
        agreement.init(privateKey)
        agreement.calculateAgreement(publicKey, ByteArray(agreement.agreementSize), 0)

        val sharedSecret = ByteArray(agreement.agreementSize)
        agreement.calculateAgreement(publicKey, sharedSecret, 0)

        return sharedSecret
    }

    private fun encrypt(data: String, sharedSecret: ByteArray): ByteArray {
        val iv = ByteArray(16)
        val random = SecureRandom()
        random.nextBytes(iv)

        val keySpec = SecretKeySpec(sharedSecret, "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val ivParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec)

        val encryptedData = cipher.doFinal(data.toByteArray())

        val result = ByteArray(iv.size + encryptedData.size)
        System.arraycopy(iv, 0, result, 0, iv.size)
        System.arraycopy(encryptedData, 0, result, iv.size, encryptedData.size)

        return result
    }

    private fun decrypt(encryptedData: ByteArray, sharedSecret: ByteArray): String {
        val ivSize = 16
        val iv = ByteArray(ivSize)
        val encryptedBytes = ByteArray(encryptedData.size - ivSize)
        System.arraycopy(encryptedData, 0, iv, 0, ivSize)
        System.arraycopy(encryptedData, ivSize, encryptedBytes, 0, encryptedData.size - ivSize)

        val keySpec = SecretKeySpec(sharedSecret, "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val ivParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec)

        val decryptedBytes = cipher.doFinal(encryptedBytes)

        return String(decryptedBytes)
    }

    private var accepted = false
    private var publicKeySent = false
    private var acceptancePrompted = false

    val activity = (activityContext as Activity)

    override fun serve(session: IHTTPSession): Response {
        val keyPair = generateKeyPair()
        //Log.d("Wristkey--S", encodeToString(keyPair.publicKey, DEFAULT))

        //val sharedSecret = exchangeKeys (keyPair.publicKey, keyPair.privateKey)
       // val dataToEncrypt = "Hello, X25519!"
       // val encryptedData = encrypt(dataToEncrypt, sharedSecret)
        //val decryptedData = decrypt(encryptedData, sharedSecret)

        //Log.d ("Wristkey--S", "Original data: $dataToEncrypt")
        //Log.d ("Wristkey--S", "Encrypted data (Base64): " + encodeToString(encryptedData, DEFAULT))
        //Log.d ("Wristkey--S", "Decrypted data: $decryptedData")

        //Log.d ("Wristkey--S", "Accepted? $accepted")

        Log.d ("Wristkey--S", "Server serving")

        val requestBody = HashMap<String, String>()
        session.parseBody(requestBody)

        val parameters = session.parameters

        lateinit var deviceName: String
        for ((key, value) in parameters.entries) { if (key == "deviceName") deviceName = value[0] }

        if (session.method == Method.POST && parameters.entries.size > 0 && deviceName.isNotBlank() && deviceName.length >= 8 &&  deviceName.contains('(')) {
            Log.d ("Wristkey--S", "POST received")

            activity.runOnUiThread {
                if (!acceptancePrompted) {
                    acceptancePrompted = true
                    AlertDialog.Builder(activityContext)
                        .setMessage("\n\n" + deviceName + " " + activityContext.getString(wristkey.R.string.wifi_connection_request))
                        .setPositiveButton("Accept") { _, _ ->
                            accepted = true
                        }
                        .setNegativeButton("Decline") { _, _ ->
                            Log.d ("Wristkey--S", "Server stopped")
                            stop()
                            activity.finish()
                        }
                        .setCancelable(false)
                        .create().show()
                }
            }

            if (publicKeySent) {
                // activity.finish()
            }

            if (accepted) {
                publicKeySent = true
                return newFixedLengthResponse(Response.Status.OK, "text/plain", "publicKey=${encodeToString(keyPair.publicKey, DEFAULT).trim()}")
            }

            return newFixedLengthResponse(Response.Status.OK, "text/plain", "requested")

        } else {
            stop()
            activity.finish()
            return newFixedLengthResponse (Response.Status.FORBIDDEN, "text/plain", activityContext.getString(
                wristkey.R.string.forbidden_wifi_transfer))
        }
    }

    fun startServer() {
        Log.d ("Wristkey--S", "Server started")
        makeSecure(makeSSLSocketFactory("/res/raw/keystore.bks", "Wristkey".toCharArray()), null)
        super.start(SOCKET_READ_TIMEOUT, false)
    }

}

interface ItemTouchHelperAdapter {
    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean
    fun onItemDismiss(position: Int)
}

class ItemTouchHelperCallback(private val adapter: ItemTouchHelperAdapter) : ItemTouchHelper.Callback() {

    lateinit var context: Context
    private lateinit var _recyclerView: RecyclerView
    lateinit var utilities: Utilities
    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        _recyclerView = recyclerView
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        var swipeFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT

        if (isRightToLeftSwipeAllowedForItem()) swipeFlags = ItemTouchHelper.LEFT

        return makeMovementFlags(dragFlags, swipeFlags)
    }

    private fun isRightToLeftSwipeAllowedForItem(): Boolean {
        return true
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return adapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
    }

    private var swipedItemViewHolder: RecyclerView.ViewHolder? = null

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        swipedItemViewHolder = viewHolder

        adapter.onItemDismiss(viewHolder.bindingAdapterPosition)
        _recyclerView.adapter?.notifyItemChanged(viewHolder.bindingAdapterPosition)

        val position = viewHolder.absoluteAdapterPosition
        val intent = Intent(context, ManualEntryActivity::class.java)
        intent.putExtra(utilities.INTENT_EDIT, utilities.getData().otpauth[position])
        context.startActivity(intent)

    }
    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        if (!::context.isInitialized || !::utilities.isInitialized) {
            context = viewHolder.itemView.context
            utilities = Utilities(context)
        }

        val icon = ContextCompat.getDrawable(recyclerView.context, wristkey.R.drawable.ic_baseline_edit_24)

        val itemView = viewHolder.itemView

        val iconMargin = (itemView.height - icon!!.intrinsicHeight) / 2
        val iconTop = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
        val iconBottom = iconTop + icon.intrinsicHeight

        val iconLeft: Int
        val iconRight: Int

        if (dX > 0) {
            iconLeft = itemView.left + iconMargin
            iconRight = itemView.left + iconMargin + icon.intrinsicWidth
        } else {
            iconLeft = itemView.right - iconMargin - icon.intrinsicWidth
            iconRight = itemView.right - iconMargin
        }
        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
        icon.draw(c)
    }

}

class LoginsAdapter(private val data: List<Utilities.MfaCode>, val timer: Timer, val isRound: Boolean) : RecyclerView.Adapter<LoginsAdapter.ViewHolder>(), ItemTouchHelperAdapter {

    lateinit var context: Context
    lateinit var utilities: Utilities
    lateinit var clipboard: ClipboardManager
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(wristkey.R.layout.login_card, parent, false)

        if (!::context.isInitialized || !::utilities.isInitialized) {
            context = itemView.context
            utilities = Utilities(context)
            clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        }

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        // Handle item movement and reordering here
        // Update your data list accordingly
        // Notify the adapter of the data change
        return true
    }

    override fun onItemDismiss(position: Int) {
        // Handle item dismissal (swipe left or right) here
        // Update your data list accordingly
        // Notify the adapter of the data change
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val code: TextView = itemView.findViewById(wristkey.R.id.code)
        private val issuer: TextView = itemView.findViewById(wristkey.R.id.issuer)
        private val accountAndLabel: TextView = itemView.findViewById(wristkey.R.id.accountAndLabel)
        private val loginInfo: LinearLayout = itemView.findViewById(wristkey.R.id.loginInfo)
        private val counterControls: LinearLayout = itemView.findViewById(wristkey.R.id.counterControls)
        private val progressIndicator: ProgressBar = itemView.findViewById(wristkey.R.id.progressIndicator)
        private val accountIcon: TextView = itemView.findViewById(wristkey.R.id.accountIcon)
        private val plus: ImageView = itemView.findViewById(wristkey.R.id.plus)
        private val minus: ImageView = itemView.findViewById(wristkey.R.id.minus)

        fun bind(item: Utilities.MfaCode) {

            accountIcon.text = item.issuer[0].toString()

            issuer.text = item.issuer

            var assembledLabel = item.account
            if (item.label.isNotBlank()) assembledLabel = "$assembledLabel (${item.label})"
            if (item.account.isNotBlank()) accountAndLabel.text = assembledLabel else accountAndLabel.visibility = View.GONE

            // Time mode
            if (item.mode.contains(utilities.MFA_TIME_MODE)) {
                counterControls.visibility = View.GONE
                var mfaCode = utilities.generateTotp (secret = item.secret, algorithm = item.algorithm, digits = item.digits, period = item.period)
                mfaCode = "${mfaCode.substring(0, mfaCode.length/2)} ${mfaCode.substring(mfaCode.length/2)}"
                code.text = mfaCode
                loginInfo.setOnClickListener { clipboard.setPrimaryClip(ClipData.newPlainText(context.getString(
                    R.string.app_name), code.text.toString().replace(" ", ""))) }
                progressIndicator.max = item.period

                if (isRound) {
                    (itemView.context as? Activity)?.runOnUiThread {
                        progressIndicator.visibility = View.INVISIBLE
                        accountIcon.visibility = View.VISIBLE
                    }
                } else accountIcon.visibility = View.INVISIBLE

                timer.scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {

                        val second = SimpleDateFormat("s", Locale.getDefault()).format(Date()).toInt()
                        val tickerValue = (item.period*2 - (second % item.period*2)) % item.period
                        try {
                            progressIndicator.progress = tickerValue
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) progressIndicator.setProgress(tickerValue, true)
                        } catch (_: Exception) { }

                        if (tickerValue == 29) {
                            mfaCode = utilities.generateTotp (secret = item.secret, algorithm = item.algorithm, digits = item.digits, period = item.period)
                            mfaCode = "${mfaCode.substring(0, mfaCode.length/2)} ${mfaCode.substring(mfaCode.length/2)}"
                            (itemView.context as? Activity)?.runOnUiThread {
                                code.text = mfaCode
                                loginInfo.setOnClickListener { clipboard.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.app_name), code.text.toString().replace(" ", ""))) }
                            }
                        }
                    }
                }, 0, 1000)
            }

            // Counter mode
            else {
                var counter = item.counter
                progressIndicator.visibility = View.INVISIBLE
                accountAndLabel.text = "$counter ⋅ $assembledLabel"

                var hmacCode = utilities.generateHotp (secret = item.secret, algorithm = item.algorithm, digits = item.digits, counter = counter)
                hmacCode = "${hmacCode.substring(0, hmacCode.length/2)} ${hmacCode.substring(hmacCode.length/2)}"
                code.text = hmacCode
                loginInfo.setOnClickListener { clipboard.setPrimaryClip(ClipData.newPlainText(context.getString(
                    R.string.app_name), code.text.toString().replace(" ", ""))) }

                plus.setOnClickListener {
                    val incrementDialog = CustomFullscreenDialogFragment(
                        title = "Increment",
                        message = "Increment ${item.issuer} from\n${item.counter} to ${item.counter+1}?",
                        positiveButtonText =  "Increment",
                        positiveButtonIcon = context.getDrawable(R.drawable.ic_add_white_24dp)!!,
                        negativeButtonText = "Go back",
                        negativeButtonIcon = context.getDrawable(R.drawable.ic_prev)!!,
                    )

                    incrementDialog.setOnPositiveClickListener {
                        counter++
                        item.counter = counter
                        utilities.overwriteLogin(utilities.encodeOtpAuthURL(item))
                        accountAndLabel.text = "$counter ⋅ $assembledLabel"
                        hmacCode = utilities.generateHotp (secret = item.secret, algorithm = item.algorithm, digits = item.digits, counter = counter)
                        hmacCode = "${hmacCode.substring(0, hmacCode.length/2)} ${hmacCode.substring(hmacCode.length/2)}"
                        code.text = hmacCode
                        loginInfo.setOnClickListener { clipboard.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.app_name), code.text.toString().replace(" ", ""))) }
                    }

                    incrementDialog.show((context as AppCompatActivity).supportFragmentManager, "CustomFullscreenDialog")

                }

                minus.setOnClickListener {

                    val decrementDialog = CustomFullscreenDialogFragment(
                        title = "Decrement",
                        message = "Decrement ${item.issuer} from\n${item.counter} to ${item.counter-1}?",
                        positiveButtonText =  "Decrement",
                        positiveButtonIcon = context.getDrawable(R.drawable.ic_prev_selector)!!,
                        negativeButtonText = "Go back",
                        negativeButtonIcon = context.getDrawable(R.drawable.ic_prev)!!,
                    )

                    decrementDialog.setOnPositiveClickListener {
                        counter--
                        item.counter = counter
                        utilities.overwriteLogin(utilities.encodeOtpAuthURL(item))
                        accountAndLabel.text = "$counter ⋅ $assembledLabel"
                        hmacCode = utilities.generateHotp (secret = item.secret, algorithm = item.algorithm, digits = item.digits, counter = counter)
                        hmacCode = "${hmacCode.substring(0, hmacCode.length/2)} ${hmacCode.substring(hmacCode.length/2)}"
                        code.text = hmacCode
                        loginInfo.setOnClickListener { clipboard.setPrimaryClip(ClipData.newPlainText(context.getString(
                            R.string.app_name), code.text.toString().replace(" ", ""))) }
                    }

                    decrementDialog.show((context as AppCompatActivity).supportFragmentManager, "CustomFullscreenDialog")

                }

            }

        }
    }
}


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
import android.os.Handler
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import dev.turingcomplete.kotlinonetimepassword.GoogleAuthenticator
import dev.turingcomplete.kotlinonetimepassword.HmacAlgorithm
import dev.turingcomplete.kotlinonetimepassword.HmacOneTimePasswordConfig
import dev.turingcomplete.kotlinonetimepassword.HmacOneTimePasswordGenerator
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordConfig
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordGenerator
import fi.iki.elonen.NanoHTTPD
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import wristkey.R
import java.io.IOException
import java.net.InetAddress
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


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
    val SETTINGS_COMPACT_ENABLED = "SETTINGS_COMPACT_ENABLED"
    val SETTINGS_CONCEALED_ENABLED = "SETTINGS_CONCEALED_ENABLED"
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
    val objectMapper: ObjectMapper

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
        var secret: String,
        val algorithm: String,
        val digits: Int,
        val period: Int,
        val lock: Boolean,
        var counter: Long,
        val label: String,
    )

    fun deviceName () : String {
        return "${Build.MANUFACTURER.toTitleCase()} ${Build.MODEL}"
    }

    fun String.toTitleCase(): String = this.split(" ").joinToString(" ") { it.capitalize() }

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

    fun isWearOsDevice(): Boolean {
        return context.packageManager.hasSystemFeature("android.hardware.type.watch")
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

    fun aegisToWristkey (unencryptedAegisJsonString: JSONObject): MutableList<MfaCode> {

        val logins = mutableListOf<MfaCode>()

        val db = unencryptedAegisJsonString["db"].toString()
        val entries = JSONObject(db)["entries"].toString()

        val itemsArray = JSONArray(entries)

        for (itemIndex in 0 until itemsArray.length()) {
            try {

                val accountData = JSONObject(itemsArray[itemIndex].toString())
                var type = accountData["type"]
                val uuid = accountData["uuid"].toString()
                val issuer = accountData["issuer"].toString()
                var username = accountData["name"].toString()

                if (username == issuer || username == "null" || username.isEmpty()) username = ""

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

    fun getTime(): String {
        val hourType = if (android.text.format.DateFormat.is24HourFormat(context)) "HH" else "hh"
        val currentHour = SimpleDateFormat(hourType, Locale.getDefault()).format(Date())
        val currentMinute = SimpleDateFormat("mm", Locale.getDefault()).format(Date())
        return "$currentHour:$currentMinute"
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

    fun decodeGoogleAuthenticator (otpAuthUrl: String): List<String> {
        // Todo: Removed this functionality for now.
        return mutableListOf()
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
        @JsonProperty("otpauth") var otpauth: MutableList<String>
    )

    fun toBase64(string: String): String {
        return Base64.encodeToString(string.toByteArray(Charsets.UTF_8), Base64.DEFAULT)
    }

    fun fromBase64(string: String): String {
        val decodedBytes = Base64.decode(string, Base64.DEFAULT)
        return String(decodedBytes, Charsets.UTF_8)
    }

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
            try {
                val loginSecret = decodeOtpAuthURL(login)!!.secret.lowercase().replace(" ", "")
                val secretToWrite = decodeOtpAuthURL(otpAuthURL)!!.secret.lowercase()
                if (loginSecret.contains(secretToWrite)) iterator.remove()
            } catch (_: java.lang.Exception) { }
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

        if (algorithm == ALGO_SHA1 && period == 30 && digits == 6) return GoogleAuthenticator(secret.toByteArray(Charset.defaultCharset())).generate()

        return TimeBasedOneTimePasswordGenerator(secret.toByteArray(Charset.defaultCharset()), config).generate()
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

    fun second (): Int {
        return SimpleDateFormat("s", Locale.getDefault()).format(Date()).toInt()
    }


}

class Cryptography () {
    val sodium = SodiumAndroid()
    val lazySodium = LazySodiumAndroid(sodium, StandardCharsets.UTF_8)
    fun encrypt(data: String, publicKey: com.goterl.lazysodium.utils.Key): String {
        //val keypair = lazySodium.cryptoKxKeypair()
        //val publicKey = keypair.publicKey
        return lazySodium.cryptoBoxSealEasy(data, publicKey)
    }
    fun decrypt (data: String, privateKey: com.goterl.lazysodium.utils.Key): String {
        //val keypair = lazySodium.cryptoKxKeypair()
        //val publicKey = keypair.publicKey
        return lazySodium.cryptoBoxSealEasy(data, privateKey)
    }
}

interface ItemTouchHelperAdapter {
    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean
    fun onItemDismiss(position: Int)
}

class ItemTouchHelperCallback(private val adapter: ItemTouchHelperAdapter, val loginsList: MutableList<Utilities.MfaCode>) : ItemTouchHelper.Callback() {

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

        val fromPosition = viewHolder.absoluteAdapterPosition
        val toPosition = target.absoluteAdapterPosition

        Collections.swap(loginsList, fromPosition, toPosition)

        var data = utilities.objectMapper.writeValueAsString (
            Utilities.WristkeyFileSystem(
                mutableListOf()
            )
        )

        val dataStore =
            utilities.objectMapper.readValue (
                utilities.db.getString(utilities.DATA_STORE, data),
                Utilities.WristkeyFileSystem::class.java
            )

        val encodedLogins = mutableListOf<String>()
        for (login in loginsList) encodedLogins.add(utilities.encodeOtpAuthURL(login))

        dataStore.otpauth = encodedLogins
        val newData = utilities.objectMapper.writeValueAsString(dataStore)
        utilities.db.edit().putString(utilities.DATA_STORE, newData).apply()

        recyclerView.adapter?.notifyItemMoved(fromPosition, toPosition)

        val scrollY = recyclerView.computeVerticalScrollOffset()
        if (viewHolder.absoluteAdapterPosition == 0 && scrollY > 0) recyclerView.smoothScrollBy(0, -1)

        return true
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

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val icon = ContextCompat.getDrawable(recyclerView.context, R.drawable.ic_baseline_edit_24)

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

}

class LoginsAdapter(private val data: MutableList<Utilities.MfaCode>, val timer: Timer, val isRound: Boolean) : RecyclerView.Adapter<LoginsAdapter.ViewHolder>(), ItemTouchHelperAdapter {

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
        // move unnecessary crap out of here and only update the text
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

            accountAndLabel.isSelected = true
            accountIcon.text = item.issuer[0].toString()
            issuer.text = item.issuer

            var assembledLabel = item.account
            if (item.label.isNotBlank()) assembledLabel = "$assembledLabel (${item.label})"
            if (item.account.isNotBlank()) accountAndLabel.text = assembledLabel else accountAndLabel.visibility = View.GONE

            // Time mode
            if (item.mode.contains(utilities.MFA_TIME_MODE)) {
                counterControls.visibility = View.GONE
                code.text = item.secret
                code.visibility = View.GONE

                loginInfo.setOnClickListener {
                    clipboard.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.app_name), code.text.toString().replace(" ", "")))
                    code.visibility = View.VISIBLE
                    Handler().postDelayed({ code.visibility = View.GONE }, 3000)
                }

                progressIndicator.max = item.period

                if (isRound) {
                    (itemView.context as? Activity)?.runOnUiThread {
                        progressIndicator.visibility = View.INVISIBLE
                        accountIcon.visibility = View.VISIBLE
                    }
                } else accountIcon.visibility = View.INVISIBLE

                timer.scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {

                        val second = utilities.second()
                        val tickerValue = (item.period - (second % item.period)) % item.period
                        try {
                            progressIndicator.progress = tickerValue
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) progressIndicator.setProgress(tickerValue, true)
                        } catch (_: Exception) { }

                        if (tickerValue == 29) {
                            (itemView.context as? Activity)?.runOnUiThread {
                                code.text = item.secret
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
                        if (counter.toInt() > 0) minus.isEnabled = true
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

                if (counter.toInt() == 0) minus.isEnabled = false
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
                        if (counter.toInt() == 0) minus.isEnabled = false
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

class Server(port: Int, val responseString: String) : NanoHTTPD(port) {
    var encryptedVault: String = ""
    var deviceName: String = ""
    override fun serve(session: NanoHTTPD.IHTTPSession): Response {
        if (session.method == Method.POST) {
            val files = HashMap<String, String>()
            try {
                session.parseBody(files)
            } catch (ioe: IOException) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Server Internal Error")
            } catch (re: ResponseException) {
                return newFixedLengthResponse(re.status, MIME_PLAINTEXT, re.message)
            }
            val data = files["postData"] ?: "No POST body received"
            // Log.d("Wristkey-Transfer Log", data)
            if (data.contains("encryptedVault")) {
                encryptedVault = JSONObject(data)["encryptedVault"] as String
                deviceName = JSONObject(data)["deviceName"] as String
            }
            return newFixedLengthResponse(Response.Status.OK, "text/plain", responseString)
        }
        return newFixedLengthResponse("This server only handles POST requests.")
    }
}
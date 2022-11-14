package app.wristkey

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.*


@RequiresApi(Build.VERSION_CODES.M)
class Utilities (context: Context) {

    val FILES_REQUEST_CODE = 69
    val CAMERA_REQUEST_CODE = 420
    val EXPORT_RESPONSE_CODE = 69420


    val OTPAUTH_SCAN_CODE = "OTPAUTH_SCAN_CODE"
    val AUTHENTICATOR_EXPORT_SCAN_CODE = "AUTHENTICATOR_EXPORT_SCAN_CODE"
    val QR_CODE_SCAN_REQUEST = "QR_CODE_SCAN_REQUEST"

    val context = context

    val QR_TIMER_DURATION = 5

    val DEFAULT_TYPE = "otpauth"
    val INTENT_UUID = "INTENT_UUID"
    val INTENT_WIPE = "INTENT_WIPE"
    val INTENT_DELETE = "INTENT_DELETE"
    val INTENT_DELETE_MODE = "INTENT_DELETE_MODE"

    val SETTINGS_BACKGROUND_COLOR = "SETTINGS_BACKGROUND_COLOR"
    val SETTINGS_ACCENT_COLOR = "SETTINGS_ACCENT_COLOR"

    val SETTINGS_CLOCK_ENABLED = "SETTINGS_CLOCK_ENABLED"
    val SETTINGS_24H_CLOCK_ENABLED = "SETTINGS_24H_CLOCK_ENABLED"
    val SETTINGS_HAPTICS_ENABLED = "SETTINGS_HAPTICS_ENABLED"
    val SETTINGS_BEEP_ENABLED = "SETTINGS_BEEP_ENABLED"
    val CONFIG_SCREEN_ROUND = "CONFIG_SCREEN_ROUND"
    val SETTINGS_LOCK_ENABLED = "SETTINGS_LOCK_ENABLED"

    val MFA_TIME_MODE = "totp"
    val MFA_COUNTER_MODE = "hotp"

    val ALGO_SHA1 = "SHA1"
    val ALGO_SHA256 = "SHA256"
    val ALGO_SHA512 = "SHA512"

    var masterKeyAlias: String = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val accountsFilename: String = "vault.wfs" // WristkeyFS
    var vault: SharedPreferences

    init {
        vault = EncryptedSharedPreferences.create (
            accountsFilename,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    data class MfaCode (
        val type: String?,
        val mode: String?,
        val issuer: String?,
        val account: String?,
        val secret: String?,
        val algorithm: String?,
        val digits: Int?,
        val period: Int?,
        val lock: Boolean?,
        val counter: Long?,
        val label: String?,
    )

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
                        type = "otpauth",
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

        for (itemIndex in 0 until itemsArray.length()) {
            val itemData = JSONObject(itemsArray[itemIndex].toString())
            try {
                val accountData = JSONObject(itemData["login"].toString())
                val totpSecret = accountData["totp"]
                val username = accountData["username"]
                val sitename = itemData["name"]
                val uuid = itemData["id"].toString()
                val accountName: String = if (username.toString() == "null" || username.toString().isBlank()) sitename.toString()
                else username.toString()

                var totp = ""
                if (totpSecret.toString() != "null" || username.toString().isNotBlank()) totp = totpSecret.toString()

                if (totp.startsWith("otpauth://")) {
                    val type: String = if (totp.substringAfter("://").substringBefore("/").contains("totp")) "totp" else "hotp"
                    val issuer: String = totp.substringAfterLast("otp/").substringBefore(":")
                    val account: String = totp.substringAfterLast(":").substringBefore("?")
                    val secret: String? = if (totp.contains("secret")) totp.substringAfter("secret=").substringBefore("&") else null
                    val algorithm: String = if (totp.contains("algorithm")) totp.substringAfter("algorithm=").substringBefore("&") else ALGO_SHA1
                    val digits: Int = if (totp.contains("digits")) totp.substringAfter("digits=").substringBefore("&").toInt() else 6
                    val period: Int = if (totp.contains("period")) totp.substringAfter("period=").substringBefore("&").toInt() else 30
                    val lock: Boolean = if (totp.contains("lock")) totp.substringAfter("lock=").substringBefore("&").toBoolean() else false
                    val counter: Long = if (totp.contains("counter")) totp.substringAfter("counter=").substringBefore("&").toLong() else 0
                    val label: String = if (totp.contains("label")) totp.substringAfter("label=").substringBefore("&") else account

                    logins.add (
                        MfaCode(
                            type = "otpauth",
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

                } else if (totp.isNotEmpty() && !totp.startsWith("otpauth://")) { // Google Authenticator

                    logins.add (
                        MfaCode(
                            type = "otpauth",
                            mode = "totp",
                            issuer = (accountData["username"] ?: itemData["name"]) as String?,
                            account = (accountData["username"] ?: itemData["name"]) as String?,
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
            } catch (_: JSONException) { }
        }

        return logins

    }

    fun andOtpToWristkey (jsonArray: JSONArray): MutableList<MfaCode> {

        val logins = mutableListOf<MfaCode>()

        for (itemIndex in 0 until jsonArray.length()) {

            val account = jsonArray[itemIndex].toString()
            val secret = JSONObject(account)["secret"].toString().replace("=", "")
            val issuer = JSONObject(account)["issuer"].toString()
            val counter = JSONObject(account)["counter"].toString().toLong()
            val algorithm = JSONObject(account)["algorithm"].toString()
            val digits = JSONObject(account)["digits"].toString().toInt()
            val period = JSONObject(account)["period"].toString().toInt()
            var type = JSONObject(account)["type"].toString().lowercase()
            if (type == "STEAM") type = "totp"
            val label = (JSONObject(account)["label"] ?: account) as String

            logins.add (
                MfaCode(
                    type = "otpauth",
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

        }

        return logins

    }

    fun aegisToWristkey (unencryptedAegisJsonString: String): MutableList<Utilities.MfaCode> {

        val logins = mutableListOf<Utilities.MfaCode>()

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
                            type = "otpauth",
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

    fun decodeOTPAuthURL (OTPAuthURL: String): MfaCode? {
        val url = URLDecoder.decode(OTPAuthURL, "UTF-8")
        if (url.contains("otpauth://")) {
            val type: String =
                if (url.substringBefore("://").contains("migration")) "Google Authenticator Backup"
                else "OTP"
            val mode: String =
                if (url.substringAfter("://").substringBefore("/").contains("totp"))
                    "totp"
                else "hotp"
            val issuer: String = url.substringAfterLast("otp/").substringBefore(":")
            val account: String = url.substringAfterLast(":").substringBefore("?")
            val secret: String? = if (url.contains("secret")) url.substringAfter("secret=").substringBefore("&") else null
            val algorithm: String? = if (url.contains("algorithm")) url.substringAfter("algorithm=").substringBefore("&") else ALGO_SHA1
            val digits: Int? = if (url.contains("digits")) url.substringAfter("digits=").substringBefore("&").toInt() else 6
            val period: Int? = if (url.contains("period")) url.substringAfter("period=").substringBefore("&").toInt() else 30
            val lock: Boolean? = if (url.contains("lock")) url.substringAfter("lock=").substringBefore("&").toBoolean() else false
            val counter: Long? = if (url.contains("counter")) url.substringAfter("counter=").substringBefore("&").toLong() else 0
            val label: String? = if (url.contains("label")) url.substringAfter("label=").substringBefore("&") else account

            return MfaCode(
                type = type,
                mode = mode,
                issuer = issuer,
                account = account,
                secret = secret,
                algorithm = algorithm,
                digits = digits,
                period = period,
                lock = lock,
                counter = counter,
                label = label,
            )
        } else {
            return null
        }
    }

    fun encodeOTPAuthURL (mfaCodeObject: MfaCode): String? {
        lateinit var type: String
        lateinit var issuer: String
        lateinit var account: String
        lateinit var secret: String

        if (mfaCodeObject.type.toString().isNotEmpty())
            type = if (mfaCodeObject.type.toString().lowercase().contains("backup") || mfaCodeObject.type.toString().lowercase().contains("migration")) "otpauth-migration" else "otpauth"
        else return null

        val mode: String = if (mfaCodeObject.mode.toString().isNotEmpty())
            if (mfaCodeObject.mode.toString().lowercase().contains("time") || mfaCodeObject.mode.toString().contains("totp")) "totp" else "hotp"
        else "totp"

        account = mfaCodeObject.account.toString().ifEmpty { "" }

        issuer = mfaCodeObject.issuer.toString().ifEmpty { account }

        if (mfaCodeObject.secret.toString().isNotEmpty())
            secret = mfaCodeObject.secret.toString().trim().trim().replace(" ", "")
        else return null

        val algorithm: String = mfaCodeObject.algorithm.toString().ifEmpty { "SHA1" }

        val digits: String = mfaCodeObject.digits.toString().ifEmpty { "6" }

        val period: String = mfaCodeObject.period.toString().ifEmpty { "30" }

        val lock: String = mfaCodeObject.lock.toString().ifEmpty { "false" }

        val counter: String = mfaCodeObject.counter.toString().ifEmpty { "0" }

        val label: String = mfaCodeObject.label.toString().ifEmpty { "" }

        return "$type://$mode/$issuer:$account?secret=$secret&algorithm=$algorithm&digits=$digits&period=$period&lock=$lock&counter=$counter&label=$label"
    }

    fun writeToVault (mfaCodeObject: MfaCode, uuid4: String): Boolean {
        val mfaCodeObjectAsString = encodeOTPAuthURL (mfaCodeObject = mfaCodeObject)
        val data = getLogins()

        vault.edit().remove(uuid4).commit()
        vault.edit().putString(uuid4, mfaCodeObjectAsString).commit()

        return true
    }

    fun deleteFromVault (uuid4: String): Boolean {
        val items  = vault.all

        for (item in items) {
            if (item.key.contains(uuid4)) {
                vault.edit().remove(item.key).apply()
            }
        }

        return true
    }

    fun getUuid (login: MfaCode): String? {
        val items  = getVaultLoginsOnly()
        for ((key, value) in items) if (value.contains(login.secret.toString())) return key
        return null
    }

    fun getLogin (uuid: String): MfaCode? {
        val items  = vault.all
        var value: MfaCode? = null

        for (item in items) {
            try {
                value = decodeOTPAuthURL(item.value as String) as MfaCode
                if (item.key == uuid) return value
            } catch (_: Exception) { }
        }

        return value
    }

    fun overwriteLogin (oldLogin: MfaCode, newLogin: MfaCode): Boolean {

        val items  = vault.all
        var key: String? = null

        for (item in items) {
            key = item.key
            if (item.value == oldLogin) {
                vault.edit().remove(key).apply()
                break
            }
        }

        vault.edit().putString(key, encodeOTPAuthURL(newLogin)).apply()

        return true
    }

    fun getVault (): List<MfaCode> {
        var vault = vault.all.values.toList()
        if (vault.isEmpty()) vault = mutableListOf<MfaCode>()
        return vault as MutableList<MfaCode>
    }

    fun getVaultLoginsOnly (): Map<String, String> {
        val vault = vault.all
        val finalVault = mutableMapOf<String, String>()
        for ((key, value) in vault) {
            try {
                UUID.fromString(key)
                finalVault[key] = value.toString()
            } catch (_: IllegalArgumentException) { }
        }
        return finalVault
    }

    fun getLogins (): List<MfaCode> {
        val items  = vault.all
        val logins = mutableListOf<MfaCode>()
        var key: String? = null

        for (item in items) {
            key = item.key
            try {
                val uuid = UUID.fromString(item.key as String)
                logins.add(decodeOTPAuthURL(item.value as String)!!)
            } catch (_: IllegalArgumentException) { }
        }

        return logins
    }

    fun beep () {
        try {
            val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            toneGen1.startTone(ToneGenerator.TONE_SUP_INTERCEPT, 150)
        } catch (_: Exception) { }
    }

}

// UI stuff
open class OnSwipeTouchListener(c: Context?) : View.OnTouchListener {
    private val gestureDetector: GestureDetector
    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(motionEvent!!)
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            onClick()
            return super.onSingleTapUp(e)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            onDoubleClick()
            return super.onDoubleTap(e)
        }

        override fun onLongPress(e: MotionEvent) {
            onLongClick()
            super.onLongPress(e)
        }

        // Determines the fling velocity and then fires the appropriate swipe event accordingly
        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            val result = false
            try {
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight()
                        } else {
                            onSwipeLeft()
                        }
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeDown()
                        } else {
                            onSwipeUp()
                        }
                    }
                }
            } catch (exception: java.lang.Exception) {
                exception.printStackTrace()
            }
            return result
        }

    }

    open fun onSwipeRight() {}
    open fun onSwipeLeft() {}
    open fun onSwipeUp() {}
    open fun onSwipeDown() {}
    open fun onClick() {}
    fun onDoubleClick() {}
    open fun onLongClick() {}

    init {
        gestureDetector = GestureDetector(c, GestureListener())
    }
}
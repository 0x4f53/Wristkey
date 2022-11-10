package app.wristkey

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.net.URLDecoder

@RequiresApi(Build.VERSION_CODES.M)
class Utilities (context: Context) {

    val context = context
    val DEFAULT_TYPE = "otpauth"
    val CONFIG_SCREEN_ROUND = "CONFIG_SCREEN_ROUND"
    val INTENT_UUID = "INTENT_UUID"
    val INTENT_WIPE = "INTENT_WIPE"
    val INTENT_DELETE = "INTENT_DELETE"
    val INTENT_DELETE_MODE = "INTENT_DELETE_MODE"

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

    fun decodeOTPAuthURL (OTPAuthURL: String): MfaCode? {
        val url = URLDecoder.decode(OTPAuthURL, "UTF-8")
        if (url.contains("otpauth") && url.contains("://")) {
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
            val algorithm: String? = if (url.contains("algorithm")) url.substringAfter("algorithm=").substringBefore("&") else null
            val digits: Int? = if (url.contains("digits")) url.substringAfter("digits=").substringBefore("&").toInt() else null
            val period: Int? = if (url.contains("period")) url.substringAfter("period=").substringBefore("&").toInt() else null
            val lock: Boolean? = if (url.contains("lock")) url.substringAfter("lock=").substringBefore("&").toBoolean() else null
            val counter: Long? = if (url.contains("counter")) url.substringAfter("counter=").substringBefore("&").toLong() else null
            val label: String? = if (url.contains("label")) url.substringAfter("label=").substringBefore("&") else account

            return MfaCode (
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
        val data = vault.getString(accountsFilename, null)

        if (data == null) Log.d ("Wristkey", "Congratulations on your first entry!")
        vault.edit().putString(uuid4, mfaCodeObjectAsString).apply()

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
        val items  = vault.all
        var key: String? = null

        for (item in items) {
            key = item.key
            if (item.value == login) {
                return key
            }
        }

        return key
    }

    fun getLogin (uuid: String): MfaCode? {
        val items  = vault.all
        var value: MfaCode? = null

        for (item in items) {
            value = decodeOTPAuthURL(item.value as String) as MfaCode
            if (item.key == uuid) {
                return value
            }
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
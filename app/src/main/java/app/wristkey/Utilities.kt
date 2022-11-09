package app.wristkey

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.net.URLDecoder
import java.util.*

@RequiresApi(Build.VERSION_CODES.M)
class Utilities (context: Context) {

    val context = context
    val DEFAULT_TYPE = "otpauth"

    var masterKeyAlias: String = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val accountsFilename: String = "vault.wfs" // WristkeyFS
    var vault: SharedPreferences

    init {
        vault = EncryptedSharedPreferences.create(
            accountsFilename,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    data class MfaCode (
        var uuid4: String,
        val type: String?,
        val mode: String?,
        val issuer: String?,
        val account: String?,
        val secret: String?,
        val algorithm: String?,
        val digits: Int?,
        val period: Int?,
        val lock: Boolean?,
        val counter: Int?,
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
            val issuer: String? = if (url.contains("issuer")) url.substringAfter("issuer=").substringBefore("&") else null
            val account: String? = if (url.contains("otp")) url.substringAfter("otp/").substringBefore("?") else null
            val secret: String? = if (url.contains("secret")) url.substringAfter("secret=").substringBefore("&") else null
            val algorithm: String? = if (url.contains("algorithm")) url.substringAfter("algorithm=").substringBefore("&") else null
            val digits: Int? = if (url.contains("digits")) url.substringAfter("digits=").substringBefore("&").toInt() else null
            val period: Int? = if (url.contains("period")) url.substringAfter("period=").substringBefore("&").toInt() else null
            val lock: Boolean? = if (url.contains("lock")) url.substringAfter("lock=").substringBefore("&").toBoolean() else null
            val counter: Int? = if (url.contains("counter")) url.substringAfter("counter=").substringBefore("&").toInt() else null
            val label: String? = if (url.contains("label")) url.substringAfter("label=").substringBefore("&") else null

            return MfaCode (
                uuid4 = UUID.randomUUID().toString(),
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

        if (mfaCodeObject.account.toString().isNotEmpty())
            account = mfaCodeObject.account.toString()
        else return null

        issuer = mfaCodeObject.issuer.toString().ifEmpty { account }

        if (mfaCodeObject.secret.toString().isNotEmpty())
            secret = mfaCodeObject.secret.toString().trim().trim().replace(" ", "")
        else return null

        val algorithm: String = mfaCodeObject.algorithm.toString().ifEmpty { "SHA1" }

        val digits: String = if (mfaCodeObject.digits.toString().isNotEmpty())
            mfaCodeObject.digits.toString()
        else "6"

        val period: String = mfaCodeObject.period.toString().ifEmpty { "30" }

        val lock: String = mfaCodeObject.lock.toString().ifEmpty { "false" }

        val counter: String = mfaCodeObject.counter.toString().ifEmpty { "0" }

        val label: String = mfaCodeObject.label.toString().ifEmpty { "label" }

        return "$type://$mode/$issuer:$account?secret=$secret&algorithm=$algorithm&digits=$digits&period=$period&lock=$lock&counter=$counter&label=$label"
    }

    fun writeToVault (mfaCodeObject: MfaCode): Boolean {
        val mfaCodeObjectAsString = encodeOTPAuthURL (mfaCodeObject = mfaCodeObject)
        val data = vault.getString(accountsFilename, null)

        if (data == null) Log.d ("Wristkey", "Congratulations on your first entry!")
        vault.edit().putString(mfaCodeObject.uuid4, mfaCodeObjectAsString).apply()

        return true
    }

    fun deleteFromVault (uuid4: String): Boolean {
        val items  = vault.all

        for (item in items) {
            if (item.key == uuid4) {
                vault.edit().remove(item.key).apply()
                return true
            }
        }

        return false
    }

    fun getVault (): List<MfaCode> {
        val mfaCodesList = mutableListOf<MfaCode>()

        val items  = vault.all

        for (item in items) {
            val login = decodeOTPAuthURL (item.value.toString())
            if (login != null) {
                login.uuid4 = item.key
                mfaCodesList.add (login)
            }
        }

        return mfaCodesList
    }

}
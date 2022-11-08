package app.wristkey

import android.Manifest
import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.support.wearable.activity.WearableActivity
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import androidx.wear.widget.BoxInsetLayout
import dev.turingcomplete.kotlinonetimepassword.*
import org.json.JSONArray
import wristkey.R
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

lateinit var masterKeyAlias: String
public const val accountsFile: String = "accounts"
public const val appDataFile: String = "app_data"
public lateinit var accounts: SharedPreferences
public lateinit var appData: SharedPreferences
public const val CODE_AUTHENTICATION_VERIFICATION = 241

class MainActivity : WearableActivity() {
    var appExited: Boolean = false
    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val code = 0x3
            try {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), code)
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }

        masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        appData = EncryptedSharedPreferences.create(
            appDataFile,
            masterKeyAlias,
            applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        if (appData.getBoolean("screen_lock", true)) {
            val lockscreen = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            if (lockscreen.isKeyguardSecure) {
                val i = lockscreen.createConfirmDeviceCredentialIntent("Wristkey", "App locked")
                startActivityForResult(i, CODE_AUTHENTICATION_VERIFICATION)
            }
        }

        accounts = EncryptedSharedPreferences.create(
            accountsFile,
            masterKeyAlias,
            applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (appData.getBoolean("ambient_mode", false)) {
            setAmbientEnabled() // disabled by default because app contains sensitive information
        }

        var timeLeft: ProgressBar

        if (applicationContext.resources.configuration.isScreenRound) {
            timeLeft = findViewById(R.id.RoundTimeLeft)
            timeLeft.visibility = View.GONE
        } else {
            timeLeft = findViewById(R.id.SquareTimeLeftTop)
            timeLeft.visibility = View.GONE
        }

        val boxinsetlayout = findViewById<BoxInsetLayout>(R.id.BoxInsetLayout)
        val addAccountButton = findViewById<CardView>(R.id.AddAccountButton)
        val settingsButton = findViewById<CardView>(R.id.SettingsButton)
        val aboutButton = findViewById<CardView>(R.id.AboutButton)

        val currentTheme = appData.getString("theme", "000000")
        val currentAccent = appData.getString("accent", "4285F4")
        boxinsetlayout.setBackgroundColor(Color.parseColor("#" + currentTheme))
        timeLeft.progressTintList = ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
        timeLeft.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#" + currentTheme))
        if (currentTheme == "F7F7F7") {
            //addAccountButton.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
            //addAccountButton.compoundDrawableTintList = ColorStateList.valueOf(Color.parseColor("#000000"))
            //aboutButton.imageTintList = ColorStateList.valueOf(Color.parseColor("#000000"))
            //settingsButton.imageTintList = ColorStateList.valueOf(Color.parseColor("#000000"))
        } else {
            //addAccountButton.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
            //addAccountButton.compoundDrawableTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
            //aboutButton.imageTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
            //settingsButton.imageTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
        }

        fun getData(){
            val timeRecyclerView = findViewById<RecyclerView>(R.id.TimeaccountList)
            timeRecyclerView.layoutManager = LinearLayoutManager(applicationContext, LinearLayout.VERTICAL, false) // adding a LayoutManager
            val counterRecyclerView = findViewById<RecyclerView>(R.id.CounteraccountList)
            counterRecyclerView.layoutManager = LinearLayoutManager(applicationContext, LinearLayout.VERTICAL, false) // adding a LayoutManager
            val timeBasedAccounts = ArrayList<Account>() // creating an arrayList to store users using the data class
            val counterBasedAccounts = ArrayList<Account>() // creating an arrayList to store users using the data class

            val keys: Map<String, *> = accounts.all

            for ((key, _) in keys) {
                val accountData = accounts.getString(key, null)
                val accountList = ArrayList<String>()

                try {
                    val jArray = JSONArray(accountData)
                    for (i in 0 until jArray.length()) {
                        accountList.add(jArray.getString(i))
                    }

                    val accountName = accountList[0]
                    val secret = accountList[1]
                    val mode = accountList[2]
                    val digits = accountList[3]
                    val algorithm = accountList[4]
                    val counter = accountList[5]
                    if (mode == "Time") {
                        if(algorithm=="HmacAlgorithm.SHA1" && digits == "6"){
                            // Google Authenticator
                            val googleAuthenticator = GoogleAuthenticator(base32secret = secret) // "OurSharedSecret" Base32-encoded
                            val totp = googleAuthenticator.generate()
                            timeBasedAccounts.add(Account(key, accountName, totp, counter))
                        }else if(algorithm=="HmacAlgorithm.SHA1" && digits != "6"){
                            val config = TimeBasedOneTimePasswordConfig(
                                codeDigits = digits.toInt(),
                                hmacAlgorithm = HmacAlgorithm.SHA1,
                                timeStep = 30,
                                timeStepUnit = TimeUnit.SECONDS
                            )
                            val totp = TimeBasedOneTimePasswordGenerator(secret.toByteArray(), config)

                        }else if(algorithm=="HmacAlgorithm.SHA256"){
                            val config = TimeBasedOneTimePasswordConfig(
                                codeDigits = digits.toInt(),
                                hmacAlgorithm = HmacAlgorithm.SHA256,
                                timeStep = 30,
                                timeStepUnit = TimeUnit.SECONDS
                            )
                            val totp = TimeBasedOneTimePasswordGenerator(secret.toByteArray(), config)

                        }else if(algorithm=="HmacAlgorithm.SHA512"){
                            val config = TimeBasedOneTimePasswordConfig(
                                codeDigits = digits.toInt(),
                                hmacAlgorithm = HmacAlgorithm.SHA512,
                                timeStep = 30,
                                timeStepUnit = TimeUnit.SECONDS
                            )
                            val totp = TimeBasedOneTimePasswordGenerator(secret.toByteArray(), config)

                        }
                    } else if (mode == "Counter") {
                        if (algorithm=="HmacAlgorithm.SHA1"){
                            val config = HmacOneTimePasswordConfig(
                                codeDigits = digits.toInt(),
                                hmacAlgorithm = HmacAlgorithm.SHA1
                            )
                            val cotp = HmacOneTimePasswordGenerator(secret.toByteArray(), config)
                            counterBasedAccounts.add(
                                Account(
                                    key,
                                    accountName,
                                    cotp.generate(counter = counter.toLong()),
                                    counter
                                )
                            )
                        }else if(algorithm=="HmacAlgorithm.SHA256"){
                            val config = HmacOneTimePasswordConfig(
                                codeDigits = digits.toInt(),
                                hmacAlgorithm = HmacAlgorithm.SHA256
                            )
                            val cotp = HmacOneTimePasswordGenerator(secret.toByteArray(), config)
                            counterBasedAccounts.add(
                                Account(
                                    key,
                                    accountName,
                                    cotp.generate(counter = counter.toLong()),
                                    counter
                                )
                            )
                        }else if(algorithm=="HmacAlgorithm.SHA512"){
                            val config = HmacOneTimePasswordConfig(
                                codeDigits = digits.toInt(),
                                hmacAlgorithm = HmacAlgorithm.SHA512
                            )
                            val cotp = HmacOneTimePasswordGenerator(secret.toByteArray(), config)
                            counterBasedAccounts.add(
                                Account(
                                    key,
                                    accountName,
                                    cotp.generate(counter = counter.toLong()),
                                    counter
                                )
                            )
                        }
                    }
                } catch (nullPointer: NullPointerException) {

                } catch (noData: ArrayIndexOutOfBoundsException) {

                }

            }

            val timeAdapter = TimeCardAdapter(applicationContext, timeBasedAccounts){} //creating adapter
            timeRecyclerView.adapter = timeAdapter //now adding adapter to recyclerview

            val counterAdapter = CounterCardAdapter(applicationContext, counterBasedAccounts){} //creating adapter
            counterRecyclerView.adapter = counterAdapter //now adding adapter to recyclerview
        }

        getData()

        object : Thread() {
            override fun run() {
                try {
                    while (!appExited) {
                        sleep(1000)
                        runOnUiThread {
                            getData()
                        }
                    }
                } catch (e: InterruptedException) { }
            }
        }.start()

        fun getTimerUI() {
            val toneG = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            val tone = ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_PING_RING
            val toneLength = 50
            var currentSecondsValue = SimpleDateFormat("s", Locale.getDefault()).format(Date()).toInt()
            if (currentSecondsValue in 30..59) {
                if (currentSecondsValue == 59) {
                    if (appData.getBoolean("beep", false)) toneG.startTone(tone, toneLength)

                    if (appData.getBoolean("vibrate", false)) {
                        val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        vibratorService.vibrate(25)
                    }
                }
                timeLeft.progress = 59-currentSecondsValue
            } else {
                if (currentSecondsValue == 29) {
                    if (appData.getBoolean("beep", false)) toneG.startTone(tone, toneLength)

                    if (appData.getBoolean("vibrate", false)) {
                        val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        vibratorService.vibrate(25)
                    }
                }
                timeLeft.progress = 29-currentSecondsValue
            }
        }

        getTimerUI()

        if (accounts.all.isNotEmpty()) {
            timeLeft.visibility = View.VISIBLE
            object : Thread() {
                override fun run() {
                    try {
                        while (!appExited) {
                            sleep(1000)
                            runOnUiThread {
                                getTimerUI()
                            }
                        }
                    } catch (e: InterruptedException) { }
                }
            }.start()
        }

        addAccountButton.setOnClickListener {
            val intent = Intent(applicationContext, AddActivity::class.java)
            startActivity(intent)
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(100)
            finish()
        }

        aboutButton.setOnClickListener {
            val intent = Intent(applicationContext, AboutActivity::class.java)
            startActivity(intent)
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
        }

        settingsButton.setOnClickListener {
            val intent = Intent(applicationContext, SettingsActivity::class.java)
            startActivity(intent)
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
            finish()
        }
    }

    override fun onStop() {
        super.onStop()
        appExited=true
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (!(resultCode == RESULT_OK && requestCode == CODE_AUTHENTICATION_VERIFICATION)) {
            finish()
        }
    }

}
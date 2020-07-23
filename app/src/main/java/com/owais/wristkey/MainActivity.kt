package com.owais.wristkey
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Vibrator
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.widget.BoxInsetLayout
import dev.turingcomplete.kotlinonetimepassword.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess


class MainActivity : WearableActivity() {
    var appExited: Boolean = false
    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // setAmbientEnabled() disabled because app contains sensitive information
        val boxinsetlayout = findViewById<BoxInsetLayout>(R.id.BoxInsetLayout)
        val addAccountButton = findViewById<TextView>(R.id.AddAccountButton)
        val settingsButton = findViewById<ImageView>(R.id.SettingsButton)
        val aboutButton = findViewById<ImageView>(R.id.AboutButton)
        val timeLeft = findViewById<ProgressBar>(R.id.TimeLeft)
        val storageFile = "app_storage"
        val storage: SharedPreferences = applicationContext.getSharedPreferences(storageFile, Context.MODE_PRIVATE)
        val storageEditor: SharedPreferences.Editor =  storage.edit()
        var currentTheme = storage.getString("theme", "000000")
        var currentAccent = storage.getString("accent", "4285F4")
        boxinsetlayout.setBackgroundColor(Color.parseColor("#"+currentTheme))
        timeLeft.progressTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        timeLeft.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#"+currentTheme))
        if (currentTheme == "F7F7F7") {
            addAccountButton.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
            addAccountButton.compoundDrawableTintList = ColorStateList.valueOf(Color.parseColor("#000000"))
            aboutButton.imageTintList = ColorStateList.valueOf(Color.parseColor("#000000"))
            settingsButton.imageTintList = ColorStateList.valueOf(Color.parseColor("#000000"))
        } else {
            addAccountButton.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
            addAccountButton.compoundDrawableTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
            aboutButton.imageTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
            settingsButton.imageTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
        }
        fun getData(){
            val timeRecyclerView = findViewById<RecyclerView>(R.id.TimeTokenList)
            timeRecyclerView.layoutManager = LinearLayoutManager(applicationContext, LinearLayout.VERTICAL, false) // adding a LayoutManager
            val counterRecyclerView = findViewById<RecyclerView>(R.id.CounterTokenList)
            counterRecyclerView.layoutManager = LinearLayoutManager(applicationContext, LinearLayout.VERTICAL, false) // adding a LayoutManager
            val timeBasedTokens = ArrayList<Token>() //creating an arrayList to store users using the data class user
            val counterBasedTokens = ArrayList<Token>() //creating an arrayList to store users using the data class user
            var serialNumberCount = storage.getInt("currentSerialNumber", 0)
            for (count in 1..serialNumberCount) {
                val tokenNumber = count
                val accountName=((((storage.getString(count.toString(), "").toString()).replaceBefore("■", "")).replaceAfter("▰", "")).replace("■", "")).replace("▰", "")
                val secret=((((storage.getString(count.toString(), "").toString()).replaceBefore("▰", "")).replaceAfter("◀", "")).replace("▰", "")).replace("◀", "")
                val mode=((((storage.getString(count.toString(), "").toString()).replaceBefore("◀", "")).replaceAfter("▾", "")).replace("◀", "")).replace("▾", "")
                val digits=((((storage.getString(count.toString(), "").toString()).replaceBefore("▾", "")).replaceAfter("●", "")).replace("▾", "")).replace("●", "")
                val algorithm=((((storage.getString(count.toString(), "").toString()).replaceBefore("●", "")).replaceAfter("◆", "")).replace("●", "")).replace("◆", "")
                val counter=((((storage.getString(count.toString(), "").toString()).replaceBefore("◆", "")).replaceAfter("▮", "")).replace("◆", "")).replace("▮", "")
                if (mode == "Time") {
                    if(algorithm=="HmacAlgorithm.SHA1" && digits == "6"){
                        // Google authenticator
                        val googleAuthenticator = GoogleAuthenticator(base32secret = secret) // "OurSharedSecret" Base32-encoded
                        var totp = googleAuthenticator.generate()
                        timeBasedTokens.add(Token(tokenNumber, accountName, totp.toString(), counter))
                    }else if(algorithm=="HmacAlgorithm.SHA1" && digits != "6"){
                        val config = TimeBasedOneTimePasswordConfig(codeDigits = digits.toInt(), hmacAlgorithm = HmacAlgorithm.SHA1, timeStep = 30, timeStepUnit = TimeUnit.SECONDS)
                        val totp = TimeBasedOneTimePasswordGenerator(secret.toByteArray(), config)
                        timeBasedTokens.add(Token(tokenNumber, accountName, totp.generate().toString(), counter))
                    }else if(algorithm=="HmacAlgorithm.SHA256"){
                        val config = TimeBasedOneTimePasswordConfig(codeDigits = digits.toInt(), hmacAlgorithm = HmacAlgorithm.SHA256, timeStep = 30, timeStepUnit = TimeUnit.SECONDS)
                        val totp = TimeBasedOneTimePasswordGenerator(secret.toByteArray(), config)
                        timeBasedTokens.add(Token(tokenNumber, accountName, totp.generate().toString(), counter))
                    }else if(algorithm=="HmacAlgorithm.SHA512"){
                        val config = TimeBasedOneTimePasswordConfig(codeDigits = digits.toInt(), hmacAlgorithm = HmacAlgorithm.SHA512, timeStep = 30, timeStepUnit = TimeUnit.SECONDS)
                        val totp = TimeBasedOneTimePasswordGenerator(secret.toByteArray(), config)
                        timeBasedTokens.add(Token(tokenNumber, accountName, totp.generate().toString(), counter))
                    }
                } else if (mode == "Counter") {
                    if (algorithm=="HmacAlgorithm.SHA1"){
                        val config = HmacOneTimePasswordConfig(codeDigits = digits.toInt(), hmacAlgorithm = HmacAlgorithm.SHA1)
                        val cotp = HmacOneTimePasswordGenerator(secret.toByteArray(), config)
                        counterBasedTokens.add(Token(tokenNumber, accountName, cotp.generate(counter = counter.toLong()), counter))
                    }else if(algorithm=="HmacAlgorithm.SHA256"){
                        val config = HmacOneTimePasswordConfig(codeDigits = digits.toInt(), hmacAlgorithm = HmacAlgorithm.SHA256)
                        val cotp = HmacOneTimePasswordGenerator(secret.toByteArray(), config)
                        counterBasedTokens.add(Token(tokenNumber, accountName, cotp.generate(counter = counter.toLong()), counter))
                    }else if(algorithm=="HmacAlgorithm.SHA512"){
                        val config = HmacOneTimePasswordConfig(codeDigits = digits.toInt(), hmacAlgorithm = HmacAlgorithm.SHA512)
                        val cotp = HmacOneTimePasswordGenerator(secret.toByteArray(), config)
                        counterBasedTokens.add(Token(tokenNumber, accountName, cotp.generate(counter = counter.toLong()), counter))
                    }
                }
            }
            val timeAdapter = TimeCardAdapter(applicationContext, timeBasedTokens){} //creating adapter
            timeRecyclerView.adapter = timeAdapter //now adding adapter to recyclerview
            val counterAdapter = CounterCardAdapter(applicationContext, counterBasedTokens){} //creating adapter
            counterRecyclerView.adapter = counterAdapter //now adding adapter to recyclerview
        }
        fun getTimerUI() {
            var currentSecondsValue = SimpleDateFormat("s", Locale.getDefault()).format(Date()).toInt()
            if (currentSecondsValue in 30..59) {
                if (currentSecondsValue == 59) {
                    val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    vibratorService.vibrate(25)
                }
                timeLeft.progress = 59-currentSecondsValue
            } else {
                if (currentSecondsValue == 29) {
                    val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    vibratorService.vibrate(25)
                }
                timeLeft.progress = 29-currentSecondsValue
            }
        }
        getTimerUI()
        getData()
        val timingThread = object : Thread() {
            override fun run() {
                try {
                    while (!appExited) {
                        sleep(1000)
                        runOnUiThread {
                            getTimerUI()
                        }
                    }
                } catch (e: InterruptedException) {
                }
            }
        }
        val tokenThread = object : Thread() {
            override fun run() {
                try {
                    while (!appExited) {
                        sleep(250)
                        runOnUiThread {
                            getData()
                        }
                    }
                } catch (e: InterruptedException) {
                }
            }
        }
        timingThread.start()
        tokenThread.start()
        addAccountButton.setOnClickListener {
            val intent = Intent(applicationContext, AddActivity::class.java)
            startActivity(intent)
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(100)
            finish()
        }
        aboutButton.setOnClickListener {
            val intent = Intent(applicationContext,AboutActivity::class.java)
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
}
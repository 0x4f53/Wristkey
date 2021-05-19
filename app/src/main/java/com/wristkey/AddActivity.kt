package com.wristkey
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.support.wearable.activity.WearableActivity
import android.text.Html
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.wear.widget.BoxInsetLayout
import com.google.gson.Gson
import java.util.*


class AddActivity : WearableActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)
        val boxinsetlayout = findViewById<BoxInsetLayout>(R.id.BoxInsetLayout)
        val importBitwardenButtonText = findViewById<TextView>(R.id.ImportBitwardenButtonLabel)
        val importBitwardenButton = findViewById<ImageView>(R.id.ImportBitwardenButton)
        val manualEntry = findViewById<LinearLayout>(R.id.ManualEntry)
        val manualEntryButton = findViewById<ImageView>(R.id.ManualEntryButton)
        val manualEntryButtonText = findViewById<TextView>(R.id.ManualEntryButtonLabel)
        val importBitwarden = findViewById<LinearLayout>(R.id.ImportBitwardenTokens)
        val importAuthenticatorButtonText = findViewById<TextView>(R.id.ImportAuthenticatorButtonLabel)
        val importAuthenticator = findViewById<LinearLayout>(R.id.ImportAuthenticatorTokens)
        val importAuthenticatorButton = findViewById<ImageView>(R.id.ImportAuthenticatorButton)
        val backButton = findViewById<ImageView>(R.id.BackButton)
        val appData: SharedPreferences = applicationContext.getSharedPreferences(appDataFile, Context.MODE_PRIVATE)
        var currentAccent = appData.getString("accent", "4285F4")
        var currentTheme = appData.getString("theme", "000000")

        boxinsetlayout.setBackgroundColor(Color.parseColor("#"+currentTheme))
        manualEntryButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        importBitwardenButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        importAuthenticatorButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        backButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))

        if (currentTheme == "F7F7F7") {
            manualEntryButtonText.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
            importBitwardenButtonText.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
            importAuthenticatorButtonText.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
        } else {
            manualEntryButtonText.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
            importBitwardenButtonText.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
            importAuthenticatorButtonText.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
        }

        manualEntry.setOnClickListener {
            val intent = Intent(applicationContext, ManualEntryActivity::class.java)
            startActivity(intent)
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
            finish()
        }

        importBitwarden.setOnClickListener {
            val intent = Intent(applicationContext, BitwardenJSONImport::class.java)
            startActivity(intent)
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
            finish()
        }

        importAuthenticator.setOnClickListener {
            val intent = Intent(applicationContext, AuthenticatorQRImport::class.java)
            startActivity(intent)
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
            finish()
        }

        backButton.setOnClickListener {
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
            finish()
        }

    }

}
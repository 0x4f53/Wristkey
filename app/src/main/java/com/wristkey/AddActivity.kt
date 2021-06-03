package com.wristkey
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Vibrator
import android.support.wearable.activity.WearableActivity
import android.widget.*
import androidx.wear.widget.BoxInsetLayout

class AddActivity : WearableActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)
        val boxinsetlayout = findViewById<BoxInsetLayout>(R.id.BoxInsetLayout)
        val importBitwardenButtonText = findViewById<TextView>(R.id.BitwardenImportLabel)
        val importBitwardenButton = findViewById<ImageView>(R.id.BitwardenImportButton)
        val manualEntry = findViewById<LinearLayout>(R.id.ManualEntry)
        val manualEntryButton = findViewById<ImageView>(R.id.ManualEntryButton)
        val manualEntryButtonText = findViewById<TextView>(R.id.ManualEntryButtonLabel)
        val importBitwarden = findViewById<LinearLayout>(R.id.BitwardenImport)
        val importAuthenticatorButtonText = findViewById<TextView>(R.id.AuthenticatorImportLabel)
        val importAuthenticator = findViewById<LinearLayout>(R.id.AuthenticatorImport)
        val importAuthenticatorButton = findViewById<ImageView>(R.id.AuthenticatorImportButton)
        val aegisImportButton = findViewById<ImageView>(R.id.AegisImportButton)
        val aegisImportLabel = findViewById<TextView>(R.id.AegisImportLabel)
        val aegisImport = findViewById<LinearLayout>(R.id.AegisImport)
        val wristkeyImportButton = findViewById<ImageView>(R.id.WristkeyImportButton)
        val wristkeyImportLabel = findViewById<TextView>(R.id.WristkeyImportLabel)
        val wristkeyImport = findViewById<LinearLayout>(R.id.WristkeyImport)
        val backButton = findViewById<ImageView>(R.id.BackButton)
        var currentAccent = appData.getString("accent", "4285F4")
        var currentTheme = appData.getString("theme", "000000")

        boxinsetlayout.setBackgroundColor(Color.parseColor("#"+currentTheme))
        manualEntryButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        importBitwardenButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        importAuthenticatorButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        aegisImportButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        wristkeyImportButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        backButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))

        if (currentTheme == "F7F7F7") {
            manualEntryButtonText.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
            importBitwardenButtonText.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
            importAuthenticatorButtonText.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
            aegisImportLabel.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
            wristkeyImportLabel.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
        } else {
            manualEntryButtonText.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
            importBitwardenButtonText.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
            importAuthenticatorButtonText.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
            aegisImportLabel.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
            wristkeyImportLabel.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
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

        aegisImport.setOnClickListener {
            val intent = Intent(applicationContext, AegisJSONImport::class.java)
            startActivity(intent)
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
            finish()
        }

        wristkeyImport.setOnClickListener {
            val intent = Intent(applicationContext, WristkeyImport::class.java)
            startActivity(intent)
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
            finish()
        }

        backButton.setOnClickListener {
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

}
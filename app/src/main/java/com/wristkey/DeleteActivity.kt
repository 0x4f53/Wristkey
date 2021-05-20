package com.wristkey

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.support.wearable.activity.WearableActivity
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.wear.widget.BoxInsetLayout

var idForDeleteActivity: String = ""

class DeleteActivity : WearableActivity() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete)
        val confirmationText = findViewById<TextView>(R.id.ConfirmationText)
        val confirmButton = findViewById<ImageButton>(R.id.AuthenticatorConfirmButton)
        val cancelButton = findViewById<ImageButton>(R.id.CancelButton)
        val boxInsetLayout = findViewById<BoxInsetLayout>(R.id.BoxInsetLayout)
        val appData: SharedPreferences =
            applicationContext.getSharedPreferences(appDataFile, Context.MODE_PRIVATE)
        var currentAccent = appData.getString("accent", "4285F4")
        var currentTheme = appData.getString("theme", "000000")
        boxInsetLayout.setBackgroundColor(Color.parseColor("#" + currentTheme))
        confirmButton.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
        if (currentTheme == "F7F7F7") {
            confirmationText.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
        } else {
            confirmationText.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
        }

        if (appData.getBoolean("screen_lock", true)) {
            val lockscreen = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            if (lockscreen.isKeyguardSecure) {
                val i = lockscreen.createConfirmDeviceCredentialIntent("Wristkey", "App locked")
                startActivityForResult(i, CODE_AUTHENTICATION_VERIFICATION)
            }
        }

        confirmButton.setOnClickListener {
            logins.edit().remove(idForDeleteActivity).apply()
            finish()
            Toast.makeText(this, "Token deleted", Toast.LENGTH_SHORT).show()
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(500)
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (!(resultCode == RESULT_OK && requestCode == CODE_AUTHENTICATION_VERIFICATION)) {
            finish()
        }
    }
}
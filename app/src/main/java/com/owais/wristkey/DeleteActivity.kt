package com.owais.wristkey

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.support.wearable.activity.WearableActivity
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.RadioGroup
import androidx.annotation.RequiresApi
import androidx.wear.widget.BoxInsetLayout

var tokenNumberForDeleteActivity: String = ""
class DeleteActivity : WearableActivity() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete)
        val confirmationText = findViewById<TextView>(R.id.ConfirmationText)
        val confirmButton = findViewById<ImageButton>(R.id.ConfirmButton)
        val cancelButton = findViewById<ImageButton>(R.id.CancelButton)
        val boxInsetLayout = findViewById<BoxInsetLayout>(R.id.BoxInsetLayout)
        val storageFile = "app_storage"
        val storage: SharedPreferences = applicationContext.getSharedPreferences(storageFile, Context.MODE_PRIVATE)
        var currentAccent = storage.getString("accent", "4285F4")
        var currentTheme = storage.getString("theme", "000000")
        boxInsetLayout.setBackgroundColor(Color.parseColor("#"+currentTheme))
        confirmButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        if (currentTheme == "F7F7F7") {
            confirmationText.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
        } else {
            confirmationText.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
        }
        confirmButton.setOnClickListener {
            var toast: Toast?
            val storageFile = "wristkey_data_storage"
            val storage: SharedPreferences = applicationContext.getSharedPreferences(storageFile, Context.MODE_PRIVATE)
            val storageEditor: SharedPreferences.Editor =  storage.edit()
            storageEditor.remove(tokenNumberForDeleteActivity)
            storageEditor.apply()
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
            toast = Toast.makeText(this, "Token deleted", Toast.LENGTH_SHORT)
            toast.show()
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(500)
        }
        cancelButton.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
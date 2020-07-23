package com.owais.wristkey


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Vibrator
import android.support.wearable.activity.WearableActivity
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.wear.widget.BoxInsetLayout
import com.google.android.wearable.intent.RemoteIntent


class AboutActivity : WearableActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        val boxinsetlayout = findViewById<BoxInsetLayout>(R.id.BoxInsetLayout)
        val doneButton = findViewById<ImageView>(R.id.DoneButton)
        val appNameText = findViewById<TextView>(R.id.AppName)
        val copyrightText = findViewById<TextView>(R.id.Copyright)
        val descriptionText = findViewById<TextView>(R.id.Description)
        val storageFile = "app_storage"
        val storage: SharedPreferences = applicationContext.getSharedPreferences(storageFile, Context.MODE_PRIVATE)
        var currentTheme = storage.getString("theme", "000000")
        boxinsetlayout.setBackgroundColor(Color.parseColor("#"+currentTheme))
        var currentAccent = storage.getString("accent", "4285F4")
        doneButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        val urlLink = findViewById<TextView>(R.id.SourceCode)
        urlLink.setTextColor(ColorStateList.valueOf(Color.parseColor("#"+currentAccent)))
        if (currentTheme == "F7F7F7") {
            appNameText.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
            copyrightText.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
            descriptionText.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
            urlLink.compoundDrawableTintList = ColorStateList.valueOf(Color.parseColor("#000000"))
        } else {
            appNameText.setTextColor(ColorStateList.valueOf(Color.parseColor("#BDBDBD")))
            copyrightText.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
            descriptionText.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
            urlLink.compoundDrawableTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
        }
        urlLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).addCategory(Intent.CATEGORY_BROWSABLE).setData(Uri.parse("https://gitlab.com/thomascat/wristkey"))
            RemoteIntent.startRemoteActivity(this, intent, null)
            Toast.makeText(applicationContext,"URL opened\non phone", Toast.LENGTH_SHORT).show()
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://gitlab.com/thomascat/wristkey"))
                startActivity(browserIntent)
                Toast.makeText(applicationContext,"URL opened\nin browser", Toast.LENGTH_SHORT).show()
            } catch (ex: Exception) { }
        }
        doneButton.setOnClickListener {
            finish()
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
        }
    }
}

package com.owais.wristkey

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Vibrator
import android.support.wearable.activity.WearableActivity
import android.text.Html
import android.util.Log
import android.widget.*
import androidx.wear.widget.BoxInsetLayout


class SettingsActivity : WearableActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val boxinsetlayout = findViewById<BoxInsetLayout>(R.id.BoxInsetLayout)
        val settingsLabelText = findViewById<TextView>(R.id.SettingsLabel)
        val themeLabelText = findViewById<TextView>(R.id.ThemeLabel)
        val accentLabelText = findViewById<TextView>(R.id.AccentLabel)
        val importButtonText = findViewById<TextView>(R.id.ImportButtonLabel)
        val deleteButtonText = findViewById<TextView>(R.id.DeleteAllTokensButtonLabel)
        val deleteButton = findViewById<ImageView>(R.id.DeleteButton)
        val importButton = findViewById<ImageView>(R.id.ImportButton)
        val backButton = findViewById<ImageButton>(R.id.BackButton)
        val accentGroup = findViewById<RadioGroup>(R.id.AccentRadioGroup)
        val themeGroup = findViewById<RadioGroup>(R.id.ThemeRadioGroup)
        var theme = "Dark"
        var accent = "Blue"
        val storageFile = "wristkey_data_storage"
        val storage: SharedPreferences = applicationContext.getSharedPreferences(storageFile, Context.MODE_PRIVATE)
        val storageEditor: SharedPreferences.Editor =  storage.edit()
        var currentAccent = storage.getString("accent", "4285F4")
        var currentTheme = storage.getString("theme", "000000")
        boxinsetlayout.setBackgroundColor(Color.parseColor("#"+currentTheme))
        backButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        deleteButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        importButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        if (currentTheme == "F7F7F7") {
            settingsLabelText.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
            themeLabelText.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
            accentLabelText.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
            importButtonText.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
            deleteButtonText.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
        } else {
            settingsLabelText.setTextColor(ColorStateList.valueOf(Color.parseColor("#BDBDBD")))
            themeLabelText.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
            accentLabelText.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
            importButtonText.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
            deleteButtonText.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
        }

        if (currentAccent == "FF4141") {
            accentGroup.check(R.id.Red)
        } else if (currentAccent == "FF6D00") {
            accentGroup.check(R.id.Saffron)
        } else if (currentAccent == "FFBB00") {
            accentGroup.check(R.id.Yellow)
        } else if (currentAccent == "4285F4") {
            accentGroup.check(R.id.Blue)
        } else if (currentAccent == "009688") {
            accentGroup.check(R.id.Teal)
        } else if (currentAccent == "434343") {
            accentGroup.check(R.id.Dark)
        }

        if (currentTheme == "F7F7F7") {
            themeGroup.check(R.id.LightTheme)
        } else if (currentTheme == "192835") {
            themeGroup.check(R.id.GrayTheme)
        } else if (currentTheme == "000000") {
            themeGroup.check(R.id.DarkTheme)
        }
        ////////
        val allKeysMap: Map<String, *> = storage.all
        val allKeys = mutableListOf("Select token")
        for (key in allKeysMap) {
            allKeys.add(((key.toString()).replaceAfter("=", "").replace("=", "")))
            allKeys.remove("theme")
            allKeys.remove("accent")
            allKeys.remove("currentSerialNumber")
        }
        Log.d("keyList", allKeys.toString())
        //////////////
        var currentPosition = 0
        val totalListItems = allKeys.size

        deleteButton.setOnClickListener {
            storageEditor.clear()
            storageEditor.apply()
            var doneToast = Toast.makeText(this, Html.fromHtml("<center><b>Deleted all\ntokens and settings<b></center>"), Toast.LENGTH_SHORT)
            doneToast.show()
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(500)
            true
        }

        importButton.setOnClickListener {
            val intent = Intent(applicationContext, BitwardenJSONImport::class.java)
            startActivity(intent)
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
            finish()
        }

        accentGroup.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { _, _ ->
            val selectedIndex = accentGroup.indexOfChild(findViewById(accentGroup.checkedRadioButtonId)).toString()
            if (selectedIndex == "0") {
                storageEditor.putString("accent", "FF4141")
            } else if (selectedIndex == "1") {
                storageEditor.putString("accent", "FF6D00")
            } else if (selectedIndex == "2") {
                storageEditor.putString("accent", "FFBB00")
            } else if (selectedIndex == "3") {
                storageEditor.putString("accent", "4285F4")
            } else if (selectedIndex == "4") {
                storageEditor.putString("accent", "009688")
            } else if (selectedIndex == "5") {
                storageEditor.putString("accent", "434343")
            }
            storageEditor.apply()
        })

        themeGroup.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { _, _ ->
            val selectedIndex = themeGroup.indexOfChild(findViewById(themeGroup.checkedRadioButtonId)).toString()
            if (selectedIndex == "0") {
                storageEditor.putString("theme", "F7F7F7")
            } else if (selectedIndex == "1") {
                storageEditor.putString("theme", "192835")
            } else if (selectedIndex == "2") {
                storageEditor.putString("theme", "000000")
            }
            storageEditor.apply()
        })

        backButton.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
            finish()
        }

    }
}
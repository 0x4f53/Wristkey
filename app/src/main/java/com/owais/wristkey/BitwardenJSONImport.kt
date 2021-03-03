package com.owais.wristkey

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment.getExternalStorageDirectory
import android.os.Vibrator
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.wear.widget.BoxInsetLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.owais.wristkey.databinding.ActivityBitwardenJsonimportBinding
import org.json.JSONObject
import java.io.File
import java.io.FileReader


class BitwardenJSONImport : Activity() {

    private lateinit var binding: ActivityBitwardenJsonimportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBitwardenJsonimportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val boxinsetlayout = findViewById<BoxInsetLayout>(R.id.BoxInsetLayout)
        val backButton = findViewById<ImageButton>(R.id.BackButton)
        val confirmButton = findViewById<ImageButton>(R.id.ConfirmButton)
        val importLabel = findViewById<TextView>(R.id.ImportLabel)
        val Description = findViewById<TextView>(R.id.Description)
        var theme = "Dark"
        var accent = "Blue"
        val storageFile = "wristkey_data_storage"
        val storage: SharedPreferences = applicationContext.getSharedPreferences(storageFile, Context.MODE_PRIVATE)
        val storageEditor: SharedPreferences.Editor =  storage.edit()
        var currentAccent = storage.getString("accent", "4285F4")
        var currentTheme = storage.getString("theme", "000000")
        boxinsetlayout.setBackgroundColor(Color.parseColor("#"+currentTheme))
        confirmButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        if (currentTheme == "F7F7F7") {
            importLabel.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
            Description.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
        } else {
            importLabel.setTextColor(ColorStateList.valueOf(Color.parseColor("#BDBDBD")))
            Description.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
        }

        backButton.setOnClickListener {
            val intent = Intent(applicationContext, SettingsActivity::class.java)
            startActivity(intent)
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
            finish()
        }

        confirmButton.setOnClickListener {
            val code = 0x3
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), code)
                } catch (e: Exception) {
                    e.printStackTrace()
                    throw e
                }
            }
            // start import
            try {
                val files: Array<File> = getExternalStorageDirectory().listFiles()
                for (file in files) {
                    if (file.name.startsWith("bitwarden") && file.name.endsWith(".json")) {
                        val reader = FileReader(file.path)
                        val jsonData = reader.readText()
                        val dataMap: Map<String, Any> = Gson().fromJson(jsonData, object : TypeToken<HashMap<String?, Any?>?>() {}.type)
                        Log.d("DATA::::::", dataMap.toString())
                        val toast = Toast.makeText(this, dataMap["items"].toString(), Toast.LENGTH_SHORT)
                        toast.show()
                    }
                }
            } catch (e: IllegalStateException) {
                val toast = Toast.makeText(this, "Couldn't find file. Check if the file exists in external storage and if Wristkey is granted storage permission.", Toast.LENGTH_SHORT)
                toast.show()
            }
            // stop import
            finish()
        }

    }
}
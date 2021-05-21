package com.wristkey
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Vibrator
import android.provider.Settings
import android.support.wearable.activity.WearableActivity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import androidx.wear.widget.BoxInsetLayout
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class ExportActivity : WearableActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export)
        val boxinsetlayout = findViewById<BoxInsetLayout>(R.id.BoxInsetLayout)
        val jsonFileExportLabel = findViewById<TextView>(R.id.FileExportLabel)
        val jsonFileExportButton = findViewById<ImageView>(R.id.FileExportButton)
        val fileExport = findViewById<LinearLayout>(R.id.FileExport)
        val jsonQrCodeExportLabel = findViewById<TextView>(R.id.QrCodeExportLabel)
        val qrCodeExport = findViewById<LinearLayout>(R.id.QrCodeExport)
        val jsonQrCodeExportButton = findViewById<ImageView>(R.id.QrCodeExportButton)
        val backButton = findViewById<ImageView>(R.id.BackButton)
        val appData: SharedPreferences = applicationContext.getSharedPreferences(appDataFile, Context.MODE_PRIVATE)
        var currentAccent = appData.getString("accent", "4285F4")
        var currentTheme = appData.getString("theme", "000000")

        boxinsetlayout.setBackgroundColor(Color.parseColor("#"+currentTheme))
        jsonFileExportButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        jsonQrCodeExportButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        backButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))

        if (currentTheme == "F7F7F7") {
            jsonFileExportLabel.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
            jsonQrCodeExportLabel.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
        } else {
            jsonFileExportLabel.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
            jsonQrCodeExportLabel.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
        }

        if (appData.getBoolean("screen_lock", true)) {
            val lockscreen = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            if (lockscreen.isKeyguardSecure) {
                val i = lockscreen.createConfirmDeviceCredentialIntent("Wristkey", "App locked")
                startActivityForResult(i, CODE_AUTHENTICATION_VERIFICATION)
            }
        }

        masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        logins = EncryptedSharedPreferences.create(
            loginsFile,
            masterKeyAlias,
            applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        // Begin unpacking data

        val exportData: String = logins.all.values.toString()

        fileExport.setOnClickListener {

            val sdf = SimpleDateFormat("yyyy-MM-dd'@'HH:mm:ss")
            val currentDateandTime: String = sdf.format(Date())
            val fileName = "$currentDateandTime.backup"

            try {
                val root = File(Environment.getExternalStorageDirectory(), "wristkey")
                if (!root.exists()) {
                    root.mkdirs()
                }
                val file = File(root, fileName)
                val writer = FileWriter(file)
                writer.append(exportData)
                writer.flush()
                writer.close()
                Toast.makeText(this, "Exported successfully. Make sure to delete after use.", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                val toast = Toast.makeText(this, "Couldn't write to file. Disable and re-enable storage permission.", Toast.LENGTH_LONG)
                toast.show()

                val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                settingsIntent.data = uri
                startActivity(settingsIntent)

                val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibratorService.vibrate(50)
                finish()
            }

            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
            finish()
        }

        qrCodeExport.setOnClickListener {
            val intent = Intent(applicationContext, QRCodeActivity::class.java)
            intent.putExtra("qr_data", exportData)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (!(resultCode == RESULT_OK && requestCode == CODE_AUTHENTICATION_VERIFICATION)) {
            finish()
        }
    }
}
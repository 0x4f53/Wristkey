package com.owais.wristkey

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment.getExternalStorageDirectory
import android.os.Vibrator
import android.provider.Settings
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.wear.widget.BoxInsetLayout
import com.google.gson.Gson
import com.owais.wristkey.databinding.ActivityBitwardenJsonimportBinding
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileReader
import java.util.*

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
        val description = findViewById<TextView>(R.id.Description)
        val importUsernames = findViewById<CheckBox>(R.id.ImportUsernames)
        var theme = "Dark"
        var accent = "Blue"
        val storageFile = "wristkey_data_storage"
        val storage: SharedPreferences = applicationContext.getSharedPreferences(storageFile, Context.MODE_PRIVATE)
        var currentAccent = storage.getString("accent", "4285F4")
        var currentTheme = storage.getString("theme", "000000")
        boxinsetlayout.setBackgroundColor(Color.parseColor("#"+currentTheme))
        confirmButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        importUsernames.buttonTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        if (currentTheme == "F7F7F7") {
            importLabel.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
            description.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
            importUsernames.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
        } else {
            importLabel.setTextColor(ColorStateList.valueOf(Color.parseColor("#BDBDBD")))
            description.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
            importUsernames.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
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
                        val items = JSONObject(jsonData)["items"].toString()
                        val itemsArray = JSONArray(items)
                        var itemsArrayLength = 0

                        setContentView(R.layout.bitwarden_jsonimport_loading)
                        val loadingLayout = findViewById<BoxInsetLayout>(R.id.LoadingLayout)
                        val loadingIcon = findViewById<ProgressBar>(R.id.LoadingIcon)
                        val importingLabel = findViewById<TextView>(R.id.ImportingLabel)
                        val importingDescription = findViewById<TextView>(R.id.ImportingDescription)
                        loadingLayout.setBackgroundColor(Color.parseColor("#"+currentTheme))
                        loadingIcon.progressTintList = ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
                        loadingIcon.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#" + currentTheme))
                        if (currentTheme == "F7F7F7") {
                            importingLabel.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
                            importingDescription.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
                        } else {
                            importingLabel.setTextColor(ColorStateList.valueOf(Color.parseColor("#BDBDBD")))
                            importingDescription.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
                        }

                        //found x number of items
                        for (i in 0 until itemsArray.length()) {
                            itemsArrayLength++
                        }

                        importingDescription.text = "Found $itemsArrayLength items"

                        for (itemIndex in 0 until itemsArrayLength) {
                            val itemData = JSONObject(itemsArray[itemIndex].toString())

                            try {
                                val loginData = JSONObject(itemData["login"].toString())
                                val totpSecret = loginData["totp"]
                                val username = loginData["username"]
                                val sitename = itemData["name"]

                                val accountName: String = if (username.toString() == "null") {
                                    sitename.toString()
                                } else {
                                    if (importUsernames.isChecked)
                                        "$sitename ($username)"
                                    else
                                        sitename.toString()
                                }

                                var totp = ""
                                if (totpSecret.toString() != "null") {
                                    totp = totpSecret.toString()
                                }


                                if (totp.isNotEmpty()) {
                                    // begin storing data
                                    importingDescription.text = "Adding $sitename account"
                                    var serialNumber = storage.getInt("currentSerialNumber", 0)
                                    serialNumber+=1
                                    val tokenData = ArrayList<String>()
                                    tokenData.add(serialNumber.toString())
                                    tokenData.add(accountName)
                                    tokenData.add(totp)

                                    // Bitwarden only supports Google Authenticator OTPS,
                                    // so default params for those are set.

                                    tokenData.add("Time")
                                    tokenData.add("6")
                                    tokenData.add("HmacAlgorithm.SHA1")
                                    tokenData.add("0")  // If counter mode is selected, initial value must be 0.
                                    val json = Gson().toJson(tokenData)
                                    storage.edit().putString(serialNumber.toString(), json).apply()
                                    storage.edit().putInt("currentSerialNumber", serialNumber).apply()
                                } else {
                                    importingDescription.text = "No TOTP secret for $sitename account"
                                }
                            } catch (noData: JSONException) {  }
                        }
                        importingDescription.text = "Saving data"
                        val toast = Toast.makeText(this, "Imported logins successfully!", Toast.LENGTH_SHORT)
                        toast.show()

                        val intent = Intent(applicationContext, SettingsActivity::class.java)
                        startActivity(intent)
                        val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        vibratorService.vibrate(50)
                        finish()
                    }
                }

            } catch (noFileFound: IllegalStateException) {
                val toast = Toast.makeText(this, "Couldn't find file. Check if the file exists in external storage and if Wristkey is granted storage permission.", Toast.LENGTH_SHORT)
                toast.show()

                val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                settingsIntent.data = uri
                startActivity(settingsIntent)

                val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibratorService.vibrate(50)
                finish()
            }
            // stop import
        }

    }
}
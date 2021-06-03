package com.wristkey

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment.getExternalStorageDirectory
import android.os.Vibrator
import android.provider.Settings
import android.widget.*
import androidx.wear.widget.BoxInsetLayout
import com.google.gson.Gson
import com.wristkey.databinding.ActivityBitwardenJsonimportBinding
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
        val backButton = findViewById<ImageButton>(R.id.AuthenticatorBackButton)
        val confirmButton = findViewById<ImageButton>(R.id.AuthenticatorConfirmButton)
        val importLabel = findViewById<TextView>(R.id.AuthenticatorImportLabel)
        val description = findViewById<TextView>(R.id.AuthenticatorDescription)
        val importUsernames = findViewById<CheckBox>(R.id.AuthenticatorImportUsernames)
        var theme = "Dark"
        var accent = "Blue"
        val appData: SharedPreferences = applicationContext.getSharedPreferences(appDataFile, Context.MODE_PRIVATE)
        var currentAccent = appData.getString("accent", "4285F4")
        var currentTheme = appData.getString("theme", "000000")
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
            val intent = Intent(applicationContext, AddActivity::class.java)
            startActivity(intent)
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
            finish()
        }

        confirmButton.setOnClickListener {
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)

            val files: Array<File> = getExternalStorageDirectory().listFiles()
            // start import
            try {
                for (file in files) {
                    if (file.name.startsWith("bitwarden") && file.name.endsWith(".json")) {
                        val reader = FileReader(file.path)
                        val jsonData = reader.readText()
                        val items = JSONObject(jsonData)["items"].toString()
                        val itemsArray = JSONArray(items)
                        var itemsArrayLength = 0

                        setContentView(R.layout.import_loading_screen)
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
                                val uuid = itemData["id"].toString()

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

                                if (totp.isNotEmpty()) { // begin storing data
                                    importingDescription.text = "Adding $sitename account"
                                    if (totp.startsWith("otpauth://")) {
                                        var type = totp.substringAfter("otpauth://").substringBefore("/")
                                        var secret = totp.substringAfter("secret=").substringBefore("&")
                                        var algorithm = totp.substringAfter("algorithm=").substringBefore("&")
                                        var digits = totp.substringAfter("digits=").substringBefore("&")
                                        var label = totp.substringAfterLast("/").substringBefore("?")
                                        var issuer = totp.substringAfterLast("issuer=").substringBefore("&")

                                        type = if (type.equals("totp")) "Time" else "Counter"

                                        if (algorithm == "SHA1") {
                                            algorithm = "HmacAlgorithm.SHA1"
                                        } else if (algorithm == "SHA256") {
                                            algorithm = "HmacAlgorithm.SHA256"
                                        } else if (algorithm == "SHA512") {
                                            algorithm = "HmacAlgorithm.SHA512"
                                        }

                                        val tokenData = ArrayList<String>()

                                        tokenData.add(accountName)
                                        tokenData.add(totp)

                                        tokenData.add(type)
                                        tokenData.add(digits)
                                        tokenData.add(algorithm)
                                        tokenData.add("0")  // If counter mode is selected, initial value must be 0.
                                        val json = Gson().toJson(tokenData)
                                        logins.edit().putString(uuid, json).apply()

                                    } else { // Google Authenticator

                                        val tokenData = ArrayList<String>()

                                        tokenData.add(accountName)
                                        tokenData.add(totp)

                                        tokenData.add("Time")
                                        tokenData.add("6")
                                        tokenData.add("HmacAlgorithm.SHA1")
                                        tokenData.add("0")  // If counter mode is selected, initial value must be 0.
                                        val json = Gson().toJson(tokenData)
                                        logins.edit().putString(uuid, json).apply()
                                    }
                                } else {
                                    importingDescription.text = "No TOTP secret for $sitename account"
                                }
                            } catch (noData: JSONException) {  }
                        }
                        importingDescription.text = "Saving data"
                        val toast = Toast.makeText(this, "Imported logins successfully!", Toast.LENGTH_SHORT)
                        toast.show()

                        val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        vibratorService.vibrate(100)
                        finish()
                    }
                }

            } catch (noFileFound: IllegalStateException) {
                val toast = Toast.makeText(this, "Couldn't find file. Check if the file exists and if Wristkey is granted storage permission.", Toast.LENGTH_LONG)
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
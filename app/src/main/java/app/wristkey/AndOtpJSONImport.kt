package app.wristkey

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment.getExternalStorageDirectory
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import android.widget.*
import androidx.wear.widget.BoxInsetLayout
import app.wristkey.AddActivity
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import wristkey.R
import wristkey.databinding.ActivityAndotpJsonimportBinding
import java.io.File
import java.io.FileReader
import java.util.*

class AndOtpJSONImport : Activity() {

    private lateinit var binding: ActivityAndotpJsonimportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAndotpJsonimportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val boxinsetlayout = findViewById<BoxInsetLayout>(R.id.BoxInsetLayout)
        val backButton = findViewById<ImageButton>(R.id.AuthenticatorBackButton)
        val confirmButton = findViewById<ImageButton>(R.id.AuthenticatorConfirmButton)
        val importLabel = findViewById<TextView>(R.id.AuthenticatorImportLabel)
        val description = findViewById<TextView>(R.id.AuthenticatorDescription)
        val importUsernames = findViewById<CheckBox>(R.id.AuthenticatorImportUsernames)

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
                    if (file.name.startsWith("otp_accounts") && file.name.endsWith(".json")) {
                        val reader = FileReader(file.path)
                        val jsonData = reader.readText()
                        val itemsArray = JSONArray(jsonData)

                        setContentView(R.layout.import_loading_screen)
                        val loadingLayout = findViewById<BoxInsetLayout>(R.id.LoadingLayout)
                        val loadingIcon = findViewById<ProgressBar>(R.id.LoadingIcon)
                        val importingLabel = findViewById<TextView>(R.id.ImportingLabel)
                        val importingDescription = findViewById<TextView>(R.id.ImportingDescription)

                        importingDescription.text = "Found ${itemsArray.length()} items"

                        for (itemIndex in 0 until itemsArray.length()) {

                            val account = itemsArray[itemIndex].toString()
                            val secret = JSONObject(account)["secret"].toString().replace("=", "")
                            val issuer = JSONObject(account)["issuer"].toString()
                            val algorithm = "HmacAlgorithm."+JSONObject(account)["algorithm"].toString()

                            val label = JSONObject(account)["label"].toString()
                            val digits = JSONObject(account)["digits"].toString()
                            var type = JSONObject(account)["type"].toString()

                            val accountData = ArrayList<String>()

                            if (label.isNullOrEmpty()) {
                                accountData.add(issuer)
                            } else {
                                if (importUsernames.isChecked) accountData.add ("$issuer ($label)")
                                else accountData.add(issuer)
                            }

                            accountData.add(secret)
                            if (type == "TOTP") accountData.add("Time")
                            else if (type == "HOTP") accountData.add("Counter")
                            else if (type == "STEAM") accountData.add("Time")
                            accountData.add(digits)
                            accountData.add(algorithm)

                            try { // If counter mode is selected, initial value must be 0.
                                accountData.add(JSONObject(account)["counter"].toString())
                            } catch (noCounter: JSONException) {
                                accountData.add("0")
                            }

                            val json = Gson().toJson(accountData)

                            val id = UUID.randomUUID().toString()

                        }
                        importingDescription.text = "Saving data"
                        val toast = Toast.makeText(this, "Imported accounts successfully!", Toast.LENGTH_SHORT)
                        toast.show()

                        val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        vibratorService.vibrate(100)
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)
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
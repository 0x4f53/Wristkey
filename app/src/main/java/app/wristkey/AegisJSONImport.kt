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
import android.widget.*
import androidx.wear.widget.BoxInsetLayout
import app.wristkey.AddActivity
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import wristkey.R
import wristkey.databinding.ActivityAegisJsonimportBinding
import java.io.File
import java.io.FileReader
import java.util.*

class AegisJSONImport : Activity() {

    private lateinit var binding: ActivityAegisJsonimportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAegisJsonimportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val boxinsetlayout = findViewById<BoxInsetLayout>(R.id.BoxInsetLayout)
        val backButton = findViewById<ImageButton>(R.id.backButton)
        val confirmButton = findViewById<ImageButton>(R.id.doneButton)
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
                    if (file.name.startsWith("aegis") && file.name.endsWith(".json")) {
                        val reader = FileReader(file.path)
                        val jsonData = reader.readText()
                        val db = JSONObject(jsonData)["db"].toString()
                        val entries = JSONObject(db)["entries"].toString()

                        var itemsArray = JSONArray(entries)

                        setContentView(R.layout.import_loading_screen)
                        val loadingLayout = findViewById<BoxInsetLayout>(R.id.LoadingLayout)
                        val loadingIcon = findViewById<ProgressBar>(R.id.LoadingIcon)
                        val importingLabel = findViewById<TextView>(R.id.ImportingLabel)
                        val importingDescription = findViewById<TextView>(R.id.ImportingDescription)
                        importingDescription.text = "Found ${itemsArray.length()} items"

                        for (itemIndex in 0 until itemsArray.length()) {
                            try {
                                val accountData = JSONObject(itemsArray[itemIndex].toString())
                                var type = accountData["type"]
                                val uuid = accountData["uuid"].toString()
                                val sitename = accountData["issuer"]
                                val username = accountData["name"]
                                val totpSecret = JSONObject(accountData["info"].toString())["secret"]
                                val digits = JSONObject(accountData["info"].toString())["digits"].toString()
                                var algorithm = JSONObject(accountData["info"].toString())["algo"].toString()

                                type = if (type.equals("totp")) "Time" else "Counter"

                                if (algorithm == "SHA1") {
                                    algorithm = "HmacAlgorithm.SHA1"
                                } else if (algorithm == "SHA256") {
                                    algorithm = "HmacAlgorithm.SHA256"
                                } else if (algorithm == "SHA512") {
                                    algorithm = "HmacAlgorithm.SHA512"
                                }

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
                                    val accountData = ArrayList<String>()

                                    val id = uuid

                                    accountData.add(accountName)
                                    accountData.add(totp)

                                    accountData.add(type)
                                    accountData.add(digits)
                                    accountData.add(algorithm)
                                    accountData.add("0")  // If counter mode is selected, initial value must be 0.
                                    val json = Gson().toJson(accountData)
                                } else {
                                    importingDescription.text = "No TOTP secret for $sitename account"
                                }
                            } catch (noData: JSONException) {  }
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
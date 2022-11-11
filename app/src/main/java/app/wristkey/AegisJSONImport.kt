package app.wristkey

import android.app.Activity
import android.media.audiofx.HapticGenerator
import android.os.Bundle
import android.os.Environment
import android.widget.*
import androidx.wear.widget.BoxInsetLayout
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import wristkey.R
import java.io.File
import java.io.FileReader

class AegisJSONImport : Activity() {

    lateinit var backButton: ImageButton
    lateinit var confirmButton: ImageButton
    lateinit var importLabel: TextView
    lateinit var description: TextView
    lateinit var importUsernames: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_aegis_jsonimport)
        backButton = findViewById<ImageButton>(R.id.backButton)
        confirmButton = findViewById<ImageButton>(R.id.doneButton)
        importLabel = findViewById<TextView>(R.id.AuthenticatorImportLabel)
        description = findViewById<TextView>(R.id.AuthenticatorDescription)
        importUsernames = findViewById<CheckBox>(R.id.AuthenticatorImportUsernames)

        backButton.setOnClickListener {
            backButton.performHapticFeedback(HapticGenerator.SUCCESS)
            finish()
        }

        confirmButton.setOnClickListener {
            backButton.performHapticFeedback(HapticGenerator.SUCCESS)
            scanAegisJson()
        }

    }

    private fun scanAegisJson (): Boolean {
        val files: Array<File> = Environment.getExternalStorageDirectory().listFiles()
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
                    Toast.makeText(this, "Imported accounts successfully!", Toast.LENGTH_SHORT).show()
                    backButton.performHapticFeedback(HapticGenerator.ERROR)

                    return true
                }
            }

        } catch (_: IllegalStateException) { }

        Toast.makeText(this, "Couldn't find an Aegis JSON file. Check if it exists and if Wristkey has storage permission.", Toast.LENGTH_LONG).show()

        return false

    }

}
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
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.wear.widget.BoxInsetLayout
import app.wristkey.AddActivity
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONException
import wristkey.R
import wristkey.databinding.ActivityWristkeyImportBinding
import java.io.File
import java.io.FileReader
import java.util.*

class WristkeyImport : Activity() {

    private lateinit var binding: ActivityWristkeyImportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWristkeyImportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val boxinsetlayout = findViewById<BoxInsetLayout>(R.id.BoxInsetLayout)
        val backButton = findViewById<ImageButton>(R.id.BackButton)
        val confirmButton = findViewById<ImageButton>(R.id.ConfirmButton)
        val importLabel = findViewById<TextView>(R.id.ImportLabel)
        val description = findViewById<TextView>(R.id.Description)

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
                    if (file.name.endsWith(".backup")) {
                        val reader = FileReader(file.path)
                        val jsonData = reader.readText()
                        val itemsArray = JSONArray(jsonData)

                        setContentView(R.layout.import_loading_screen)
                        val loadingLayout = findViewById<BoxInsetLayout>(R.id.LoadingLayout)
                        val loadingIcon = findViewById<ProgressBar>(R.id.LoadingIcon)
                        val importingLabel = findViewById<TextView>(R.id.ImportingLabel)
                        val importingDescription = findViewById<TextView>(R.id.ImportingDescription)


                        //found x number of items

                        importingDescription.text = "Found ${itemsArray.length()} items"

                        for (itemIndex in 0 until itemsArray.length()) {
                            try {
                                val item = itemsArray[itemIndex].toString()
                                val name = JSONArray(item)[0].toString()
                                val totpSecret = JSONArray(item)[1].toString()
                                val mode = JSONArray(item)[2].toString()
                                val digits = JSONArray(item)[3].toString()
                                val algorithm = JSONArray(item)[4].toString()
                                val counter = JSONArray(item)[5].toString()

                                if (totpSecret.isNotEmpty()) { // begin storing data
                                    importingDescription.text = "Adding $name"
                                    val accountData = ArrayList<String>()
                                    accountData.add(name)
                                    accountData.add(totpSecret)
                                    accountData.add(mode)
                                    accountData.add(digits)
                                    accountData.add(algorithm)
                                    accountData.add(counter)
                                    val json = Gson().toJson(accountData)
                                    val id = UUID.randomUUID().toString()
                                } else {
                                    importingDescription.text = "No TOTP secret for $name"
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
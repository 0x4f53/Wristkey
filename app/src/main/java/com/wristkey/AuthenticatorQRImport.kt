package com.wristkey

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment.getExternalStorageDirectory
import android.os.Vibrator
import android.provider.Settings
import android.widget.*
import androidx.wear.widget.BoxInsetLayout
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.gson.Gson
import com.google.zxing.*
import com.google.zxing.Reader
import com.google.zxing.common.HybridBinarizer
import com.wristkey.databinding.ActivityAuthenticatorQrimportBinding
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class AuthenticatorQRImport : Activity() {

    private lateinit var binding: ActivityAuthenticatorQrimportBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticatorQrimportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val boxinsetlayout = findViewById<BoxInsetLayout>(R.id.BoxInsetLayout)
        val backButton = findViewById<ImageButton>(R.id.AuthenticatorBackButton)
        val confirmButton = findViewById<ImageButton>(R.id.AuthenticatorConfirmButton)
        val importLabel = findViewById<TextView>(R.id.AuthenticatorImportLabel)
        val description = findViewById<TextView>(R.id.AuthenticatorDescription)
        val importUsernames = findViewById<CheckBox>(R.id.AuthenticatorImportUsernames)
        var theme = "Dark"
        var accent = "Blue"
        val appData: SharedPreferences =
            applicationContext.getSharedPreferences(appDataFile, Context.MODE_PRIVATE)
        var currentAccent = appData.getString("accent", "4285F4")
        var currentTheme = appData.getString("theme", "000000")
        boxinsetlayout.setBackgroundColor(Color.parseColor("#" + currentTheme))
        confirmButton.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
        importUsernames.buttonTintList =
            ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
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
                    if (file.name.endsWith(".png", ignoreCase = true) || file.name.endsWith(".jpg", ignoreCase = true) || file.name.endsWith(".jpeg", ignoreCase = true)) {
                        val reader: InputStream = BufferedInputStream(FileInputStream(file.path))
                        val imageBitmap = BitmapFactory.decodeStream(reader)
                        val decodedQRCodeData: String = scanQRImage(imageBitmap)

                        if (decodedQRCodeData.contains("otpauth-migration://")) {
                            setContentView(R.layout.import_loading_screen)
                            val loadingLayout = findViewById<BoxInsetLayout>(R.id.LoadingLayout)
                            val loadingIcon = findViewById<ProgressBar>(R.id.LoadingIcon)
                            val importingLabel = findViewById<TextView>(R.id.ImportingLabel)
                            val importingDescription =
                                findViewById<TextView>(R.id.ImportingDescription)
                            loadingLayout.setBackgroundColor(Color.parseColor("#" + currentTheme))
                            loadingIcon.progressTintList =
                                ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
                            loadingIcon.backgroundTintList =
                                ColorStateList.valueOf(Color.parseColor("#" + currentTheme))
                            if (currentTheme == "F7F7F7") {
                                importingLabel.setTextColor(
                                    ColorStateList.valueOf(
                                        Color.parseColor(
                                            "#000000"
                                        )
                                    )
                                )
                                importingDescription.setTextColor(
                                    ColorStateList.valueOf(
                                        Color.parseColor(
                                            "#000000"
                                        )
                                    )
                                )
                            } else {
                                importingLabel.setTextColor(
                                    ColorStateList.valueOf(
                                        Color.parseColor(
                                            "#BDBDBD"
                                        )
                                    )
                                )
                                importingDescription.setTextColor(
                                    ColorStateList.valueOf(
                                        Color.parseColor(
                                            "#FFFFFF"
                                        )
                                    )
                                )
                            }

                            //found QR Code

                            importingDescription.text = "Found QR Code"

                            // put data in Python script and extract Authenticator data
                            if (!Python.isStarted()) {
                                Python.start(AndroidPlatform(this))
                            }

                            Python.getInstance().getModule("extract_otp_secret_keys").callAttr("decode", decodedQRCodeData)
                            val timeStamp: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())

                            var logcat: Process
                            val log = StringBuilder()
                            try {
                                logcat = Runtime.getRuntime().exec(arrayOf("logcat", "-d"))
                                val br =
                                    BufferedReader(InputStreamReader(logcat.inputStream), 4 * 1024)
                                var line: String?
                                val separator = System.getProperty("line.separator")
                                while (br.readLine().also { line = it } != null) {
                                    log.append(line)
                                    log.append(separator)
                                }
                                Runtime.getRuntime().exec("clear")
                            } catch (e: java.lang.Exception) {
                                e.printStackTrace()
                            }

                            val logExtractedString = log.toString()
                                .substringAfter(timeStamp)  // get most recent occurence of data
                                .substringAfter("python.stdout")
                                .substringAfter("<\$beginwristkeygoogleauthenticatorimport\$>")
                                .substringBefore("<\$endwristkeygoogleauthenticatorimport\$>")

                            // convert json data and store in sharedprefs
                            val items = JSONObject(logExtractedString)
                            for (key in items.keys()) {
                                val tokenData = ArrayList<String>()
                                if (!importUsernames.isChecked) {
                                    tokenData.add(key.toString().replaceAfter("(", "").replace("(", "")) // name without username
                                } else {
                                    tokenData.add(key) // name with username
                                }
                                val itemData = JSONObject(items[key].toString())
                                tokenData.add(itemData["secret"].toString()) //secret
                                if (itemData["type"] == "2") tokenData.add("Time") else tokenData.add("Counter") // mode
                                tokenData.add("6")  // length
                                tokenData.add("HmacAlgorithm.SHA1")  // algorithm
                                tokenData.add("0")  // If counter mode is selected, initial value must be 0.

                                val id = UUID.randomUUID().toString()
                                val json = Gson().toJson(tokenData)
                                logins.edit().putString(id, json).apply()
                            }

                            importingDescription.text = "Saving data"
                            Toast.makeText(
                                this,
                                "Imported logins successfully!",
                                Toast.LENGTH_SHORT
                            ).show()

                            val vibratorService =
                                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                            vibratorService.vibrate(50)

                            val intent = Intent(applicationContext, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "Couldn't find Google Authenticator data in image!",
                                Toast.LENGTH_SHORT
                            ).show()

                            val vibratorService =
                                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                            vibratorService.vibrate(50)

                            val intent = Intent(applicationContext, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                }

            } catch (noFileFound: FileNotFoundException) {
                Toast.makeText(
                    this,
                    "Couldn't find file. Check if the file exists and if Wristkey is granted storage permission.",
                    Toast.LENGTH_LONG
                ).show()

                val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                settingsIntent.data = uri
                startActivity(settingsIntent)

                val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibratorService.vibrate(50)
                finish()

            } catch (badData: IllegalArgumentException) {
                Toast.makeText(this, "QR code data corrupt.", Toast.LENGTH_SHORT).show()

                val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibratorService.vibrate(50)
                finish()

            }
            // stop import
        }
    }

    fun scanQRImage(bMap: Bitmap): String {
        var contents: String
        val intArray = IntArray(bMap.width * bMap.height)
        //copy pixel data from the Bitmap into the 'intArray' array
        bMap.getPixels(intArray, 0, bMap.width, 0, 0, bMap.width, bMap.height)
        val source: LuminanceSource = RGBLuminanceSource(bMap.width, bMap.height, intArray)
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        val reader: Reader = MultiFormatReader()
        try {
            val result = reader.decode(bitmap)
            contents = result.text
        } catch (e: Exception) {
            contents = "No data found"
        }
        return contents
    }
}
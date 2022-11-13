package app.wristkey

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.audiofx.HapticGenerator
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.HapticFeedbackConstants
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import wristkey.R
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.*

class AuthenticatorQRImport : Activity() {

    lateinit var utilities: Utilities

    lateinit var backButton: ImageButton
    lateinit var doneButton: ImageButton
    lateinit var importLabel: TextView
    lateinit var description: TextView

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authenticator_qrimport)

        utilities = Utilities (applicationContext)

        initializeUI()

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeUI () {
        backButton = findViewById (R.id.backButton)
        doneButton = findViewById (R.id.doneButton)
        importLabel = findViewById (R.id.aegisLabel)
        description = findViewById (R.id.aegisDescription)

        description.text = getString (R.string.authenticator_import_blurb) + " " + applicationContext.filesDir.toString() + "\n\n" + getString (
            R.string.use_adb_blurb)

        backButton.setOnClickListener {
            backButton.performHapticFeedback(HapticGenerator.SUCCESS)
            finish()
        }

        doneButton.setOnClickListener {
            doneButton.performHapticFeedback(HapticGenerator.SUCCESS)
            checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, utilities.FILES_REQUEST_CODE)
        }

    }

    // Function to check and request permission.
    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this@AuthenticatorQRImport, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this@AuthenticatorQRImport, arrayOf(permission), requestCode)
        } else {
            initializeScanUI()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == utilities.FILES_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeScanUI()
            } else {
                Toast.makeText(this@AuthenticatorQRImport, "Please grant Wristkey storage permissions in settings", Toast.LENGTH_LONG).show()
                val intent = Intent (Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeScanUI () {
        setContentView(R.layout.import_loading_screen)
        val importingDescription = findViewById<TextView>(R.id.ImportingDescription)

        var logins = mutableListOf<Utilities.MfaCode>()

        try {
            val directory = File (applicationContext.filesDir.toString())
            Log.d ("Wristkey", "Looking for files in: " + applicationContext.filesDir.toString())
            importingDescription.text = "Looking for files in: \n${directory}"

            for (file in directory.listFiles()!!) {

                if (
                    file.name.endsWith(".png", ignoreCase = true)
                    || file.name.endsWith(".jpg", ignoreCase = true)
                    || file.name.endsWith(".jpeg", ignoreCase = true)
                ) {

                    val reader: InputStream = BufferedInputStream(FileInputStream(file.path))
                    val imageBitmap = BitmapFactory.decodeStream(reader)
                    val decodedQRCodeData: String = utilities.scanQRImage(imageBitmap)

                    importingDescription.text = "Found file: \n${file.name}"

                    logins = utilities.authenticatorToWristkey (decodedQRCodeData)

                    Toast.makeText(applicationContext, "Imported ${logins.size} accounts", Toast.LENGTH_SHORT).show()
                    importingDescription.performHapticFeedback(HapticFeedbackConstants.REJECT)
                    file.delete()
                }

            }

            if (logins.isEmpty()) {
                Toast.makeText(this, "No files found.", Toast.LENGTH_LONG).show()
                finish()

            } else {
                for (login in logins) {
                    utilities.writeToVault(login, UUID.randomUUID().toString())
                }
                finishAffinity()
                startActivity(Intent(applicationContext, MainActivity::class.java))
            }

        } catch (noDirectory: NullPointerException) {
            setContentView(R.layout.activity_authenticator_qrimport)
            Toast.makeText(this, "Couldn't access storage. Please raise an issue on Wristkey's GitHub repo.", Toast.LENGTH_LONG).show()
            noDirectory.printStackTrace()

        } catch (_: java.io.FileNotFoundException) { }

    }

    /*private lateinit var binding: ActivityAuthenticatorQrimportBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticatorQrimportBinding.inflate(layoutInflater)
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
                                .substringAfter("<\$wristkey\$>")
                                .substringBefore("<\$\\wristkey\$>")

                            // convert json data and store in sharedprefs
                            val items = JSONObject(logExtractedString)
                            for (key in items.keys()) {
                                val accountData = ArrayList<String>()
                                if (!importUsernames.isChecked) {
                                    accountData.add(key.toString().replaceAfter("(", "").replace("(", "")) // name without username
                                } else {
                                    accountData.add(key) // name with username
                                }
                                val itemData = JSONObject(items[key].toString())
                                accountData.add(itemData["secret"].toString()) //secret
                                if (itemData["type"] == "2") accountData.add("Time") else accountData.add("Counter") // mode
                                accountData.add("6")  // length
                                accountData.add("HmacAlgorithm.SHA1")  // algorithm
                                accountData.add("0")  // If counter mode is selected, initial value must be 0.

                                val id = UUID.randomUUID().toString()
                                val json = Gson().toJson(accountData)
                            }

                            importingDescription.text = "Saving data"
                            Toast.makeText(
                                this,
                                "Imported accounts successfully!",
                                Toast.LENGTH_SHORT
                            ).show()

                            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
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

                            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
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
    }*/


}
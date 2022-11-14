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
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import wristkey.R
import java.io.*
import java.util.*

class OtpAuthImport : Activity() {

    lateinit var utilities: Utilities

    lateinit var backButton: ImageButton
    lateinit var doneButton: ImageButton
    lateinit var scanViaCameraButton: CardView
    lateinit var importLabel: TextView
    lateinit var description: TextView

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otpauth_import)

        utilities = Utilities (applicationContext)

        initializeUI()

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeUI () {
        setContentView(R.layout.activity_otpauth_import)
        importLabel = findViewById (R.id.label)
        description = findViewById (R.id.description)

        scanViaCameraButton = findViewById (R.id.scanViaCameraButton)
        backButton = findViewById (R.id.backButton)
        doneButton = findViewById (R.id.doneButton)

        description.text = getString (R.string.otpauth_import_blurb) + " " + applicationContext.filesDir.toString() + "\n\n" + getString (
            R.string.use_adb_blurb)

        backButton.setOnClickListener {
            backButton.performHapticFeedback(HapticGenerator.SUCCESS)
            finish()
        }

        doneButton.setOnClickListener {
            doneButton.performHapticFeedback(HapticGenerator.SUCCESS)
            checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, utilities.FILES_REQUEST_CODE)
        }

        scanViaCameraButton.setOnClickListener {
            scanViaCameraButton.performHapticFeedback(HapticGenerator.SUCCESS)
            checkPermission(Manifest.permission.CAMERA, utilities.CAMERA_REQUEST_CODE)
        }

        if (applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY))
            scanViaCameraButton.visibility = View.VISIBLE
        else scanViaCameraButton.visibility = View.GONE

    }

    // Function to check and request permission.
    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this@OtpAuthImport, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this@OtpAuthImport, arrayOf(permission), requestCode)
        } else {
            when (requestCode) {
                utilities.FILES_REQUEST_CODE -> {
                    initializeScanUI()
                }

                utilities.CAMERA_REQUEST_CODE -> {
                    startScannerUI()
                }

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == utilities.FILES_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeScanUI()
            } else {
                Toast.makeText(this@OtpAuthImport, "Please grant Wristkey storage permissions in settings", Toast.LENGTH_LONG).show()
                val intent = Intent (Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        } else if (requestCode == utilities.CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScannerUI()
            } else {
                Toast.makeText(this@OtpAuthImport, "Please grant Wristkey camera permissions in settings", Toast.LENGTH_LONG).show()
                val intent = Intent (Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun startScannerUI () {
        val intent = Intent (applicationContext, QRScannerActivity::class.java)
        intent.putExtra (utilities.QR_CODE_SCAN_REQUEST, utilities.OTPAUTH_SCAN_CODE)
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeScanUI () {
        setContentView(R.layout.import_loading_screen)
        val importingDescription = findViewById<TextView>(R.id.ImportingDescription)

        val logins = mutableListOf<Utilities.MfaCode>()

        try {
            val directory = File (applicationContext.filesDir.toString())
            Log.d ("Wristkey", "Looking for files in: " + applicationContext.filesDir.toString())
            importingDescription.text = "Looking for files in: \n${directory}"

            for (file in directory.listFiles()!!) {

                try {
                    val fileData = FileReader(file.path).readText()

                    if (
                        file.name.endsWith(".png", ignoreCase = true)
                        || file.name.endsWith(".jpg", ignoreCase = true)
                        || file.name.endsWith(".jpeg", ignoreCase = true)
                    ) {

                        val reader: InputStream = BufferedInputStream(FileInputStream(file.path))
                        val imageBitmap = BitmapFactory.decodeStream(reader)
                        val decodedQRCodeData: String = utilities.scanQRImage(imageBitmap)

                        if (decodedQRCodeData.contains("otpauth://") && !decodedQRCodeData.contains("otpauth-migration://"))
                            logins. add(utilities.decodeOTPAuthURL (decodedQRCodeData)!!)
                        else if (decodedQRCodeData.contains("otpauth-migration://")) {
                            Toast.makeText(this, "This appears to be a Google Authenticator export. Please choose that option instead to proceed.", Toast.LENGTH_LONG).show()
                            break
                        }

                    }

                } catch (_: Exception) {
                    Log.d ("Wristkey", "${file.name} is invalid")
                }

                importingDescription.text = "Found file: \n${file.name}"
                Toast.makeText(applicationContext, "Imported ${logins.size} accounts", Toast.LENGTH_SHORT).show()
                importingDescription.performHapticFeedback(HapticFeedbackConstants.REJECT)
                file.delete()

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
            initializeUI()
            Toast.makeText(this, "Couldn't access storage. Please raise an issue on Wristkey's GitHub repo.", Toast.LENGTH_LONG).show()
            noDirectory.printStackTrace()

        } catch (_: java.io.FileNotFoundException) { }

    }

}
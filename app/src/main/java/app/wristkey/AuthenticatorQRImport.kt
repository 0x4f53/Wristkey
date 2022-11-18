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
import com.anggrayudi.storage.SimpleStorageHelper
import org.json.JSONException
import wristkey.R
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.*

class AuthenticatorQRImport : Activity() {

    lateinit var utilities: Utilities
    lateinit var storageHelper: SimpleStorageHelper

    lateinit var backButton: ImageButton
    lateinit var doneButton: ImageButton
    lateinit var importLabel: TextView
    lateinit var description: TextView
    lateinit var scanViaCameraButton: CardView
    lateinit var pickFileButton: CardView

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authenticator_qrimport)

        utilities = Utilities (applicationContext)
        storageHelper = SimpleStorageHelper(this, utilities.FILES_REQUEST_CODE, savedInstanceState)

        initializeUI()

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeUI () {
        setContentView(R.layout.activity_authenticator_qrimport)
        pickFileButton = findViewById (R.id.pickFileButton)
        backButton = findViewById (R.id.backButton)
        doneButton = findViewById (R.id.doneButton)
        importLabel = findViewById (R.id.label)
        description = findViewById (R.id.description)
        scanViaCameraButton = findViewById (R.id.scanViaCameraButton)

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

        pickFileButton.setOnClickListener {
            storageHelper.openFilePicker (
                allowMultiple = false,
                filterMimeTypes = arrayOf (utilities.JPG_MIME_TYPE, utilities.PNG_MIME_TYPE)
            )
        }

        storageHelper.onFileSelected = { requestCode, files ->
            initializeScanUI(files[0].uri)
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
        if (ContextCompat.checkSelfPermission(this@AuthenticatorQRImport, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this@AuthenticatorQRImport, arrayOf(permission), requestCode)
        } else {
            when (requestCode) {
                utilities.FILES_REQUEST_CODE -> {
                    initializeScanUI(null)
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
                initializeScanUI(null)
            } else {
                Toast.makeText(this@AuthenticatorQRImport, "Please grant Wristkey storage permissions in settings", Toast.LENGTH_LONG).show()
                val intent = Intent (Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        } else if (requestCode == utilities.CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScannerUI()
            } else {
                Toast.makeText(this@AuthenticatorQRImport, "Please grant Wristkey camera permissions in settings", Toast.LENGTH_LONG).show()
                val intent = Intent (Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        storageHelper.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        storageHelper.onRestoreInstanceState(savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        storageHelper.storage.onActivityResult(requestCode, resultCode, data)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun startScannerUI () {
        val intent = Intent (applicationContext, QRScannerActivity::class.java)
        intent.putExtra (utilities.QR_CODE_SCAN_REQUEST, utilities.AUTHENTICATOR_EXPORT_SCAN_CODE)
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeScanUI (fileName: Uri?) {
        setContentView(R.layout.import_loading_screen)
        val importingDescription = findViewById<TextView>(R.id.ImportingDescription)

        var logins = mutableListOf<Utilities.MfaCode>()

        try {

            if (fileName!!.toString().isNotBlank()) {
                val file = contentResolver.openInputStream(fileName)

                Log.d ("Wristkey", "Reading: $fileName")
                importingDescription.text = "Reading \n$fileName"

                val imageBitmap = BitmapFactory.decodeStream(file)
                val decodedQRCodeData: String = utilities.scanQRImage(imageBitmap)

                if (decodedQRCodeData.contains("otpauth-migration://") && !decodedQRCodeData.contains("otpauth://")) {
                    logins.add(utilities.decodeOTPAuthURL(decodedQRCodeData)!!)
                } else if (decodedQRCodeData.contains("otpauth://")) {
                    Toast.makeText(this, "This appears to be a regular 2FA code. Please choose that option instead.", Toast.LENGTH_LONG).show()
                }

                importingDescription.text = "${logins.size}"
                for (login in logins) {
                    utilities.writeToVault(login, UUID.randomUUID().toString())
                }

                Toast.makeText(applicationContext, "Imported ${logins.size} account(s)", Toast.LENGTH_SHORT).show()
                importingDescription.performHapticFeedback(HapticFeedbackConstants.REJECT)

                file?.close()

            } else {
                val directory = File (applicationContext.filesDir.toString())
                Log.d ("Wristkey", "Looking for files in: " + applicationContext.filesDir.toString())
                importingDescription.text = "Looking for files in: \n${directory}"

                for (file in directory.listFiles()!!) {

                    try {

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
                                Toast.makeText(this, "This appears to be a Google Authenticator export. Please choose that option instead.", Toast.LENGTH_LONG).show()
                                break
                            }

                        }

                    } catch (_: Exception) {
                        Log.d ("Wristkey", "${file.name} is invalid")
                    }

                    importingDescription.text = "Found file: \n${file.name}"

                    Toast.makeText(applicationContext, "Imported ${logins.size} account(s)", Toast.LENGTH_SHORT).show()
                    importingDescription.performHapticFeedback(HapticFeedbackConstants.REJECT)
                    file.delete()

                    for (login in logins) {
                        importingDescription.text = "${login.issuer}"
                        utilities.writeToVault(login, UUID.randomUUID().toString())
                    }
                }

            }

            if (logins.isEmpty()) {
                Toast.makeText(this, "No files found.", Toast.LENGTH_LONG).show()
                finish()
            } else {
                finishAffinity()
                startActivity(Intent(applicationContext, MainActivity::class.java))
            }

        } catch (noDirectory: NullPointerException) {
            initializeUI()
            Toast.makeText(this, "Couldn't access file.", Toast.LENGTH_LONG).show()
            noDirectory.printStackTrace()

        } catch (invalidFile: JSONException) {
            initializeUI()
            Toast.makeText(this, "Invalid file. Please follow the instructions.", Toast.LENGTH_LONG).show()
            invalidFile.printStackTrace()

        }

    }

}
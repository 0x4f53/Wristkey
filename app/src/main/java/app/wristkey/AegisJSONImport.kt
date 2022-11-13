package app.wristkey

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.audiofx.HapticGenerator
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import wristkey.R
import java.io.File
import java.io.FileReader
import java.util.*


class AegisJSONImport : Activity() {

    lateinit var utilities: Utilities

    lateinit var backButton: ImageButton
    lateinit var doneButton: ImageButton
    lateinit var importLabel: TextView
    lateinit var description: TextView

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aegis_jsonimport)

        utilities = Utilities (applicationContext)

        initializeUI()

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeUI () {
        backButton = findViewById (R.id.backButton)
        doneButton = findViewById (R.id.doneButton)
        importLabel = findViewById (R.id.label)
        description = findViewById (R.id.description)

        description.text = getString (R.string.wristkey_import_blurb) + " " + applicationContext.filesDir.toString() + "\n\n" + getString (R.string.use_adb_blurb)

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
        if (ContextCompat.checkSelfPermission(this@AegisJSONImport, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this@AegisJSONImport, arrayOf(permission), requestCode)
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
                Toast.makeText(this@AegisJSONImport, "Please grant Wristkey storage permissions in settings", Toast.LENGTH_LONG).show()
                val intent = Intent (android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
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
                val reader = FileReader(file.path)
                val jsonData = reader.readText()

                if (file.name.contains("aegis") && file.name.endsWith(".json")) {
                    logins = utilities.aegisToWristkey (jsonData)
                }

                importingDescription.text = "Found file: \n${file.name}"

                Toast.makeText(applicationContext, "Imported ${logins.size} accounts", Toast.LENGTH_SHORT).show()
                importingDescription.performHapticFeedback(HapticFeedbackConstants.REJECT)
                file.delete()

                for (login in logins) {
                    importingDescription.text = "${login.issuer}"
                    utilities.writeToVault(login, UUID.randomUUID().toString())
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
            setContentView(R.layout.activity_aegis_jsonimport)
            Toast.makeText(this, "Couldn't access storage. Please raise an issue on Wristkey's GitHub repo.", Toast.LENGTH_LONG).show()
            noDirectory.printStackTrace()
        }

    }

}
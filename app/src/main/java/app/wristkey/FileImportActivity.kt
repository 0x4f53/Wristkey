package app.wristkey

import android.content.Intent
import android.content.pm.PackageManager
import android.media.audiofx.HapticGenerator
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anggrayudi.storage.SimpleStorageHelper
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import wristkey.R
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class FileImportActivity : AppCompatActivity() {

    lateinit var mfaCodesTimer: Timer
    lateinit var utilities: Utilities
    lateinit var storageHelper: SimpleStorageHelper

    private lateinit var clock: TextView

    var isRound: Boolean = false

    lateinit var backButton: Button
    lateinit var pickFileButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_import)

        utilities = Utilities (applicationContext)
        mfaCodesTimer = Timer()
        storageHelper = SimpleStorageHelper(this, utilities.FILES_REQUEST_CODE, savedInstanceState)

        initializeUI()

    }

    private fun initializeUI () {
        setContentView(R.layout.activity_file_import)

        clock = findViewById(R.id.clock)
        startClock()

        pickFileButton = findViewById (R.id.filePickerButton)
        backButton = findViewById (R.id.backButton)

        isRound = utilities.db.getBoolean (utilities.CONFIG_SCREEN_ROUND, resources.configuration.isScreenRound)

        backButton.setOnClickListener {
            backButton.performHapticFeedback(HapticGenerator.SUCCESS)
            finish()
        }

        pickFileButton.setOnClickListener {
            storageHelper.openFilePicker (
                allowMultiple = false,
                filterMimeTypes = arrayOf (utilities.JSON_MIME_TYPE)
            )
        }

        storageHelper.onFileSelected = { _, files ->
            val path = files[0].uri
            readData(path)
        }

        // Wear OS doesn't have a file picker, disable button
        if (utilities.isWearOsDevice()) pickFileButton.visibility = View.GONE

    }

    private fun startClock () {
        if (!utilities.db.getBoolean(utilities.SETTINGS_CLOCK_ENABLED, true)) clock.visibility = View.GONE

        try {
            mfaCodesTimer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    val hourType = if (android.text.format.DateFormat.is24HourFormat(applicationContext)) "hh" else "HH"
                    val currentHour = SimpleDateFormat(hourType, Locale.getDefault()).format(Date())
                    val currentMinute = SimpleDateFormat("mm", Locale.getDefault()).format(Date())
                    runOnUiThread { clock.text = "$currentHour:$currentMinute" }
                }
            }, 0, 1000)
        } catch (_: IllegalStateException) { }
    }

    override fun onStop() {
        super.onStop()
        mfaCodesTimer.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        mfaCodesTimer.cancel()
        finish()
    }

    override fun onStart() {
        super.onStart()
        mfaCodesTimer = Timer()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        storageHelper.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        storageHelper.onRestoreInstanceState(savedInstanceState)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        storageHelper.storage.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == utilities.FILES_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readData(null)
            } else {
                Toast.makeText(this@FileImportActivity, "Please grant Wristkey storage permissions in settings", Toast.LENGTH_LONG).show()
                val intent = Intent (android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }

    private fun readData(fileName: Uri?) {
        setContentView(R.layout.import_loading_screen)

        clock = findViewById(R.id.clock)
        startClock()

        var logins: MutableList<Utilities.MfaCode> = mutableListOf()

        val title = findViewById<TextView>(R.id.title)
        val description = findViewById<TextView>(R.id.description)

        val progress = findViewById<LinearProgressIndicator>(R.id.progress)
        val progressRound = findViewById<CircularProgressIndicator>(R.id.progressRound)

        if (isRound) {
            progress.visibility = View.GONE
            progressRound.visibility = View.VISIBLE
        } else {
            progress.visibility = View.VISIBLE
            progressRound.visibility = View.GONE
        }

        val doneButton: Button = findViewById(R.id.doneButton)

        description.text = "Reading data"
        doneButton.visibility = View.GONE

        fun setNegative(message: String) {
            title.text = "Error"
            description.text = message

            progress.visibility = View.GONE
            progressRound.visibility = View.GONE
            doneButton.visibility = View.VISIBLE

            doneButton.setCompoundDrawablesWithIntrinsicBounds (getDrawable(R.drawable.ic_prev)!!, null, null, null)
            doneButton.text = "Go back"
            doneButton.setOnClickListener { finish() }
        }

        if (fileName != null) {
            lateinit var file: InputStream
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    file = contentResolver.openInputStream(fileName)!!
                    val fileData = String(file.readBytes())
                    logins = try { utilities.bitwardenToWristkey(JSONObject(fileData)) } catch (_: Exception) { logins }
                    logins = try { utilities.aegisToWristkey(JSONObject(fileData)) } catch (_: Exception) { logins }
                    logins = try { utilities.andOtpToWristkey(JSONArray(fileData)) } catch (_: Exception) { logins }
                    withContext(Dispatchers.IO) { file.close() }
                    if (logins.isEmpty()) throw NoSuchFieldException()
                    withContext(Dispatchers.Main) {
                        title.text = "Import from file"
                        description.text = "Imported ${logins.size} account(s)!"
                        description.append("\n\n")
                        for ((index, login) in logins.withIndex()) description.append("${if (index != 0) " ⋅ " else ""}${login.issuer}")
                        progress.visibility = View.GONE
                        progressRound.visibility = View.GONE
                        doneButton.visibility = View.VISIBLE
                        doneButton.setCompoundDrawablesWithIntrinsicBounds (getDrawable(R.drawable.outline_save_24)!!, null, null, null)
                        doneButton.setOnClickListener {
                            logins.forEach { utilities.overwriteLogin(utilities.encodeOtpAuthURL(it)) }
                            finishAffinity()
                            startActivity(Intent(applicationContext, MainActivity::class.java))
                        }
                    }
                } catch (noDirectory: NullPointerException) {
                    withContext(Dispatchers.Main) { setNegative("Couldn't access file.") }
                } catch (invalidFile: JSONException) {
                    withContext(Dispatchers.Main) { setNegative("Invalid file. Please follow the instructions on the previous screen.") }
                } catch (noData: NoSuchFieldException) {
                    withContext(Dispatchers.Main) { setNegative("No data found in file. It may be corrupt or may have no 2FA secrets in it.") }
                }
            }
        }

    }



}
package app.wristkey
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import wristkey.R
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class ExportActivity : AppCompatActivity() {

    lateinit var mfaCodesTimer: Timer

    lateinit var utilities: Utilities

    private lateinit var clock: TextView

    private lateinit var qrExportButton: CardView
    private lateinit var fileExportButton: CardView

    private lateinit var backButton: CardView

    private lateinit var logins: List<Utilities.MfaCode>
    var loginNumber = 0

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        utilities = Utilities (applicationContext)
        mfaCodesTimer = Timer()

        initializeUI()
        startClock()

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


    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeUI () {

        clock = findViewById(R.id.clock)

        qrExportButton = findViewById (R.id.qrExportButton)
        fileExportButton = findViewById (R.id.fileExportButton)

        backButton = findViewById (R.id.backButton)

        //logins = utilities.getLogins()

        qrExportButton.setOnClickListener {
            exportViaQrCodes()
        }

        fileExportButton.setOnClickListener {
            exportViaFile()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun exportViaFile () {

        if (logins.isEmpty()) {
            Toast.makeText(this, "Your vault is empty!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val directory = File (applicationContext.filesDir.toString())

        val rfc3339Timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())

        val filename = directory.absolutePath + '/' + rfc3339Timestamp + ".wfs"

        Log.d ("Wristkey", "Writing export file to: " + applicationContext.filesDir.toString())

        val writer = FileWriter(filename)
        // writer.write(JSONObject(utilities.getVaultLoginsOnly()).toString(4))
        writer.flush()
        writer.close()

        Toast.makeText(this, "Exported Wristkey vault to ${directory.absolutePath}", Toast.LENGTH_LONG).show()
        finish()

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun exportViaQrCodes() {

        if (logins.isEmpty()) {
            Toast.makeText(this, "Your vault is empty!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val intent = Intent (applicationContext, QRCodeActivity::class.java)
        // intent.putExtra (utilities.INTENT_QR_DATA, utilities.getUuid(logins[loginNumber]))
        loginNumber += 1
        startActivityForResult (intent, utilities.EXPORT_RESPONSE_CODE)

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            utilities.EXPORT_RESPONSE_CODE -> {
                if (logins.size > 2) {
                    if (loginNumber < logins.size) {
                        val intent = Intent (applicationContext, QRCodeActivity::class.java)
                        // intent.putExtra (utilities.INTENT_QR_DATA, utilities.getUuid(logins[loginNumber]))
                        loginNumber += 1
                        startActivityForResult (intent, utilities.EXPORT_RESPONSE_CODE)
                    } else {
                        Toast.makeText(applicationContext, "Done!", Toast.LENGTH_SHORT).show()
                        qrExportButton.performHapticFeedback(HapticFeedbackConstants.REJECT)
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
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

}
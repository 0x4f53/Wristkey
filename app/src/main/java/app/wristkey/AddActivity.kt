package app.wristkey
import android.content.Intent
import android.media.audiofx.HapticGenerator
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import wristkey.R
import java.text.SimpleDateFormat
import java.util.*

class AddActivity : AppCompatActivity() {

    lateinit var mfaCodesTimer: Timer
    lateinit var utilities: Utilities

    private lateinit var clock: TextView

    private lateinit var manualEntry: Button
    private lateinit var wifiTransfer: Button
    private lateinit var aegisImportButton: CardView
    private lateinit var googleAuthenticatorImport: CardView
    private lateinit var bitwardenImport: CardView
    private lateinit var andOtpImport: CardView
    private lateinit var backupFileButton: CardView
    private lateinit var scanQRCode: Button

    private lateinit var backButton: Button

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        utilities = Utilities(applicationContext)
        mfaCodesTimer = Timer()
        initializeUI()
        startClock()

    }

    private fun startClock () {
        if (!utilities.vault.getBoolean(utilities.SETTINGS_CLOCK_ENABLED, true)) clock.visibility = View.GONE

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

    private fun initializeUI () {

        clock = findViewById(R.id.clock)

        manualEntry = findViewById (R.id.manualEntry)
        wifiTransfer = findViewById (R.id.wifiTransfer)
        aegisImportButton = findViewById (R.id.aegisImportButton)
        googleAuthenticatorImport = findViewById (R.id.googleAuthenticatorImport)
        andOtpImport = findViewById (R.id.andOtpImportButton)
        bitwardenImport = findViewById (R.id.bitwardenImport)
        backupFileButton = findViewById (R.id.backupFileButton)
        scanQRCode = findViewById (R.id.scanQrCode)

        backButton = findViewById (R.id.backButton)

        manualEntry.setOnClickListener {
            startActivity(Intent(applicationContext, ManualEntryActivity::class.java))
            manualEntry.performHapticFeedback(HapticGenerator.SUCCESS)
        }

        wifiTransfer.setOnClickListener {
            if (utilities.wiFiExists(applicationContext)) {
                startActivity(Intent(applicationContext, WiFiTransferActivity::class.java))
                wifiTransfer.performHapticFeedback(HapticGenerator.SUCCESS)
            } else {
                wifiTransfer.performHapticFeedback(HapticGenerator.ERROR)
                MaterialAlertDialogBuilder(this@AddActivity)
                    .setTitle("No Wi-Fi")
                    .setMessage(getString(R.string.wifi_error))
                    .setPositiveButton("Back", null)
                    .setCancelable(false)
                    .create().show()
            }
        }

        backupFileButton.setOnClickListener {
            startActivity(Intent(applicationContext, WristkeyImport::class.java))
            aegisImportButton.performHapticFeedback(HapticGenerator.SUCCESS)
        }

        scanQRCode.setOnClickListener {
            startActivity(Intent(applicationContext, OtpAuthImport::class.java))
            aegisImportButton.performHapticFeedback(HapticGenerator.SUCCESS)
        }

        aegisImportButton.setOnClickListener {
            startActivity(Intent(applicationContext, AegisJSONImport::class.java))
            aegisImportButton.performHapticFeedback(HapticGenerator.SUCCESS)
        }

        googleAuthenticatorImport.setOnClickListener {
            startActivity(Intent(applicationContext, AuthenticatorQRImport::class.java))
            aegisImportButton.performHapticFeedback(HapticGenerator.SUCCESS)
        }

        bitwardenImport.setOnClickListener {
            startActivity(Intent(applicationContext, BitwardenJSONImport::class.java))
            aegisImportButton.performHapticFeedback(HapticGenerator.SUCCESS)
        }

        andOtpImport.setOnClickListener {
            startActivity(Intent(applicationContext, AndOtpJSONImport::class.java))
            aegisImportButton.performHapticFeedback(HapticGenerator.SUCCESS)
        }

        backButton.setOnClickListener {
            finish()
        }
    }

}
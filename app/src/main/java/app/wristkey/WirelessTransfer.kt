package app.wristkey
import android.content.Intent
import android.media.audiofx.HapticGenerator
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import wristkey.R
import java.text.SimpleDateFormat
import java.util.*

class WirelessTransfer : AppCompatActivity() {

    lateinit var mfaCodesTimer: Timer
    lateinit var utilities: Utilities

    private lateinit var clock: TextView

    private lateinit var manualEntry: CardView
    private lateinit var aegisImportButton: CardView
    private lateinit var googleAuthenticatorImport: CardView
    private lateinit var bitwardenImport: CardView
    private lateinit var andOtpImport: CardView
    private lateinit var backupFileButton: CardView
    private lateinit var scanQRCode: CardView

    private lateinit var backButton: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        utilities = Utilities(applicationContext)
        mfaCodesTimer = Timer()
        initializeUI()
        startClock()

    }

    private fun startClock () {

        if (!utilities.vault.getBoolean(utilities.SETTINGS_CLOCK_ENABLED, true)) {
            findViewById<CardView>(R.id.clockBackground).visibility = View.GONE
        }

        try {
            mfaCodesTimer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    val currentHour24 = SimpleDateFormat("HH", Locale.getDefault()).format(Date())
                    val currentHour = SimpleDateFormat("hh", Locale.getDefault()).format(Date())
                    val currentMinute = SimpleDateFormat("mm", Locale.getDefault()).format(Date())
                    val currentSecond = SimpleDateFormat("s", Locale.getDefault()).format(Date()).toInt()
                    val currentAmPm = SimpleDateFormat("a", Locale.getDefault()).format(Date())
                    runOnUiThread {
                        try {
                            clock.text = "$currentHour:$currentMinute"
                            if (utilities.vault.getBoolean(utilities.SETTINGS_24H_CLOCK_ENABLED, false)) clock.text = "$currentHour24:$currentMinute"
                        } catch (_: Exception) { }
                    }
                }
            }, 0, 1000) // 1000 milliseconds = 1 second
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
        aegisImportButton = findViewById (R.id.aegisImportButton)
        googleAuthenticatorImport = findViewById (R.id.googleAuthenticatorImport)
        andOtpImport = findViewById (R.id.andOtpImportButton)
        bitwardenImport = findViewById (R.id.bitwardenImport)
        backupFileButton = findViewById (R.id.backupFileButton)

        backButton = findViewById (R.id.backButton)

        manualEntry.setOnClickListener {
            startActivity(Intent(applicationContext, ManualEntryActivity::class.java))
            manualEntry.performHapticFeedback(HapticGenerator.SUCCESS)
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
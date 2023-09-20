package app.wristkey
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.audiofx.HapticGenerator
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import wristkey.R
import java.text.SimpleDateFormat
import java.util.*

class SendActivity : AppCompatActivity() {

    lateinit var mfaCodesTimer: Timer
    lateinit var utilities: Utilities

    private lateinit var clock: TextView
    private lateinit var scanQrCodeDescription: TextView
    private lateinit var scanQrCode: Button
    private lateinit var ipLayout: TextInputLayout
    private lateinit var ipInput: TextInputEditText
    private lateinit var ipAndPort: String

    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send)

        utilities = Utilities(applicationContext)
        mfaCodesTimer = Timer()
        initializeUI()
        startClock()

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

    private fun initializeUI () {
        clock = findViewById(R.id.clock)

        scanQrCode = findViewById (R.id.scanQrCode)
        scanQrCodeDescription = findViewById(R.id.scanQrCodeDescription)

        scanQrCode.setOnClickListener {
            scanQrCode.performHapticFeedback(HapticGenerator.SUCCESS)
            checkPermission(Manifest.permission.CAMERA, utilities.CAMERA_REQUEST_CODE)
        }

        if (!utilities.hasCamera()) {
            scanQrCode.visibility = View.GONE
            scanQrCodeDescription.text = getString(R.string.send_description_no_camera)
        }


        ipLayout = findViewById (R.id.ipLayout)
        ipInput = findViewById (R.id.ipInput)
        ipInput.doOnTextChanged { text, _, _, _ ->
            if (utilities.isIp(text.toString())) {
                startEncryptSend()
                ipInput.performHapticFeedback(HapticGenerator.SUCCESS)
            }
        }

        backButton = findViewById (R.id.backButton)
        backButton.setOnClickListener { finish() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == utilities.CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            ipAndPort = data?.getStringExtra(utilities.QR_CODE_SCAN_REQUEST).toString()
            if (utilities.isIp(ipAndPort)) {
                startEncryptSend()
                scanQrCode.performHapticFeedback(HapticGenerator.SUCCESS)
            } else {
                AlertDialog.Builder(this@SendActivity)
                    .setMessage(R.string.invalid_send_qr_code)
                    .setNegativeButton("Go back") { _, _ -> finish() }
                    .create().show()
            }
        }
    }

    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this@SendActivity, permission) == PackageManager.PERMISSION_DENIED) ActivityCompat.requestPermissions(this@SendActivity, arrayOf(permission), requestCode)
        else {
            when (requestCode) {
                utilities.CAMERA_REQUEST_CODE -> startScannerUI()
            }
        }
    }

    private fun startScannerUI () {
        val intent = Intent (applicationContext, QRScannerActivity::class.java)
        startActivityForResult(intent, utilities.CAMERA_REQUEST_CODE)
    }

    private fun startEncryptSend() {
        val intent = Intent (applicationContext, EncryptSendActivity::class.java)
        intent.putExtra (utilities.INTENT_WIFI_IP, ipAndPort)
        startActivityForResult (intent, utilities.EXPORT_RESPONSE_CODE)
        finish()
    }

}
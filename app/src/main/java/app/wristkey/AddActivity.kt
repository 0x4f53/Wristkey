package app.wristkey
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import wristkey.R
import java.text.SimpleDateFormat
import java.util.*


class AddActivity : AppCompatActivity() {

    lateinit var mfaCodesTimer: Timer
    lateinit var utilities: Utilities

    private lateinit var clock: TextView

    private lateinit var manualEntry: Button
    private lateinit var wifiTransfer: Button
    private lateinit var fileImport: Button
    private lateinit var scanQRCode: Button

    private lateinit var backButton: Button

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        utilities = Utilities(applicationContext)
        mfaCodesTimer = Timer()
        initializeUI()

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

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == utilities.CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) startScannerUI()
            else {
                Toast.makeText(this@AddActivity, "Please grant Wristkey camera permissions in settings", Toast.LENGTH_LONG).show()
                val intent = Intent (Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this@AddActivity, permission) == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this@AddActivity, arrayOf(permission), requestCode)
        else {
            when (requestCode) {
                utilities.CAMERA_REQUEST_CODE -> startScannerUI()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == utilities.CAMERA_REQUEST_CODE+1 && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra(utilities.QR_CODE_SCAN_REQUEST)) {
                val scannedData = data.getStringExtra(utilities.QR_CODE_SCAN_REQUEST)

                if (!scannedData.isNullOrBlank()) {
                    if (scannedData.contains("otpauth://")) {
                        utilities.overwriteLogin(scannedData)
                        finishAffinity()
                        startActivity(Intent(applicationContext, MainActivity::class.java))
                        return
                    } else if (scannedData.contains("otpauth-migration://")) {

                    }

                }

                Toast.makeText(this@AddActivity, getString(R.string.invalid_qr_code), Toast.LENGTH_LONG).show()

            }
        }
    }

    private fun startScannerUI () {
        val intent = Intent(this@AddActivity, QRScannerActivity::class.java)
        startActivityForResult(intent, utilities.CAMERA_REQUEST_CODE+1)
    }

    private fun initializeUI () {

        clock = findViewById(R.id.clock)
        startClock()

        manualEntry = findViewById (R.id.manualEntry)
        wifiTransfer = findViewById (R.id.wifiTransfer)
        scanQRCode = findViewById (R.id.scanQrCode)
        fileImport = findViewById (R.id.fileImport)

        backButton = findViewById (R.id.backButton)

        manualEntry.setOnClickListener {
            startActivity(Intent(applicationContext, ManualEntryActivity::class.java))
            finish()
        }

        wifiTransfer.setOnClickListener {
            if (utilities.wiFiExists(applicationContext)) {
                startActivity(Intent(applicationContext, WiFiTransferActivity::class.java))
                finish()
            } else {

                CustomFullscreenDialogFragment(
                    title = "Network error",
                    message = getString(R.string.wifi_error),
                    positiveButtonText = null,
                    positiveButtonIcon = null,
                    negativeButtonText = "Go back",
                    negativeButtonIcon = getDrawable(R.drawable.ic_prev)!!,
                ).show(supportFragmentManager, "CustomFullscreenDialog")

            }
        }

        if (utilities.hasCamera()) scanQRCode.visibility = View.VISIBLE else scanQRCode.visibility = View.GONE

        scanQRCode.setOnClickListener {
            checkPermission(Manifest.permission.CAMERA, utilities.CAMERA_REQUEST_CODE)
        }

        fileImport.setOnClickListener {
            startActivity(Intent(applicationContext, FileImportActivity::class.java))
            finish()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

}
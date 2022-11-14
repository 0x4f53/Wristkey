package app.wristkey

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.budiyev.android.codescanner.*
import wristkey.R
import java.util.*

class QRScannerActivity : AppCompatActivity() {
    private lateinit var codeScanner: CodeScanner

    private lateinit var scannerView: CodeScannerView

    private lateinit var scanType: String

    lateinit var utilities: Utilities

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scanner)

        utilities = Utilities(applicationContext)
        scanType = intent.getStringExtra(utilities.QR_CODE_SCAN_REQUEST)!!

        scannerView = findViewById(R.id.scanner_view)
        codeScanner = CodeScanner(this, scannerView)
        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS
        codeScanner.autoFocusMode = AutoFocusMode.CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false

        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                if (scanType == utilities.OTPAUTH_SCAN_CODE) _2faScan(it.text)
                else if (scanType == utilities.AUTHENTICATOR_EXPORT_SCAN_CODE) authenticatorExportScan(it.text)
                finish()
            }
        }

        codeScanner.errorCallback = ErrorCallback {
            runOnUiThread {
                Toast.makeText(this, "Error initializing camera", Toast.LENGTH_LONG).show()
            }
        }

        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun _2faScan (scanData: String) {
        if (
            scanData.contains("otpauth://")
            && !scanData.contains("otpauth-migration://")
            && scanData.contains("secret=")
        ) {
            Toast.makeText(this, "Saving \"${utilities.decodeOTPAuthURL(scanData)?.issuer}\"", Toast.LENGTH_LONG).show()
            utilities.writeToVault(utilities.decodeOTPAuthURL(scanData)!!, UUID.randomUUID().toString())
            scannerView.performHapticFeedback(HapticFeedbackConstants.REJECT)
            finishAffinity()
            startActivity(Intent(applicationContext, MainActivity::class.java))
        } else {
            Toast.makeText(this, "This QR code is invalid", Toast.LENGTH_LONG).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun authenticatorExportScan (scanData: String) {
        if (
            !scanData.contains("otpauth://")
            && scanData.contains("otpauth-migration://")
        ) {
            val logins = utilities.authenticatorToWristkey(scanData)
            Toast.makeText(this, "Saving login(s)", Toast.LENGTH_LONG).show()
            for (login in logins) {
                utilities.writeToVault(login, UUID.randomUUID().toString())
            }
            scannerView.performHapticFeedback(HapticFeedbackConstants.REJECT)
            finishAffinity()
            startActivity(Intent(applicationContext, MainActivity::class.java))
        } else {
            Toast.makeText(this, "This QR code is invalid", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }
}
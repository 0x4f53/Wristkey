package zeroxfourf.wristkey

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.budiyev.android.codescanner.*
import wristkey.R

class QRScannerActivity : AppCompatActivity() {
    private lateinit var codeScanner: CodeScanner

    private lateinit var scannerView: CodeScannerView

    lateinit var utilities: Utilities

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scanner)

        utilities = Utilities(applicationContext)

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
                it.text
                val resultIntent = Intent()
                resultIntent.putExtra(utilities.QR_CODE_SCAN_REQUEST, it.text)
                setResult(Activity.RESULT_OK, resultIntent)
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

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }
}
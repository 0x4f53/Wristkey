package app.wristkey
import android.content.Intent
import android.media.audiofx.HapticGenerator
import android.os.Build
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import wristkey.R

class ExportActivity : AppCompatActivity() {

    lateinit var utilities: Utilities

    private lateinit var qrExportButton: CardView
    private lateinit var fileExportButton: CardView

    private lateinit var backButton: CardView

    private lateinit var logins: List<Utilities.MfaCode>
    var loginNumber = 0

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export)

        utilities = Utilities (applicationContext)

        initializeUI()

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeUI () {
        qrExportButton = findViewById (R.id.qrExportButton)
        fileExportButton = findViewById (R.id.fileExportButton)

        backButton = findViewById (R.id.backButton)

        qrExportButton.setOnClickListener {
            exportViaQrCodes()
        }

        fileExportButton.setOnClickListener {
            startActivity(Intent(applicationContext, WristkeyImport::class.java))
            fileExportButton.performHapticFeedback(HapticGenerator.SUCCESS)
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun exportViaFile () {

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun exportViaQrCodes() {
        logins = utilities.getLogins()

        val intent = Intent (applicationContext, QRCodeActivity::class.java)
        intent.putExtra (utilities.INTENT_UUID, utilities.getUuid(logins[loginNumber]))
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
                        intent.putExtra (utilities.INTENT_UUID, utilities.getUuid(logins[loginNumber]))
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

}
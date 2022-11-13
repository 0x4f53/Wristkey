package app.wristkey
import android.content.Intent
import android.media.audiofx.HapticGenerator
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import wristkey.R

class AddActivity : AppCompatActivity() {
    private lateinit var manualEntry: CardView
    private lateinit var aegisImportButton: CardView
    private lateinit var googleAuthenticatorImport: CardView
    private lateinit var backupFileButton: CardView
    private lateinit var scanQRCode: CardView

    private lateinit var backButton: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        initializeUI()

        /*val importBitwardenButtonText = findViewById<TextView>(R.id.BitwardenImportLabel)
        val importBitwardenButton = findViewById<ImageView>(R.id.BitwardenImportButton)
        val importBitwarden = findViewById<LinearLayout>(R.id.BitwardenImport)

        importBitwarden.setOnClickListener {
            val intent = Intent(applicationContext, BitwardenJSONImport::class.java)
            startActivity(intent)
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
            finish()
        }

        andOTPImport.setOnClickListener {
            val intent = Intent(applicationContext, AndOtpJSONImport::class.java)
            startActivity(intent)
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
            finish()
        }
*/

    }

    private fun initializeUI () {
        manualEntry = findViewById (R.id.manualEntry)
        aegisImportButton = findViewById (R.id.aegisImportButton)
        googleAuthenticatorImport = findViewById (R.id.googleAuthenticatorImport)
        backupFileButton = findViewById (R.id.backupFileButton)
        scanQRCode = findViewById (R.id.scanQRCode)

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

        backButton.setOnClickListener {
            finish()
        }
    }

}
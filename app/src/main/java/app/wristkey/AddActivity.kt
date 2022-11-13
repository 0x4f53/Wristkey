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

    private lateinit var backButton: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        initializeUI()

        /*val importBitwardenButtonText = findViewById<TextView>(R.id.BitwardenImportLabel)
        val importBitwardenButton = findViewById<ImageView>(R.id.BitwardenImportButton)
        val importBitwarden = findViewById<LinearLayout>(R.id.BitwardenImport)

        val manualEntry = findViewById<LinearLayout>(R.id.ManualEntry)

        val importAuthenticatorButtonText = findViewById<TextView>(R.id.AuthenticatorImportLabel)
        val importAuthenticator = findViewById<LinearLayout>(R.id.AuthenticatorImport)
        val importAuthenticatorButton = findViewById<ImageView>(R.id.AuthenticatorImportButton)
        val scanQRCode = findViewById<LinearLayout>(R.id.ScanQRCode)

        val backButton = findViewById<ImageView>(R.id.BackButton)

        importBitwarden.setOnClickListener {
            val intent = Intent(applicationContext, BitwardenJSONImport::class.java)
            startActivity(intent)
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
            finish()
        }

        importAuthenticator.setOnClickListener {
            val intent = Intent(applicationContext, AuthenticatorQRImport::class.java)
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

        wristkeyImport.setOnClickListener {
            val intent = Intent(applicationContext, WristkeyImport::class.java)
            startActivity(intent)
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
            finish()
        }*/
/*
        backButton.setOnClickListener {
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }*/

    }

    private fun initializeUI () {
        manualEntry = findViewById (R.id.manualEntry)
        aegisImportButton = findViewById (R.id.aegisImportButton)
        googleAuthenticatorImport = findViewById (R.id.googleAuthenticatorImport)

        backButton = findViewById (R.id.backButton)

        manualEntry.setOnClickListener {
            startActivity(Intent(applicationContext, ManualEntryActivity::class.java))
            manualEntry.performHapticFeedback(HapticGenerator.SUCCESS)
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
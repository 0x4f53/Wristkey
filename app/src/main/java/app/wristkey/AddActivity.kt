package app.wristkey
import android.content.Intent
import android.media.audiofx.HapticGenerator
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import androidx.cardview.widget.CardView
import wristkey.R

class AddActivity : WearableActivity() {
    private lateinit var manualEntry: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        initializeUI()

        manualEntry.setOnClickListener {
            startActivity(Intent(applicationContext, ManualEntryActivity::class.java))
            manualEntry.performHapticFeedback(HapticGenerator.SUCCESS)
        }

        /*val importBitwardenButtonText = findViewById<TextView>(R.id.BitwardenImportLabel)
        val importBitwardenButton = findViewById<ImageView>(R.id.BitwardenImportButton)
        val importBitwarden = findViewById<LinearLayout>(R.id.BitwardenImport)

        val manualEntry = findViewById<LinearLayout>(R.id.ManualEntry)

        val importAuthenticatorButtonText = findViewById<TextView>(R.id.AuthenticatorImportLabel)
        val importAuthenticator = findViewById<LinearLayout>(R.id.AuthenticatorImport)
        val importAuthenticatorButton = findViewById<ImageView>(R.id.AuthenticatorImportButton)
        val scanQRCode = findViewById<LinearLayout>(R.id.ScanQRCode)

        val backButton = findViewById<ImageView>(R.id.BackButton)

        manualEntry.setOnClickListener {
            val intent = Intent(applicationContext, ManualEntryActivity::class.java)
            startActivity(intent)
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
            finish()
        }

        scanQRCode.setOnClickListener {
            val intent = Intent(applicationContext, OtpAuthImport::class.java)
            startActivity(intent)
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
            finish()
        }

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

        aegisImport.setOnClickListener {
            val intent = Intent(applicationContext, AegisJSONImport::class.java)
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

    fun initializeUI () {
        manualEntry = findViewById (R.id.manualEntry)
    }

}
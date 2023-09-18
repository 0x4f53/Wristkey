package app.wristkey
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import wristkey.R
import java.util.*
import kotlin.properties.Delegates


class ManualEntryActivity : AppCompatActivity() {

    lateinit var utilities: Utilities

    private lateinit var issuerInput: TextInputEditText
    private lateinit var labelInput: TextInputEditText
    private lateinit var secretInput: TextInputEditText

    private lateinit var periodLabel: TextView
    private lateinit var lengthLabel: TextView

    private lateinit var showQrCodeButton: Button
    private lateinit var doneButton: Button
    private lateinit var deleteButton: Button
    private lateinit var backButton: Button

    private lateinit var mode: String
    private lateinit var hashingAlgorithm: String
    private var length by Delegates.notNull<Int>()
    private var period by Delegates.notNull<Int>()

    private lateinit var uuid: String

    private lateinit var loginData: Utilities.MfaCode

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_entry)

        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        utilities = Utilities(applicationContext)
        uuid = UUID.randomUUID().toString()

        initializeUI()

        if (intent.hasExtra(utilities.INTENT_QR_DATA)) {
            uuid = intent.getStringExtra(utilities.INTENT_QR_DATA)!!
            loadLogin ()
        }

    }

    private fun initializeUI () {
        issuerInput = findViewById (R.id.issuerInput)
        labelInput = findViewById (R.id.labelInput)
        secretInput = findViewById (R.id.secretInput)

        showQrCodeButton = findViewById (R.id.showQrCodeButton)
        doneButton = findViewById (R.id.doneButton)
        deleteButton = findViewById (R.id.deleteButton)
        backButton = findViewById (R.id.backButton)

        secretInput.transformationMethod = PasswordTransformationMethod()

        mode = "totp"

        hashingAlgorithm = "SHA1"

        length = 6

        period = 30

        doneButton.setOnClickListener {

            if (issuerInput.length() < 2) {
                Toast.makeText(applicationContext, "Enter issuer name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (secretInput.length() <= 7) {
                Toast.makeText(applicationContext, "Enter a valid secret", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginData = Utilities.MfaCode (
                type = utilities.DEFAULT_TYPE,
                mode = mode,
                issuer = issuerInput.text.toString(),
                account = if (!labelInput.text.isNullOrEmpty()) labelInput.text.toString()  else "",
                secret = secretInput.text.toString(),
                algorithm = hashingAlgorithm,
                digits = length,
                period = period,
                lock = false,
                counter = 0,
                label = labelInput.text.toString()
            )

            utilities.writeToVault(loginData, uuid)

            finish()
            finishAffinity()
            startActivity(Intent(applicationContext, MainActivity::class.java))

        }

        showQrCodeButton.visibility = View.GONE
        deleteButton.visibility = View.GONE

        backButton.setOnClickListener {
            finish()
        }

    }

    private fun loadLogin () {
        val login = utilities.getLogin(uuid)

        issuerInput.setText (login?.issuer.toString())

        val label = if (login?.account.isNullOrEmpty()) login?.label.toString() else login?.account
        labelInput.setText (label)
        secretInput.setText (login?.secret.toString())

        showQrCodeButton.visibility = View.VISIBLE

        showQrCodeButton.setOnClickListener {
            val intent = Intent(applicationContext, QRCodeActivity::class.java)
            intent.putExtra(utilities.INTENT_QR_DATA, uuid)
            startActivity(intent)
            deleteButton.performHapticFeedback(HapticFeedbackConstants.REJECT)
        }

        deleteButton.visibility = View.VISIBLE

        deleteButton.setOnClickListener {
            MaterialAlertDialogBuilder(this@ManualEntryActivity)
                .setTitle("Delete")
                .setMessage("Would you like to delete this item?")
                .setPositiveButton("Yes, delete", null)
                .setPositiveButton("Back", null)
                .setCancelable(false)
                .create().show()
        }

    }

}
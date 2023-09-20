package app.wristkey

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckedTextView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import wristkey.R
import java.util.*


class ManualEntryActivity : AppCompatActivity() {

    lateinit var utilities: Utilities

    private lateinit var issuerInput: TextInputEditText
    private lateinit var labelInput: TextInputEditText
    private lateinit var secretInput: TextInputEditText
    private lateinit var accountInput: TextInputEditText

    private var mode: String = "totp"
    private lateinit var timeButton: CheckedTextView
    private lateinit var counterButton: CheckedTextView


    private var algorithm: String = "SHA1"
    private lateinit var sha1Button: CheckedTextView
    private lateinit var sha256Button: CheckedTextView
    private lateinit var sha512Button: CheckedTextView


    private var digits: Int = 6
    private lateinit var fourButton: CheckedTextView
    private lateinit var sixButton: CheckedTextView
    private lateinit var eightButton: CheckedTextView


    private var validity: Int = 30
    private lateinit var validityGroup: LinearLayout
    private lateinit var periodText: TextView
    private lateinit var periodSlider: Slider


    private var counter: Long = 0
    private lateinit var counterInput: TextInputEditText
    private lateinit var counterLayout: TextInputLayout

    private lateinit var doneButton: Button
    private lateinit var deleteButton: Button
    private lateinit var backButton: Button

    private lateinit var uuid: String

    private lateinit var data: Utilities.MfaCode

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
        accountInput = findViewById (R.id.accountInput)

        counterInput = findViewById(R.id.counterInput)
        counterLayout = findViewById(R.id.counter)
        counterLayout.visibility = View.GONE

        timeButton = findViewById(R.id.timeButton)
        counterButton = findViewById(R.id.counterButton)

        sha1Button = findViewById(R.id.sha1Button)
        sha256Button = findViewById(R.id.sha256Button)
        sha512Button = findViewById(R.id.sha512Button)

        sha1Button.setOnClickListener { v ->
            if (!(v as CheckedTextView).isChecked) {
                v.isChecked = true
                algorithm = utilities.ALGO_SHA1
            }
            if (sha256Button.isChecked) sha256Button.isChecked = false
            if (sha512Button.isChecked) sha512Button.isChecked = false
        }

        sha256Button.setOnClickListener { v ->
            if (!(v as CheckedTextView).isChecked) {
                v.isChecked = true
                algorithm = utilities.ALGO_SHA256
            }
            if (sha1Button.isChecked) sha1Button.isChecked = false
            if (sha512Button.isChecked) sha512Button.isChecked = false
        }

        sha512Button.setOnClickListener { v ->
            if (!(v as CheckedTextView).isChecked) {
                v.isChecked = true
                algorithm = utilities.ALGO_SHA512
            }
            if (sha1Button.isChecked) sha1Button.isChecked = false
            if (sha256Button.isChecked) sha256Button.isChecked = false
        }

        fourButton = findViewById(R.id.fourButton)
        sixButton = findViewById(R.id.sixButton)
        eightButton = findViewById(R.id.eightButton)

        fourButton.setOnClickListener { v ->
            if (!(v as CheckedTextView).isChecked) {
                v.isChecked = true
                digits = 4
            }
            if (sixButton.isChecked) sixButton.isChecked = false
            if (eightButton.isChecked) eightButton.isChecked = false
        }

        sixButton.setOnClickListener { v ->
            if (!(v as CheckedTextView).isChecked) {
                v.isChecked = true
                digits = 6
            }
            if (fourButton.isChecked) fourButton.isChecked = false
            if (eightButton.isChecked) eightButton.isChecked = false
        }

        eightButton.setOnClickListener { v ->
            if (!(v as CheckedTextView).isChecked) {
                v.isChecked = true
                digits = 8
            }
            if (fourButton.isChecked) fourButton.isChecked = false
            if (sixButton.isChecked) sixButton.isChecked = false
        }

        validityGroup = findViewById(R.id.validityGroup)
        periodText = findViewById(R.id.periodText)
        periodSlider = findViewById(R.id.periodSlider)
        periodSlider.addOnChangeListener { slider, value, fromUser ->
            if (fromUser) {
                periodText.text = slider.value.toInt().toString()
                validity = slider.value.toInt()
            }
        }

        mode = utilities.MFA_TIME_MODE
        timeButton.setOnClickListener { v ->
            if (!(v as CheckedTextView).isChecked) {
                v.isChecked = true
                mode = utilities.MFA_TIME_MODE
                counterButton.isChecked = false
                validityGroup.visibility = View.VISIBLE
                counterLayout.visibility = View.GONE
                counterButton.isChecked = false
            }
        }

        counterButton.setOnClickListener { v ->
            if (!(v as CheckedTextView).isChecked) {
                mode = utilities.MFA_COUNTER_MODE
                v.isChecked = true
                timeButton.isChecked = false
                validityGroup.visibility = View.GONE
                counterLayout.visibility = View.VISIBLE
                counterButton.isChecked = true
            }
        }

        deleteButton = findViewById(R.id.deleteButton)
        backButton = findViewById (R.id.backButton)

        secretInput.transformationMethod = PasswordTransformationMethod()

        doneButton = findViewById (R.id.doneButton)
        doneButton.setOnClickListener {

            if (issuerInput.length() <= 1) {
                AlertDialog.Builder(this@ManualEntryActivity)
                    .setMessage(getString(R.string.issuer_empty))
                    .setPositiveButton("Go back", null)
                    .create().show()
                return@setOnClickListener
            }

            if (accountInput.length() <= 2) {
                AlertDialog.Builder(this@ManualEntryActivity)
                    .setMessage(R.string.account_empty)
                    .setPositiveButton("Go back", null)
                    .create().show()
                return@setOnClickListener
            }

            if (secretInput.length() <= 7) {
                AlertDialog.Builder(this@ManualEntryActivity)
                    .setMessage(R.string.secret_empty)
                    .setPositiveButton("Go back", null)
                    .create().show()
                return@setOnClickListener
            }

            data = Utilities.MfaCode (
                mode = mode,
                issuer = issuerInput.text.toString(),
                account = accountInput.text.toString(),
                secret = secretInput.text.toString(),
                algorithm = algorithm,
                digits = digits,
                period = validity,
                lock = false,
                counter = counter,
                label = labelInput.text.toString()
            )

            val dataUrl = utilities.encodeOtpAuthURL(data)
            utilities.overwriteLogin(dataUrl)

            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()

        }

        deleteButton.visibility = View.GONE

        backButton.setOnClickListener {
            if (secretInput.text!!.isNotEmpty() || issuerInput.text!!.isNotEmpty() || labelInput.text!!.isNotEmpty()) {
                AlertDialog.Builder(this@ManualEntryActivity)
                    .setMessage(R.string.go_back)
                    .setPositiveButton("Keep editing", null)
                    .setNegativeButton("Discard") { _, _ -> finish() }
                    .create().show()
            } else finish()
        }

    }

    private fun loadLogin () {
        val login = utilities.getLogin(uuid)

        issuerInput.setText (login?.issuer.toString())

        val label = if (login?.account.isNullOrEmpty()) login?.label.toString() else login?.account
        labelInput.setText (label)
        secretInput.setText (login?.secret.toString())

        deleteButton.visibility = View.VISIBLE

        deleteButton.setOnClickListener {
            AlertDialog.Builder(this@ManualEntryActivity)
                .setMessage(R.string.delete)
                .setPositiveButton("Yes, delete", null)
                .setPositiveButton("Back", null)
                .create().show()
        }

    }

}
package zeroxfourf.wristkey

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

        if (intent.hasExtra(utilities.INTENT_EDIT)) {
            val intentData = intent.getStringExtra(utilities.INTENT_EDIT)!!
            loadLogin (intentData)
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
        periodSlider.addOnChangeListener { slider, _, fromUser ->
            if (fromUser) {
                periodText.text = slider.value.toInt().toString()
                validity = slider.value.toInt()
            }
        }

        mode = utilities.MFA_TIME_MODE
        timeButton.setOnClickListener { v ->
            mode = utilities.MFA_TIME_MODE
            if (!(v as CheckedTextView).isChecked) {
                v.isChecked = true
                counterButton.isChecked = false
                validityGroup.visibility = View.VISIBLE
                counterLayout.visibility = View.GONE
                counterButton.isChecked = false
            }
        }

        counterButton.setOnClickListener { v ->
            mode = utilities.MFA_COUNTER_MODE
            if (!(v as CheckedTextView).isChecked) {
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

                CustomFullscreenDialogFragment(
                    title = "Invalid Issuer",
                    message = getString(R.string.issuer_empty),
                    positiveButtonText = null,
                    positiveButtonIcon = null,
                    negativeButtonText = "Go back",
                    negativeButtonIcon = getDrawable(R.drawable.ic_prev)!!,
                ).show(supportFragmentManager, "CustomFullscreenDialog")

                return@setOnClickListener
            }

            if (accountInput.length() <= 2) {

                CustomFullscreenDialogFragment(
                    title = "Invalid Issuer",
                    message = getString(R.string.account_empty),
                    positiveButtonText = null,
                    positiveButtonIcon = null,
                    negativeButtonText = "Go back",
                    negativeButtonIcon = getDrawable(R.drawable.ic_prev)!!,
                ).show(supportFragmentManager, "CustomFullscreenDialog")

                return@setOnClickListener
            }

            if (secretInput.length() <= 7) {

                CustomFullscreenDialogFragment(
                    title = "Invalid Issuer",
                    message = getString(R.string.secret_empty),
                    positiveButtonText = null,
                    positiveButtonIcon = null,
                    negativeButtonText = "Go back",
                    negativeButtonIcon = getDrawable(R.drawable.ic_prev)!!,
                ).show(supportFragmentManager, "CustomFullscreenDialog")

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
                counter = counterInput.text.toString().toLong(),
                label = labelInput.text.toString()
            )

            val dataUrl = utilities.encodeOtpAuthURL(data)

            utilities.overwriteLogin(dataUrl)

            finishAffinity()
            startActivity(Intent(applicationContext, MainActivity::class.java))

        }

        deleteButton.visibility = View.GONE

        backButton.setOnClickListener {
            if (secretInput.text!!.isNotEmpty() || issuerInput.text!!.isNotEmpty() || labelInput.text!!.isNotEmpty()) {
                val dialog = CustomFullscreenDialogFragment(
                    title = "Go back",
                    message = getString(R.string.go_back),
                    positiveButtonText = "Keep editing",
                    positiveButtonIcon = getDrawable(R.drawable.ic_baseline_edit_24),
                    negativeButtonText = "Go back",
                    negativeButtonIcon = getDrawable(R.drawable.ic_prev)!!,
                )
                dialog.setOnNegativeClickListener { finish() }
                dialog.show(supportFragmentManager, "CustomFullscreenDialog")
            } else finish()
        }

    }

    private fun loadLogin (loginData: String) {
        val login = utilities.decodeOtpAuthURL (loginData)!!
        issuerInput.setText (login.issuer)
        secretInput.setText (login.secret)
        accountInput.setText (login.account)
        labelInput.setText (login.label)

        if (login.mode.contains("hotp")) {
            counterButton.performClick()
            counterInput.setText (login.counter.toString())
        }

        periodText.text = login.period.toString()
        periodSlider.value = login.period.toFloat()

        when (login.algorithm) {
            utilities.ALGO_SHA1 -> sha1Button.performClick()
            utilities.ALGO_SHA256 -> sha256Button.performClick()
            utilities.ALGO_SHA512 -> sha512Button.performClick()
        }

        when (login.digits) {
            4 -> fourButton.performClick()
            6 -> sixButton.performClick()
            8 -> eightButton.performClick()
        }

        deleteButton.visibility = View.VISIBLE

        deleteButton.setOnClickListener {
            val deleteDialog = CustomFullscreenDialogFragment(
                title = "Delete \"${login.issuer}\"",
                message = getString(R.string.delete),
                positiveButtonText = "Delete",
                positiveButtonIcon = getDrawable(R.drawable.ic_outline_delete_24)!!,
                negativeButtonText = "Go back",
                negativeButtonIcon = getDrawable(R.drawable.ic_prev)!!,
            )

            deleteDialog.setOnPositiveClickListener {
                utilities.deleteFromDataStore (loginData)
                finishAffinity()
                startActivity(Intent(applicationContext, MainActivity::class.java))
            }

            deleteDialog.show(supportFragmentManager, "CustomFullscreenDialog")
        }

    }

}
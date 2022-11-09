package app.wristkey
import android.os.Build
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import com.google.android.material.textfield.TextInputEditText
import wristkey.R
import java.util.*
import kotlin.properties.Delegates


class ManualEntryActivity : WearableActivity() {

    lateinit var utilities: Utilities

    private lateinit var issuerInput: TextInputEditText
    private lateinit var labelInput: TextInputEditText
    private lateinit var secretInput: TextInputEditText

    private lateinit var typeLabel: TextView
    private lateinit var periodLabel: TextView
    private lateinit var hashLabel: TextView
    private lateinit var lengthLabel: TextView

    private lateinit var modeSeekbar: SeekBar
    private lateinit var periodSeekbar: SeekBar
    private lateinit var hashSeekbar: SeekBar
    private lateinit var lengthSeekbar: SeekBar

    private lateinit var doneButton: CardView
    private lateinit var deleteButton: CardView
    private lateinit var backButton: CardView

    private lateinit var mode: String
    private lateinit var hashingAlgorithm: String
    private var length by Delegates.notNull<Int>()
    private var period by Delegates.notNull<Int>()

    private lateinit var loginData: Utilities.MfaCode

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_entry)

        utilities = Utilities(applicationContext)

        initializeUI()

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeUI () {
        issuerInput = findViewById (R.id.issuerInput)
        labelInput = findViewById (R.id.labelInput)
        secretInput = findViewById (R.id.secretInput)

        modeSeekbar = findViewById (R.id.modeSeekbar)
        hashSeekbar = findViewById (R.id.hashSeekbar)
        lengthSeekbar = findViewById (R.id.lengthSeekbar)
        periodSeekbar = findViewById (R.id.periodSeekbar)

        typeLabel = findViewById (R.id.typeLabel)
        hashLabel = findViewById (R.id.hashLabel)
        lengthLabel = findViewById (R.id.lengthLabel)
        periodLabel = findViewById (R.id.periodLabel)

        doneButton = findViewById (R.id.doneButton)
        deleteButton = findViewById (R.id.deleteButton)
        backButton = findViewById (R.id.backButton)

        secretInput.transformationMethod = PasswordTransformationMethod()

        mode = "totp"
        modeSeekbar.setOnSeekBarChangeListener (object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged (p0: SeekBar?, p1: Int, p2: Boolean) {
                when (p0?.progress) {
                    0 -> {
                        typeLabel.text = "Time"
                        mode = "totp"
                        periodSeekbar.visibility = View.VISIBLE
                        periodLabel.visibility = View.VISIBLE
                    }

                    1 -> {
                        typeLabel.text = "Counter"
                        mode = "hotp"
                        periodSeekbar.visibility = View.GONE
                        periodLabel.visibility = View.GONE
                    }
                }
            }
            override fun onStartTrackingTouch (seekBar: SeekBar?) { }
            override fun onStopTrackingTouch (seekBar: SeekBar?) { }
        })

        hashingAlgorithm = "SHA1"
        hashSeekbar.setOnSeekBarChangeListener (object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged (p0: SeekBar?, p1: Int, p2: Boolean) {
                when (p0?.progress) {
                    0 -> hashingAlgorithm = "SHA1"
                    1 -> hashingAlgorithm = "SHA256"
                    2 -> hashingAlgorithm = "SHA512"
                }
                hashLabel.text = hashingAlgorithm
            }
            override fun onStartTrackingTouch (seekBar: SeekBar?) { }
            override fun onStopTrackingTouch (seekBar: SeekBar?) { }
        })

        length = 6
        lengthSeekbar.setOnSeekBarChangeListener (object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged (p0: SeekBar?, p1: Int, p2: Boolean) {
                when (p0?.progress) {
                    0 -> length = 6
                    1 -> length = 8
                }
                lengthLabel.text = "$length digits"
            }
            override fun onStartTrackingTouch (seekBar: SeekBar?) { }
            override fun onStopTrackingTouch (seekBar: SeekBar?) { }
        })

        period = 30
        periodSeekbar.setOnSeekBarChangeListener (object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged (p0: SeekBar?, p1: Int, p2: Boolean) {
                periodLabel.text = "${((p0?.progress)?.plus(1)).toString()} seconds"
                period = (p0?.progress)?.plus(1)!!
            }
            override fun onStartTrackingTouch (seekBar: SeekBar?) { }
            override fun onStopTrackingTouch (seekBar: SeekBar?) { }
        })

        doneButton.setOnClickListener {

            if (issuerInput.length() < 2) {
                Toast.makeText(applicationContext, "Enter issuer name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (secretInput.length() <= 7) {
                Toast.makeText(applicationContext, "Enter a secret", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginData = Utilities.MfaCode (
                uuid4 = UUID.randomUUID().toString(),
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

            Log.d ("Wristkey", "Saved data: " + utilities.encodeOTPAuthURL(loginData)!!)
            finish()
        }

        deleteButton.setOnClickListener {
            finish()
        }

        backButton.setOnClickListener {
            finish()
        }

    }

}
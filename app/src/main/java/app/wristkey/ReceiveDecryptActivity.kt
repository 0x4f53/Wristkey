package app.wristkey
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import wristkey.R
import java.text.SimpleDateFormat
import java.util.*

class ReceiveDecryptActivity : AppCompatActivity() {

    lateinit var timer: Timer
    lateinit var utilities: Utilities

    private lateinit var clock: TextView

    private lateinit var payload: String
    private lateinit var otp: TextInputLayout
    private lateinit var otpInput: TextInputEditText

    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receive_decrypt)
        utilities = Utilities(applicationContext)

        if (intent != null) payload = intent.getStringExtra(utilities.INTENT_WIFI_TRANSFER_PAYLOAD).toString()

        timer = Timer()
        initializeUI()
        startClock()

    }

    private fun startClock () {
        if (!utilities.db.getBoolean(utilities.SETTINGS_CLOCK_ENABLED, true)) clock.visibility = View.GONE

        try {
            timer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    val hourType = if (android.text.format.DateFormat.is24HourFormat(applicationContext)) "hh" else "HH"
                    val currentHour = SimpleDateFormat(hourType, Locale.getDefault()).format(Date())
                    val currentMinute = SimpleDateFormat("mm", Locale.getDefault()).format(Date())
                    runOnUiThread { clock.text = "$currentHour:$currentMinute" }
                }
            }, 0, 1000)
        } catch (_: IllegalStateException) { }
    }

    override fun onStop() {
        super.onStop()
        timer.cancel()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        finish()
    }

    override fun onStart() {
        super.onStart()
        timer = Timer()
    }

    private fun initializeUI () {
        clock = findViewById(R.id.clock)

        otp = findViewById(R.id.otp)
        otpInput = findViewById(R.id.otpInput)

        otpInput.addTextChangedListener { s ->
            if (s?.length == 6) {
                val decryptedData = utilities.decrypt(payload, s.toString())
                if (decryptedData != null) {
                    Log.d ("WRISTKEY-decryptTest", "Received -> $payload")
                    Log.d ("WRISTKEY-decryptTest", "Decrypted -> $decryptedData")
                    utilities.db.edit().putString(utilities.DATA_STORE, decryptedData)
                    Toast.makeText(applicationContext, "Transfer complete! :)", Toast.LENGTH_LONG).show()
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    finish()
                } else {
                    otp.error = "Invalid OTP entered!"
                    otp.performHapticFeedback(HapticFeedbackConstants.REJECT)
                }
            } else {
                otp.error = null
            }
        }

        backButton = findViewById (R.id.backButton)
        backButton.setOnClickListener { finish() }
    }

}
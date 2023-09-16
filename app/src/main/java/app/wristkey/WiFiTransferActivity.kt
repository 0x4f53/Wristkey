package app.wristkey
import android.content.Intent
import android.media.audiofx.HapticGenerator
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import wristkey.R
import java.text.SimpleDateFormat
import java.util.*

class WiFiTransferActivity : AppCompatActivity() {

    lateinit var mfaCodesTimer: Timer
    lateinit var utilities: Utilities

    private lateinit var clock: TextView

    private lateinit var sendButton: Button
    private lateinit var receiveButton: Button

    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi_transfer)

        utilities = Utilities(applicationContext)
        mfaCodesTimer = Timer()
        initializeUI()
        startClock()

    }

    private fun startClock () {
        if (!utilities.vault.getBoolean(utilities.SETTINGS_CLOCK_ENABLED, true)) clock.visibility = View.GONE

        try {
            mfaCodesTimer.scheduleAtFixedRate(object : TimerTask() {
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
        mfaCodesTimer.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        mfaCodesTimer.cancel()
        finish()
    }

    override fun onStart() {
        super.onStart()
        mfaCodesTimer = Timer()
    }

    private fun initializeUI () {
        clock = findViewById(R.id.clock)

        sendButton = findViewById (R.id.send)
        sendButton.setOnClickListener {
            startActivity(Intent(applicationContext, SendActivity::class.java))
            sendButton.performHapticFeedback(HapticGenerator.SUCCESS)
        }

        receiveButton = findViewById (R.id.receive)
        receiveButton.setOnClickListener {
            startActivity(Intent(applicationContext, ReceiveActivity::class.java))
            sendButton.performHapticFeedback(HapticGenerator.SUCCESS) }

        backButton = findViewById (R.id.backButton)
        backButton.setOnClickListener { finish() }
    }

}
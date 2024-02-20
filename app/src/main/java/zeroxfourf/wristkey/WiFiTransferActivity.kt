package zeroxfourf.wristkey
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import wristkey.R
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
        if (!utilities.db.getBoolean(utilities.SETTINGS_CLOCK_ENABLED, true)) clock.visibility = View.GONE

        try {
            mfaCodesTimer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    runOnUiThread { clock.text = utilities.getTime() }
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
        }

        receiveButton = findViewById (R.id.receive)
        receiveButton.setOnClickListener {
            startActivity(Intent(applicationContext, ReceiveActivity::class.java))
        }

        backButton = findViewById (R.id.backButton)
        backButton.setOnClickListener { finish() }
    }

}
package app.wristkey


import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.wearable.intent.RemoteIntent
import wristkey.R
import java.text.SimpleDateFormat
import java.util.*

class DonateActivity : AppCompatActivity() {

    lateinit var mfaCodesTimer: Timer
    lateinit var utilities: Utilities

    private lateinit var clock: TextView
    private lateinit var paypal: Button
    private lateinit var sponsors: Button
    private lateinit var bitcoin: Button

    private lateinit var backButton: Button

    private lateinit var donateButton: Button
    private lateinit var licenseButton: Button

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donate)

        utilities = Utilities(applicationContext)
        mfaCodesTimer = Timer()
        initializeUI()
        startClock()

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

    fun initializeUI () {
        clock = findViewById(R.id.clock)

        paypal = findViewById(R.id.paypal)
        paypal.setOnClickListener {
            val uri: String = getString(R.string.paypal_uri)
            val intent = Intent(Intent.ACTION_VIEW).addCategory(Intent.CATEGORY_BROWSABLE).setData(Uri.parse(uri))
            RemoteIntent.startRemoteActivity(this, intent, null)
            Toast.makeText(this, "Opening PayPal URL in browser", Toast.LENGTH_SHORT).show()
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                startActivity(browserIntent)
            } catch (ex: Exception) { }
        }

        sponsors = findViewById(R.id.github)
        sponsors.setOnClickListener {
            val uri: String = getString(R.string.sponsors_uri)
            val intent = Intent(Intent.ACTION_VIEW).addCategory(Intent.CATEGORY_BROWSABLE).setData(Uri.parse(uri))
            RemoteIntent.startRemoteActivity(this, intent, null)
            Toast.makeText(this, "Opening GitHub Sponsors URL in browser", Toast.LENGTH_SHORT).show()
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                startActivity(browserIntent)
            } catch (ex: Exception) { }
        }

        bitcoin = findViewById(R.id.bitcoin)
        bitcoin.setOnClickListener {
            val intent = Intent (applicationContext, QRCodeActivity::class.java)
            intent.putExtra (utilities.INTENT_QR_DATA, getString(R.string.bitcoin_wallet_address))
            intent.putExtra (utilities.INTENT_QR_METADATA, getString(R.string.bitcoin_wallet_address))
            startActivityForResult (intent, utilities.EXPORT_RESPONSE_CODE)
        }

        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            utilities.EXPORT_RESPONSE_CODE -> {  }
        }
    }

}

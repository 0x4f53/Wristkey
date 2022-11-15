package app.wristkey


import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.wearable.intent.RemoteIntent
import wristkey.BuildConfig
import wristkey.R
import java.text.SimpleDateFormat
import java.util.*

class AboutActivity : AppCompatActivity() {

    lateinit var mfaCodesTimer: Timer
    lateinit var utilities: Utilities

    private lateinit var clock: TextView

    private lateinit var backButton: CardView
    private lateinit var appNameText: TextView
    private lateinit var heart: TextView
    private lateinit var versionText: TextView
    private lateinit var bitcoinDonateQrCode: ImageView
    private lateinit var urlLink: TextView

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

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

    @RequiresApi(Build.VERSION_CODES.M)
    private fun startClock () {

        if (!utilities.vault.getBoolean(utilities.SETTINGS_CLOCK_ENABLED, true)) {
            findViewById<CardView>(R.id.clockBackground).visibility = View.GONE
        }

        try {
            mfaCodesTimer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    val currentHour24 = SimpleDateFormat("HH", Locale.getDefault()).format(Date())
                    val currentHour = SimpleDateFormat("hh", Locale.getDefault()).format(Date())
                    val currentMinute = SimpleDateFormat("mm", Locale.getDefault()).format(Date())
                    val currentSecond = SimpleDateFormat("s", Locale.getDefault()).format(Date()).toInt()
                    val currentAmPm = SimpleDateFormat("a", Locale.getDefault()).format(Date())
                    runOnUiThread {
                        try {

                            if (utilities.vault.getBoolean(utilities.SETTINGS_24H_CLOCK_ENABLED, false)) {
                                clock.text = "$currentHour24:$currentMinute"
                                if ((currentSecond % 2) == 0) clock.text = "$currentHour24 $currentMinute"
                            } else {
                                clock.text = "$currentHour:$currentMinute"
                                if ((currentSecond % 2) == 0) clock.text = "$currentHour $currentMinute"
                            }

                        } catch (_: Exception) { }
                    }
                }
            }, 0, 1000) // 1000 milliseconds = 1 second
        } catch (_: IllegalStateException) { }
    }

    fun initializeUI () {

        clock = findViewById(R.id.clock)

        backButton = findViewById<CardView>(R.id.backButton)
        appNameText = findViewById<TextView>(R.id.AppName)
        heart = findViewById<TextView>(R.id.heart)
        versionText = findViewById<TextView>(R.id.Version)
        bitcoinDonateQrCode = findViewById<ImageView>(R.id.bitcoinDonateQrCode)
        urlLink = findViewById<TextView>(R.id.SourceCode)

        versionText.text = "v${BuildConfig.VERSION_NAME}"
        val uri: String = getString(R.string.about_url)

        heart.startAnimation(AnimationUtils.loadAnimation(this, R.anim.heartbeat))

        urlLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).addCategory(Intent.CATEGORY_BROWSABLE).setData(Uri.parse(uri))
            RemoteIntent.startRemoteActivity(this, intent, null)
            Toast.makeText(this, "URL opened\non phone", Toast.LENGTH_SHORT).show()
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                startActivity(browserIntent)
                val toast2 = Toast.makeText(this, "URL opened\nin browser", Toast.LENGTH_SHORT)
                toast2.show()
            } catch (ex: Exception) { }
        }

        backButton.setOnClickListener {
            finish()
        }
    }

}

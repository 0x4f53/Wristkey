package app.wristkey


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
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

    private lateinit var backButton: Button
    private lateinit var appNameText: TextView
    private lateinit var heart: TextView
    private lateinit var versionText: TextView
    private lateinit var urlLink: TextView

    private lateinit var donateButton: Button
    private lateinit var licenseButton: Button

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

    private fun startClock () {
        if (!utilities.vault.getBoolean(utilities.SETTINGS_CLOCK_ENABLED, true)) findViewById<CardView>(R.id.clockBackground).visibility = View.GONE
        try {
            mfaCodesTimer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    val currentHour24 = SimpleDateFormat("HH", Locale.getDefault()).format(Date())
                    val currentHour = SimpleDateFormat("hh", Locale.getDefault()).format(Date())
                    val currentMinute = SimpleDateFormat("mm", Locale.getDefault()).format(Date())
                    runOnUiThread {
                        try {
                            clock.text = "$currentHour:$currentMinute"
                            if (utilities.vault.getBoolean(utilities.SETTINGS_24H_CLOCK_ENABLED, false)) clock.text = "$currentHour24:$currentMinute"
                        } catch (_: Exception) { }
                    }
                }
            }, 0, 1000) // 1000 milliseconds = 1 second
        } catch (_: IllegalStateException) { }
    }
    fun initializeUI () {
        clock = findViewById(R.id.clock)

        versionText = findViewById(R.id.Version)
        versionText.text = "v${BuildConfig.VERSION_NAME}"

        heart = findViewById(R.id.heart)
        heart.startAnimation(AnimationUtils.loadAnimation(this, R.anim.heartbeat))

        val uri: String = getString(R.string.about_url)
        urlLink = findViewById(R.id.sourceCode)
        urlLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).addCategory(Intent.CATEGORY_BROWSABLE).setData(Uri.parse(uri))
            RemoteIntent.startRemoteActivity(this, intent, null)
            Toast.makeText(this, "Opening repository URL in browser", Toast.LENGTH_SHORT).show()
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                startActivity(browserIntent)
            } catch (ex: Exception) { }
        }

        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        donateButton = findViewById(R.id.donateButton)
        donateButton.setOnClickListener {
            startActivity(Intent(applicationContext, DonateActivity::class.java))
        }

        licenseButton = findViewById(R.id.licenseButton)
        licenseButton.setOnClickListener {
            AlertDialog.Builder(this@AboutActivity)
                .setTitle("MIT License")
                .setMessage(getString(R.string.copyright))
                .setPositiveButton("Back", null)
                .setCancelable(false)
                .create().show()
        }
    }

}

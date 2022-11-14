package app.wristkey

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import wristkey.R
import java.text.SimpleDateFormat
import java.util.*


class ColorPickerActivity : AppCompatActivity() {

    lateinit var mfaCodesTimer: Timer
    lateinit var utilities: Utilities

    private lateinit var clock: TextView

    private lateinit var backButton: CardView

    var settingsChanged = false

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_color_picker)

        utilities = Utilities(applicationContext)
        mfaCodesTimer = Timer()

        initializeUI()
        startClock()

    }

    override fun onStop() {
        super.onStop()
        mfaCodesTimer.cancel()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mfaCodesTimer.cancel()
        finish()

        if (settingsChanged) {
            finishAffinity()
            startActivity(Intent(applicationContext, MainActivity::class.java))
        }

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

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeUI () {

        clock = findViewById(R.id.clock)

        backButton = findViewById (R.id.backButton)

        val backgroundColor = utilities.vault.getString (
            utilities.SETTINGS_BACKGROUND_COLOR,
            getColor(R.color.darkDemo).toString()
        )

        val lightTheme: RadioButton = findViewById(R.id.lightTheme)
        lightTheme.isChecked = (backgroundColor == getColor(R.color.lightDemo).toString())
        lightTheme.setOnCheckedChangeListener { _, isChecked ->
            utilities
                .vault
                .edit()
                .putString(
                    utilities.SETTINGS_BACKGROUND_COLOR,
                    getColor(R.color.lightDemo).toString()
                )
                .apply()

            finish(); startActivity(intent)
        }

        backButton.setOnClickListener {
            super.onBackPressed()
        }

    }

}
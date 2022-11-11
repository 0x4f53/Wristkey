package app.wristkey

import android.content.Intent
import android.media.audiofx.HapticGenerator
import android.os.Build
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import com.google.android.material.switchmaterial.SwitchMaterial
import wristkey.R
import java.text.SimpleDateFormat
import java.util.*

class SettingsActivity : WearableActivity() {

    lateinit var mfaCodesTimer: Timer
    lateinit var utilities: Utilities

    private lateinit var clock: TextView

    lateinit var beepButton: SwitchMaterial
    lateinit var vibrateButton: SwitchMaterial
    lateinit var lockButton: SwitchMaterial
    lateinit var clockButton: SwitchMaterial
    lateinit var twentyFourHourClockButton: SwitchMaterial
    lateinit var roundButton: SwitchMaterial
    lateinit var deleteButton: CardView
    lateinit var exportButton: CardView
    lateinit var themeButton: CardView
    lateinit var backButton: CardView

    var settingsChanged = false

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

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
    private fun initializeUI () {

        clock = findViewById(R.id.clock)

        themeButton = findViewById (R.id.themeButton)
        beepButton = findViewById (R.id.beepButton)
        vibrateButton = findViewById (R.id.vibrateButton)
        lockButton = findViewById (R.id.lockButton)
        clockButton = findViewById (R.id.clockButton)
        twentyFourHourClockButton = findViewById (R.id.twentyFourHourClockButton)
        roundButton = findViewById (R.id.roundButton)
        deleteButton = findViewById (R.id.deleteButton)
        exportButton = findViewById (R.id.exportButton)
        backButton = findViewById (R.id.backButton)

        themeButton.setOnClickListener {
            val intent = Intent(applicationContext, ColorPickerActivity::class.java)
            startActivity(intent)
            themeButton.performHapticFeedback(HapticGenerator.SUCCESS)
        }

        val clockButton: SwitchMaterial = findViewById(R.id.clockButton)
        clockButton.isChecked = utilities.vault.getBoolean(utilities.SETTINGS_CLOCK_ENABLED, true)
        clockButton.setOnCheckedChangeListener { _, isChecked ->
            settingsChanged = true
            utilities.vault.edit().remove(utilities.SETTINGS_CLOCK_ENABLED).apply()
            utilities.vault.edit().putBoolean(utilities.SETTINGS_CLOCK_ENABLED, isChecked).apply()
            if (!isChecked)
                findViewById<CardView>(R.id.clockBackground).visibility = View.GONE
            else
                findViewById<CardView>(R.id.clockBackground).visibility = View.VISIBLE
        }


        val twentyFourHourClockButton: SwitchMaterial = findViewById(R.id.twentyFourHourClockButton)
        twentyFourHourClockButton.isChecked = utilities.vault.getBoolean(utilities.SETTINGS_24H_CLOCK_ENABLED, true)
        twentyFourHourClockButton.setOnCheckedChangeListener { _, isChecked ->
            settingsChanged = true
            utilities.vault.edit().remove(utilities.SETTINGS_24H_CLOCK_ENABLED).apply()
            utilities.vault.edit().putBoolean(utilities.SETTINGS_24H_CLOCK_ENABLED, isChecked).apply()
        }

        val vibrateButton: SwitchMaterial = findViewById(R.id.vibrateButton)
        vibrateButton.isChecked = utilities.vault.getBoolean(utilities.SETTINGS_HAPTICS_ENABLED, true)
        vibrateButton.setOnCheckedChangeListener { _, isChecked ->
            settingsChanged = true
            utilities.vault.edit().remove(utilities.SETTINGS_HAPTICS_ENABLED).apply()
            utilities.vault.edit().putBoolean(utilities.SETTINGS_HAPTICS_ENABLED, isChecked).apply()
        }

        val beepButton: SwitchMaterial = findViewById(R.id.beepButton)
        beepButton.isChecked = utilities.vault.getBoolean(utilities.SETTINGS_BEEP_ENABLED, true)
        beepButton.setOnCheckedChangeListener { _, isChecked ->
            settingsChanged = true
            utilities.vault.edit().remove(utilities.SETTINGS_BEEP_ENABLED).apply()
            utilities.vault.edit().putBoolean(utilities.SETTINGS_BEEP_ENABLED, isChecked).apply()
            if (isChecked) utilities.beep()
        }

        val roundButton: SwitchMaterial = findViewById(R.id.roundButton)
        roundButton.isChecked = utilities.vault.getBoolean(utilities.CONFIG_SCREEN_ROUND, resources.configuration.isScreenRound)
        roundButton.setOnCheckedChangeListener { _, isChecked ->
            settingsChanged = true
            utilities.vault.edit().remove(utilities.CONFIG_SCREEN_ROUND).apply()
            utilities.vault.edit().putBoolean(utilities.CONFIG_SCREEN_ROUND, isChecked).apply()
        }

        backButton.setOnClickListener {
            finish()
        }

        deleteButton.setOnClickListener {
            val intent = Intent(applicationContext, DeleteActivity::class.java)
            intent.putExtra(utilities.INTENT_DELETE_MODE, utilities.INTENT_WIPE)
            startActivity(intent)
            deleteButton.performHapticFeedback(HapticGenerator.ERROR)
        }

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

}
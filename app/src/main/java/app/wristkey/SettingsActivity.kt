package app.wristkey

import android.app.KeyguardManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.materialswitch.MaterialSwitch
import wristkey.R
import java.util.*

class SettingsActivity : AppCompatActivity() {

    lateinit var mfaCodesTimer: Timer
    lateinit var utilities: Utilities

    private lateinit var clock: TextView

    lateinit var searchButton: MaterialSwitch
    lateinit var lockButton: MaterialSwitch
    lateinit var clockButton: MaterialSwitch
    lateinit var roundButton: MaterialSwitch
    lateinit var compactButton: MaterialSwitch
    lateinit var concealedButton: MaterialSwitch

    lateinit var aboutButton: Button
    lateinit var backButton: Button

    var settingsChanged = false

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

    private fun initializeUI () {

        clock = findViewById(R.id.clock)

        searchButton = findViewById (R.id.searchButtonToggle)
        lockButton = findViewById (R.id.lockButton)
        clockButton = findViewById (R.id.clockButton)
        roundButton = findViewById (R.id.roundButton)
        compactButton = findViewById (R.id.compactButton)
        concealedButton = findViewById (R.id.concealedButton)
        aboutButton = findViewById (R.id.aboutButton)
        backButton = findViewById (R.id.backButton)

        findViewById<TextView>(R.id.searchText).isSelected = true
        findViewById<TextView>(R.id.lockscreenText).isSelected = true
        findViewById<TextView>(R.id.clockText).isSelected = true
        findViewById<TextView>(R.id.roundText).isSelected = true
        findViewById<TextView>(R.id.compactText).isSelected = true
        findViewById<TextView>(R.id.concealedText).isSelected = true

        searchButton.isChecked = utilities.db.getBoolean(utilities.SETTINGS_SEARCH_ENABLED, true)
        searchButton.setOnCheckedChangeListener { _, isChecked ->
            settingsChanged = true
            utilities.db.edit().remove(utilities.SETTINGS_SEARCH_ENABLED).apply()
            utilities.db.edit().putBoolean(utilities.SETTINGS_SEARCH_ENABLED, isChecked).apply()
        }

        clockButton.isChecked = utilities.db.getBoolean(utilities.SETTINGS_CLOCK_ENABLED, true)
        clockButton.setOnCheckedChangeListener { _, isChecked ->
            settingsChanged = true
            utilities.db.edit().remove(utilities.SETTINGS_CLOCK_ENABLED).apply()
            utilities.db.edit().putBoolean(utilities.SETTINGS_CLOCK_ENABLED, isChecked).apply()
            if (!isChecked) clock.visibility = View.GONE else clock.visibility = View.VISIBLE
        }

        val lockscreen = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        if (!lockscreen.isKeyguardSecure) utilities.db.edit().putBoolean(utilities.SETTINGS_LOCK_ENABLED, false).apply()
        lockButton.isChecked = utilities.db.getBoolean(utilities.SETTINGS_LOCK_ENABLED, false)
        lockButton.setOnCheckedChangeListener { _, isChecked ->
            if (!lockscreen.isKeyguardSecure) {
                lockButton.isChecked = false
                Toast.makeText(this, "Enable screen lock in device settings first!", Toast.LENGTH_LONG).show()
                utilities.db.edit().remove(utilities.SETTINGS_LOCK_ENABLED).apply()
                utilities.db.edit().putBoolean(utilities.SETTINGS_LOCK_ENABLED, false).apply()
            } else {
                settingsChanged = true
                utilities.db.edit().remove(utilities.SETTINGS_LOCK_ENABLED).apply()
                utilities.db.edit().putBoolean(utilities.SETTINGS_LOCK_ENABLED, isChecked).apply()
            }
        }

        roundButton.isChecked = utilities.db.getBoolean(utilities.CONFIG_SCREEN_ROUND, resources.configuration.isScreenRound)
        roundButton.setOnCheckedChangeListener { _, isChecked ->
            settingsChanged = true
            utilities.db.edit().remove(utilities.CONFIG_SCREEN_ROUND).apply()
            utilities.db.edit().putBoolean(utilities.CONFIG_SCREEN_ROUND, isChecked).apply()
        }


        var compactButtonChecked = false
        var concealedButtonChecked = false
        compactButton.isChecked = utilities.db.getBoolean(utilities.SETTINGS_COMPACT_ENABLED, false)
        compactButton.setOnCheckedChangeListener { _, isChecked ->
            if (!compactButtonChecked) {
                settingsChanged = true
                utilities.db.edit().remove(utilities.SETTINGS_COMPACT_ENABLED).apply()
                utilities.db.edit().putBoolean(utilities.SETTINGS_COMPACT_ENABLED, isChecked).apply()
                concealedButton.isChecked = false

                concealedButtonChecked = true
                concealedButton.isChecked = !isChecked
                concealedButtonChecked = false
            }
        }

        concealedButton.isChecked = utilities.db.getBoolean(utilities.SETTINGS_CONCEALED_ENABLED, true)
        concealedButton.setOnCheckedChangeListener { _, isChecked ->
            if (!concealedButtonChecked) {
                settingsChanged = true
                utilities.db.edit().remove(utilities.SETTINGS_CONCEALED_ENABLED).apply()
                utilities.db.edit().putBoolean(utilities.SETTINGS_CONCEALED_ENABLED, isChecked).apply()
                compactButton.isChecked = false

                compactButtonChecked = true
                compactButton.isChecked = !isChecked
                compactButtonChecked = false
            }
        }

        aboutButton.setOnClickListener {
            startActivity(Intent(applicationContext, AboutActivity::class.java))
        }

        backButton.setOnClickListener {
            finish()
        }

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
}
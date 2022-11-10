package app.wristkey

import android.content.Intent
import android.media.audiofx.HapticGenerator
import android.os.Build
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import com.google.android.material.switchmaterial.SwitchMaterial
import wristkey.R

class SettingsActivity : WearableActivity() {

    lateinit var utilities: Utilities

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

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        utilities = Utilities(applicationContext)

        initializeUI()

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeUI () {
        beepButton = findViewById (R.id.beepButton)
        vibrateButton = findViewById (R.id.vibrateButton)
        lockButton = findViewById (R.id.lockButton)
        clockButton = findViewById (R.id.clockButton)
        twentyFourHourClockButton = findViewById (R.id.twentyFourHourClockButton)
        roundButton = findViewById (R.id.roundButton)
        deleteButton = findViewById (R.id.deleteButton)
        exportButton = findViewById (R.id.exportButton)
        backButton = findViewById (R.id.backButton)
        themeButton = findViewById (R.id.themeButton)

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

}
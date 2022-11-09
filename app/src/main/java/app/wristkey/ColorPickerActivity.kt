package app.wristkey

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import android.support.wearable.activity.WearableActivity
import android.widget.RadioGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.switchmaterial.SwitchMaterial
import wristkey.R

class ColorPickerActivity : WearableActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_color_picker)
        val boxinsetlayout = findViewById<ConstraintLayout>(R.id.BoxInsetLayout)
        val themeButton = findViewById<CardView>(R.id.themeButton)
        val beep = findViewById<SwitchMaterial>(R.id.beepButton)
        val vibrate = findViewById<SwitchMaterial>(R.id.vibrateButton)
        val screenLock = findViewById<SwitchMaterial>(R.id.lockButton)
        val deleteButton = findViewById<CardView>(R.id.deleteButton)
        val exportButton = findViewById<CardView>(R.id.exportButton)
        val backButton = findViewById<CardView>(R.id.backButton)
        val accentGroup = findViewById<RadioGroup>(R.id.AccentRadioGroup)
        val themeGroup = findViewById<RadioGroup>(R.id.ThemeRadioGroup)

        backButton.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
            finish()
        }

    }
}
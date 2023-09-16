package app.wristkey

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.media.audiofx.HapticGenerator
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.zxing.WriterException
import wristkey.R
import java.util.*

class QRCodeActivity : AppCompatActivity() {

    lateinit var mfaCodesTimer: Timer

    lateinit var utilities: Utilities

    lateinit var data: String
    lateinit var metadata: String

    private lateinit var roundTimeLeft: ProgressBar
    private lateinit var squareTimeLeft: ProgressBar

    lateinit var qrCodeRoot: ConstraintLayout
    lateinit var qrCode: ImageView
    lateinit var qrCodeSubtitle: TextView

    lateinit var backButton: Button

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode)

        utilities = Utilities(applicationContext)
        mfaCodesTimer = Timer()

        data = intent.getStringExtra(utilities.INTENT_QR_DATA)!!
        metadata = intent.getStringExtra(utilities.INTENT_QR_METADATA)!!

        initializeUI()
        setShape()
        startTimer()

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setShape () {
        if (
            utilities.vault.getBoolean (
                utilities.CONFIG_SCREEN_ROUND,
                resources.configuration.isScreenRound
            )
        ) {
            roundTimeLeft.visibility = View.VISIBLE
            squareTimeLeft.visibility = View.GONE
        } else {
            roundTimeLeft.visibility = View.GONE
            squareTimeLeft.visibility = View.VISIBLE
        }
    }

    override fun onStop() {
        super.onStop()
        mfaCodesTimer.cancel()
        finish()
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

    override fun onResume() {
        super.onResume()
        mfaCodesTimer = Timer()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun startTimer () {
        try {
            var timerDuration = utilities.QR_TIMER_DURATION
            mfaCodesTimer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    if (timerDuration <= 0) {
                        setResult(Activity.RESULT_OK, Intent())
                        finish()
                    }
                    timerDuration -= 1
                } }, 0, 1000)
        } catch (_: IllegalStateException) {}
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeUI () {
        qrCodeRoot = findViewById(R.id.qrCodeRoot)
        qrCode = findViewById(R.id.qrCode)
        qrCodeSubtitle = findViewById(R.id.qrCodeSubtitle)

        roundTimeLeft = findViewById(R.id.RoundTimeLeft)
        squareTimeLeft = findViewById(R.id.SquareTimeLeftTop)
        startProgressBarAnimation(roundTimeLeft, utilities.QR_TIMER_DURATION)
        startProgressBarAnimation(squareTimeLeft, utilities.QR_TIMER_DURATION)

        backButton = findViewById(R.id.backButton)

        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        try { qrCode.setImageDrawable(BitmapDrawable(utilities.generateQrCode(data, wm))) } catch (_: WriterException) { }

        qrCodeSubtitle.text = metadata

        var state = 0
        qrCode.setOnClickListener {
            when (state) {

                0 -> {
                    state += 1
                    qrCode.imageTintList = ColorStateList.valueOf(Color.parseColor("#818181"))
                    Toast.makeText(this, "Dimmed", Toast.LENGTH_SHORT).show()
                }

                1 -> {
                    state = 0
                    qrCode.imageTintList = ColorStateList.valueOf(Color.WHITE)
                }

            }

        }

        backButton.setOnClickListener {
            backButton.performHapticFeedback(HapticGenerator.SUCCESS)
            finish()
        }

    }

    private fun startProgressBarAnimation(progressBar: ProgressBar, durationInSeconds: Int) {
        val animationDuration = durationInSeconds*1000 // 5000 milliseconds (5 seconds)
        val animationSteps = 100 // Number of animation steps
        var progress = 100
        val handler = Handler(Looper.getMainLooper())
        val delay = animationDuration / animationSteps.toLong()
        val runnable = object : Runnable {
            override fun run() {
                if (progress >= 0) {
                    progressBar.progress = progress
                    progressBar.animate()
                    progress--
                    handler.postDelayed(this, delay)
                }
            }
        }
        handler.postDelayed(runnable, delay)
    }

}
package app.wristkey

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.media.audiofx.HapticGenerator
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.zxing.WriterException
import wristkey.R
import java.util.*
import kotlin.concurrent.thread

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
            thread {

                // round timer
                var timerDuration = utilities.QR_TIMER_DURATION
                roundTimeLeft.max = timerDuration
                squareTimeLeft.max = timerDuration
                mfaCodesTimer.scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        try {
                            roundTimeLeft.progress = timerDuration
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) roundTimeLeft.setProgress(timerDuration, true)
                            squareTimeLeft.progress = timerDuration
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) squareTimeLeft.setProgress(timerDuration, true)
                        } catch (_: Exception) {
                            runOnUiThread {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) roundTimeLeft.setProgress(timerDuration, true)
                                squareTimeLeft.progress = timerDuration
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) squareTimeLeft.setProgress(timerDuration, true)
                            }
                        }
                        if (timerDuration == 0) {
                            val resultIntent = Intent()
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()
                        }
                        else timerDuration -= 1
                    }
                }, 0, 1000) // 1000 milliseconds = 1 second

            }
        } catch (_: IllegalStateException) {}
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeUI () {
        qrCodeRoot = findViewById(R.id.qrCodeRoot)
        qrCode = findViewById(R.id.qrCode)
        qrCodeSubtitle = findViewById(R.id.qrCodeSubtitle)

        roundTimeLeft = findViewById(R.id.RoundTimeLeft)
        squareTimeLeft = findViewById(R.id.SquareTimeLeftTop)

        backButton = findViewById(R.id.backButton)

        qrCodeSubtitle.text = metadata
        try { qrCode.setImageDrawable(BitmapDrawable(generateQrCode(data))) } catch (_: WriterException) { }

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

    private fun generateQrCode (qrData: String): Bitmap? {
        val manager = getSystemService(WINDOW_SERVICE) as WindowManager
        val display = manager.defaultDisplay
        val point = Point()
        display.getSize(point)
        val width: Int = point.x
        val height: Int = point.y
        val dimensions = if (width < height) width else height

        val qrEncoder = QRGEncoder(qrData, null, QRGContents.Type.TEXT, dimensions)
        return qrEncoder.bitmap
    }

}
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
import android.support.wearable.activity.WearableActivity
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.zxing.WriterException
import wristkey.R
import java.util.*
import kotlin.concurrent.thread

class QRCodeActivity : WearableActivity() {

    lateinit var mfaCodesTimer: Timer

    lateinit var utilities: Utilities

    lateinit var mfaCode: Utilities.MfaCode

    private lateinit var roundTimeLeft: ProgressBar
    private lateinit var squareTimeLeft: ProgressBar

    lateinit var qrCodeRoot: ConstraintLayout
    lateinit var qrCode: ImageView
    lateinit var qrCodeIssuer: TextView
    lateinit var qrCodeAccount: TextView

    lateinit var doneButton: ImageButton

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode)

        utilities = Utilities(applicationContext)
        mfaCodesTimer = Timer()

        mfaCode = utilities.getLogin (
            intent.getStringExtra(utilities.INTENT_UUID)!!
        )!!

        initializeUI()
        setShape()
        start2faTimer()

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
    private fun start2faTimer () {
        try {
            thread {

                // round timer
                var timerDuration = utilities.QR_TIMER_DURATION
                mfaCodesTimer.scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        if (timerDuration == 0) {
                            val resultIntent = Intent()
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()
                        }
                        else timerDuration -= 1
                        try {
                            roundTimeLeft.progress = timerDuration
                            squareTimeLeft.progress = timerDuration
                        } catch (_: Exception) { }
                    }
                }, 0, 1000) // 1000 milliseconds = 1 second

            }
        } catch (_: IllegalStateException) {}
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeUI () {
        qrCodeRoot = findViewById(R.id.qrCodeRoot)
        qrCode = findViewById(R.id.qrCode)
        qrCodeIssuer = findViewById(R.id.qrCodeIssuer)
        qrCodeAccount = findViewById(R.id.qrCodeAccount)

        roundTimeLeft = findViewById(R.id.RoundTimeLeft)
        squareTimeLeft = findViewById(R.id.SquareTimeLeftTop)

        doneButton = findViewById(R.id.doneButton)

        qrCodeIssuer.text = mfaCode.issuer
        qrCodeAccount.text = mfaCode.account

        val qrData = utilities.encodeOTPAuthURL(mfaCode)

        try {
            qrCode.setImageDrawable(BitmapDrawable(generateQrCode(qrData!!)))
        } catch (_: WriterException) { }

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
                    Toast.makeText(this, "Normal", Toast.LENGTH_SHORT).show()
                }

            }

        }

        doneButton.setOnClickListener {
            doneButton.performHapticFeedback(HapticGenerator.SUCCESS)
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
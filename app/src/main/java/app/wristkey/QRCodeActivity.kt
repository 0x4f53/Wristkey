package app.wristkey

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.media.audiofx.HapticGenerator
import android.os.Build
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.annotation.RequiresApi
import androidx.wear.widget.BoxInsetLayout
import com.google.zxing.WriterException
import wristkey.R

class QRCodeActivity : WearableActivity() {

    lateinit var utilities: Utilities

    lateinit var mfaCode: Utilities.MfaCode

    lateinit var qrCodeRoot: BoxInsetLayout
    lateinit var qrCode: ImageView
    lateinit var qrCodeIssuer: TextView
    lateinit var qrCodeAccount: TextView

    lateinit var doneButton: ImageButton

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode)

        utilities = Utilities(applicationContext)

        mfaCode = utilities.getLogin (
            intent.getStringExtra(utilities.INTENT_UUID)!!
        )!!

        initializeUI()

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeUI () {
        qrCodeRoot = findViewById(R.id.qrCodeRoot)
        qrCode = findViewById(R.id.qrCode)
        qrCodeIssuer = findViewById(R.id.qrCodeIssuer)
        qrCodeAccount = findViewById(R.id.qrCodeAccount)

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
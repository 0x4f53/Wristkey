package app.wristkey

import android.app.KeyguardManager
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.wear.widget.BoxInsetLayout
import com.google.zxing.WriterException
import wristkey.R

class QRCodeActivity : WearableActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode)


        val manager = getSystemService(WINDOW_SERVICE) as WindowManager
        val display = manager.defaultDisplay
        val point = Point()
        display.getSize(point)
        val width: Int = point.x
        val height: Int = point.y
        val dimensions = if (width < height) width else height
        val qrCode = findViewById<ImageView>(R.id.qrCode)
        val boxInsetLayout = findViewById<BoxInsetLayout>(R.id.BoxInsetLayout)

        val qrData = intent.getStringExtra("qr_data")

        val qrEncoder = QRGEncoder(qrData, null, QRGContents.Type.TEXT, dimensions)

        try {
            //qrCode.setImageBitmap(qrEncoder.encodeAsBitmap())
        } catch (e: WriterException) { }

        qrCode.setOnClickListener {
            if (qrCode.imageTintList == ColorStateList.valueOf(Color.parseColor("#BF000000"))) {
                qrCode.imageTintList = ColorStateList.valueOf(Color.parseColor("#00000000"))
                boxInsetLayout.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFFFF"))
                Toast.makeText(applicationContext, "Normal", Toast.LENGTH_SHORT).show()
            } else {
                qrCode.imageTintList = ColorStateList.valueOf(Color.parseColor("#BF000000"))
                boxInsetLayout.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#40FFFFFF"))
                Toast.makeText(applicationContext, "Dimmed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (!(resultCode == RESULT_OK && requestCode == CODE_AUTHENTICATION_VERIFICATION)) {
            finish()
        }
    }
}
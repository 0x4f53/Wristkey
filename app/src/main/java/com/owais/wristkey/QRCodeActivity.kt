package com.owais.wristkey

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
import com.google.zxing.WriterException


var otpAuthData: String = ""

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
        var dimensions = if (width < height) width else height
        val qrCode = findViewById<ImageView>(R.id.qrCode)
        val qrEncoder = QRGEncoder(otpAuthData, null, QRGContents.Type.TEXT, dimensions)

        try {
            qrCode.setImageBitmap(qrEncoder.encodeAsBitmap())
        } catch (e: WriterException) { }

        qrCode.setOnClickListener {
            if (qrCode.imageTintList == ColorStateList.valueOf(Color.parseColor("#BF000000"))) {
                qrCode.imageTintList = ColorStateList.valueOf(Color.parseColor("#00000000"))
                Toast.makeText(applicationContext, "Normal", Toast.LENGTH_SHORT).show()
            } else {
                qrCode.imageTintList = ColorStateList.valueOf(Color.parseColor("#BF000000"))
                Toast.makeText(applicationContext, "Dimmed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
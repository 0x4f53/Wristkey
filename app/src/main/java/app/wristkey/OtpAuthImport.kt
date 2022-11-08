package app.wristkey

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.os.Environment.getExternalStorageDirectory
import android.os.Vibrator
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.wear.widget.BoxInsetLayout
import app.wristkey.AddActivity
import com.google.gson.Gson
import com.google.zxing.*
import com.google.zxing.Reader
import com.google.zxing.common.HybridBinarizer
import wristkey.R
import wristkey.databinding.ActivityOtpauthImportBinding
import java.io.*
import java.util.*

class OtpAuthImport : Activity() {

    private lateinit var binding: ActivityOtpauthImportBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpauthImportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val boxinsetlayout = findViewById<BoxInsetLayout>(R.id.BoxInsetLayout)

        val qrCodesLayout = findViewById<LinearLayout>(R.id.QRCodesLayout)
        val qrPreview = findViewById<ImageView>(R.id.QRPreview)
        val previous = findViewById<ImageView>(R.id.Previous)
        val next = findViewById<ImageView>(R.id.Next)

        val backButton = findViewById<ImageButton>(R.id.AuthenticatorBackButton)
        val confirmButton = findViewById<ImageButton>(R.id.AuthenticatorConfirmButton)
        val importLabel = findViewById<TextView>(R.id.AuthenticatorImportLabel)

        val otpAuth = findViewById<EditText>(R.id.OtpAuth)

        var currentAccent = appData.getString("accent", "4285F4")
        var currentTheme = appData.getString("theme", "000000")
        boxinsetlayout.setBackgroundColor(Color.parseColor("#" + currentTheme))

        otpAuth.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
        otpAuth.foregroundTintList = ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
        otpAuth.compoundDrawableTintList = ColorStateList.valueOf(Color.parseColor("#" + currentAccent))

        confirmButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#" + currentAccent))

        if (currentTheme == "F7F7F7") {
            importLabel.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
            otpAuth.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
            otpAuth.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
            previous.imageTintList = ColorStateList.valueOf(Color.parseColor("#000000"))
            next.imageTintList = ColorStateList.valueOf(Color.parseColor("#000000"))
        } else {
            importLabel.setTextColor(ColorStateList.valueOf(Color.parseColor("#BDBDBD")))
            otpAuth.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#BDBDBD")))
            otpAuth.setTextColor(ColorStateList.valueOf(Color.parseColor("#BDBDBD")))
            previous.imageTintList = ColorStateList.valueOf(Color.parseColor("#BDBDBD"))
            next.imageTintList = ColorStateList.valueOf(Color.parseColor("#BDBDBD"))
        }

        val files: Array<File> = getExternalStorageDirectory().listFiles()

        val filenameList = ArrayList<String>()
        var index = 0

        try {
            for (file in files) {

                if (file.name.endsWith(".png", ignoreCase = true) || file.name.endsWith(".jpg", ignoreCase = true) || file.name.endsWith(".jpeg", ignoreCase = true)) {
                    val reader: InputStream = BufferedInputStream(FileInputStream(file.absoluteFile))
                    val imageBitmap = BitmapFactory.decodeStream(reader)
                    val decodedQRCodeData: String = scanQRImage(imageBitmap)

                    if (decodedQRCodeData.contains("otpauth://")) {
                        filenameList.add(file.absolutePath.toString())
                    }
                }
            }


            fun getData () {
                val manager = getSystemService(WINDOW_SERVICE) as WindowManager
                val display = manager.defaultDisplay
                val point = Point()
                display.getSize(point)
                val width: Int = point.x
                val height: Int = point.y
                val dimensions = if (width < height) width else height

                val reader: InputStream = BufferedInputStream(FileInputStream(filenameList[index]))
                val imageBitmap = BitmapFactory.decodeStream(reader)
                val decodedQRCodeData: String = scanQRImage(imageBitmap)

                var qrEncoder = QRGEncoder(decodedQRCodeData, null, QRGContents.Type.TEXT, dimensions)
                // qrPreview.setImageBitmap(qrEncoder.encodeAsBitmap())
                otpAuth.setText(decodedQRCodeData)
            }

            getData()

            if (filenameList.size == 1) {
                previous.visibility = View.GONE
                next.visibility = View.GONE
            } else if (filenameList.size > 1) {
                previous.visibility = View.VISIBLE
                next.visibility = View.VISIBLE

                previous.setOnClickListener {
                    if (index > 0) index-- else index = 0
                    getData()
                }

                next.setOnClickListener {
                    if (index == filenameList.size-1) index = filenameList.size-1 else index++
                    getData()
                }

            }

            otpAuth.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) { }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
            })

        } catch (noFileFound: FileNotFoundException) {
            Toast.makeText(this, "Couldn't find file. Check if the file exists and if Wristkey is granted storage permission.", Toast.LENGTH_LONG).show()

            val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package", packageName, null)
            settingsIntent.data = uri
            startActivity(settingsIntent)

            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
            finish()

        } catch (badData: IllegalArgumentException) {
            Toast.makeText(this, "QR code data corrupt.", Toast.LENGTH_SHORT).show()

            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
            finish()

        } catch (noData: IndexOutOfBoundsException) {
            qrCodesLayout.visibility = View.GONE
            importLabel.text = "No QR Codes Found.\nEnter otpauth URL manually."
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
        }

        backButton.setOnClickListener {
            val intent = Intent(applicationContext, AddActivity::class.java)
            startActivity(intent)
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
            finish()
        }

        confirmButton.setOnClickListener {
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
            // decode otpauth

            val otpAuthUrl = otpAuth.text.toString()
            if (!otpAuthUrl.contains("otpauth://") ||
                !otpAuthUrl.contains("secret") ||
                !otpAuthUrl.contains("otp")) {
                Toast.makeText(this, "Invalid otpauth url", Toast.LENGTH_LONG).show()
            } else {
                val accountData = ArrayList<String>()
                if (otpAuthUrl.substringAfter("otp/").substringBefore("?").isNotEmpty()) { // name
                    accountData.add(otpAuthUrl.substringAfter("otp/").substringBefore("?"))
                } else {
                    accountData.add(otpAuthUrl.substringAfter("&issuer=").substringBefore("&"))
                }

                if (otpAuthUrl.contains("?secret=")) { // secret
                    accountData.add(otpAuthUrl.substringAfter("?secret=").substringBefore("&"))
                } else {
                    Toast.makeText(this, "Couldn't find secret", Toast.LENGTH_LONG).show()
                    finish()
                }

                if (otpAuthUrl.contains("totp")) { // type
                    accountData.add("Time")
                } else if (otpAuthUrl.contains("hotp")) {
                    accountData.add("Counter")
                }

                if (otpAuthUrl.contains("&digits=")) { // digits
                    accountData.add(otpAuthUrl.substringAfter("&digits=").substringBefore("&"))
                } else {
                    accountData.add("6")
                }

                if (otpAuthUrl.contains("&algorithm=")) { // algorithm
                    accountData.add("HmacAlgorithm."+otpAuthUrl.substringAfter("&algorithm=").substringBefore("&"))
                } else {
                    accountData.add("HmacAlgorithm.SHA1")
                }

                if (otpAuthUrl.contains("&counter=")) { // counter
                    accountData.add(otpAuthUrl.substringAfter("&counter=").substringBefore("&"))
                } else {
                    accountData.add("0")
                }

                val id = UUID.randomUUID().toString()
                val json = Gson().toJson(accountData)
                if (accounts.all.values.toString().contains(accountData[1])) {
                    Toast.makeText(this, "This account already exists.", Toast.LENGTH_LONG).show()
                } else {
                    accounts.edit().putString(id, json).apply()
                    val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    vibratorService.vibrate(50)

                    val intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }

            }
        }
    }

    fun scanQRImage(bMap: Bitmap): String {
        var contents: String
        val intArray = IntArray(bMap.width * bMap.height)
        //copy pixel data from the Bitmap into the 'intArray' array
        bMap.getPixels(intArray, 0, bMap.width, 0, 0, bMap.width, bMap.height)
        val source: LuminanceSource = RGBLuminanceSource(bMap.width, bMap.height, intArray)
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        val reader: Reader = MultiFormatReader()
        try {
            val result = reader.decode(bitmap)
            contents = result.text
        } catch (e: Exception) {
            contents = "No data found"
        }
        return contents
    }
}
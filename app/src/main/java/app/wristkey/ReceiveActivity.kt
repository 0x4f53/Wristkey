package app.wristkey
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.WriterException
import fi.iki.elonen.NanoHTTPD
import wristkey.R
import java.text.SimpleDateFormat
import java.util.*

class ReceiveActivity : AppCompatActivity() {

    lateinit var timer: Timer
    lateinit var utilities: Utilities

    private lateinit var clock: TextView

    private lateinit var qrCode: ImageView  
    private lateinit var ipAndPort: TextView

    private lateinit var server: NanoHTTPD
    private var ip = "192.168.xxx.xxx"
    private var port = 8080

    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receive)

        utilities = Utilities(applicationContext)
        timer = Timer()
        initializeUI()
        startClock()

    }

    private fun startClock () {
        if (!utilities.vault.getBoolean(utilities.SETTINGS_CLOCK_ENABLED, true)) clock.visibility = View.GONE

        try {
            timer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    val hourType = if (android.text.format.DateFormat.is24HourFormat(applicationContext)) "hh" else "HH"
                    val currentHour = SimpleDateFormat(hourType, Locale.getDefault()).format(Date())
                    val currentMinute = SimpleDateFormat("mm", Locale.getDefault()).format(Date())
                    runOnUiThread { clock.text = "$currentHour:$currentMinute" }
                }
            }, 0, 1000)
        } catch (_: IllegalStateException) { }
    }

    override fun onStop() {
        super.onStop()
        server.stop()
        timer.cancel()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        server.stop()
        finish()
    }

    override fun onStart() {
        super.onStart()
        timer = Timer()
    }

    private fun initializeUI () {
        clock = findViewById(R.id.clock)

        qrCode = findViewById(R.id.qrCode)
        ipAndPort = findViewById(R.id.ipAndPort)

        ip = utilities.getLocalIpAddress(applicationContext).toString()

        val server: NanoHTTPD = object : NanoHTTPD(ip, port) {
            override fun serve(session: IHTTPSession): Response {
                val headers = session.headers
                val deviceName = headers["device-name"]

                if (deviceName != null) {
                    Log.d("Wristkey-server", deviceName)
                    val response = utilities.encrypt("test", "12345678")
                    Log.d("Wristkey-server", response.toString())
                    return newFixedLengthResponse(response)
                } else {
                    stop()
                }

                return newFixedLengthResponse("This page only works with the Wristkey app.")
            }
        }
        server.start()

        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val data = "$ip:$port"

        try { qrCode.setImageDrawable(BitmapDrawable(utilities.generateQrCode(data, wm))) } catch (_: WriterException) { }
        ipAndPort.text = data

        backButton = findViewById (R.id.backButton)
        backButton.setOnClickListener { finish() }
    }

}
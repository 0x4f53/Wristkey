package app.wristkey
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.WriterException
import org.json.JSONObject
import wristkey.BuildConfig
import wristkey.R
import java.lang.Exception
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*

class ReceiveActivity : AppCompatActivity() {

    lateinit var timer: Timer
    lateinit var utilities: Utilities

    private lateinit var clock: TextView

    private lateinit var qrCode: ImageView  
    private lateinit var ipAndPort: TextView

    private var ip = "192.168.xxx.xxx"
    private var port = 4200
    private lateinit var server: HttpServer

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
        if (!utilities.db.getBoolean(utilities.SETTINGS_CLOCK_ENABLED, true)) clock.visibility = View.GONE

        try {
            timer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    val hourType = if (android.text.format.DateFormat.is24HourFormat(applicationContext)) "HH" else "hh"
                    val currentHour = SimpleDateFormat(hourType, Locale.getDefault()).format(Date())
                    val currentMinute = SimpleDateFormat("mm", Locale.getDefault()).format(Date())
                    runOnUiThread { clock.text = "$currentHour:$currentMinute" }
                }
            }, 0, 1000)
        } catch (_: IllegalStateException) { }
    }

    override fun onStop() {
        super.onStop()
        timer.cancel()
        try { server.stop() } catch (_: Exception) { }
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        try { server.stop() } catch (_: Exception) { }
        finish()
    }

    override fun onStart() {
        super.onStart()
        timer = Timer()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initializeUI () {
        clock = findViewById(R.id.clock)

        qrCode = findViewById(R.id.qrCode)
        ipAndPort = findViewById(R.id.ipAndPort)

        ip = utilities.getLocalIpAddress(applicationContext).toString()
        port = 8080 // (1000..9999).random()

        server = HttpServer(this@ReceiveActivity, ip, port)
        server.startServer()

        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val data = "$ip:$port"

        try { qrCode.setImageDrawable(BitmapDrawable(utilities.generateQrCode(data, wm))) } catch (_: WriterException) { }
        ipAndPort.text = data

        backButton = findViewById (R.id.backButton)
        backButton.setOnClickListener { finish() }
    }

}
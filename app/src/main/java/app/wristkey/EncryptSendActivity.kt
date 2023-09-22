package app.wristkey
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import wristkey.R
import java.text.SimpleDateFormat
import java.util.*

class EncryptSendActivity : AppCompatActivity() {

    lateinit var timer: Timer
    lateinit var utilities: Utilities

    private lateinit var clock: TextView

    private lateinit var ipAndPort: String
    private lateinit var otp: TextView

    private lateinit var doneButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_encrypt_send)
        utilities = Utilities(applicationContext)

        if (intent != null) ipAndPort = intent.getStringExtra(utilities.INTENT_WIFI_IP).toString()

        timer = Timer()
        initializeUI()
        startClock()

    }

    private fun startClock () {
        if (!utilities.db.getBoolean(utilities.SETTINGS_CLOCK_ENABLED, true)) clock.visibility = View.GONE

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
        timer.cancel()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        finish()
    }

    override fun onStart() {
        super.onStart()
        timer = Timer()
    }

    private fun initializeUI () {
        clock = findViewById(R.id.clock)

        otp = findViewById(R.id.otp)
        val otpString = (100000..999999).random().toString()

        otp.text = otpString
        val data = encrypt(utilities.db.getString(utilities.DATA_STORE, "").toString(), otpString.toByteArray()).toString()

        doneButton = findViewById (R.id.doneButton)
        doneButton.setOnClickListener { finish() }
    }

    /*private fun sendData(ipAndPort: String, data: String) {
        val client = OkHttpClient()
        val headers = Headers.Builder()
            .add("device-name", Build.MODEL)
            .add("data", data.trim())
            .build()

        val url = "http://$ipAndPort"

        val request = Request.Builder().url(url).headers(headers).get().build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: okio.IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.d("Wristkey", "Response code: ${response.code}")
                } else {
                    val responseBody = response.body.string()
                    Log.d("Wristkey", "Response body: $responseBody")
                }
            }
        })
    }*/

}
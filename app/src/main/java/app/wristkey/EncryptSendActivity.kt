package app.wristkey

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.io.InputStream
import java.security.KeyStore
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import kotlin.concurrent.thread


class EncryptSendActivity : AppCompatActivity() {

    lateinit var timer: Timer
    lateinit var utilities: Utilities

    private lateinit var clock: TextView

    private lateinit var ipAndPort: String
    private lateinit var otp: TextView

    private lateinit var doneButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(wristkey.R.layout.activity_encrypt_send)
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
        clock = findViewById(wristkey.R.id.clock)

        otp = findViewById(wristkey.R.id.otp)
        val otpString = (100000..999999).random().toString()

        otp.text = otpString
        val data = ""//encrypt(utilities.db.getString(utilities.DATA_STORE, "").toString(), otpString.toByteArray()).toString()
        sendData(ipAndPort)

        doneButton = findViewById (wristkey.R.id.doneButton)
        doneButton.setOnClickListener { finish() }

    }

    class VolleyHelper (private val activityContext: Context) {

        private var requestQueue: RequestQueue

        init {
            requestQueue = Volley.newRequestQueue(activityContext, HurlStack(null, createSslSocketFactory()))
        }

        private fun createSslSocketFactory(): SSLSocketFactory? {
            return try {
                val inputStream: InputStream = activityContext.resources.openRawResource(wristkey.R.raw.keystore)

                val keyStore = KeyStore.getInstance("BKS")
                keyStore.load(inputStream, "keystore_password".toCharArray())

                val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                trustManagerFactory.init(keyStore)

                val sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, trustManagerFactory.trustManagers, null)

                sslContext.socketFactory
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        fun makeJsonPostRequest(url: String, payload: String, callback: (String?) -> Unit) {
            val stringRequest = object : StringRequest (Method.POST, url, Response.Listener { response -> callback(response) }, Response.ErrorListener { callback(null) }) {
                override fun getBody(): ByteArray {
                    return payload.toByteArray()
                }
            }

            requestQueue.add(stringRequest)
        }

    }

    private fun sendData(ipAndPort: String) {

        val volleyHelper = VolleyHelper(this@EncryptSendActivity)
        val jsonPayload = JSONObject()
        jsonPayload.put("deviceName", "${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})")

        var pubKey: String? = null
        var status: String? = null
        volleyHelper.makeJsonPostRequest("https://$ipAndPort/", jsonPayload.toString()) { response -> status = response }
        val stopPinging = false

        thread {
            while (!stopPinging) {
                volleyHelper.makeJsonPostRequest("https://$ipAndPort/", jsonPayload.toString()) { response -> pubKey = response }

                Log.d ("Wristkey-client", pubKey.toString())

                if (status != "requested") {
                    break
                }

                Thread.sleep(2000)
            }
        }

    }



}
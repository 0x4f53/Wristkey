package app.wristkey
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.audiofx.HapticGenerator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.goterl.lazysodium.utils.Key
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import wristkey.R
import java.io.IOException
import java.util.*
import kotlin.properties.Delegates


class SendActivity : AppCompatActivity() {

    lateinit var mfaCodesTimer: Timer
    lateinit var utilities: Utilities
    lateinit var cryptography: Cryptography

    private lateinit var clock: TextView
    private lateinit var scanQrCodeDescription: TextView
    private lateinit var scanQrCode: Button
    private lateinit var ipLayout: TextInputLayout
    private lateinit var ipInput: TextInputEditText
    private lateinit var url: String

    private lateinit var senderIP: String
    private var senderPort by Delegates.notNull<Int>()
    private lateinit var senderServerURL: String

    private lateinit var senderServer: Server

    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send)

        utilities = Utilities(applicationContext)
        cryptography = Cryptography()

        startSendingServer()

        mfaCodesTimer = Timer()
        initializeUI()
        startClock()

    }

    private fun startClock () {
        if (!utilities.db.getBoolean(utilities.SETTINGS_CLOCK_ENABLED, true)) clock.visibility = View.GONE

        try {
            mfaCodesTimer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    runOnUiThread { clock.text = utilities.getTime() }
                }
            }, 0, 1000)
        } catch (_: IllegalStateException) { }
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

    private fun initializeUI () {
        clock = findViewById(R.id.clock)

        scanQrCode = findViewById (R.id.scanQrCode)
        scanQrCodeDescription = findViewById(R.id.scanQrCodeDescription)

        scanQrCode.setOnClickListener {
            scanQrCode.performHapticFeedback(HapticGenerator.SUCCESS)
            checkPermission(Manifest.permission.CAMERA, utilities.CAMERA_REQUEST_CODE)
        }

        if (!utilities.hasCamera()) {
            scanQrCode.visibility = View.GONE
            scanQrCodeDescription.text = getString(R.string.send_description_no_camera)
        }

        ipLayout = findViewById (R.id.ipLayout)
        ipInput = findViewById (R.id.ipInput)
        val delayMillis = 2000

        val handler = Handler(Looper.getMainLooper())
        var runnable: Runnable? = null

        ipInput.doOnTextChanged { text, _, _, _ -> // Remove any previously posted callbacks
            runnable?.let { handler.removeCallbacks(it) }
            if (utilities.isIp(text.toString())) { // Post a delayed action to startEncryptSend()
                runnable = Runnable {
                    url = text.toString()
                    sendPubKeyRequest()
                    ipInput.performHapticFeedback(HapticGenerator.SUCCESS)
                }
                handler.postDelayed(runnable!!, delayMillis.toLong())
            }
        }

        backButton = findViewById (R.id.backButton)
        backButton.setOnClickListener { finish() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == utilities.CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            url = data?.getStringExtra(utilities.QR_CODE_SCAN_REQUEST).toString()
            if (utilities.isIp(url.replace("http:", "").replace("/", ""))) {
                sendPubKeyRequest()
                scanQrCode.performHapticFeedback(HapticGenerator.SUCCESS)
            } else {
                AlertDialog.Builder(this@SendActivity)
                    .setMessage(R.string.invalid_qr_code)
                    .setNegativeButton("Go back") { _, _ -> finish() }
                    .create().show()
            }
        }
    }

    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this@SendActivity, permission) == PackageManager.PERMISSION_DENIED) ActivityCompat.requestPermissions(this@SendActivity, arrayOf(permission), requestCode)
        else {
            when (requestCode) {
                utilities.CAMERA_REQUEST_CODE -> startScannerUI()
            }
        }
    }

    private fun startScannerUI () {
        val intent = Intent (applicationContext, QRScannerActivity::class.java)
        startActivityForResult(intent, utilities.CAMERA_REQUEST_CODE)
    }

    private fun startSendingServer(){
        senderIP = utilities.getLocalIpAddress(applicationContext).toString()
        senderPort = (1000..9999).random()

        senderServerURL = "http://$senderIP:$senderPort"

        senderServer = Server(senderPort, "")
        senderServer.start()

        Log.d("Wristkey-Transfer", "Started sender server at $senderServerURL")
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun sendPubKeyRequest () {
        GlobalScope.launch(Dispatchers.IO) { // Perform network request on IO Dispatcher
            val receiverResponse = postRequest(
                """{"phoneURL":"$senderServerURL","deviceName":"${utilities.deviceName()}"}""".trimIndent(),
                url
            )
            withContext(Dispatchers.Main) { // Switch back to Main Thread to update UI
                val responseJson = JSONObject(receiverResponse)

                val deviceName = responseJson["deviceName"]
                Toast.makeText(this@SendActivity, "Sending to $deviceName", Toast.LENGTH_SHORT).show()

                val publicKeyString = responseJson["publicKey"] as String
                val publicKey = Key.fromHexString(publicKeyString)

                val wfsJson = utilities.objectMapper.writeValueAsString (utilities.getData())
                val wfsBase64 = utilities.toBase64(wfsJson)

                val encryptedData = cryptography.lazySodium.cryptoBoxSealEasy(wfsBase64, publicKey)

                GlobalScope.launch(Dispatchers.IO) {
                    val transferData = postRequest(
                        """{"encryptedVault":"$encryptedData","deviceName":"${utilities.deviceName()}"}""".trimIndent(),
                        url
                    )
                    withContext(Dispatchers.Main) {
                        Log.d("Wristkey-Transfer", "Data to send: $encryptedData")
                        Toast.makeText(this@SendActivity, "Transfer complete!", Toast.LENGTH_SHORT).show()

                        finishAffinity()
                        startActivity(Intent(applicationContext, MainActivity::class.java))
                    }
                    senderServer.stop()
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun postRequest(jsonString: String, url: String): String? {
        var url = url
        if (!url.contains("://")) url = "http://$url"
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonString.toRequestBody(jsonMediaType)
        val request = Request.Builder().url(url).post(requestBody).build()
        val client = OkHttpClient()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            return response.body?.string()
        }
    }

}
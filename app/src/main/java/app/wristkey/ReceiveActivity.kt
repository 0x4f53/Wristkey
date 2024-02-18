package app.wristkey
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.WriterException
import com.goterl.lazysodium.utils.KeyPair
import kotlinx.coroutines.DelicateCoroutinesApi
import wristkey.R
import java.util.*
import kotlin.properties.Delegates

class ReceiveActivity : AppCompatActivity() {

    lateinit var timer: Timer
    lateinit var utilities: Utilities
    lateinit var cryptography: Cryptography

    private lateinit var clock: TextView

    private lateinit var qrCode: ImageView  
    private lateinit var ipAndPort: TextView

    private lateinit var receiverIP: String
    private var receiverPort by Delegates.notNull<Int>()
    private lateinit var receiverServerURL: String

    private lateinit var receiverKeyPair: KeyPair

    private lateinit var receiverServer: Server

    private lateinit var backButton: Button

    @OptIn(DelicateCoroutinesApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receive)

        utilities = Utilities(applicationContext)
        cryptography = Cryptography()

        startReceivingServer()

        timer = Timer()
        initializeUI()
        startClock()

    }

    private fun startClock () {
        if (!utilities.db.getBoolean(utilities.SETTINGS_CLOCK_ENABLED, true)) clock.visibility = View.GONE

        try {
            timer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    runOnUiThread { clock.text = utilities.getTime() }
                }
            }, 0, 1000)
        } catch (_: IllegalStateException) { }
    }

    override fun onStop() {
        super.onStop()
        timer.cancel()
        receiverServer.stop()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        receiverServer.stop()
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

        val wm = getSystemService(WINDOW_SERVICE) as WindowManager

        try {
            qrCode.setImageDrawable(BitmapDrawable(utilities.generateQrCode(receiverServerURL, wm)))
        } catch (_: WriterException) { }
        ipAndPort.text = receiverServerURL

        backButton = findViewById (R.id.backButton)
        backButton.setOnClickListener { finish() }
    }

    private fun startReceivingServer(){

        receiverKeyPair = cryptography.lazySodium.cryptoBoxKeypair()

        receiverIP = utilities.getLocalIpAddress(applicationContext).toString()
        receiverPort = (1000..9999).random()

        receiverServerURL = "http://$receiverIP:$receiverPort"

        receiverServer = Server(receiverPort, """{"deviceName":"${utilities.deviceName()}", "publicKey":"${receiverKeyPair.publicKey.asHexString}"}""".trimIndent())
        receiverServer.start()

        val cipherTextListenerTimer = Timer()
        cipherTextListenerTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (receiverServer.encryptedVault != "" && receiverServer.deviceName != "") {
                    receiverServer.stop()
                    val decryptedBase64String = cryptography.lazySodium.cryptoBoxSealOpenEasy(receiverServer.encryptedVault, receiverKeyPair)
                    val decryptedVault = utilities.fromBase64(decryptedBase64String)
                    receiverServer.encryptedVault
                    runOnUiThread {
                        Toast.makeText(this@ReceiveActivity, "Received data from ${receiverServer.deviceName}", Toast.LENGTH_SHORT).show()
                        val dataStore = utilities.objectMapper.readValue (
                                decryptedVault,
                                Utilities.WristkeyFileSystem::class.java
                            )
                        val iterator = dataStore.otpauth.iterator()
                        while (iterator.hasNext()) {
                            val login = iterator.next()
                            utilities.overwriteLogin(otpAuthURL = login)
                        }

                        // Hooray, transfer complete!  ;)
                        // P.S. this took me ages!
                        finishAffinity()
                        startActivity(Intent(applicationContext, MainActivity::class.java))
                    }
                    cipherTextListenerTimer.cancel()
                }
            }
        }, 0, 1000)

        Log.d("Wristkey-Transfer", "Started receiver server at $receiverServerURL")
    }

}
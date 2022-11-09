package app.wristkey

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.media.audiofx.HapticGenerator
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.turingcomplete.kotlinonetimepassword.GoogleAuthenticator
import wristkey.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread
import kotlin.math.abs

public const val CODE_AUTHENTICATION_VERIFICATION = 241

class MainActivity : AppCompatActivity() {

    lateinit var mfaCodesTimer: Timer
    lateinit var utilities: Utilities

    private lateinit var clock: TextView
    private lateinit var roundTimeLeft: ProgressBar
    private lateinit var loginsRecycler: RecyclerView
    private lateinit var squareTimeLeft: ProgressBar
    private lateinit var addAccountButton: CardView
    private lateinit var settingsButton: CardView
    private lateinit var aboutButton: CardView

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        utilities = Utilities (applicationContext)
        mfaCodesTimer = Timer()

        initializeUI()

        startClock()
        start2faTimer()

        Log.d ("Wristkey", "Vault: ${utilities.vault.all}")

        addAccountButton.setOnClickListener {
            startActivity(Intent(applicationContext, AddActivity::class.java))
            aboutButton.performHapticFeedback(HapticGenerator.SUCCESS)
        }

        aboutButton.setOnClickListener {
            startActivity(Intent(applicationContext, AboutActivity::class.java))
            aboutButton.performHapticFeedback(HapticGenerator.SUCCESS)
        }

        settingsButton.setOnClickListener {
            startActivity(Intent(applicationContext, SettingsActivity::class.java))
            aboutButton.performHapticFeedback(HapticGenerator.SUCCESS)
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (!(resultCode == RESULT_OK && requestCode == CODE_AUTHENTICATION_VERIFICATION)) {
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setShape () {
        if (!resources.configuration.isScreenRound && !utilities.vault.contains(utilities.CONFIG_SCREEN_ROUND)) {
            roundTimeLeft.visibility = View.GONE
            squareTimeLeft.visibility = View.VISIBLE
        } else {
            if (utilities.vault.getBoolean(utilities.CONFIG_SCREEN_ROUND, false)) {
                roundTimeLeft.visibility = View.GONE
                squareTimeLeft.visibility = View.VISIBLE
            } else {
                roundTimeLeft.visibility = View.VISIBLE
                squareTimeLeft.visibility = View.GONE
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeUI () {
        clock = findViewById(R.id.clock)
        loginsRecycler = findViewById(R.id.loginsRecycler)
        roundTimeLeft = findViewById(R.id.RoundTimeLeft)
        squareTimeLeft = findViewById(R.id.SquareTimeLeftTop)
        addAccountButton = findViewById(R.id.AddAccountButton)
        settingsButton = findViewById(R.id.SettingsButton)
        aboutButton = findViewById(R.id.AboutButton)

        setShape()

        val adapter = LoginsAdapter(utilities.vault.all.values.toMutableList() as MutableList<String>)
        loginsRecycler.layoutManager = LinearLayoutManager(this@MainActivity)
        loginsRecycler.adapter = adapter
        loginsRecycler.invalidate()
        loginsRecycler.refreshDrawableState()
        loginsRecycler.scheduleLayoutAnimation()

    }

    private fun start2faTimer () {
        try {
            thread {
                // round timer
                mfaCodesTimer.scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        val currentSecond = SimpleDateFormat("s", Locale.getDefault()).format(Date()).toInt()
                        var halfMinuteElapsed = abs((60-currentSecond))
                        if (halfMinuteElapsed >= 30) halfMinuteElapsed -= 30
                        try {
                            roundTimeLeft.progress = halfMinuteElapsed
                        } catch (_: Exception) {  }
                    }
                }, 0, 1000) // 1000 milliseconds = 1 second

                // square timer
                mfaCodesTimer.scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        val currentSecond = SimpleDateFormat("s", Locale.getDefault()).format(Date()).toInt()
                        var halfMinuteElapsed = abs((60-currentSecond))
                        if (halfMinuteElapsed >= 30) halfMinuteElapsed -= 30
                        try {
                            squareTimeLeft.progress = halfMinuteElapsed
                        } catch (_: Exception) {  }
                    }
                }, 0, 1000) // 1000 milliseconds = 1 second

            }
        } catch (_: IllegalStateException) {}
    }

    private fun startClock () {
        try {
            mfaCodesTimer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    val currentHour24 = SimpleDateFormat("HH", Locale.getDefault()).format(Date())
                    val currentHour = SimpleDateFormat("hh", Locale.getDefault()).format(Date())
                    val currentMinute = SimpleDateFormat("mm", Locale.getDefault()).format(Date())
                    val currentSecond = SimpleDateFormat("s", Locale.getDefault()).format(Date()).toInt()
                    runOnUiThread {
                        try {
                            clock.text = "$currentHour:$currentMinute"
                            if ((currentSecond % 2) == 0) clock.text = clock.text.toString().replace(":", " ")
                            else clock.text.toString().replace(" ", ":")
                        } catch (no2faData: Exception) {
                            when (no2faData) {
                                is IllegalArgumentException, is NullPointerException -> {}
                            }
                        }
                    }
                }
            }, 0, 1000) // 1000 milliseconds = 1 second
        } catch (timerError: IllegalStateException) { }
    }

    inner class LoginsAdapter (private val logins: MutableList<String>) : RecyclerView.Adapter<LoginsAdapter.ViewHolder>() {

        lateinit var blinkAnimation: AlphaAnimation

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ViewHolder {  // create new views
            val loginCard: View = LayoutInflater.from(parent.context).inflate(R.layout.login_card, parent, false)
            loginCard.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT)
            return ViewHolder(loginCard)
        }

        @RequiresApi(Build.VERSION_CODES.M)
        override fun onBindViewHolder(loginCard: ViewHolder, position: Int) {  // binds the list items to a view
            val login = utilities.decodeOTPAuthURL(logins[loginCard.adapterPosition])!!

            loginCard.name.text = login.issuer


            loginCard.setIsRecyclable(false)

            var otpCode: String?
            otpCode = GoogleAuthenticator(base32secret = login.secret!!).generate()
            loginCard.code.text = otpCode!!.replace("...".toRegex(), "$0 ")

            try {
                mfaCodesTimer.scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        runOnUiThread {
                            try {
                                val currentSecond = SimpleDateFormat("s", Locale.getDefault()).format(Date()).toInt()
                                otpCode = GoogleAuthenticator(base32secret = login.secret).generate()
                                var halfMinuteElapsed = abs((60-currentSecond))
                                if (halfMinuteElapsed >= 30) halfMinuteElapsed -= 30
                                if (halfMinuteElapsed in 0..1) {
                                    loginCard.code.animation = blinkAnimation
                                }
                                loginCard.code.text = otpCode!!.replace("...".toRegex(), "$0 ")
                            } catch (_: Exception) { }
                        }
                    }
                }, 0, 1000) // 1000 milliseconds = 1 second
            } catch (_: IllegalStateException) { }

            // tap on totp / mfa / 2fa
            loginCard.code.setOnClickListener {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Wristkey", loginCard.code.text.toString())
                clipboard.setPrimaryClip(clip)
                Toast.makeText(applicationContext, "Code copied!", Toast.LENGTH_LONG).show()
            }

        }

        override fun getItemCount(): Int {  // return the number of the items in the list
            return logins.size
        }

        inner class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {  // Holds the views for adding it to image and text
            val name: TextView = itemView.findViewById(R.id.name)
            val code: TextView = itemView.findViewById(R.id.code)

            val incrementCounter: ImageView = itemView.findViewById(R.id.increment_counter)
            val counter: TextView = itemView.findViewById(R.id.counter)
            val decrementCounter: ImageView = itemView.findViewById(R.id.decrement_counter)

            init {
                blinkAnimation = AlphaAnimation (0.25f, 1f)
                blinkAnimation.duration = 500
                blinkAnimation.startOffset = 20
                blinkAnimation.repeatCount = 2
            }

        }
    }


}
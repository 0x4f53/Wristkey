package app.wristkey

import android.annotation.SuppressLint
import android.content.Intent
import android.media.audiofx.HapticGenerator
import android.os.Build
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
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

    private fun initializeUI () {
        clock = findViewById (R.id.clock)
        roundTimeLeft = findViewById (R.id.RoundTimeLeft)
        squareTimeLeft = findViewById (R.id.SquareTimeLeftTop)
        addAccountButton = findViewById (R.id.AddAccountButton)
        settingsButton = findViewById (R.id.SettingsButton)
        aboutButton = findViewById (R.id.AboutButton)
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



}
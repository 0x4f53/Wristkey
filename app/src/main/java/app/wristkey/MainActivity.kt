package app.wristkey

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Intent
import android.media.audiofx.HapticGenerator
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dev.turingcomplete.kotlinonetimepassword.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import wristkey.R
import java.text.SimpleDateFormat
import java.util.*


const val CODE_AUTHENTICATION_VERIFICATION = 241

class MainActivity : AppCompatActivity() {

    lateinit var timer: Timer
    var isTimerRunning: Boolean = false
    lateinit var utilities: Utilities
    private var isRound: Boolean = false

    private lateinit var scrollView: NestedScrollView
    private lateinit var clock: TextView
    private lateinit var searchButton: ImageView
    private lateinit var searchBox: TextInputLayout
    private lateinit var searchBoxInput: TextInputEditText
    private lateinit var roundTimeLeft: ProgressBar
    private lateinit var addAccountButton: Button
    private lateinit var settingsButton: Button

    private lateinit var data: Utilities.WristkeyFileSystem
    private lateinit var logins: MutableList<Utilities.MfaCode>
    private lateinit var loginsAdapter: LoginsAdapter
    private lateinit var callback: ItemTouchHelperCallback
    private lateinit var loginsRecycler: RecyclerView
    private lateinit var touchHelper: ItemTouchHelper

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        utilities = Utilities (applicationContext)
        timer = Timer()
        lockScreen()
    }

    override fun onStop() {
        super.onStop()
        timer.cancel()
        isTimerRunning = false
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        isTimerRunning = false
    }

    override fun onPause() {
        super.onPause()
        timer.cancel()
        isTimerRunning = false
    }

    override fun onStart() {
        super.onStart()
        if (!isTimerRunning) startTimer()
    }

    override fun onResume() {
        super.onResume()
        if (!isTimerRunning) startTimer()
    }

    private var activated = false
    private fun searchBox() {

        val searchScope = CoroutineScope(Dispatchers.IO)
        searchBoxInput.doAfterTextChanged { text ->
            val text = text.toString().lowercase()
            searchScope.launch {
                val searchResults = logins.filter { login ->
                    login.issuer.lowercase().contains(text) || login.account.lowercase().contains(text) || login.label.lowercase().contains(text)
                }

                loginsAdapter = LoginsAdapter(searchResults, timer, isRound)

                withContext(Dispatchers.Main) {
                    delay(250)
                    loginsAdapter.notifyDataSetChanged()
                    loginsRecycler.adapter = loginsAdapter
                }

            }

        }

        if (!activated) {

            searchBox.visibility = View.VISIBLE
            searchButton.setImageDrawable(getDrawable(R.drawable.ic_cancel))

            searchBox.requestFocus()

            searchBox.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    if (searchBoxInput.text?.isEmpty() == true) {
                        searchBox.visibility = View.GONE
                    }
                }
            }

            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(searchBoxInput, InputMethodManager.SHOW_IMPLICIT)

            activated = true

        } else {

            searchButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_search_24))
            searchBoxInput.text?.clear()
            searchBox.clearFocus()
            searchBox.visibility = View.GONE

            loginsAdapter = LoginsAdapter (logins, timer, isRound)
            loginsAdapter.notifyDataSetChanged()
            loginsRecycler.adapter = loginsAdapter

            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(searchBoxInput.windowToken, 0)

            activated = false

        }
    }

    private fun setShape () {
        isRound = utilities.db.getBoolean (utilities.CONFIG_SCREEN_ROUND, resources.configuration.isScreenRound)
        if (isRound) roundTimeLeft.visibility = View.VISIBLE else roundTimeLeft.visibility = View.GONE
    }

    private fun initializeUI () {
        setContentView(R.layout.activity_main)

        searchBox = findViewById(R.id.searchBox)
        searchBoxInput = findViewById(R.id.searchBoxInput)
        searchBox.visibility = View.GONE

        searchButton = findViewById(R.id.searchButton)

        if (utilities.db.getBoolean (utilities.SETTINGS_SEARCH_ENABLED, true)) {
            scrollView = findViewById(R.id.scrollView)
            scrollView.post { scrollView.smoothScrollTo (0, 175) }
            searchButton.visibility = View.VISIBLE
        } else searchButton.visibility = View.GONE

        clock = findViewById(R.id.clock)

        loginsRecycler = findViewById(R.id.loginsRecycler)

        roundTimeLeft = findViewById(R.id.RoundTimeLeft)

        addAccountButton = findViewById(R.id.addAccount)
        settingsButton = findViewById(R.id.settings)

        data = utilities.getData()
        logins = mutableListOf()
        for (login in data.otpauth) { utilities.decodeOtpAuthURL(login)?.let { logins.add(it) } }

        setShape()

        loginsAdapter = LoginsAdapter (logins, timer, isRound)

        callback = ItemTouchHelperCallback (loginsAdapter, logins)
        touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(loginsRecycler)

        val layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        val snapHelper: SnapHelper = PagerSnapHelper()
        loginsRecycler.layoutManager = layoutManager
        snapHelper.attachToRecyclerView(loginsRecycler)

        loginsRecycler.adapter = loginsAdapter
        loginsRecycler.invalidate()
        loginsRecycler.refreshDrawableState()
        loginsRecycler.scheduleLayoutAnimation()
        // loginsRecycler.setItemViewCacheSize (logins.size)

        searchButton.setOnClickListener {
            searchBox()
            searchButton.performHapticFeedback(HapticGenerator.SUCCESS)
        }

        addAccountButton.setOnClickListener { startActivity(Intent(applicationContext, AddActivity::class.java)) }
        settingsButton.setOnClickListener { startActivity(Intent(applicationContext, SettingsActivity::class.java)) }

    }

    private fun startTimer () {

        val largestPeriod = logins.maxByOrNull { it.period }?.period ?: 30
        roundTimeLeft.max = largestPeriod

        if (!isTimerRunning) timer = Timer()
        timer.scheduleAtFixedRate (object : TimerTask() {
            override fun run() {
                isTimerRunning = true
                val second = SimpleDateFormat("s", Locale.getDefault()).format(Date()).toInt()
                val tickerValue = (largestPeriod*2 - (second % largestPeriod*2)) % largestPeriod
                try {
                    roundTimeLeft.progress = tickerValue
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) roundTimeLeft.setProgress(tickerValue, true)
                } catch (_: Exception) { }
            }
        }, 0, 1000) // 1000 milliseconds = 1 second
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

    private fun lockScreen () {
        if (utilities.db.getBoolean(utilities.SETTINGS_LOCK_ENABLED, false)) {
            val lockscreen = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            if (lockscreen.isKeyguardSecure) {
                val i = lockscreen.createConfirmDeviceCredentialIntent ("Wristkey", "App locked")
                startActivityForResult(i, CODE_AUTHENTICATION_VERIFICATION)
            }
        } else {
            initializeUI()
            startClock()
            startTimer()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (!(resultCode == RESULT_OK && requestCode == CODE_AUTHENTICATION_VERIFICATION)) {
            finish()
        } else {
            initializeUI()
            startClock()
            startTimer()
        }
    }

}
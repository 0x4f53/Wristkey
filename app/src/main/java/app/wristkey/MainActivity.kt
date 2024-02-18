package app.wristkey

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Intent
import android.media.audiofx.HapticGenerator
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import wristkey.R
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
    private lateinit var processedLogins: MutableList<Utilities.MfaCode> // This one goes to RecyclerView
    private lateinit var loginsAdapter: LoginsAdapter
    private lateinit var callback: ItemTouchHelperCallback
    private lateinit var loginsRecycler: RecyclerView
    private lateinit var touchHelper: ItemTouchHelper

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        utilities = Utilities(applicationContext)
        timer = Timer()

        if (utilities.db.getBoolean(utilities.SETTINGS_LOCK_ENABLED, false)) {
            val lockscreen = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            if (lockscreen.isKeyguardSecure) {
                val i = lockscreen.createConfirmDeviceCredentialIntent("Wristkey", "App locked")
                startActivityForResult(i, CODE_AUTHENTICATION_VERIFICATION)
            }
        } else {
            initializeUI()
            startClock()
            startTimer()
        }

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

    private var searchJob: Job? = null
    private var activated = false

    private fun searchBox() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val searchScope = CoroutineScope(Dispatchers.IO)
        val searchDelayMillis = 300

        searchBoxInput.doAfterTextChanged { text ->
            searchJob?.cancel()
            searchJob = searchScope.launch {
                delay(searchDelayMillis.toLong()) // Throttle the search
                val searchTerms = text.toString().lowercase()
                val searchResults = mutableListOf<Utilities.MfaCode>()

                for (item in processedLogins) {
                    if (("${item.issuer} ${item.account} ${item.label}").lowercase()
                            .contains(searchTerms)
                    ) {
                        searchResults.add(item)
                    }
                }

                withContext(Dispatchers.Main) {
                    loginsAdapter = LoginsAdapter(processedLogins, timer, isRound)
                    loginsRecycler.adapter = loginsAdapter
                }

                searchResults.clear()
            }
        }

        if (!activated) {

            searchBox.visibility = View.VISIBLE
            searchButton.setImageDrawable(getDrawable(R.drawable.ic_cancel))

            searchBox.requestFocus()

            searchBox.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    if (searchBoxInput.text?.isEmpty() == true) {
                        searchBox.visibility = View.GONE
                    }
                }
            }

            imm.showSoftInput(searchBoxInput, InputMethodManager.SHOW_IMPLICIT)

            activated = true

        } else {

            loginsAdapter.notifyDataSetChanged()

            searchButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_search_24))
            searchBoxInput.text?.clear()
            searchBox.clearFocus()
            searchBox.visibility = View.GONE

            imm.hideSoftInputFromWindow(searchBoxInput.windowToken, 0)

            activated = false

        }
    }

    private fun setShape() {
        isRound = utilities.db.getBoolean(
            utilities.CONFIG_SCREEN_ROUND,
            resources.configuration.isScreenRound
        )
        if (isRound) roundTimeLeft.visibility = View.VISIBLE else roundTimeLeft.visibility =
            View.GONE
    }

    private fun initializeUI() {
        setContentView(R.layout.activity_main)

        clock = findViewById(R.id.clock)

        loginsRecycler = findViewById(R.id.loginsRecycler)

        roundTimeLeft = findViewById(R.id.RoundTimeLeft)

        addAccountButton = findViewById(R.id.addAccount)
        settingsButton = findViewById(R.id.settings)

        setShape()

        data = utilities.getData()
        logins = mutableListOf()
        processedLogins = mutableListOf()

        for (login in data.otpauth) utilities.decodeOtpAuthURL(login)?.let { logins.add(it) }

        processedLogins = logins

        updateLogins()

        callback = ItemTouchHelperCallback(loginsAdapter, processedLogins)
        touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(loginsRecycler)

        val layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        val snapHelper: SnapHelper = PagerSnapHelper()
        loginsRecycler.layoutManager = layoutManager
        snapHelper.attachToRecyclerView(loginsRecycler)

        if (utilities.db.getBoolean(utilities.SETTINGS_SEARCH_ENABLED, true)) {
            scrollView = findViewById(R.id.scrollView)
            scrollView.post { scrollView.smoothScrollTo(0, 175) }

            searchBox = findViewById(R.id.searchBox)
            searchBoxInput = findViewById(R.id.searchBoxInput)
            searchBox.visibility = View.GONE

            searchButton = findViewById(R.id.searchButton)
            searchButton.visibility = View.VISIBLE

            searchButton.setOnClickListener {
                searchButton.performHapticFeedback(HapticGenerator.SUCCESS)
                searchBox()
            }

        } else searchButton.visibility = View.GONE

        addAccountButton.setOnClickListener {
            startActivity(
                Intent(
                    applicationContext,
                    AddActivity::class.java
                )
            )
        }
        settingsButton.setOnClickListener {
            startActivity(
                Intent(
                    applicationContext,
                    SettingsActivity::class.java
                )
            )
        }

    }

    private fun updateLogins() {
        processedLogins = logins
        for (processedLogin in processedLogins) {
            var mfaCode = utilities.generateTotp(
                secret = processedLogin.secret,
                algorithm = processedLogin.algorithm,
                digits = processedLogin.digits,
                period = processedLogin.period
            )
            mfaCode = "${
                mfaCode.substring(
                    0,
                    mfaCode.length / 2
                )
            } ${mfaCode.substring(mfaCode.length / 2)}"
            runOnUiThread { processedLogin.secret = mfaCode }
        }

        if (!::loginsAdapter.isInitialized) {
            loginsAdapter = LoginsAdapter(processedLogins, timer, isRound)
            loginsRecycler.adapter = loginsAdapter
        }

        runOnUiThread { loginsAdapter.notifyItemRangeChanged(0, processedLogins.size) }

    }

    private fun startTimer() {

        // Set circle around screen edge to show appropriate time interval
        val largestPeriod = logins.maxByOrNull { it.period }?.period ?: 30
        roundTimeLeft.max = largestPeriod

        // Start timer
        if (!isTimerRunning) timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                isTimerRunning = true
                val second = utilities.second()
                val tickerValue = (largestPeriod - (second % largestPeriod)) % largestPeriod
                try {
                    // Set ticker progress per seconds here.
                    roundTimeLeft.progress = tickerValue
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) roundTimeLeft.setProgress(
                        tickerValue,
                        true
                    )
                } catch (_: Exception) {
                }

                /*
                * Instead of setting TOTP secret in RecyclerView's ViewHolder,
                * iterate through logins list when time is up, replace the secret with the code itself and simple render it in the ViewHolder.
                * This prevents sequential secret computation on UI thread and reduces lag.
                * Note: This is for TOTPs only. Since HOTPs are updated once at a time after user input, they do not cause performanca issues.
                * */

                if (tickerValue == 29) updateLogins()

            }
        }, 0, 1000) // 1000 milliseconds = 1 second
    }

    private fun startClock() {
        if (!utilities.db.getBoolean(utilities.SETTINGS_CLOCK_ENABLED, true)) clock.visibility =
            View.GONE

        try {
            timer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    runOnUiThread { clock.text = utilities.getTime() }
                }
            }, 0, 1000)
        } catch (_: IllegalStateException) {
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
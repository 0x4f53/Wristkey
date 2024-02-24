package zeroxfourf.wristkey

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Intent
import android.media.audiofx.HapticGenerator
import android.os.Build
import android.os.Bundle
import android.view.*
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
import kotlin.math.hypot


const val CODE_AUTHENTICATION_VERIFICATION = 241

class MainActivity : AppCompatActivity() {

    lateinit var timer: Timer
    var isTimerRunning: Boolean = false
    lateinit var utilities: Utilities
    private var isRound: Boolean = false
    private var unlocked: Boolean = false

    private lateinit var scrollView: NestedScrollView
    private lateinit var clock: TextView
    private lateinit var searchLayout: LinearLayout
    private lateinit var searchButton: ImageView
    private lateinit var searchBox: TextInputLayout
    private lateinit var searchBoxInput: TextInputEditText
    private lateinit var searchProgress: ProgressBar
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
        utilities = Utilities(applicationContext)

        if (utilities.db.getBoolean(utilities.SETTINGS_LOCK_ENABLED, false)) {
            val lockscreen = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            if (lockscreen.isKeyguardSecure) {
                val i = lockscreen.createConfirmDeviceCredentialIntent("Wristkey", "App locked")
                startActivityForResult(i, CODE_AUTHENTICATION_VERIFICATION)
            }
        } else initializeUI()
    }

    override fun onStop() {
        super.onStop()
        if (isTimerRunning) timer.cancel()
        isTimerRunning = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isTimerRunning) timer.cancel()
        isTimerRunning = false
    }

    override fun onPause() {
        super.onPause()
        if (isTimerRunning) timer.cancel()
        isTimerRunning = false
    }

    override fun onResume() {
        super.onResume()
        if (!isTimerRunning && unlocked) {
            initializeUI()
            startTimer()
            isTimerRunning = true
        }
    }

    private var searchJob: Job? = null
    private var activated = false

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun searchBox() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val searchDelayMillis = 300L // Use Long for delay

        searchBoxInput.doAfterTextChanged { text ->
            searchProgress.visibility = View.VISIBLE
            searchJob?.cancel() // Cancel any existing job to ensure only one search operation runs at a time
            searchJob = CoroutineScope(Dispatchers.IO).launch {
                delay(searchDelayMillis) // Throttle the search to avoid flickering and excessive processing
                val searchTerms = text.toString().lowercase(Locale.getDefault())
                val searchResults = logins.filter { item ->  // Filter the logins list based on the search terms
                    ("${item.issuer} ${item.account} ${item.label}").lowercase(Locale.getDefault()).contains(searchTerms)
                }
                withContext(Dispatchers.Main) { // Update the RecyclerView on the main thread
                    loginsAdapter.updateData(searchResults)
                    searchProgress.visibility = View.GONE
                    scrollView.post { scrollView.smoothScrollTo(0, 175) }
                }
            }
        }

        // Toggle search box visibility and input handling
        if (!activated) {
            searchBox.post {
                val cx: Int = searchBox.width / 2
                val cy: Int = searchBox.height / 2
                val finalRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()
                val anim = ViewAnimationUtils.createCircularReveal(searchBox, cx, cy, 0f, finalRadius)
                searchBox.visibility = View.VISIBLE
                anim.start()
                searchButton.setImageDrawable(getDrawable(R.drawable.ic_cancel))
                searchBox.requestFocus()
                imm.showSoftInput(searchBoxInput, InputMethodManager.SHOW_IMPLICIT)
            }
            activated = true
        } else {
            searchButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_search_24))
            searchBoxInput.text?.clear()
            searchBox.clearFocus()
            searchBox.visibility = View.GONE
            imm.hideSoftInputFromWindow(searchBoxInput.windowToken, 0)
            scrollView.post { scrollView.smoothScrollTo(0, 175) }
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

        scrollView = findViewById(R.id.scrollView)
        if (utilities.db.getBoolean(utilities.SETTINGS_SEARCH_ENABLED, true)) scrollView.post { scrollView.smoothScrollTo(0, 175) }

        clock = findViewById(R.id.clock)

        roundTimeLeft = findViewById(R.id.RoundTimeLeft)

        addAccountButton = findViewById(R.id.addAccount)
        settingsButton = findViewById(R.id.settings)

        setShape()

        data = utilities.getData()
        logins = mutableListOf()

        startTimer()
        startClock()

        for (login in data.otpauth) utilities.decodeOtpAuthURL(login)?.let { logins.add(it) }

        loginsRecycler = findViewById(R.id.loginsRecycler)
        loginsAdapter = LoginsAdapter(logins, timer, isRound)
        loginsRecycler.adapter = loginsAdapter

        callback = ItemTouchHelperCallback(loginsAdapter, logins)
        touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(loginsRecycler)

        val layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        val snapHelper: SnapHelper = PagerSnapHelper()
        loginsRecycler.layoutManager = layoutManager
        snapHelper.attachToRecyclerView(loginsRecycler)

        searchLayout = findViewById(R.id.searchLayout)
        searchBox = findViewById(R.id.searchBox)
        searchButton = findViewById(R.id.searchButton)
        searchProgress = findViewById(R.id.searchProgress)
        searchProgress.visibility = View.GONE

        if (utilities.db.getBoolean(utilities.SETTINGS_SEARCH_ENABLED, true)) {
            searchBoxInput = findViewById(R.id.searchBoxInput)
            searchBox.visibility = View.GONE

            searchButton.visibility = View.VISIBLE

            searchButton.setOnClickListener {
                searchButton.performHapticFeedback(HapticGenerator.SUCCESS)
                searchBox()
            }

        } else searchLayout.visibility = View.INVISIBLE

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

    private fun startTimer() {

        // Start timer
        timer = Timer()
        isTimerRunning = true

        // Set circle around screen edge to show appropriate time interval
        val largestPeriod = logins.maxByOrNull { it.period }?.period ?: 30
        roundTimeLeft.max = largestPeriod

        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                isTimerRunning = true
                val second = utilities.second()
                val tickerValue = (largestPeriod - (second % largestPeriod)) % largestPeriod
                try { // Set ticker progress per seconds here.
                    roundTimeLeft.progress = tickerValue
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) roundTimeLeft.setProgress(
                        tickerValue,
                        true
                    )
                } catch (_: Exception) { }
            }
        }, 0, 1000) // 1000 milliseconds = 1 second
    }

    private fun startClock() {
        if (!utilities.db.getBoolean(utilities.SETTINGS_CLOCK_ENABLED, true)) {
            clock.visibility = View.GONE
            return
        }

        clock.text = utilities.getTime()
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
            unlocked = true
        }
    }

}
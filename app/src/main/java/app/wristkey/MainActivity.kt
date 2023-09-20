package app.wristkey

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.audiofx.HapticGenerator
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.View.OnFocusChangeListener
import android.view.animation.AlphaAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dev.turingcomplete.kotlinonetimepassword.*
import wristkey.R
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates


const val CODE_AUTHENTICATION_VERIFICATION = 241

class MainActivity : AppCompatActivity() {

    lateinit var timer: Timer
    var isTimerRunning: Boolean = false
    lateinit var utilities: Utilities

    private lateinit var scrollView: NestedScrollView
    private lateinit var clock: TextView
    private lateinit var searchButton: ImageView
    private lateinit var searchBox: TextInputLayout
    private lateinit var searchBoxInput: TextInputEditText
    private lateinit var roundTimeLeft: ProgressBar
    private lateinit var loginsRecycler: RecyclerView
    private lateinit var addAccountButton: Button
    private lateinit var settingsButton: Button

    @RequiresApi(Build.VERSION_CODES.M)
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

    var activated = false
    @RequiresApi(Build.VERSION_CODES.M)
    private fun searchBox() {
        if (!activated) {
            searchBox.visibility = View.VISIBLE

            searchBox.requestFocus()

            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

            searchBox.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(v.windowToken, 0)
                    if (searchBoxInput.text?.isEmpty() == true) {
                        searchBox.visibility = View.GONE
                    }
                }
            }

            searchBox.performClick()

            searchBoxInput.doOnTextChanged { text, start, before, count ->

                // val logins = utilities.searchLogins(text.toString(), utilities.getLogins().toMutableList())

                // val adapter = LoginsAdapter(logins)

                loginsRecycler.layoutManager = LinearLayoutManager(this@MainActivity)
                // loginsRecycler.adapter = adapter
                loginsRecycler.invalidate()
                loginsRecycler.refreshDrawableState()
                loginsRecycler.scheduleLayoutAnimation()
                // loginsRecycler.setItemViewCacheSize(vault.size)

            }

            activated = true
        } else {
            searchBoxInput.text?.clear()
            searchBox.clearFocus()
            searchBox.visibility = View.GONE

            activated = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setShape () {
        if (utilities.db.getBoolean (utilities.CONFIG_SCREEN_ROUND, resources.configuration.isScreenRound)) {
            roundTimeLeft.visibility = View.VISIBLE

        } else {
            roundTimeLeft.visibility = View.GONE
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
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

        // val logins = utilities.getLogins().toMutableList()

        // val adapter = LoginsAdapter(logins)

        val layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        val snapHelper: SnapHelper = PagerSnapHelper()
        loginsRecycler.layoutManager = layoutManager
        snapHelper.attachToRecyclerView(loginsRecycler)

        // loginsRecycler.adapter = adapter
        loginsRecycler.invalidate()
        loginsRecycler.refreshDrawableState()
        loginsRecycler.scheduleLayoutAnimation()
        // loginsRecycler.setItemViewCacheSize(vault.size)

        searchButton.setOnClickListener {
            searchBox()
            searchButton.performHapticFeedback(HapticGenerator.SUCCESS)
        }

        addAccountButton.setOnClickListener { startActivity(Intent(applicationContext, AddActivity::class.java)) }
        settingsButton.setOnClickListener { startActivity(Intent(applicationContext, SettingsActivity::class.java)) }

    }

    private fun startTimer () {
        if (!isTimerRunning) timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                isTimerRunning = true
                val second = SimpleDateFormat("s", Locale.getDefault()).format(Date()).toInt()
                var tickerValue = (60 - (second % 60)) % 30
                try {
                    roundTimeLeft.progress = tickerValue
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) roundTimeLeft.setProgress(tickerValue, true)
                //squareTimeLeft.progress = halfMinuteElapsed
                //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) squareTimeLeft.setProgress(halfMinuteElapsed, true)
                } catch (_: Exception) { }
            } }, 0, 1000) // 1000 milliseconds = 1 second
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

    @RequiresApi(Build.VERSION_CODES.M)
    private fun lockScreen () {
        if (utilities.db.getBoolean(utilities.SETTINGS_LOCK_ENABLED, false)) {
            val lockscreen = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            if (lockscreen.isKeyguardSecure) {
                val i = lockscreen.createConfirmDeviceCredentialIntent ("Wristkey", "App locked")
                startActivityForResult(i, CODE_AUTHENTICATION_VERIFICATION)
            }
        } else {
            initializeUI()
            setShape()
            startClock()
            startTimer()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (!(resultCode == RESULT_OK && requestCode == CODE_AUTHENTICATION_VERIFICATION)) {
            finish()
        } else {
            initializeUI()
            setShape()
            startClock()
            startTimer()
        }
    }

    inner class LoginsAdapter (private val logins: MutableList<Utilities.MfaCode>) : RecyclerView.Adapter<LoginsAdapter.ViewHolder>() {

        lateinit var blinkAnimation: AlphaAnimation
        lateinit var singleBlinkAnimation: AlphaAnimation
        var beepEnabled by Delegates.notNull<Boolean>()
        var hapticsEnabled by Delegates.notNull<Boolean>()

        @RequiresApi(Build.VERSION_CODES.M)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ViewHolder {  // create new views
            val loginCard: View = LayoutInflater.from(parent.context).inflate(R.layout.login_card, parent, false)
            loginCard.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT)
            return ViewHolder(loginCard)
        }

        @SuppressLint("ClickableViewAccessibility")
        @RequiresApi(Build.VERSION_CODES.M)
        override fun onBindViewHolder(loginCard: ViewHolder, position: Int) {  // binds the list items to a view

            loginCard.setIsRecyclable(false)

            val login = logins[loginCard.adapterPosition]

            lateinit var algorithm: HmacAlgorithm
            if (login.algorithm!!.contains(utilities.ALGO_SHA1)) algorithm = HmacAlgorithm.SHA1
            else if (login.algorithm.contains(utilities.ALGO_SHA256)) algorithm = HmacAlgorithm.SHA256
            else if (login.algorithm.contains(utilities.ALGO_SHA512)) algorithm = HmacAlgorithm.SHA512

            var loginString = ""
            if (!login.issuer.isNullOrBlank()) loginString = login.issuer
            if (!login.label.isNullOrBlank()) loginString += " — ${login.label}"
            if (!login.account.isNullOrBlank() && login.label != login.account) loginString += " (${login.account})"

            if (loginString.isNullOrBlank()) loginCard.label.visibility = View.GONE
            else loginCard.label.text = loginString

            var otp: String?

            when (login.mode) {
                utilities.MFA_TIME_MODE -> {

                    when (login.period) {
                        15, 60 -> {
                            loginCard.counterControls.visibility = View.VISIBLE
                            loginCard.incrementCounter.visibility = View.GONE
                            loginCard.decrementCounter.visibility = View.GONE
                            loginCard.counter.visibility = View.VISIBLE
                            loginCard.counter.text = "${login.period}s"
                        }

                        30 -> loginCard.counterControls.visibility = View.GONE

                    }

                    val config = TimeBasedOneTimePasswordConfig (
                        timeStep = login.period!!.toLong(),
                        timeStepUnit = TimeUnit.SECONDS,
                        codeDigits = login.digits!!,
                        hmacAlgorithm = algorithm
                    )

                    otp = TimeBasedOneTimePasswordGenerator(login.secret!!.toByteArray(), config).generate()
                    if (login.algorithm == utilities.ALGO_SHA1 && login.period == 30 && login.digits == 6)
                        otp = GoogleAuthenticator(login.secret.toByteArray()).generate()

                    loginCard.code.text =
                        if (otp.length == 6) otp.replace("...".toRegex(), "$0 ") else otp.replace("....".toRegex(), "$0 ")

                    var timerElapsed: Int
                    try {
                        timer.scheduleAtFixedRate (object : TimerTask() {
                            override fun run() {
                                timerElapsed = SimpleDateFormat("s", Locale.getDefault()).format(Date()).toInt()
                                timerElapsed = 60 - timerElapsed

                                when (login.period) {
                                    15 -> when(timerElapsed) {
                                        15, 30, 45, 60 -> {
                                            try { loginCard.code.startAnimation(blinkAnimation) } catch (_: Exception) { }
                                            if (beepEnabled) utilities.beep()
                                            runOnUiThread {
                                                loginCard.code.text =
                                                    if (otp!!.length == 6) otp!!.replace("...".toRegex(), "$0 ")
                                                    else otp!!.replace("....".toRegex(), "$0 ")
                                            }
                                        }
                                    }

                                    30 -> when(timerElapsed) {
                                        30, 60 -> {
                                            try { loginCard.code.startAnimation(blinkAnimation) } catch (_: Exception) { }
                                            if (beepEnabled) utilities.beep()
                                            runOnUiThread {
                                                loginCard.code.text =
                                                    if (otp!!.length == 6) otp!!.replace("...".toRegex(), "$0 ")
                                                    else otp!!.replace("....".toRegex(), "$0 ")
                                                }
                                        }
                                    }

                                    60 -> when(timerElapsed) {
                                        60 -> {
                                            try { loginCard.code.startAnimation(blinkAnimation) } catch (_: Exception) { }
                                            if (beepEnabled) utilities.beep()
                                            runOnUiThread {
                                                loginCard.code.text =
                                                    if (otp!!.length == 6) otp!!.replace("...".toRegex(), "$0 ")
                                                    else otp!!.replace("....".toRegex(), "$0 ")
                                                }
                                        }
                                    }
                                }

                                otp = TimeBasedOneTimePasswordGenerator(login.secret.toByteArray(), config).generate()
                                if (login.algorithm == utilities.ALGO_SHA1 && login.period == 30 && login.digits == 6)
                                    otp = GoogleAuthenticator(login.secret.toByteArray()).generate()
                            }
                        }, 0, 1000) // 1000 milliseconds = 1 second
                    } catch (_: Exception) { }
                }

                utilities.MFA_COUNTER_MODE -> {

                    loginCard.counterControls.visibility = View.VISIBLE

                    val config = HmacOneTimePasswordConfig (
                        codeDigits = login.digits!!,
                        hmacAlgorithm = algorithm
                    )

                    var currentCount: Long

                    currentCount = login.counter!!
                    loginCard.counter.text = currentCount.toString()

                    try {

                        loginCard.incrementCounter.setOnClickListener {
                            currentCount = loginCard.counter.text.toString().toLong()
                            loginCard.counter.text = (currentCount + 1).toString()

                            otp = HmacOneTimePasswordGenerator(login.secret!!.toByteArray(), config)
                                .generate(loginCard.counter.text.toString().toLong())

                            loginCard.code.text =
                                if (otp!!.length == 6) otp!!.replace("...".toRegex(), "$0 ") else otp!!.replace("....".toRegex(), "$0 ")

                            try {
                                loginCard.code.startAnimation(singleBlinkAnimation)
                                loginCard.incrementCounter.startAnimation(singleBlinkAnimation)
                            } catch (_: Exception) { }

                            loginCard.incrementCounter.performHapticFeedback(HapticGenerator.ALREADY_EXISTS)
                            if (beepEnabled) utilities.beep()
                            if (hapticsEnabled) loginCard.incrementCounter.performHapticFeedback(HapticFeedbackConstants.REJECT)

                            if (currentCount+1 == 69L) Toast.makeText(applicationContext, "Nice ;)", Toast.LENGTH_SHORT).show()

                            val loginData = Utilities.MfaCode (
                                mode = login.mode,
                                issuer = login.issuer,
                                account = login.account,
                                secret = login.secret,
                                algorithm = login.algorithm,
                                digits = login.digits,
                                period = login.period,
                                lock = false,
                                counter = loginCard.counter.text.toString().toLong(),
                                label = login.label
                            )

                            // utilities.overwriteLogin(oldLogin = login, newLogin = loginData)

                        }

                        loginCard.decrementCounter.setOnClickListener {
                            currentCount = loginCard.counter.text.toString().toLong()
                            if (currentCount != 0L) {
                                loginCard.counter.text = (currentCount - 1).toString()
                            }

                            otp = HmacOneTimePasswordGenerator(login.secret!!.toByteArray(), config)
                                .generate(loginCard.counter.text.toString().toLong())

                            loginCard.code.text =
                                if (otp!!.length == 6) otp!!.replace("...".toRegex(), "$0 ") else otp!!.replace("....".toRegex(), "$0 ")

                            try {
                                loginCard.code.startAnimation(singleBlinkAnimation)
                                loginCard.incrementCounter.startAnimation(singleBlinkAnimation)
                            } catch (_: Exception) { }

                            loginCard.decrementCounter.performHapticFeedback(HapticGenerator.ALREADY_EXISTS)
                            if (beepEnabled) utilities.beep()
                            if (hapticsEnabled) loginCard.incrementCounter.performHapticFeedback(HapticFeedbackConstants.REJECT)

                            if (currentCount-1 == 69L) Toast.makeText(applicationContext, "Nice ;)", Toast.LENGTH_SHORT).show()

                            val loginData = Utilities.MfaCode (
                                mode = login.mode,
                                issuer = login.issuer,
                                account = login.account,
                                secret = login.secret,
                                algorithm = login.algorithm,
                                digits = login.digits,
                                period = login.period,
                                lock = false,
                                counter = loginCard.counter.text.toString().toLong(),
                                label = login.label
                            )

                            // utilities.overwriteLogin(oldLogin = login, newLogin = loginData)

                        }

                    } catch (_: IllegalStateException) { }

                    otp = HmacOneTimePasswordGenerator(login.secret!!.toByteArray(), config)
                        .generate(loginCard.counter.text.toString().toLong())

                    loginCard.code.text =
                        if (otp!!.length == 6) otp!!.replace("...".toRegex(), "$0 ") else otp!!.replace("....".toRegex(), "$0 ")

                }

            }

            // tap on totp / mfa / 2fa
            loginCard.code.setOnClickListener {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Wristkey", loginCard.code.text.toString().replace(" ", ""))
                clipboard.setPrimaryClip(clip)
                Toast.makeText(applicationContext, "Code copied!", Toast.LENGTH_LONG).show()
            }

            loginCard.loginCard.setOnTouchListener(object : OnSwipeTouchListener(this@MainActivity) {
                override fun onLongClick() {
                    val intent = Intent(applicationContext, ManualEntryActivity::class.java)
                    // intent.putExtra(utilities.INTENT_QR_DATA, utilities.getUuid(login))
                    startActivity(intent)
                    loginCard.loginCard.performHapticFeedback(HapticGenerator.SUCCESS)
                    super.onSwipeRight()
                }
            })

        }

        override fun getItemCount(): Int {  // return the number of the items in the list
            return logins.size
        }

        @RequiresApi(Build.VERSION_CODES.M)
        inner class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {  // Holds the views for adding it to image and text
            val loginCard: ConstraintLayout = itemView.findViewById(R.id.loginCard)

            val label: TextView = itemView.findViewById(R.id.label)
            val code: TextView = itemView.findViewById(R.id.code)

            val counterControls: LinearLayout = itemView.findViewById(R.id.counterControls)
            val incrementCounter: ImageView = itemView.findViewById(R.id.plus)
            val counter: TextView = itemView.findViewById(R.id.counter)
            val decrementCounter: ImageView = itemView.findViewById(R.id.minus)

            init {

                if (utilities.db.getBoolean (utilities.CONFIG_SCREEN_ROUND, resources.configuration.isScreenRound)) {
                    val centeringParameters = LinearLayout.LayoutParams (
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        weight = 1.0f
                        gravity = Gravity.CENTER_HORIZONTAL
                    }

                    code.layoutParams = centeringParameters
                    label.layoutParams = centeringParameters

                }

                label.isSelected = true

                blinkAnimation = AlphaAnimation (0.25f, 1f)
                blinkAnimation.duration = 500
                blinkAnimation.startOffset = 20
                blinkAnimation.repeatCount = 1

                singleBlinkAnimation = AlphaAnimation (0.25f, 1f)
                singleBlinkAnimation.duration = 500
                singleBlinkAnimation.startOffset = 20
                singleBlinkAnimation.repeatCount = 0

                beepEnabled = utilities.db.getBoolean(utilities.SETTINGS_BEEP_ENABLED, false)
                hapticsEnabled = utilities.db.getBoolean(utilities.SETTINGS_HAPTICS_ENABLED, true)

            }

        }
    }


}
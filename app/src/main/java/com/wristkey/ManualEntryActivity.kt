package com.wristkey
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.wear.widget.BoxInsetLayout
import com.google.gson.Gson
import org.json.JSONArray
import java.util.*


class ManualEntryActivity : WearableActivity() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_entry)
        val boxinsetlayout = findViewById<BoxInsetLayout>(R.id.BoxInsetLayout)
        val addAccountLabel = findViewById<TextView>(R.id.ManualEntryLabel)
        val confirmButton = findViewById<ImageButton>(R.id.AuthenticatorConfirmButton)
        val deleteButton = findViewById<ImageButton>(R.id.DeleteButton)
        val cancelButton = findViewById<ImageButton>(R.id.CancelButton)
        val other = findViewById<LinearLayout>(R.id.Other)
        val account = findViewById<EditText>(R.id.AccountField)
        val sharedSecret = findViewById<EditText>(R.id.SharedSecretField)
        sharedSecret.transformationMethod = PasswordTransformationMethod.getInstance()
        val modeGroup = findViewById<RadioGroup>(R.id.GeneratorMode)
        val timeMode = findViewById<RadioButton>(R.id.TimeMode)
        val counterMode = findViewById<RadioButton>(R.id.CounterMode)
        modeGroup.check(R.id.TimeMode)
        var mode = "Time"
        val digitLength = findViewById<SeekBar>(R.id.DigitLengthSeekbar)
        digitLength.progress = 1
        var selectedDigitLength = "6"
        val digitLengthLabel = findViewById<TextView>(R.id.DigitLength)
        val algorithm = findViewById<SeekBar>(R.id.AlgorithmKeylengthSeekbar)
        algorithm.progress = 0
        var selectedAlgorithm = "HmacAlgorithm.SHA1"
        val algorithmLabel = findViewById<TextView>(R.id.AlgorithmLength)

        var currentAccent = appData.getString("accent", "4285F4")
        var currentTheme = appData.getString("theme", "000000")
        boxinsetlayout.setBackgroundColor(Color.parseColor("#" + currentTheme))
        account.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
        account.foregroundTintList = ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
        account.compoundDrawableTintList = ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
        sharedSecret.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
        sharedSecret.foregroundTintList = ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
        sharedSecret.compoundDrawableTintList = ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
        timeMode.buttonTintList = ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
        counterMode.buttonTintList = ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
        digitLength.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
        digitLength.foregroundTintList = ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
        digitLength.progressTintList = ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
        digitLength.progressBackgroundTintList = ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
        digitLength.secondaryProgressTintList = ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
        digitLength.indeterminateTintList = ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
        digitLength.thumbTintList = ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
        digitLength.tickMarkTintList = ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
        algorithm.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
        algorithm.foregroundTintList = ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
        algorithm.progressTintList = ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
        algorithm.progressBackgroundTintList = ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
        algorithm.secondaryProgressTintList = ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
        algorithm.indeterminateTintList = ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
        algorithm.thumbTintList = ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
        algorithm.tickMarkTintList = ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
        confirmButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#" + currentAccent))
        if (currentTheme == "F7F7F7") {
            addAccountLabel.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
            account.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
            sharedSecret.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
            timeMode.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
            counterMode.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
            digitLengthLabel.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
            algorithmLabel.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
        } else {
            addAccountLabel.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
            account.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
            sharedSecret.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
            timeMode.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
            counterMode.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
            digitLengthLabel.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
            algorithmLabel.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
        }

        val tokenId = intent.getStringExtra("token_id")

        if (tokenId != null) {
            addAccountLabel.text = "Edit Account"
            other.visibility = View.VISIBLE

            val data = JSONArray(logins.getString(tokenId, null))

            account.setText(data[0].toString())
            sharedSecret.setText(data[1].toString())

            if (data[2].toString() == "Time") {
                modeGroup.check(R.id.TimeMode)
            } else {
                modeGroup.check(R.id.CounterMode)
            }

            when(data[3].toString()) {
                "4" -> digitLength.progress = 0
                "6" -> digitLength.progress = 1
                "7" -> digitLength.progress = 2
                "8" -> digitLength.progress = 3
                else -> digitLength.progress = 1
            }

            when(data[4].toString()) {
                "HmacAlgorithm.SHA1" -> algorithm.progress = 0
                "HmacAlgorithm.SHA256" -> algorithm.progress = 1
                "HmacAlgorithm.SHA512" -> algorithm.progress = 2
                else -> algorithm.progress = 1
            }

            deleteButton.setOnClickListener {
                val intent = Intent(applicationContext, DeleteActivity::class.java)
                intent.putExtra("token_id", tokenId)
                startActivity(intent)
                finish()
            }
        }

        confirmButton.setOnClickListener {
            val errorToast: Toast?
            val tokenData = ArrayList<String>()
            if (account.text.toString() == ""){
                errorToast = Toast.makeText(this, "Enter account name", Toast.LENGTH_SHORT)
                errorToast.show()
            }else if (sharedSecret.text.toString() == ""){
                errorToast = Toast.makeText(this, "Enter shared secret", Toast.LENGTH_SHORT)
                errorToast.show()
            }else if((sharedSecret.text.toString()).length < 8 && selectedDigitLength == "6" && selectedAlgorithm == "HmacAlgorithm.SHA1" && mode == "Time"){
                errorToast = Toast.makeText(this, "Invalid shared secret", Toast.LENGTH_SHORT)
                errorToast.show()
            }else{
                tokenData.add(account.text.toString())
                tokenData.add(sharedSecret.text.toString())
                tokenData.add(mode)
                tokenData.add(selectedDigitLength)
                tokenData.add(selectedAlgorithm)
                tokenData.add("0")  // If counter mode is selected, initial value must be 0.
                val json = Gson().toJson(tokenData)

                val id = UUID.randomUUID().toString()

                logins.edit().putString(id, json).apply()
                val addedToast = Toast.makeText(this, "Added account", Toast.LENGTH_SHORT)
                addedToast.show()

                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        cancelButton.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        modeGroup.setOnCheckedChangeListener { _, checkedId ->
            mode = if (checkedId != -1) {
                (findViewById<View>(checkedId) as RadioButton).text.toString()
            } else {
                ""
            }
        }
        digitLength.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (digitLength.progress == 0) {
                    digitLengthLabel.text = "4 digits"
                    selectedDigitLength = "4"
                } else if (digitLength.progress == 1) {
                    selectedDigitLength = "6"
                    digitLengthLabel.text = "6 digits"
                } else if (digitLength.progress == 2) {
                    selectedDigitLength = "7"
                    digitLengthLabel.text = "7 digits"
                } else if (digitLength.progress == 3) {
                    selectedDigitLength = "8"
                    digitLengthLabel.text = "8 digits"
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                if (digitLength.progress == 0) {
                    digitLengthLabel.text = "4 digits"
                    selectedDigitLength = "4"
                } else if (digitLength.progress == 1) {
                    selectedDigitLength = "6"
                    digitLengthLabel.text = "6 digits"
                } else if (digitLength.progress == 2) {
                    selectedDigitLength = "7"
                    digitLengthLabel.text = "7 digits"
                } else if (digitLength.progress == 3) {
                    selectedDigitLength = "8"
                    digitLengthLabel.text = "8 digits"
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (digitLength.progress == 0) {
                    digitLengthLabel.text = "4 digits"
                    selectedDigitLength = "4"
                } else if (digitLength.progress == 1) {
                    selectedDigitLength = "6"
                    digitLengthLabel.text = "6 digits"
                } else if (digitLength.progress == 2) {
                    selectedDigitLength = "7"
                    digitLengthLabel.text = "7 digits"
                } else if (digitLength.progress == 3) {
                    selectedDigitLength = "8"
                    digitLengthLabel.text = "8 digits"
                }
            }
        })

        algorithm.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (algorithm.progress == 0) {
                    algorithmLabel.text = "SHA-1"
                    selectedAlgorithm = "HmacAlgorithm.SHA1"
                } else if (algorithm.progress == 1) {
                    algorithmLabel.text = "SHA-256"
                    selectedAlgorithm = "HmacAlgorithm.SHA256"
                } else if (algorithm.progress == 2) {
                    algorithmLabel.text = "SHA-512"
                    selectedAlgorithm = "HmacAlgorithm.SHA512"
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                if (algorithm.progress == 0) {
                    algorithmLabel.text = "SHA-1"
                    selectedAlgorithm = "HmacAlgorithm.SHA1"
                } else if (algorithm.progress == 1) {
                    algorithmLabel.text = "SHA-256"
                    selectedAlgorithm = "HmacAlgorithm.SHA256"
                } else if (algorithm.progress == 2) {
                    algorithmLabel.text = "SHA-512"
                    selectedAlgorithm = "HmacAlgorithm.SHA512"
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (algorithm.progress == 0) {
                    algorithmLabel.text = "SHA-1"
                    selectedAlgorithm = "HmacAlgorithm.SHA1"
                } else if (algorithm.progress == 1) {
                    algorithmLabel.text = "SHA-256"
                    selectedAlgorithm = "HmacAlgorithm.SHA256"
                } else if (algorithm.progress == 2) {
                    algorithmLabel.text = "SHA-512"
                    selectedAlgorithm = "HmacAlgorithm.SHA512"
                }
            }
        })
    }
}
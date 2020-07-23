package com.owais.wristkey
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.*
import android.widget.RadioGroup
import androidx.annotation.RequiresApi
import androidx.wear.widget.BoxInsetLayout

class AddActivity : WearableActivity() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)
        val boxinsetlayout = findViewById<BoxInsetLayout>(R.id.BoxInsetLayout)
        val addAccountLabel = findViewById<TextView>(R.id.AddAccountLabel)
        val confirmButton = findViewById<ImageButton>(R.id.ConfirmButton)
        val cancelButton = findViewById<ImageButton>(R.id.CancelButton)
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
        val storageFile = "app_storage"
        val storage: SharedPreferences = applicationContext.getSharedPreferences(storageFile, Context.MODE_PRIVATE)
        var currentAccent = storage.getString("accent", "4285F4")
        var currentTheme = storage.getString("theme", "000000")
        boxinsetlayout.setBackgroundColor(Color.parseColor("#"+currentTheme))
        account.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        account.foregroundTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        account.compoundDrawableTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        sharedSecret.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        sharedSecret.foregroundTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        sharedSecret.compoundDrawableTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        timeMode.buttonTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        counterMode.buttonTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        digitLength.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        digitLength.foregroundTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        digitLength.progressTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        digitLength.progressBackgroundTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        digitLength.secondaryProgressTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        digitLength.indeterminateTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        digitLength.thumbTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        digitLength.tickMarkTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        algorithm.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        algorithm.foregroundTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        algorithm.progressTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        algorithm.progressBackgroundTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        algorithm.secondaryProgressTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        algorithm.indeterminateTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        algorithm.thumbTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        algorithm.tickMarkTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
        confirmButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#"+currentAccent))
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
        confirmButton.setOnClickListener {
            var errorToast: Toast? = null
            val storageFile = "app_storage"
            val storage: SharedPreferences = applicationContext.getSharedPreferences(storageFile, Context.MODE_PRIVATE)
            val storageEditor: SharedPreferences.Editor =  storage.edit()
            if (account.text.toString() == ""){
                errorToast = Toast.makeText(this, "Enter account name", Toast.LENGTH_SHORT)
                errorToast.show()
            }else if (sharedSecret.text.toString() == ""){
                errorToast = Toast.makeText(this, "Enter shared secret", Toast.LENGTH_SHORT)
                errorToast.show()
            }else if((sharedSecret.text.toString()).length < 20 && selectedDigitLength == "6" && selectedAlgorithm == "HmacAlgorithm.SHA1" && mode == "Time"){
                errorToast = Toast.makeText(this, "Invalid shared secret", Toast.LENGTH_SHORT)
                errorToast.show()
            }else{
                var serialNumber = storage.getInt("currentSerialNumber", 0)
                serialNumber+=1
                storageEditor.putString(serialNumber.toString(), "■"+account.text.toString()+"▰"+sharedSecret.text.toString()+"◀"+mode+"▾"+selectedDigitLength+"●"+selectedAlgorithm+"◆"+"0"+"▮")
                storageEditor.putInt("currentSerialNumber", serialNumber)
                storageEditor.apply()
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
        modeGroup.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { _, checkedId ->
            if (checkedId != -1) {
                mode = (findViewById<View>(checkedId) as RadioButton).text.toString()
            } else {
                mode = ""
            }
        })
        digitLength.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (digitLength.progress==0){
                    digitLengthLabel.text="4 digits"
                    selectedDigitLength = "4"
                }else if (digitLength.progress==1){
                    selectedDigitLength = "6"
                    digitLengthLabel.text="6 digits"
                }else if (digitLength.progress==2){
                    selectedDigitLength = "7"
                    digitLengthLabel.text="7 digits"
                }else if (digitLength.progress==3){
                    selectedDigitLength = "8"
                    digitLengthLabel.text="8 digits"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                if (digitLength.progress==0){
                    digitLengthLabel.text="4 digits"
                    selectedDigitLength = "4"
                }else if (digitLength.progress==1){
                    selectedDigitLength = "6"
                    digitLengthLabel.text="6 digits"
                }else if (digitLength.progress==2){
                    selectedDigitLength = "7"
                    digitLengthLabel.text="7 digits"
                }else if (digitLength.progress==3){
                    selectedDigitLength = "8"
                    digitLengthLabel.text="8 digits"
                }
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (digitLength.progress==0){
                    digitLengthLabel.text="4 digits"
                    selectedDigitLength = "4"
                }else if (digitLength.progress==1){
                    selectedDigitLength = "6"
                    digitLengthLabel.text="6 digits"
                }else if (digitLength.progress==2){
                    selectedDigitLength = "7"
                    digitLengthLabel.text="7 digits"
                }else if (digitLength.progress==3){
                    selectedDigitLength = "8"
                    digitLengthLabel.text="8 digits"
                }
            }
        })

        algorithm.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (algorithm.progress==0){
                    algorithmLabel.text="SHA-1"
                    selectedAlgorithm="HmacAlgorithm.SHA1"
                }else if (algorithm.progress==1){
                    algorithmLabel.text="SHA-256"
                    selectedAlgorithm="HmacAlgorithm.SHA256"
                }else if (algorithm.progress==2){
                    algorithmLabel.text="SHA-512"
                    selectedAlgorithm="HmacAlgorithm.SHA512"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                if (algorithm.progress==0){
                    algorithmLabel.text="SHA-1"
                    selectedAlgorithm="HmacAlgorithm.SHA1"
                }else if (algorithm.progress==1){
                    algorithmLabel.text="SHA-256"
                    selectedAlgorithm="HmacAlgorithm.SHA256"
                }else if (algorithm.progress==2){
                    algorithmLabel.text="SHA-512"
                    selectedAlgorithm="HmacAlgorithm.SHA512"
                }
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (algorithm.progress==0){
                    algorithmLabel.text="SHA-1"
                    selectedAlgorithm="HmacAlgorithm.SHA1"
                }else if (algorithm.progress==1){
                    algorithmLabel.text="SHA-256"
                    selectedAlgorithm="HmacAlgorithm.SHA256"
                }else if (algorithm.progress==2){
                    algorithmLabel.text="SHA-512"
                    selectedAlgorithm="HmacAlgorithm.SHA512"
                }
            }
        })
    }
}
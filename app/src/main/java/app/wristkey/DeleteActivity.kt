package app.wristkey

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.support.wearable.activity.WearableActivity
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.wear.widget.BoxInsetLayout
import wristkey.R

class DeleteActivity : WearableActivity() {



    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete)
        val confirmationText = findViewById<TextView>(R.id.ConfirmationText)
        val confirmButton = findViewById<ImageButton>(R.id.AuthenticatorConfirmButton)
        val cancelButton = findViewById<ImageButton>(R.id.CancelButton)
        val boxInsetLayout = findViewById<BoxInsetLayout>(R.id.BoxInsetLayout)

        cancelButton.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
            finish()
        }

        val isDeleteAll = intent.getBooleanExtra("delete_all", false)
        val accountIDToDelete = intent.getStringExtra("account_id")

        if (isDeleteAll) {
            confirmationText.text = "Delete all accounts and app settings?"
            confirmButton.setOnClickListener {
                finish()
                Toast.makeText(this, "All items deleted!", Toast.LENGTH_SHORT).show()
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
                val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibratorService.vibrate(500)
            }
        } else {
            confirmButton.setOnClickListener {
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
                finish()
                Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show()
                val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibratorService.vibrate(500)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (!(resultCode == RESULT_OK && requestCode == CODE_AUTHENTICATION_VERIFICATION)) {
            finish()
        }
    }
}
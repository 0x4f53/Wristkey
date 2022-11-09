package app.wristkey
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Vibrator
import android.provider.Settings
import android.support.wearable.activity.WearableActivity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.wear.widget.BoxInsetLayout
import wristkey.R
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class ExportActivity : WearableActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export)
        val boxinsetlayout = findViewById<BoxInsetLayout>(R.id.BoxInsetLayout)
        val jsonFileExportLabel = findViewById<TextView>(R.id.BitwardenImportLabel)
        val jsonFileExportButton = findViewById<ImageView>(R.id.BitwardenImportButton)
        val fileExport = findViewById<LinearLayout>(R.id.BitwardenImport)
        val jsonQrCodeExportLabel = findViewById<TextView>(R.id.AuthenticatorImportLabel)
        val qrCodeExport = findViewById<LinearLayout>(R.id.AuthenticatorImport)
        val jsonQrCodeExportButton = findViewById<ImageView>(R.id.AuthenticatorImportButton)
        val backButton = findViewById<ImageView>(R.id.BackButton)

        fileExport.setOnClickListener {

            val sdf = SimpleDateFormat("yyyy-MM-dd'@'HH:mm:ss")
            val currentDateandTime: String = sdf.format(Date())
            val fileName = "$currentDateandTime.backup"

            try {
                val root = File(Environment.getExternalStorageDirectory(), "wristkey")
                if (!root.exists()) {
                    root.mkdirs()
                }
                val file = File(root, fileName)
                val writer = FileWriter(file)
                writer.flush()
                writer.close()
                Toast.makeText(this, "Exported successfully. Make sure to delete after use.", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                val toast = Toast.makeText(this, "Couldn't write to file. Disable and re-enable storage permission.", Toast.LENGTH_LONG)
                toast.show()

                val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                settingsIntent.data = uri
                startActivity(settingsIntent)

                val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibratorService.vibrate(50)
                finish()
            }

            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
            finish()
        }

        qrCodeExport.setOnClickListener {
            val intent = Intent(applicationContext, QRCodeActivity::class.java)
            startActivity(intent)
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
            finish()
        }

        backButton.setOnClickListener {
            val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibratorService.vibrate(50)
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (!(resultCode == RESULT_OK && requestCode == CODE_AUTHENTICATION_VERIFICATION)) {
            finish()
        }
    }
}
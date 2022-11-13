package app.wristkey


import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.wearable.intent.RemoteIntent
import wristkey.BuildConfig
import wristkey.R

class AboutActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        val backButton = findViewById<CardView>(R.id.backButton)
        val appNameText = findViewById<TextView>(R.id.AppName)
        val heart = findViewById<TextView>(R.id.heart)
        val versionText = findViewById<TextView>(R.id.Version)
        val bitcoinDonateQrCode = findViewById<ImageView>(R.id.bitcoinDonateQrCode)
        val urlLink = findViewById<TextView>(R.id.SourceCode)

        versionText.text = "v${BuildConfig.VERSION_NAME}"
        val uri: String = getString(R.string.about_url)

        heart.startAnimation(AnimationUtils.loadAnimation(this, R.anim.heartbeat))

        urlLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).addCategory(Intent.CATEGORY_BROWSABLE).setData(Uri.parse(uri))
            RemoteIntent.startRemoteActivity(this, intent, null)
            Toast.makeText(this, "URL opened\non phone", Toast.LENGTH_SHORT).show()
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                startActivity(browserIntent)
                val toast2 = Toast.makeText(this, "URL opened\nin browser", Toast.LENGTH_SHORT)
                toast2.show()
            } catch (ex: Exception) { }
        }

        backButton.setOnClickListener {
            finish()
        }
    }
}

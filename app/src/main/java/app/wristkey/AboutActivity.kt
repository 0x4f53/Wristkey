package app.wristkey


import android.annotation.SuppressLint
import android.content.Intent
import android.media.audiofx.HapticGenerator
import android.net.Uri
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.wear.widget.BoxInsetLayout
import com.google.android.wearable.intent.RemoteIntent
import wristkey.BuildConfig
import wristkey.R


class AboutActivity : WearableActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        val boxinsetlayout = findViewById<BoxInsetLayout>(R.id.BoxInsetLayout)
        val doneButton = findViewById<CardView>(R.id.DoneButton)
        val appNameText = findViewById<TextView>(R.id.AppName)
        val heart = findViewById<TextView>(R.id.heart)
        val versionText = findViewById<TextView>(R.id.Version)
        val descriptionText = findViewById<TextView>(R.id.AuthenticatorDescription)
        val urlLink = findViewById<TextView>(R.id.SourceCode)

        versionText.text = "v${BuildConfig.VERSION_NAME}"
        val uri: String = getString(R.string.about_url)

        heart.startAnimation(AnimationUtils.loadAnimation(this, R.anim.heartbeat))

        urlLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).addCategory(Intent.CATEGORY_BROWSABLE).setData(Uri.parse(uri))
            RemoteIntent.startRemoteActivity(this, intent, null)
            val toast = Toast.makeText(this, "URL opened\non phone", Toast.LENGTH_SHORT)
            toast.show()
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                startActivity(browserIntent)
                val toast2 = Toast.makeText(this, "URL opened\nin browser", Toast.LENGTH_SHORT)
                toast2.show()
            } catch (ex: Exception) { }
        }

        doneButton.setOnClickListener {
            doneButton.performHapticFeedback(HapticGenerator.SUCCESS)
            finish()
        }
    }
}

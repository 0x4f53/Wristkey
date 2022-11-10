package app.wristkey

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import wristkey.R

class DeleteActivity : WearableActivity() {

    lateinit var utilities: Utilities

    private lateinit var deleteLabel: TextView

    private lateinit var deleteButton: CardView
    private lateinit var deleteButtonLabel: TextView
    private lateinit var deleteButtonIcon: ImageView

    private lateinit var backButton: CardView

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate (savedInstanceState: Bundle?) {
        super.onCreate (savedInstanceState)
        setContentView (R.layout.activity_delete)

        utilities = Utilities (applicationContext)

        initializeUI()

        if (intent.getStringExtra(utilities.INTENT_DELETE_MODE) == utilities.INTENT_WIPE) {
            initializeForWipe()
        }

    }

    private fun initializeUI () {
        deleteLabel = findViewById (R.id.deleteLabel)

        deleteButton = findViewById (R.id.deleteButton)
        deleteButtonLabel = findViewById (R.id.deleteButtonLabel)
        deleteButtonIcon = findViewById (R.id.deleteButtonIcon)

        backButton = findViewById (R.id.backButton)

        backButton.setOnClickListener {
            finish()
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeForWipe () {

        deleteLabel.text = "Wipe Wristkey vault?\nThis cannot be undone."
        deleteButtonIcon.setImageDrawable(AppCompatResources.getDrawable(applicationContext, R.drawable.ic_outline_delete_forever_24))
        deleteButtonLabel.text = "Wipe vault"

        deleteButton.setOnClickListener {
            utilities.vault.edit().clear().apply()
            finish()
            finishAffinity()
            startActivity(Intent(applicationContext, MainActivity::class.java))
        }

    }

}
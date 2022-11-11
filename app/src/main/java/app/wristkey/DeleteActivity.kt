package app.wristkey

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import wristkey.R

class DeleteActivity : WearableActivity() {

    lateinit var utilities: Utilities

    private lateinit var deleteLabel: TextView
    private lateinit var deleteButton: CardView

    private lateinit var backButton: CardView

    private lateinit var uuid: String

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate (savedInstanceState: Bundle?) {
        super.onCreate (savedInstanceState)
        setContentView (R.layout.activity_delete)

        utilities = Utilities (applicationContext)

        initializeUI()

        if (intent.getStringExtra(utilities.INTENT_DELETE_MODE) == utilities.INTENT_WIPE) {
            initializeForWipe()
        }

        if (intent.hasExtra(utilities.INTENT_UUID)) {
            uuid = intent.getStringExtra(utilities.INTENT_UUID)!!
            initializeForDeletingLogin ()
        }

    }

    private fun initializeUI () {
        deleteLabel = findViewById (R.id.deleteLabel)

        deleteButton = findViewById (R.id.deleteButton)

        backButton = findViewById (R.id.backButton)

        backButton.setOnClickListener {
            finish()
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeForDeletingLogin () {
        var login = utilities.getLogin(uuid)
        var itemName = login?.issuer
        if (!login?.account.isNullOrEmpty()) itemName += " (${login?.account})"

        deleteLabel.text = "Would you like to delete \"$itemName\"?"

        deleteButton.setOnClickListener {
            utilities.deleteFromVault (uuid)
            finish()
            finishAffinity()
            startActivity(Intent(applicationContext, MainActivity::class.java))
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeForWipe () {

        deleteLabel.text = "Wipe Wristkey vault?\nThis cannot be undone."

        deleteButton.setOnClickListener {
            utilities.vault.edit().clear().apply()
            finish()
            finishAffinity()
            startActivity(Intent(applicationContext, MainActivity::class.java))
        }

    }

}
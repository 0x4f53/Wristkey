package app.wristkey

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import wristkey.R

class DeleteActivity : AppCompatActivity() {

    lateinit var utilities: Utilities

    private lateinit var deleteLabel: TextView
    private lateinit var deleteButton: ImageButton

    private lateinit var backButton: ImageButton

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

        val logins = utilities.getLogins().size
        val settings = utilities.vault.all.size - utilities.getLogins().size

        var deleteLabelString =
            if (logins == 1) "Wipe $logins login"
            else if (logins > 1) "Wipe $logins logins"
            else "Reset Wristkey"

        deleteLabelString +=
            if (settings == 1 && logins != 0) " and $settings setting"
            else if (settings > 1 && logins != 0) " and $settings settings"
            else if (settings > 1) if (!deleteLabelString.contains("Reset")) "Reset Wristkey" else ""
            else ""

        deleteLabelString += "?"

        deleteLabel.text = deleteLabelString
        deleteButton.setOnClickListener {
            utilities.vault.edit().clear().apply()
            finish()
            finishAffinity()
            startActivity(Intent(applicationContext, MainActivity::class.java))
        }

    }

}
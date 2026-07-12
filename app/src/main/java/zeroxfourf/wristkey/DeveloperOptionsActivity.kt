package zeroxfourf.wristkey

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.material3.*
import wristkey.R

class DeveloperOptionsActivity : AppCompatActivity() {

    private lateinit var utilities: Utilities
    private var dataGenerated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        utilities = Utilities(applicationContext)

        setContent {
            WristkeyM3Theme {
                AppScaffold(
                    timeText = { TimeText() }
                ) {
                    DeveloperOptionsScreen(
                        utilities = utilities,
                        onBackClick = { finish() },
                        onDataGenerated = { dataGenerated = true }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (dataGenerated) {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }
}

@Composable
fun DeveloperOptionsScreen(
    utilities: Utilities,
    onBackClick: () -> Unit,
    onDataGenerated: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    val context = LocalContext.current
    val amoled = LocalAmoledEnabled.current

    ScreenScaffold(
        scrollState = listState,
        edgeButton = {
            EdgeButton(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth().height(80.dp),
                border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text(
                    text = stringResource(R.string.back),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    ) {
        ScalingLazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                top = 30.dp,
                start = 10.dp,
                end = 10.dp,
                bottom = 84.dp
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            autoCentering = null
        ) {
            item {
                Text(
                    text = stringResource(R.string.developer_options),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                Button(
                    onClick = {
                        val services = listOf("Google", "GitHub", "Microsoft", "Amazon", "Discord", "Facebook", "Twitter", "Reddit", "LinkedIn", "Dropbox")
                        val fakeAccounts = mutableListOf<String>()
                        for (i in 1..25) {
                            val service = services.random()
                            val secret = utilities.randomString(16)
                            fakeAccounts.add("otpauth://totp/$service:user$i@example.com?secret=$secret&issuer=$service")
                        }
                        fakeAccounts.forEach { utilities.overwriteLogin(it) }
                        onDataGenerated()
                        Toast.makeText(context, context.getString(R.string.fake_accounts_generated), Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = CircleShape,
                    border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(
                        text = stringResource(R.string.generate_fake_accounts),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        maxLines = 2,
                        softWrap = true
                    )
                }
            }
        }
    }
}

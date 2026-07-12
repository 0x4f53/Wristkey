package zeroxfourf.wristkey

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.material3.*
import com.google.android.wearable.intent.RemoteIntent
import wristkey.R

class DonateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WristkeyM3Theme {
                AppScaffold(
                    timeText = { TimeText() }
                ) {
                    DonateScreen(
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun DonateScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val listState = rememberScalingLazyListState()
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
                    text = stringResource(R.string.donate_label),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            item {
                Text(
                    text = stringResource(R.string.donation_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp)
                )
            }

            item {
                Button(
                    onClick = {
                        val uri = context.getString(R.string.donation_uri)
                        val intent = Intent(Intent.ACTION_VIEW)
                            .addCategory(Intent.CATEGORY_BROWSABLE)
                            .setData(Uri.parse(uri))
                        RemoteIntent.startRemoteActivity(context, intent, null)
                        Toast.makeText(context, context.getString(R.string.opening_donation_page), Toast.LENGTH_SHORT).show()
                        try {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                            context.startActivity(browserIntent)
                        } catch (_: Exception) { }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = CircleShape,
                    border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(painterResource(R.drawable.outline_attach_money_24), contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(stringResource(R.string.donate_label), style = MaterialTheme.typography.labelMedium, maxLines = 2, softWrap = true)
                    }
                }
            }
        }
    }
}

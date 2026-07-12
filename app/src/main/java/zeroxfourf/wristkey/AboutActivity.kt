package zeroxfourf.wristkey

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.material3.*
import com.google.android.wearable.intent.RemoteIntent
import wristkey.R

class AboutActivity : AppCompatActivity() {

    private lateinit var utilities: Utilities

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        utilities = Utilities(applicationContext)

        setContent {
            WristkeyM3Theme {
                AppScaffold(
                    timeText = { TimeText() }
                ) {
                    AboutScreen(
                        utilities = utilities,
                        onDonateClick = {
                            startActivity(Intent(this@AboutActivity, DonateActivity::class.java))
                        },
                        onLicenseClick = {
                            val licenseDialog = CustomFullscreenDialogFragment(
                                title = getString(R.string.mit_license),
                                message = getString(R.string.copyright),
                                positiveButtonText = null,
                                positiveButtonIcon = null,
                                negativeButtonText = getString(R.string.back),
                                negativeButtonIcon = R.drawable.ic_prev,
                            )
                            licenseDialog.show(supportFragmentManager, "CustomFullscreenDialog")
                        },
                        onBackClick = {
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AboutScreen(
    utilities: Utilities,
    onDonateClick: () -> Unit,
    onLicenseClick: () -> Unit,
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_info_24),
                        contentDescription = stringResource(R.string.app_icon),
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = context.getString(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = utilities.appVersion(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            item {
                Text(
                    text = context.getString(R.string.about_app),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp)
                )
            }

            item {
                val uri = context.getString(R.string.about_url)
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW)
                            .addCategory(Intent.CATEGORY_BROWSABLE)
                            .setData(Uri.parse(uri))
                        RemoteIntent.startRemoteActivity(context, intent, null)
                        Toast.makeText(context, context.getString(R.string.open_repo_url), Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = CircleShape,
                    border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.source_code), style = MaterialTheme.typography.labelMedium, maxLines = 2, softWrap = true)
                        Text(uri, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    }
                }
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text(stringResource(R.string.with_text), style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    Text("❤️", color = Color.Red, style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.by_owais), style = MaterialTheme.typography.bodyMedium, color = Color.White)
                }
            }

            item {
                Button(
                    onClick = onDonateClick,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = CircleShape,
                    border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(painterResource(id = R.drawable.outline_attach_money_24), contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(stringResource(R.string.donate_label), style = MaterialTheme.typography.labelMedium, maxLines = 2, softWrap = true)
                    }
                }
            }

            item {
                Button(
                    onClick = onLicenseClick,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = CircleShape,
                    border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(painterResource(id = R.drawable.outline_gavel_24), contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(stringResource(R.string.view_license), style = MaterialTheme.typography.labelMedium, maxLines = 2, softWrap = true)
                    }
                }
            }
        }
    }
}

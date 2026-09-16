package zeroxfourf.wristkey

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.*
import wristkey.R

class QRCodeActivity : ComponentActivity() {

    private lateinit var utilities: Utilities

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        utilities = Utilities(applicationContext)

        val data = intent.getStringExtra(utilities.INTENT_QR_DATA) ?: ""
        val issuer = intent.getStringExtra(utilities.INTENT_QR_METADATA) ?: ""
        val account = intent.getStringExtra("INTENT_ACCOUNT") ?: ""
        val label = intent.getStringExtra("INTENT_LABEL") ?: ""

        setContent {
            WristkeyM3Theme {
                QRCodeScreen(
                    data = data,
                    issuer = issuer,
                    account = account,
                    label = label,
                    utilities = utilities,
                    onDismiss = {
                        setResult(Activity.RESULT_OK, Intent())
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun QRCodeScreen(
    data: String,
    issuer: String = "",
    account: String = "",
    label: String = "",
    utilities: Utilities,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val listState = rememberScalingLazyListState(
        initialCenterItemIndex = 0,
        initialCenterItemScrollOffset = 0
    )
    val amoled = remember { utilities.db.getBoolean(utilities.SETTINGS_AMOLED_ENABLED, false) }

    val totalSeconds = utilities.QR_TIMER_DURATION
    val totalDurationMillis = totalSeconds * 1000L
    val startTime = remember { System.currentTimeMillis() }
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        listState.scrollToItem(0, 0)
        while (true) {
            currentTime = System.currentTimeMillis()
            kotlinx.coroutines.delay(16) // 60 FPS smooth animation loop
        }
    }

    val elapsedMillis = (currentTime - startTime).coerceAtLeast(0L)
    val remainingMillis = (totalDurationMillis - elapsedMillis).coerceAtLeast(0L)

    LaunchedEffect(remainingMillis) {
        if (remainingMillis <= 0L) {
            onDismiss()
        }
    }

    val currentProgress = (remainingMillis.toFloat() / totalDurationMillis.toFloat()).coerceIn(0f, 1f)

    val activity = context as? Activity
    val qrBitmap = remember(data, activity) {
        if (activity != null && data.isNotBlank()) {
            val wm = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            utilities.generateQrCode(data, wm)
        } else null
    }

    var isDimmed by remember { mutableStateOf(false) }

    ScreenScaffold(
        scrollState = listState,
        timeText = { TimeText() },
        scrollIndicator = null,
        edgeButton = {
            EdgeButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp),
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
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(
                    top = 0.dp,
                    start = 12.dp,
                    end = 12.dp,
                    bottom = 84.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                autoCentering = AutoCenteringParams(itemIndex = 0)
            ) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "QR Code",
                                modifier = Modifier
                                    .size(138.dp)
                                    .clickable {
                                        isDimmed = !isDimmed
                                        val msg = if (isDimmed) context.getString(R.string.dimmed) else "Bright"
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    },
                                colorFilter = if (isDimmed) ColorFilter.tint(Color(0xFF818181), BlendMode.Modulate) else null
                            )
                        }
                        if (issuer.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = issuer,
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                                color = if (amoled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier = Modifier.basicMarquee()
                            )
                        }
                    }
                }
            }

            SpeedometerProgressIndicator(
                progress = currentProgress,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2f),
                activeColor = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                knobColor = Color.White
            )
        }
    }
}

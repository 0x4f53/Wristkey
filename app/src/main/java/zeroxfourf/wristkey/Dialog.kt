package zeroxfourf.wristkey

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.*
import wristkey.R

class CustomFullscreenDialogFragment(
    private val title: String,
    private val message: String,
    private val positiveButtonText: String?,
    private val positiveButtonIcon: Int?,
    private val negativeButtonText: String?,
    private val negativeButtonIcon: Int?
) : DialogFragment() {
    private var onPositiveClickListener: (() -> Unit)? = null

    fun setOnPositiveClickListener (listener: () -> Unit) {
        onPositiveClickListener = listener
    }

    private var onNegativeClickListener: (() -> Unit)? = null

    fun setOnNegativeClickListener (listener: () -> Unit) {
        onNegativeClickListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.Wristkey)
    }

    override fun onCreateView (inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                WristkeyM3Theme {
                    AppScaffold(
                        timeText = { TimeText() }
                    ) {
                        DialogScreen(
                            title = title,
                            message = message,
                            positiveButtonText = positiveButtonText,
                            positiveButtonIcon = positiveButtonIcon,
                            negativeButtonText = negativeButtonText,
                            negativeButtonIcon = negativeButtonIcon,
                            onPositiveClick = {
                                onPositiveClickListener?.invoke()
                                dismiss()
                            },
                            onNegativeClick = {
                                onNegativeClickListener?.invoke()
                                dismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DialogScreen(
    title: String,
    message: String,
    positiveButtonText: String?,
    positiveButtonIcon: Int?,
    negativeButtonText: String?,
    negativeButtonIcon: Int?,
    onPositiveClick: () -> Unit,
    onNegativeClick: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    val amoled = LocalAmoledEnabled.current

    ScreenScaffold(
        scrollState = listState,
        edgeButton = {
            if (negativeButtonText != null) {
                EdgeButton(
                    onClick = onNegativeClick,
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(
                        text = negativeButtonText,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        softWrap = false
                    )
                }
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
            autoCentering = null
        ) {
            item {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            item {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 8.dp)
                )
            }

            if (positiveButtonText != null) {
                item {
                    Button(
                        onClick = onPositiveClick,
                        modifier = Modifier.fillMaxWidth(),
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
                            if (positiveButtonIcon != null) {
                                Icon(painterResource(positiveButtonIcon), contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            Text(
                                text = positiveButtonText,
                                color = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

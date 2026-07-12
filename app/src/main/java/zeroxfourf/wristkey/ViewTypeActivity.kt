package zeroxfourf.wristkey

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.material3.*
import wristkey.R
import androidx.core.content.edit

class ViewTypeActivity : AppCompatActivity() {

    private lateinit var utilities: Utilities

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        utilities = Utilities(applicationContext)

        setContent {
            val themeColorInt = utilities.db.getInt(utilities.SETTINGS_ACCENT_COLOR, Color(0xFFA970FF).toArgb())
            val themeColor = Color(themeColorInt)
            val isAmoled = utilities.db.getBoolean(utilities.SETTINGS_AMOLED_ENABLED, false)

            WristkeyM3Theme(accentColor = themeColor, isAmoled = isAmoled) {
                AppScaffold(
                    timeText = { TimeText() }
                ) {
                    ViewTypeScreen(
                        utilities = utilities,
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun ViewTypeScreen(
    utilities: Utilities,
    onBackClick: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    val amoled = LocalAmoledEnabled.current
    
    var currentViewType by remember {
        mutableIntStateOf(utilities.db.getInt(utilities.SETTINGS_VIEW_TYPE, 0))
    }

    val themeColor = MaterialTheme.colorScheme.primary

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
                    text = stringResource(R.string.layout_type),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                RadioButton(
                    selected = currentViewType == 0,
                    onSelect = {
                        currentViewType = 0
                        utilities.db.edit { putInt(utilities.SETTINGS_VIEW_TYPE, 0) }
                        onBackClick()
                    },
                    label = { Text(stringResource(R.string.view_type_list), maxLines = 2, softWrap = true) },
                    icon = { Icon(painterResource(R.drawable.outline_lists_24), contentDescription = null, tint = Color.White) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp).then(
                        if (amoled) Modifier.border(BorderStroke(1.dp, if (currentViewType == 0) themeColor else Color.Gray), CircleShape) else Modifier
                    ),
                    colors = RadioButtonDefaults.radioButtonColors(
                        selectedContainerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                        selectedControlColor = themeColor,
                        selectedContentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                        unselectedContainerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.surfaceContainer,
                        unselectedContentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            item {
                RadioButton(
                    selected = currentViewType == 1,
                    onSelect = {
                        currentViewType = 1
                        utilities.db.edit { putInt(utilities.SETTINGS_VIEW_TYPE, 1) }
                        onBackClick()
                    },
                    label = { Text(stringResource(R.string.view_type_dash), maxLines = 2, softWrap = true) },
                    icon = { Icon(painterResource(R.drawable.outline_dashboard_24), contentDescription = null, tint = Color.White) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp).then(
                        if (amoled) Modifier.border(BorderStroke(1.dp, if (currentViewType == 1) themeColor else Color.Gray), CircleShape) else Modifier
                    ),
                    colors = RadioButtonDefaults.radioButtonColors(
                        selectedContainerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                        selectedControlColor = themeColor,
                        selectedContentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                        unselectedContainerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.surfaceContainer,
                        unselectedContentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            item {
                RadioButton(
                    selected = currentViewType == 2,
                    onSelect = {
                        currentViewType = 2
                        utilities.db.edit { putInt(utilities.SETTINGS_VIEW_TYPE, 2) }
                        onBackClick()
                    },
                    label = { Text(stringResource(R.string.view_type_bubbles), maxLines = 2, softWrap = true) },
                    icon = { Icon(painterResource(R.drawable.outline_bubbles), contentDescription = null, tint = Color.White) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp).then(
                        if (amoled) Modifier.border(BorderStroke(1.dp, if (currentViewType == 2) themeColor else Color.Gray), CircleShape) else Modifier
                    ),
                    colors = RadioButtonDefaults.radioButtonColors(
                        selectedContainerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                        selectedControlColor = themeColor,
                        selectedContentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                        unselectedContainerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.surfaceContainer,
                        unselectedContentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}

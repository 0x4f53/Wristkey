package zeroxfourf.wristkey

import android.app.KeyguardManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.material3.*
import wristkey.R
import androidx.core.content.edit

class SettingsActivity : AppCompatActivity() {

    private lateinit var utilities: Utilities
    private var settingsChanged by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        utilities = Utilities(applicationContext)

        setContent {
            val lifecycleOwner = LocalLifecycleOwner.current
            var themeColor by remember { 
                mutableStateOf(Color(utilities.db.getInt(utilities.SETTINGS_ACCENT_COLOR, Color(0xFFA970FF).toArgb())))
            }
            
            var isAmoled by remember {
                mutableStateOf(utilities.db.getBoolean(utilities.SETTINGS_AMOLED_ENABLED, false))
            }

            var viewType by remember {
                mutableIntStateOf(utilities.db.getInt(utilities.SETTINGS_VIEW_TYPE, 0))
            }

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        val newThemeColor = Color(utilities.db.getInt(utilities.SETTINGS_ACCENT_COLOR, Color(0xFFA970FF).toArgb()))
                        val newIsAmoled = utilities.db.getBoolean(utilities.SETTINGS_AMOLED_ENABLED, false)
                        val newViewType = utilities.db.getInt(utilities.SETTINGS_VIEW_TYPE, 0)

                        if (newThemeColor != themeColor || newIsAmoled != isAmoled || newViewType != viewType) {
                            settingsChanged = true
                        }

                        themeColor = newThemeColor
                        isAmoled = newIsAmoled
                        viewType = newViewType
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }
            
            WristkeyM3Theme(accentColor = themeColor, isAmoled = isAmoled) {
                AppScaffold(
                    timeText = { TimeText() }
                ) {
                    SettingsScreen(
                        utilities = utilities,
                        viewType = viewType,
                        onSettingChanged = { 
                            settingsChanged = true
                            themeColor = Color(utilities.db.getInt(utilities.SETTINGS_ACCENT_COLOR, Color(0xFFA970FF).toArgb()))
                            isAmoled = utilities.db.getBoolean(utilities.SETTINGS_AMOLED_ENABLED, false)
                            viewType = utilities.db.getInt(utilities.SETTINGS_VIEW_TYPE, 0)
                        },
                        onAboutClick = {
                            startActivity(Intent(this@SettingsActivity, AboutActivity::class.java))
                        },
                        onDeveloperOptionsClick = {
                            startActivity(Intent(this@SettingsActivity, DeveloperOptionsActivity::class.java))
                        },
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (settingsChanged) {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }
}

@Composable
fun ThemeColorCircle(
    color: Color,
    showRing: Boolean,
    modifier: Modifier = Modifier,
    scale: Float = 1f,
    onClick: () -> Unit
) {
    if (showRing) {
        Box(
            modifier = Modifier
                .offset(x = 0.dp, y = 39.dp)
                .size(38.dp)
                .border(2.dp, color, CircleShape)
        )
    }

    Box(
        modifier = modifier
            .size(36.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .background(color, CircleShape)
            .clickable(onClick = onClick)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(
    utilities: Utilities,
    viewType: Int,
    onSettingChanged: () -> Unit,
    onAboutClick: () -> Unit,
    onDeveloperOptionsClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    val context = LocalContext.current
    val lockscreen = context.getSystemService(AppCompatActivity.KEYGUARD_SERVICE) as KeyguardManager

    var lockEnabled by remember { mutableStateOf(utilities.db.getBoolean(utilities.SETTINGS_LOCK_ENABLED, false)) }
    var amoledEnabled by remember { mutableStateOf(utilities.db.getBoolean(utilities.SETTINGS_AMOLED_ENABLED, false)) }
    var recentsEnabled by remember { mutableStateOf(utilities.db.getBoolean(utilities.SETTINGS_RECENTS_ENABLED, false)) }
    
    val themeColors = listOf(
        Color(0xFFA970FF), // Lavender
        Color(0xFF92CCFF), // Sky Blue
        Color(0xFFACD370), // Mint Green
        Color(0xFFFFB945), // Orange
        Color(0xFFFFD965), // Yellow
        Color(0xFFFFB2BE), // Rose
        Color(0xFFFFFFFF), // White
    )
    
    var currentThemeColor by remember { 
        val colorInt = utilities.db.getInt(utilities.SETTINGS_ACCENT_COLOR, themeColors[0].toArgb())
        mutableStateOf(Color(colorInt))
    }

    var orderedColors by remember {
        val initialOrder = themeColors.toMutableList()
        val savedColorInt = utilities.db.getInt(utilities.SETTINGS_ACCENT_COLOR, themeColors[0].toArgb())
        val savedColor = Color(savedColorInt)
        val currentIndex = initialOrder.indexOfFirst { it.toArgb() == savedColor.toArgb() }
        if (currentIndex != -1 && currentIndex != 3) {
            val temp = initialOrder[3]
            initialOrder[3] = initialOrder[currentIndex]
            initialOrder[currentIndex] = temp
        }
        mutableStateOf(initialOrder.toList())
    }

    val amoled = LocalAmoledEnabled.current
    val cardBorder = if (amoled) BorderStroke(1.dp, currentThemeColor) else null

    val switchColors = SwitchButtonDefaults.switchButtonColors(
        checkedContainerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
        checkedContentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
        uncheckedContainerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.surfaceContainer,
        uncheckedContentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onSurface,
        checkedThumbColor = if (amoled) Color.White else MaterialTheme.colorScheme.primary,
        checkedThumbIconColor = if (amoled) Color.Black else MaterialTheme.colorScheme.onPrimary,
        checkedTrackColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
        checkedTrackBorderColor = if (amoled) MaterialTheme.colorScheme.primary else Color.Transparent,
        checkedIconColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
        uncheckedIconColor = if (amoled) Color.White else MaterialTheme.colorScheme.onSurface
    )

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
                    text = stringResource(R.string.settings_label),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                val viewTypeIcon = when (viewType) {
                    1 -> R.drawable.outline_dashboard_24
                    2 -> R.drawable.outline_bubbles
                    else -> R.drawable.outline_lists_24
                }
                Button(
                    onClick = {
                        context.startActivity(Intent(context, ViewTypeActivity::class.java))
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                    shape = CircleShape,
                    border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    icon = { Icon(painterResource(viewTypeIcon), contentDescription = null, tint = Color.White) },
                    label = { Text(stringResource(R.string.layout_type), maxLines = 2, softWrap = true) }
                )
            }

            item {
                SwitchButton(
                    checked = recentsEnabled,
                    onCheckedChange = {
                        recentsEnabled = it
                        utilities.db.edit { putBoolean(utilities.SETTINGS_RECENTS_ENABLED, it) }
                        onSettingChanged()
                    },
                    label = { Text(stringResource(R.string.favorites_section), maxLines = 2, softWrap = true) },
                    icon = { Icon(painterResource(R.drawable.outline_bookmark_24), contentDescription = null, tint = Color.White) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp).then(
                        if (amoled) Modifier.border(BorderStroke(1.dp, if (recentsEnabled) currentThemeColor else Color.Gray), CircleShape) else Modifier
                    ),
                    colors = switchColors
                )
            }

            item {
                Card(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    border = cardBorder,
                    colors = CardDefaults.cardColors(
                        containerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_outline_palette_24),
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Theme", 
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Box(
                            modifier = Modifier.fillMaxWidth().height(125.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            themeColors.forEach { color ->
                                val index = orderedColors.indexOf(color)
                                val xPos = when (index) {
                                    0 -> (-22).dp
                                    1 -> 22.dp
                                    2 -> (-44).dp
                                    3 -> 0.dp
                                    4 -> 44.dp
                                    5 -> (-22).dp
                                    6 -> 22.dp
                                    else -> 0.dp
                                }
                                val yPos = when (index) {
                                    0, 1 -> 0.dp
                                    2, 3, 4 -> 40.dp
                                    5, 6 -> 80.dp
                                    else -> 0.dp
                                }

                                val animX by animateDpAsState(
                                    targetValue = xPos,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    label = "x"
                                )
                                val animY by animateDpAsState(
                                    targetValue = yPos,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    label = "y"
                                )

                                val targetScale = if (index == 3) 0.8f else 1f
                                val animScale by animateFloatAsState(
                                    targetValue = targetScale,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    label = "scale"
                                )

                                ThemeColorCircle(
                                    modifier = Modifier.offset(x = animX, y = animY),
                                    color = color,
                                    showRing = index == 3,
                                    scale = animScale,
                                    onClick = {
                                        currentThemeColor = color
                                        utilities.db.edit {
                                            putInt(
                                                utilities.SETTINGS_ACCENT_COLOR,
                                                color.toArgb()
                                            )
                                        }
                                        onSettingChanged()

                                        val newList = orderedColors.toMutableList()
                                        val clickedIndex = newList.indexOf(color)
                                        val centerIndex = 3
                                        if (clickedIndex != centerIndex) {
                                            val temp = newList[centerIndex]
                                            newList[centerIndex] = newList[clickedIndex]
                                            newList[clickedIndex] = temp
                                            orderedColors = newList.toList()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (lockscreen.isDeviceSecure) {
                item {
                    SwitchButton(
                        checked = lockEnabled,
                        onCheckedChange = {
                            lockEnabled = it
                            utilities.db.edit {
                                putBoolean(
                                    utilities.SETTINGS_LOCK_ENABLED,
                                    it
                                )
                            }
                            onSettingChanged()
                        },
                        label = { Text(stringResource(R.string.security_lock), maxLines = 2, softWrap = true) },
                        icon = { Icon(painterResource(R.drawable.ic_outline_lock_24), contentDescription = null, tint = Color.White) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp).then(
                            if (amoled) Modifier.border(BorderStroke(1.dp, if (lockEnabled) currentThemeColor else Color.Gray), CircleShape) else Modifier
                        ),
                        colors = switchColors
                    )
                }
            }

            item {
                SwitchButton(
                    checked = amoledEnabled,
                    onCheckedChange = {
                        amoledEnabled = it
                        utilities.db.edit { putBoolean(utilities.SETTINGS_AMOLED_ENABLED, it) }
                        onSettingChanged()
                    },
                    label = { Text("AMOLED Mode", maxLines = 2, softWrap = true) },
                    icon = { Icon(painterResource(R.drawable.baseline_dark_mode_24), contentDescription = null, tint = Color.White) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp).then(
                        if (amoled) Modifier.border(BorderStroke(1.dp, if (amoledEnabled) currentThemeColor else Color.Gray), CircleShape) else Modifier
                    ),
                    colors = switchColors
                )
            }

            item {
                Button(
                    onClick = onAboutClick,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                    shape = CircleShape,
                    border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    icon = { Icon(painterResource(R.drawable.outline_info_24), contentDescription = null, tint = Color.White) },
                    label = { Text(stringResource(R.string.about_wristkey), maxLines = 2, softWrap = true) }
                )
            }

            item {
                Button(
                    onClick = onDeveloperOptionsClick,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                    shape = CircleShape,
                    border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    icon = { Icon(painterResource(R.drawable.baseline_data_array_24), contentDescription = null, tint = Color.White) },
                    label = { Text(stringResource(R.string.developer_options), maxLines = 2, softWrap = true) }
                )
            }

        }
    }
}

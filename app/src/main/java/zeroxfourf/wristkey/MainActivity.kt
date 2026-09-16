package zeroxfourf.wristkey

import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.graphics.TransformOrigin
import android.widget.Toast
import android.app.KeyguardManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.util.lerp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState as rememberVerticalPagerState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.HorizontalPageIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ProgressIndicatorDefaults
import androidx.wear.compose.material3.RadioButton
import androidx.wear.compose.material3.RadioButtonDefaults
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Slider
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.SimpleIcons
import compose.icons.simpleicons.*
import androidx.wear.compose.material.SwipeToDismissBox
import androidx.wear.compose.material.rememberSwipeToDismissBoxState
import wristkey.R
import kotlin.math.abs
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var utilities: Utilities
    private var unlocked: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        utilities = Utilities(applicationContext)

        if (utilities.db.getBoolean(utilities.SETTINGS_LOCK_ENABLED, false)) {
            val lockscreen = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            if (lockscreen.isKeyguardSecure) {
                val i = lockscreen.createConfirmDeviceCredentialIntent(getString(R.string.app_name), getString(R.string.app_locked))
                startActivityForResult(i, CODE_AUTHENTICATION_VERIFICATION)
            } else {
                startComposeUI()
            }
        } else {
            startComposeUI()
        }
    }

    private fun startComposeUI() {
        setContent {
            WristkeyM3Theme {
                var selectedLogin by remember { mutableStateOf<Utilities.MfaCode?>(null) }
                var dataVersion by remember { mutableIntStateOf(0) }

                fun refreshData() {
                    dataVersion++
                    val currentSelected = selectedLogin
                    if (currentSelected != null) {
                        val allLogins = utilities.getData().otpauth.mapNotNull { utilities.decodeOtpAuthURL(it) }
                        val fresh = allLogins.find {
                            it.secret.equals(currentSelected.secret, ignoreCase = true) ||
                            (it.issuer == currentSelected.issuer && it.account == currentSelected.account)
                        }
                        if (fresh != null) {
                            selectedLogin = fresh
                        }
                    }
                }

                AppScaffold(
                    timeText = { TimeText() }
                ) {
                    MainScreen(
                        utilities = utilities,
                        selectedLogin = selectedLogin,
                        dataVersion = dataVersion,
                        onDataChanged = { refreshData() },
                        onLoginSelected = { selectedLogin = it },
                        onAddClick = {
                            startActivity(Intent(this@MainActivity, AddActivity::class.java))
                        },
                        onSettingsClick = {
                            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                        }
                    )
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CODE_AUTHENTICATION_VERIFICATION) {
            if (resultCode == RESULT_OK) {
                unlocked = true
                startComposeUI()
            } else {
                finish()
            }
        }
    }

    companion object {
        const val CODE_AUTHENTICATION_VERIFICATION = 241
    }
}

val Cookie6SidedShape = GenericShape { size, _ ->
    val cx = size.width / 2f
    val cy = size.height / 2f
    val rOut = minOf(size.width, size.height) / 2f
    val rIn = rOut * 0.84f
    val numLobes = 6
    val numPoints = 180

    for (i in 0..numPoints) {
        val angle = (i * 2.0 * Math.PI / numPoints).toFloat()
        val normCos = Math.pow((kotlin.math.cos(numLobes * angle) + 1.0) / 2.0, 0.75).toFloat()
        val r = rIn + (rOut - rIn) * normCos
        val x = cx + r * kotlin.math.cos(angle)
        val y = cy + r * kotlin.math.sin(angle)
        if (i == 0) {
            moveTo(x, y)
        } else {
            lineTo(x, y)
        }
    }
    close()
}

val Cookie4SidedShape = GenericShape { size, _ ->
    val cx = size.width / 2f
    val cy = size.height / 2f
    val rOut = minOf(size.width, size.height) / 2f
    val rIn = rOut * 0.78f
    val numLobes = 4
    val numPoints = 180

    for (i in 0..numPoints) {
        val angle = (i * 2.0 * Math.PI / numPoints).toFloat()
        val normCos = Math.pow((kotlin.math.cos(numLobes * angle) + 1.0) / 2.0, 0.7).toFloat()
        val r = rIn + (rOut - rIn) * normCos
        val x = cx + r * kotlin.math.cos(angle)
        val y = cy + r * kotlin.math.sin(angle)
        if (i == 0) {
            moveTo(x, y)
        } else {
            lineTo(x, y)
        }
    }
    close()
}

val PebbleBlobShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val path = Path()
    path.moveTo(w * 0.45f, 0f)
    path.cubicTo(w * 0.85f, 0f, w, h * 0.25f, w, h * 0.60f)
    path.cubicTo(w, h * 0.92f, w * 0.65f, h, w * 0.35f, h)
    path.cubicTo(0f, h, 0f, h * 0.70f, 0f, h * 0.35f)
    path.cubicTo(0f, 0f, w * 0.15f, 0f, w * 0.45f, 0f)
    path.close()
    addPath(path)
}

val RoundedHexagonShape = GenericShape { size, _ ->
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r = minOf(size.width, size.height) / 2f
    val numPoints = 6
    val cornerRadius = r * 0.18f

    val path = Path()
    val points = Array(numPoints) { i ->
        val angle = Math.toRadians((i * 360.0 / numPoints).toDouble())
        Offset(cx + r * kotlin.math.cos(angle).toFloat(), cy + r * kotlin.math.sin(angle).toFloat())
    }

    for (i in 0 until numPoints) {
        val pPrev = points[(i + numPoints - 1) % numPoints]
        val pCurr = points[i]
        val pNext = points[(i + 1) % numPoints]

        val v1 = Offset(pPrev.x - pCurr.x, pPrev.y - pCurr.y)
        val v2 = Offset(pNext.x - pCurr.x, pNext.y - pCurr.y)

        val l1 = kotlin.math.hypot(v1.x, v1.y)
        val l2 = kotlin.math.hypot(v2.x, v2.y)

        val pStart = Offset(pCurr.x + (v1.x / l1) * cornerRadius, pCurr.y + (v1.y / l1) * cornerRadius)
        val pEnd = Offset(pCurr.x + (v2.x / l2) * cornerRadius, pCurr.y + (v2.y / l2) * cornerRadius)

        if (i == 0) {
            path.moveTo(pStart.x, pStart.y)
        } else {
            path.lineTo(pStart.x, pStart.y)
        }
        path.quadraticTo(pCurr.x, pCurr.y, pEnd.x, pEnd.y)
    }
    path.close()
    addPath(path)
}

val RoundedPentagonShape = GenericShape { size, _ ->
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r = minOf(size.width, size.height) / 2f
    val numPoints = 5
    val cornerRadius = r * 0.22f

    val path = Path()
    val points = Array(numPoints) { i ->
        val angle = Math.toRadians((i * 360.0 / numPoints - 90.0).toDouble())
        Offset(cx + r * kotlin.math.cos(angle).toFloat(), cy + r * kotlin.math.sin(angle).toFloat())
    }

    for (i in 0 until numPoints) {
        val pPrev = points[(i + numPoints - 1) % numPoints]
        val pCurr = points[i]
        val pNext = points[(i + 1) % numPoints]

        val v1 = Offset(pPrev.x - pCurr.x, pPrev.y - pCurr.y)
        val v2 = Offset(pNext.x - pCurr.x, pNext.y - pCurr.y)

        val l1 = kotlin.math.hypot(v1.x, v1.y)
        val l2 = kotlin.math.hypot(v2.x, v2.y)

        val pStart = Offset(pCurr.x + (v1.x / l1) * cornerRadius, pCurr.y + (v1.y / l1) * cornerRadius)
        val pEnd = Offset(pCurr.x + (v2.x / l2) * cornerRadius, pCurr.y + (v2.y / l2) * cornerRadius)

        if (i == 0) {
            path.moveTo(pStart.x, pStart.y)
        } else {
            path.lineTo(pStart.x, pStart.y)
        }
        path.quadraticTo(pCurr.x, pCurr.y, pEnd.x, pEnd.y)
    }
    path.close()
    addPath(path)
}

val ParallelogramShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val skew = w * 0.12f
    val r = h * 0.28f

    val path = Path()
    val numPoints = 4
    val points = arrayOf(
        Offset(skew, 0f),
        Offset(w, 0f),
        Offset(w - skew, h),
        Offset(0f, h)
    )

    for (i in 0 until numPoints) {
        val pPrev = points[(i + numPoints - 1) % numPoints]
        val pCurr = points[i]
        val pNext = points[(i + 1) % numPoints]

        val v1 = Offset(pPrev.x - pCurr.x, pPrev.y - pCurr.y)
        val v2 = Offset(pNext.x - pCurr.x, pNext.y - pCurr.y)

        val l1 = kotlin.math.hypot(v1.x, v1.y)
        val l2 = kotlin.math.hypot(v2.x, v2.y)

        val pStart = Offset(pCurr.x + (v1.x / l1) * r, pCurr.y + (v1.y / l1) * r)
        val pEnd = Offset(pCurr.x + (v2.x / l2) * r, pCurr.y + (v2.y / l2) * r)

        if (i == 0) {
            path.moveTo(pStart.x, pStart.y)
        } else {
            path.lineTo(pStart.x, pStart.y)
        }
        path.quadraticTo(pCurr.x, pCurr.y, pEnd.x, pEnd.y)
    }
    path.close()
    addPath(path)
}

val RoundedTriangleShape = GenericShape { size, _ ->
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r = minOf(size.width, size.height) / 2f
    val numPoints = 3
    val cornerRadius = r * 0.28f

    val path = Path()
    val points = Array(numPoints) { i ->
        val angle = Math.toRadians((i * 360.0 / numPoints - 90.0).toDouble())
        Offset(cx + r * kotlin.math.cos(angle).toFloat(), cy + r * kotlin.math.sin(angle).toFloat())
    }

    for (i in 0 until numPoints) {
        val pPrev = points[(i + numPoints - 1) % numPoints]
        val pCurr = points[i]
        val pNext = points[(i + 1) % numPoints]

        val v1 = Offset(pPrev.x - pCurr.x, pPrev.y - pCurr.y)
        val v2 = Offset(pNext.x - pCurr.x, pNext.y - pCurr.y)

        val l1 = kotlin.math.hypot(v1.x, v1.y)
        val l2 = kotlin.math.hypot(v2.x, v2.y)

        val pStart = Offset(pCurr.x + (v1.x / l1) * cornerRadius, pCurr.y + (v1.y / l1) * cornerRadius)
        val pEnd = Offset(pCurr.x + (v2.x / l2) * cornerRadius, pCurr.y + (v2.y / l2) * cornerRadius)

        if (i == 0) {
            path.moveTo(pStart.x, pStart.y)
        } else {
            path.lineTo(pStart.x, pStart.y)
        }
        path.quadraticTo(pCurr.x, pCurr.y, pEnd.x, pEnd.y)
    }
    path.close()
    addPath(path)
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    amoled: Boolean,
    modifier: Modifier = Modifier
) {
    val themeColor = MaterialTheme.colorScheme.primary
    val containerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.surfaceContainer
    val contentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onSurface

    var itemYInWindow by remember { mutableFloatStateOf(0f) }
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val screenRadius = screenHeightPx / 2f

    val dynamicHorizontalPadding by remember {
        derivedStateOf {
            if (screenRadius > 0f && itemYInWindow > 0f) {
                val dy = kotlin.math.abs(itemYInWindow - screenRadius)
                val normalizedDy = (dy / screenRadius).coerceIn(0f, 1f)
                val widthFactor = kotlin.math.sqrt(1f - normalizedDy * normalizedDy)
                val availableWidthDp = screenWidthDp * widthFactor
                val requiredPaddingDp = (screenWidthDp - availableWidthDp) / 2f
                requiredPaddingDp.coerceAtLeast(0.dp)
            } else {
                0.dp
            }
        }
    }

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                itemYInWindow = coordinates.boundsInWindow().center.y
            }
            .padding(horizontal = dynamicHorizontalPadding)
            .fillMaxWidth()
            .height(40.dp)
            .background(containerColor, CircleShape)
            .border(
                if (amoled) BorderStroke(1.dp, themeColor.copy(alpha = 0.5f)) else BorderStroke(0.dp, Color.Transparent),
                CircleShape
            )
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_baseline_search_24),
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(R.string.search_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor.copy(alpha = 0.5f),
                        maxLines = 1
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = contentColor),
                    singleLine = true,
                    cursorBrush = SolidColor(themeColor),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (query.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onQueryChange("") },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✕",
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    utilities: Utilities,
    selectedLogin: Utilities.MfaCode?,
    dataVersion: Int,
    onDataChanged: () -> Unit,
    onLoginSelected: (Utilities.MfaCode?) -> Unit,
    onAddClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    val logins = remember(dataVersion) {
        val allLogins = utilities.getData().otpauth.mapNotNull { utilities.decodeOtpAuthURL(it) }
        val favoritesEnabled = utilities.db.getBoolean(utilities.SETTINGS_RECENTS_ENABLED, true)
        if (favoritesEnabled) {
            allLogins.sortedByDescending { utilities.db.getBoolean("FAVORITE_${it.secret}", false) }
        } else {
            allLogins
        }
    }
    
    var viewType by remember {
        mutableIntStateOf(utilities.db.getInt(utilities.SETTINGS_VIEW_TYPE, 0))
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewType = utilities.db.getInt(utilities.SETTINGS_VIEW_TYPE, 0)
                onDataChanged()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedLoginBounds by remember { mutableStateOf<Rect?>(null) }

    val filteredLogins = if (searchQuery.isEmpty()) {
        logins
    } else {
        logins.filter {
            ("${it.issuer} ${it.account} ${it.label}").lowercase().contains(searchQuery.lowercase())
        }
    }

    val amoled = LocalAmoledEnabled.current
    val density = LocalDensity.current
    val bubbleOffsetPx = with(density) { 50.dp.roundToPx() }

    val hasSearchBar = logins.size > 5 || searchQuery.isNotEmpty()
    val firstAccountIndex = if (hasSearchBar) 1 else 0

    val rotation by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            if (layoutInfo.visibleItemsInfo.isNotEmpty()) {
                try {
                    (listState.centerItemIndex * 89f + listState.centerItemScrollOffset / 2f)
                } catch (_: Exception) {
                    0f
                }
            } else {
                0f
            }
        }
    }

    ScreenScaffold(
        scrollState = listState,
        timeText = { TimeText() },
        edgeButton = {
            EdgeButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp),
                border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.settings_label),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Icon(
                        painter = painterResource(R.drawable.outline_settings_24),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer(rotationZ = rotation),
                        tint = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    ) {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            state = listState,
            contentPadding = PaddingValues(
                top = 44.dp,
                start = 10.dp,
                end = 10.dp,
                bottom = 52.dp
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            autoCentering = if (viewType == 2) {
                AutoCenteringParams(itemIndex = firstAccountIndex, itemOffset = bubbleOffsetPx)
            } else {
                AutoCenteringParams(itemIndex = firstAccountIndex)
            },
            scalingParams = if (viewType == 2) ScalingLazyColumnDefaults.scalingParams(
                edgeScale = 0.20f,
                edgeAlpha = 0.2f,
                minTransitionArea = 0.35f,
                maxTransitionArea = 0.65f
            ) else ScalingLazyColumnDefaults.scalingParams(
                edgeScale = 0.45f,
                edgeAlpha = 0.4f,
                minTransitionArea = 0.35f,
                maxTransitionArea = 0.65f
            )
        ) {
            if (hasSearchBar) {
                item {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        amoled = amoled,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            if (filteredLogins.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.vault_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                when (viewType) {
                    1 -> {
                        items(filteredLogins.chunked(2)) { chunk ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                chunk.forEach { login ->
                                    DashLoginCard(
                                        login = login,
                                        amoled = amoled,
                                        onLoginClick = { item, bounds ->
                                            selectedLoginBounds = bounds
                                            onLoginSelected(item)
                                        },
                                        modifier = Modifier.weight(1f),
                                        utilities = utilities,
                                        dataVersion = dataVersion
                                    )
                                }
                                if (chunk.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                    2 -> {
                        items(filteredLogins.chunked(3)) { chunk ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
                            ) {
                                chunk.forEach { login ->
                                    BubbleLoginCard(
                                        login = login,
                                        amoled = amoled,
                                        onLoginClick = { item, bounds ->
                                            selectedLoginBounds = bounds
                                            onLoginSelected(item)
                                        },
                                        utilities = utilities,
                                        dataVersion = dataVersion
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        items(filteredLogins) { login ->
                            ListLoginCard(
                                login = login,
                                amoled = amoled,
                                onLoginClick = { item, bounds ->
                                    selectedLoginBounds = bounds
                                    onLoginSelected(item)
                                },
                                utilities = utilities,
                                dataVersion = dataVersion
                            )
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(1.dp)
                            .background(
                                color = if (amoled) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onAddClick,
                        modifier = Modifier
                            .size(96.dp)
                            .graphicsLayer(rotationZ = rotation),
                        shape = Cookie6SidedShape,
                        border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                            contentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer(rotationZ = -rotation)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_baseline_add_24),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = Color.White
                            )
                            Text(
                                text = "Add",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    if (selectedLogin != null) {
        val initialIndex = filteredLogins.indexOf(selectedLogin)
        SharedElementDetailOverlay(
            selectedLogin = selectedLogin,
            startBounds = selectedLoginBounds,
            filteredLogins = filteredLogins,
            initialIndex = if (initialIndex != -1) initialIndex else 0,
            utilities = utilities,
            amoled = amoled,
            onDataChanged = onDataChanged,
            onDismiss = { onLoginSelected(null) },
            dataVersion = dataVersion
        )
    }
}

@Composable
fun SharedElementDetailOverlay(
    selectedLogin: Utilities.MfaCode,
    startBounds: Rect?,
    filteredLogins: List<Utilities.MfaCode>,
    initialIndex: Int,
    utilities: Utilities,
    amoled: Boolean,
    onDataChanged: () -> Unit,
    onDismiss: () -> Unit,
    dataVersion: Int = 0
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    val validStartBounds = remember(startBounds, screenWidthPx, screenHeightPx) {
        if (startBounds != null && startBounds.width > 0f && startBounds.height > 0f && startBounds.width < screenWidthPx * 2f && startBounds.height < screenHeightPx * 2f) {
            startBounds
        } else {
            Rect(
                left = screenWidthPx * 0.15f,
                top = screenHeightPx * 0.15f,
                right = screenWidthPx * 0.85f,
                bottom = screenHeightPx * 0.85f
            )
        }
    }

    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(selectedLogin) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    val handleDismiss: () -> Unit = {
        coroutineScope.launch {
            animProgress.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            onDismiss()
        }
    }

    val t = animProgress.value

    if (t > 0.01f) {
        val currentLeft = lerp(validStartBounds.left, 0f, t).coerceIn(-screenWidthPx, screenWidthPx * 2f)
        val currentTop = lerp(validStartBounds.top, 0f, t).coerceIn(-screenHeightPx, screenHeightPx * 2f)
        val currentWidth = lerp(validStartBounds.width, screenWidthPx, t).coerceIn(1f, screenWidthPx * 1.5f)
        val currentHeight = lerp(validStartBounds.height, screenHeightPx, t).coerceIn(1f, screenHeightPx * 1.5f)
        val currentCornerRadius = lerp(24f, 0f, t).coerceAtLeast(0f).dp

        val cardBg = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer
        val overlayBg = if (amoled) Color.Black else MaterialTheme.colorScheme.background
        val currentBgColor = lerp(cardBg, overlayBg, t.coerceIn(0f, 1f))
        val contentAlpha = ((t - 0.15f) / 0.85f).coerceIn(0f, 1f)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = t.coerceIn(0f, 1f) }
                .background(Color.Black.copy(alpha = (t * 0.95f).coerceIn(0f, 0.95f)))
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(currentLeft.roundToInt(), currentTop.roundToInt()) }
                    .size(
                        width = with(density) { currentWidth.toDp() },
                        height = with(density) { currentHeight.toDp() }
                    )
                    .clip(RoundedCornerShape(currentCornerRadius))
                    .background(currentBgColor)
            ) {
                if (t > 0.15f) {
                    Box(modifier = Modifier.graphicsLayer { alpha = contentAlpha }) {
                        LoginDetailOverlay(
                            logins = filteredLogins,
                            initialIndex = initialIndex,
                            utilities = utilities,
                            amoled = amoled,
                            onDataChanged = onDataChanged,
                            onDismiss = handleDismiss,
                            dataVersion = dataVersion
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoginDetailOverlay(
    logins: List<Utilities.MfaCode>,
    initialIndex: Int,
    utilities: Utilities,
    amoled: Boolean,
    onDataChanged: () -> Unit,
    onDismiss: () -> Unit,
    dataVersion: Int = 0
) {
    // Infinite scroll logic: use a very large number of pages and map to actual logins list
    val totalPages = Int.MAX_VALUE
    val initialPage = totalPages / 2 - (totalPages / 2 % logins.size) + initialIndex
    
    val verticalPagerState = rememberVerticalPagerState(
        initialPage = initialPage,
        pageCount = { totalPages }
    )

    var verticalScrollEnabled by remember { mutableStateOf(true) }

    val swipeState = rememberSwipeToDismissBoxState()

    SwipeToDismissBox(
        state = swipeState,
    ) { isBackground ->
        LaunchedEffect(swipeState.currentValue) {
            if (swipeState.currentValue == androidx.wear.compose.material.SwipeToDismissValue.Dismissed) {
                onDismiss()
            }
        }

        if (!isBackground) {
            VerticalPager(
                state = verticalPagerState,
                userScrollEnabled = verticalScrollEnabled,
                modifier = Modifier.fillMaxSize()
            ) { verticalPage ->
                val actualIndex = ((verticalPage % logins.size) + logins.size) % logins.size
                LoginDetailContent(
                    login = logins[actualIndex],
                    utilities = utilities,
                    amoled = amoled,
                    onDismiss = onDismiss,
                    onDataChanged = onDataChanged,
                    dataVersion = dataVersion,
                    onHorizontalPageChanged = { horizontalPage ->
                        if (verticalPagerState.currentPage == verticalPage) {
                            verticalScrollEnabled = horizontalPage == 0
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SpeedometerProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
    knobColor: Color = Color.White
) {
    val strokeWidthDp = 6.dp
    val knobRadiusDp = 4.5.dp

    Canvas(modifier = modifier) {
        val strokeWidthPx = strokeWidthDp.toPx()
        val knobRadiusPx = knobRadiusDp.toPx()
        val diameter = minOf(size.width, size.height) - strokeWidthPx - 4.dp.toPx()
        val radius = diameter / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        val startAngle = 135f
        val totalSweep = 270f
        val clampedProgress = progress.coerceIn(0f, 1f)
        val activeSweep = totalSweep * clampedProgress

        val topLeft = Offset(center.x - radius, center.y - radius)
        val arcSize = Size(diameter, diameter)

        // 1. Draw Track Arc
        drawArc(
            color = trackColor,
            startAngle = startAngle,
            sweepAngle = totalSweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
        )

        // 2. Draw Active Progress Arc
        if (activeSweep > 0f) {
            drawArc(
                color = activeColor,
                startAngle = startAngle,
                sweepAngle = activeSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }

        // 3. Draw Indicator Knob & Uniformly Diffused Shadow at tip
        val tipAngleRad = Math.toRadians((startAngle + activeSweep).toDouble())
        val tipX = center.x + radius * kotlin.math.cos(tipAngleRad).toFloat()
        val tipY = center.y + radius * kotlin.math.sin(tipAngleRad).toFloat()

        // Concentric radial shadow rings for uniform 360-degree diffusion
        drawCircle(
            color = Color.Black.copy(alpha = 0.15f),
            radius = knobRadiusPx + 4.dp.toPx(),
            center = Offset(tipX, tipY)
        )
        drawCircle(
            color = Color.Black.copy(alpha = 0.30f),
            radius = knobRadiusPx + 2.5.dp.toPx(),
            center = Offset(tipX, tipY)
        )
        drawCircle(
            color = Color.Black.copy(alpha = 0.50f),
            radius = knobRadiusPx + 1.2.dp.toPx(),
            center = Offset(tipX, tipY)
        )

        // Main Knob Circle
        drawCircle(
            color = knobColor,
            radius = knobRadiusPx,
            center = Offset(tipX, tipY)
        )
    }
}

@Composable
fun LoginDetailContent(
    login: Utilities.MfaCode,
    utilities: Utilities,
    amoled: Boolean,
    onDismiss: () -> Unit,
    onDataChanged: () -> Unit,
    onHorizontalPageChanged: (Int) -> Unit,
    dataVersion: Int = 0
) {
    val context = LocalContext.current
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val coroutineScope = rememberCoroutineScope()

    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    
    // Timer that updates every 16ms (60fps) for buttery-smooth 60Hz movement
    LaunchedEffect(Unit) {
        while(true) {
            currentTime = System.currentTimeMillis()
            kotlinx.coroutines.delay(16)
        }
    }

    val periodMillis = remember(login.period) { login.period * 1000L }
    val refillDurationMillis = 800L
    val effectiveTime = currentTime + refillDurationMillis

    // Code updates 800ms before the period boundary, in 1:1 synchronization with the zwoop refill
    val code = remember(login, (effectiveTime / periodMillis)) {
        val generated = if (login.mode.contains("totp")) {
            utilities.generateTotp(login.secret, login.algorithm, login.digits, login.period, effectiveTime)
        } else {
            utilities.generateHotp(login.secret, login.algorithm, login.digits, login.counter)
        }
        "${generated.substring(0, generated.length / 2)} ${generated.substring(generated.length / 2)}"
    }

    val pagerState = rememberPagerState(pageCount = { 2 })
    var showIconPicker by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(pagerState.currentPage) {
        onHorizontalPageChanged(pagerState.currentPage)
    }

    var showSwipeHint by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val hintKey = "SWIPE_HINT_COUNT"
        val currentCount = utilities.db.getInt(hintKey, 0)
        if (currentCount < 10) {
            kotlinx.coroutines.delay(3000)
            showSwipeHint = true
            utilities.db.edit { putInt(hintKey, currentCount + 1) }
            kotlinx.coroutines.delay(3000)
            showSwipeHint = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(enabled = !showIconPicker && !isEditing) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = !showIconPicker && !isEditing,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            Box(modifier = Modifier.fillMaxSize()) {
                when (page) {
                    0 -> {
                        if (login.mode.contains("totp")) {
                            val refillDuration = 800f
                            val countdownDuration = (periodMillis - refillDuration).coerceAtLeast(1f)
                            val cycleMillis = (currentTime % periodMillis).toFloat()

                            val elasticRefillEasing = remember { CubicBezierEasing(0.25f, 1f, 0.5f, 1f) }

                            val currentProgress = if (cycleMillis < countdownDuration) {
                                (1f - (cycleMillis / countdownDuration)).coerceIn(0f, 1f)
                            } else {
                                val refillRatio = ((cycleMillis - countdownDuration) / refillDuration).coerceIn(0f, 1f)
                                elasticRefillEasing.transform(refillRatio)
                            }

                            SpeedometerProgressIndicator(
                                progress = currentProgress,
                                activeColor = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                                knobColor = Color.White,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Column(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 20.dp)
                                .padding(horizontal = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Empty space for static page indicator
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            Text(
                                text = login.issuer,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (amoled) MaterialTheme.colorScheme.primary else lerp(MaterialTheme.colorScheme.primary, Color.White, 0.4f),
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.basicMarquee()
                            )
                            
                            Text(
                                text = login.account.ifBlank { stringResource(R.string.account) },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier = Modifier.fillMaxWidth().basicMarquee()
                            )

                            if (login.label.isNotBlank()) {
                                Text(
                                    text = login.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    modifier = Modifier.basicMarquee()
                                )
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 24.dp)
                        ) {
                            Row(
                                modifier = Modifier.clickable {
                                    clipboard.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.app_name), code.replace(" ", "")))
                                },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                val digitStyle = when {
                                    code.length > 8 -> MaterialTheme.typography.titleLarge
                                    code.length > 6 -> MaterialTheme.typography.displaySmall
                                    else -> MaterialTheme.typography.displayMedium
                                }.let { it.copy(fontSize = (it.fontSize.value + 4).sp) }

                                code.forEachIndexed { index, char ->
                                    if (char.isDigit()) {
                                        RolodexDigit(char.digitToInt(), digitStyle, Color.White)
                                    } else {
                                        Text(
                                            text = char.toString(),
                                            style = digitStyle,
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }

                        // Back action at the bottom of Page 0 (Slides with page)
                        EdgeButton(
                            onClick = { onDismiss() },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
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
                    1 -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            ManualEntryScreen(
                                login = login,
                                utilities = utilities,
                                onDismiss = onDismiss,
                                onShowIconPicker = { showIconPicker = true },
                                onEditingModeChange = { isEditing = it },
                                onDataChanged = onDataChanged,
                                onShowToast = { msg ->
                                    coroutineScope.launch {
                                        toastMessage = msg
                                        delay(2000)
                                        toastMessage = null
                                    }
                                },
                                dataVersion = dataVersion
                            )
                        }
                    }
                }
            }
        }

        if (!showIconPicker && !isEditing) {
            // Hint for horizontal navigation (Z-axis: Above page dots, Page 0 only)
            AnimatedVisibility(
                visible = showSwipeHint && pagerState.currentPage == 0,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 17.dp)
                    .zIndex(2f)
            ) {
                Text(
                    text = "◀ Swipe for more",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                    color = Color.Gray,
                    modifier = Modifier
                        .background(Color.Black, CircleShape)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            // Static Page Indicator (Z-axis: Below the swipe hint)
            HorizontalPageIndicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 17.dp)
                    .zIndex(1f),
                pagerState = pagerState,
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
        }

        val animDuration = 320
        val animEasing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)

        // Dynamic Island Favorite Toast (Z-axis: Above page dots & pager)
        AnimatedVisibility(
            visible = toastMessage != null,
            enter = fadeIn(animationSpec = tween(animDuration, easing = animEasing)) +
                    scaleIn(initialScale = 0.0f, transformOrigin = TransformOrigin(0.5f, 0f), animationSpec = tween(animDuration, easing = animEasing)) +
                    expandVertically(expandFrom = Alignment.Top, animationSpec = tween(animDuration, easing = animEasing)),
            exit = fadeOut(animationSpec = tween(animDuration, easing = animEasing)) +
                   scaleOut(targetScale = 0.0f, transformOrigin = TransformOrigin(0.5f, 0f), animationSpec = tween(animDuration, easing = animEasing)) +
                   shrinkVertically(shrinkTowards = Alignment.Top, animationSpec = tween(animDuration, easing = animEasing)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 22.dp)
                .zIndex(1000f)
        ) {
            toastMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .background(
                            color = if (amoled) Color.Black else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 1f),
                            shape = CircleShape
                        )
                        .border(
                            border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else BorderStroke(0.dp, Color.Transparent),
                            shape = CircleShape
                        )
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val isFavMsg = msg == "Favorited"
                        Icon(
                            painter = painterResource(if (isFavMsg) R.drawable.ic_star_24 else R.drawable.ic_star_outline_24),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                            color = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        if (showIconPicker) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(100f)
            ) {
                IconPickerOverlay(
                    secret = login.secret,
                    utilities = utilities,
                    amoled = amoled,
                    onDataChanged = onDataChanged,
                    onDismiss = { showIconPicker = false }
                )
            }
        }
    }
}

val ScallopedFlowerShape = GenericShape { size, _ ->
    val center = Offset(size.width / 2f, size.height / 2f)
    val outerRadius = minOf(size.width, size.height) / 2f
    val innerRadius = outerRadius * 0.82f
    val numPetals = 12
    val path = Path()
    for (i in 0 until numPetals * 2) {
        val angle = Math.toRadians((i * 360.0 / (numPetals * 2)).toDouble())
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val x = center.x + r * kotlin.math.cos(angle).toFloat()
        val y = center.y + r * kotlin.math.sin(angle).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    addPath(path)
}

val StarburstFlowerShape = GenericShape { size, _ ->
    val center = Offset(size.width / 2f, size.height / 2f)
    val outerRadius = minOf(size.width, size.height) / 2f
    val innerRadius = outerRadius * 0.72f
    val numPoints = 10
    val path = Path()
    for (i in 0 until numPoints * 2) {
        val angle = Math.toRadians((i * 360.0 / (numPoints * 2)).toDouble())
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val x = center.x + r * kotlin.math.cos(angle).toFloat()
        val y = center.y + r * kotlin.math.sin(angle).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    addPath(path)
}

@Composable
fun TextEditorModal(
    title: String,
    initialValue: String,
    amoled: Boolean,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }
    val focusRequester = remember { FocusRequester() }
    val listState = rememberScalingLazyListState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .zIndex(100f)
    ) {
        ScreenScaffold(
            scrollState = listState,
            timeText = { TimeText() }
        ) {
            ScalingLazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                state = listState,
                anchorType = ScalingLazyListAnchorType.ItemStart,
                contentPadding = PaddingValues(
                    top = 28.dp,
                    start = 12.dp,
                    end = 12.dp,
                    bottom = 84.dp
                ),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                autoCentering = null
            ) {
                item {
                    Text(
                        text = "Edit $title",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                item {
                    val themeColor = MaterialTheme.colorScheme.primary
                    val containerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.surfaceContainer
                    val contentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onSurface

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .background(containerColor, CircleShape)
                            .border(
                                border = if (amoled) BorderStroke(1.dp, themeColor.copy(alpha = 0.5f)) else BorderStroke(0.dp, Color.Transparent),
                                shape = CircleShape
                            )
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = text,
                            onValueChange = { newValue ->
                                text = newValue
                                onValueChange(newValue)
                            },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = contentColor),
                            cursorBrush = SolidColor(themeColor),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                        )
                    }
                }
            }
        }

        EdgeButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(62.dp),
            border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                contentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Text(
                text = "Done",
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                softWrap = false
            )
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

private data class StarShard(
    val initialX: Float,
    val initialY: Float,
    val vx: Float,
    val vy: Float,
    val rotationSpeed: Float,
    val size: Float
)

@Composable
fun ManualEntryScreen(
    login: Utilities.MfaCode,
    utilities: Utilities,
    onDismiss: () -> Unit,
    onShowIconPicker: () -> Unit,
    onEditingModeChange: (Boolean) -> Unit,
    onDataChanged: () -> Unit,
    onShowToast: (String) -> Unit = {},
    dataVersion: Int = 0
) {
    val context = LocalContext.current
    var currentLogin by remember(login) { mutableStateOf(login) }
    var editingField by remember { mutableStateOf<String?>(null) }
    var iconVersion by remember { mutableIntStateOf(0) }

    var isFavorite by remember(currentLogin.secret, dataVersion) {
        mutableStateOf(utilities.db.getBoolean("FAVORITE_${currentLogin.secret}", false))
    }

    val coroutineScope = rememberCoroutineScope()
    val starScale = remember { Animatable(1f) }
    val starSpinRotation = remember { Animatable(0f) }
    val starShakeOffset = remember { Animatable(0f) }
    val shardAnimProgress = remember { Animatable(0f) }
    val shards = remember {
        listOf(
            StarShard(0f, -6f, 0f, -35f, -360f, 9f),
            StarShard(6f, -2f, 30f, -25f, 420f, 8f),
            StarShard(-6f, -2f, -30f, -25f, -400f, 8f),
            StarShard(4f, 6f, 20f, 5f, 300f, 8f),
            StarShard(-4f, 6f, -20f, 5f, -320f, 8f)
        )
    }
    
    LaunchedEffect(login) {
        currentLogin = login
        iconVersion++
    }
    
    LaunchedEffect(editingField) {
        onEditingModeChange(editingField != null)
    }

    fun saveLogin(updated: Utilities.MfaCode) {
        currentLogin = updated
        utilities.overwriteLogin(utilities.encodeOtpAuthURL(updated))
        onDataChanged()
    }

    val listState = rememberScalingLazyListState(
        initialCenterItemIndex = 0,
        initialCenterItemScrollOffset = 0
    )
    val amoled = LocalAmoledEnabled.current

    val rotation by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            if (layoutInfo.visibleItemsInfo.isNotEmpty()) {
                try {
                    (listState.centerItemIndex * 89f + listState.centerItemScrollOffset / 2f)
                } catch (_: Exception) {
                    0f
                }
            } else {
                0f
            }
        }
    }



    Box(modifier = Modifier.fillMaxSize()) {
        ScreenScaffold(
            scrollState = listState,
            timeText = { TimeText() },
            edgeButton = {
                EdgeButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(62.dp),
                    border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null,
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
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                state = listState,
                anchorType = ScalingLazyListAnchorType.ItemStart,
                contentPadding = PaddingValues(
                    top = 33.dp,
                    start = 12.dp,
                    end = 12.dp,
                    bottom = 72.dp
                ),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                autoCentering = null,
                scalingParams = ScalingLazyColumnDefaults.scalingParams(
                    edgeScale = 0.45f,
                    edgeAlpha = 0.4f,
                    minTransitionArea = 0.35f,
                    maxTransitionArea = 0.65f
                )
            ) {
                // Top Section: Time/Counter Pill
                item {
                    val isTotp = currentLogin.mode.contains("totp")
                    val iconRes = if (isTotp) R.drawable.ic_outline_access_time_24 else R.drawable.baseline_exposure_count_plus_1_24
                    val labelText = if (isTotp) "Time" else "Counter"

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 1.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .height(34.dp)
                                .background(
                                    color = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                                    shape = CircleShape
                                )
                                .border(
                                    border = BorderStroke(1.dp, if (amoled) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent),
                                    shape = CircleShape
                                )
                                .clickable {
                                    val nextMode = if (currentLogin.mode.contains("totp")) "hotp" else "totp"
                                    saveLogin(currentLogin.copy(mode = nextMode))
                                }
                                .padding(start = 10.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val accentTextColor = if (amoled) Color.White else lerp(MaterialTheme.colorScheme.primary, Color.White, 0.4f)
                                Icon(
                                    painter = painterResource(iconRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = accentTextColor
                                )
                                Text(
                                    text = labelText,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = accentTextColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Issuer Card (Full width)
                item {
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                            .height(64.dp)
                            .background(
                                color = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                                shape = ParallelogramShape
                            )
                            .border(
                                border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else BorderStroke(0.dp, Color.Transparent),
                                shape = ParallelogramShape
                            )
                            .clickable { editingField = "Issuer" }
                            .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val headerStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 14.sp)
                            Text(
                                text = "Issuer",
                                style = headerStyle,
                                color = if (amoled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = currentLogin.issuer,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (amoled) Color.White else lerp(MaterialTheme.colorScheme.primary, Color.White, 0.4f),
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().basicMarquee()
                            )
                        }
                    }
                }

                // Row: Cookie Flower + Star Pentagon + QR Code Button (Expanded card sizes)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-2).dp)
                            .height(68.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Left: Cookie Flower Badge
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(70.dp)
                                .clickable { onShowIconPicker() },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { rotationZ = rotation }
                                    .background(
                                        color = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                                        shape = Cookie6SidedShape
                                    )
                                    .border(
                                        border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else BorderStroke(0.dp, Color.Transparent),
                                        shape = Cookie6SidedShape
                                    )
                            )
                            BrandLogo(
                                issuer = currentLogin.issuer,
                                amoled = amoled,
                                modifier = Modifier.size(48.dp),
                                showCircle = false,
                                textStyle = MaterialTheme.typography.displaySmall,
                                secret = currentLogin.secret,
                                utilities = utilities,
                                dataVersion = dataVersion,
                                overrideTint = if (amoled) Color.White else lerp(MaterialTheme.colorScheme.primary, Color.White, 0.4f)
                            )
                        }

                        // 2. Middle: Star Pentagon Button (Favorite / Unfavorite toggle with animation)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(70.dp)
                                .clickable {
                                    val newFav = !isFavorite
                                    isFavorite = newFav
                                    utilities.db.edit { putBoolean("FAVORITE_${currentLogin.secret}", newFav) }
                                    onDataChanged()
                                    onShowToast(if (newFav) "Favorited" else "Unfavorited")

                                    coroutineScope.launch {
                                        if (newFav) {
                                            // Elastic Spinny Animation for Favorite!
                                            starSpinRotation.snapTo(0f)
                                            starScale.snapTo(0.4f)
                                            launch {
                                                starSpinRotation.animateTo(
                                                    targetValue = 360f,
                                                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                                                )
                                            }
                                            launch {
                                                starScale.animateTo(
                                                    targetValue = 1f,
                                                    animationSpec = spring(
                                                        dampingRatio = Spring.DampingRatioHighBouncy,
                                                        stiffness = Spring.StiffnessLow
                                                    )
                                                )
                                            }
                                        } else {
                                            // Glass Shatter & Falling Fragments Animation for Unfavorite!
                                            launch {
                                                shardAnimProgress.snapTo(0f)
                                                shardAnimProgress.animateTo(
                                                    targetValue = 1f,
                                                    animationSpec = tween(durationMillis = 650, easing = FastOutLinearInEasing)
                                                )
                                            }
                                            launch {
                                                starScale.animateTo(0.7f, animationSpec = tween(60))
                                                starScale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                            }
                                            launch {
                                                repeat(2) {
                                                    starShakeOffset.animateTo(10f, animationSpec = tween(30))
                                                    starShakeOffset.animateTo(-10f, animationSpec = tween(30))
                                                }
                                                starShakeOffset.animateTo(0f, animationSpec = spring())
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            // Rotating Background Shape
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { rotationZ = rotation }
                                    .background(
                                        color = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedPentagonShape
                                    )
                                    .border(
                                        border = BorderStroke(1.dp, if (amoled) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent),
                                        shape = RoundedPentagonShape
                                    )
                            )

                            // Glass Shatter Shards Overlay (Triggers on Unfavorite)
                            if (shardAnimProgress.value > 0.01f && shardAnimProgress.value < 0.99f) {
                                val t = shardAnimProgress.value
                                val shardAlpha = (1f - t * 1.2f).coerceAtLeast(0f)

                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val gravityY = 160.dp.toPx() * t * t

                                    shards.forEach { shard ->
                                        val shardX = center.x + (shard.initialX + shard.vx * t).dp.toPx()
                                        val shardY = center.y + (shard.initialY + shard.vy * t).dp.toPx() + gravityY
                                        val shardSizePx = shard.size.dp.toPx()
                                        val shardRot = shard.rotationSpeed * t

                                        withTransform({
                                            translate(left = shardX, top = shardY)
                                            rotate(degrees = shardRot, pivot = Offset.Zero)
                                        }) {
                                            val shardPath = Path().apply {
                                                moveTo(0f, -shardSizePx / 2f)
                                                lineTo(shardSizePx / 2f, shardSizePx / 2f)
                                                lineTo(-shardSizePx / 3f, shardSizePx / 3f)
                                                close()
                                            }
                                            drawPath(
                                                path = shardPath,
                                                color = Color.White.copy(alpha = shardAlpha)
                                            )
                                        }
                                    }
                                }
                            }

                            // Star Icon with Elastic Spin & Glass Shatter Shake
                            val starIconRes = if (isFavorite) R.drawable.ic_star_24 else R.drawable.ic_star_outline_24
                            Icon(
                                painter = painterResource(starIconRes),
                                contentDescription = if (isFavorite) "Unfavorite" else "Favorite",
                                modifier = Modifier
                                    .size(30.dp)
                                    .graphicsLayer {
                                        scaleX = starScale.value
                                        scaleY = starScale.value
                                        rotationZ = starSpinRotation.value + starShakeOffset.value
                                    },
                                tint = if (amoled) Color.White else lerp(MaterialTheme.colorScheme.primary, Color.White, 0.4f)
                            )
                        }

                        // 3. Right: QR Code Button (Cookie4SidedShape)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(70.dp)
                                .clickable {
                                    val intent = Intent(context, QRCodeActivity::class.java)
                                    intent.putExtra(utilities.INTENT_QR_DATA, utilities.encodeOtpAuthURL(login))
                                    intent.putExtra(utilities.INTENT_QR_METADATA, currentLogin.issuer)
                                    intent.putExtra("INTENT_ACCOUNT", currentLogin.account)
                                    intent.putExtra("INTENT_LABEL", currentLogin.label)
                                    context.startActivity(intent)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { rotationZ = 45f + rotation }
                                    .background(
                                        color = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                                        shape = Cookie4SidedShape
                                    )
                                    .border(
                                        border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else BorderStroke(0.dp, Color.Transparent),
                                        shape = Cookie4SidedShape
                                    )
                            )
                            Icon(
                                painter = painterResource(R.drawable.ic_outline_qr_code_2_24),
                                contentDescription = "Show QR code",
                                modifier = Modifier.size(28.dp),
                                tint = if (amoled) Color.White else lerp(MaterialTheme.colorScheme.primary, Color.White, 0.4f)
                            )
                        }
                    }
                }

                // Account Card (Full width under Row 1)
                item {
                    Box(
                        modifier = Modifier
                            .offset(y = (-4).dp)
                            .fillMaxWidth()
                            .height(58.dp)
                            .background(
                                color = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(22.dp)
                            )
                            .border(
                                border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else BorderStroke(0.dp, Color.Transparent),
                                shape = RoundedCornerShape(22.dp)
                            )
                            .clickable { editingField = "Account" }
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val headerStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 14.sp)
                            Text(
                                text = "Account",
                                style = headerStyle,
                                color = if (amoled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = currentLogin.account.ifBlank { currentLogin.label.ifBlank { "None" } },
                                style = MaterialTheme.typography.titleMedium,
                                color = if (amoled) Color.White else lerp(MaterialTheme.colorScheme.primary, Color.White, 0.4f),
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().basicMarquee()
                            )
                        }
                    }
                }

                // Bento Grid Row 2 (3 items: Timer Pill, Algo Hexagon, Digits Card)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Left: Narrow Vertical Pill Badge (Timer / Counter Icon + Value)
                        Box(
                            modifier = Modifier
                                .weight(0.7f)
                                .height(58.dp)
                                .background(
                                    color = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                                    shape = CircleShape
                                )
                                .border(
                                    border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else BorderStroke(0.dp, Color.Transparent),
                                    shape = CircleShape
                                )
                                .clickable {
                                    if (currentLogin.mode.contains("totp")) {
                                        val nextPeriod = when (currentLogin.period) { 30 -> 60; 60 -> 15; else -> 30 }
                                        saveLogin(currentLogin.copy(period = nextPeriod))
                                    } else {
                                        saveLogin(currentLogin.copy(counter = currentLogin.counter + 1))
                                    }
                                }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val isTotp = currentLogin.mode.contains("totp")
                            val iconRes = if (isTotp) R.drawable.ic_outline_access_time_24 else R.drawable.baseline_exposure_count_plus_1_24
                            val accentTextColor = if (amoled) Color.White else lerp(MaterialTheme.colorScheme.primary, Color.White, 0.4f)
                            val headerIconColor = if (amoled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(iconRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = headerIconColor
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isTotp) "${currentLogin.period}s" else "#${currentLogin.counter}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    color = accentTextColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // 2. Middle: Wide Rounded Hexagon Card (Algo)
                        Box(
                            modifier = Modifier
                                .weight(1.35f)
                                .height(68.dp)
                                .clickable {
                                    val nextAlgo = when (currentLogin.algorithm) { "SHA1" -> "SHA256"; "SHA256" -> "SHA512"; else -> "SHA1" }
                                    saveLogin(currentLogin.copy(algorithm = nextAlgo))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { rotationZ = rotation }
                                    .background(
                                        color = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedHexagonShape
                                    )
                                    .border(
                                        border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else BorderStroke(0.dp, Color.Transparent),
                                        shape = RoundedHexagonShape
                                    )
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val headerStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp)
                                Text(
                                    text = "Algo",
                                    style = headerStyle,
                                    color = if (amoled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = currentLogin.algorithm,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (amoled) Color.White else lerp(MaterialTheme.colorScheme.primary, Color.White, 0.4f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // 3. Right: Digit Card (Shorter)
                        Box(
                            modifier = Modifier
                                .weight(0.95f)
                                .height(58.dp)
                                .background(
                                    color = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(22.dp)
                                )
                                .border(
                                    border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else BorderStroke(0.dp, Color.Transparent),
                                    shape = RoundedCornerShape(22.dp)
                                )
                                .clickable {
                                    val nextDigits = if (currentLogin.digits == 6) 8 else 6
                                    saveLogin(currentLogin.copy(digits = nextDigits))
                                }
                                .padding(horizontal = 6.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val headerStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp)
                                Text(
                                    text = "Digit",
                                    style = headerStyle,
                                    color = if (amoled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "${currentLogin.digits}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (amoled) Color.White else lerp(MaterialTheme.colorScheme.primary, Color.White, 0.4f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Secret Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 58.dp)
                            .background(
                                color = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(22.dp)
                            )
                            .border(
                                border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else BorderStroke(0.dp, Color.Transparent),
                                shape = RoundedCornerShape(22.dp)
                            )
                            .clickable { editingField = "Secret" }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            val headerStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 14.sp)
                            Text(
                                text = "Secret",
                                style = headerStyle,
                                color = if (amoled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                            val secretStr = currentLogin.secret.ifBlank { "None" }.chunked(4).joinToString(" ")
                            val baseColor = if (amoled) Color.White else lerp(MaterialTheme.colorScheme.primary, Color.White, 0.4f)
                            val isWhite = amoled || (baseColor.red > 0.9f && baseColor.green > 0.9f && baseColor.blue > 0.9f)
                            val normalColor = baseColor
                            val alternateColor = if (isWhite) Color.LightGray else lerp(baseColor, Color.White, 0.75f)

                            val annotatedSecret = remember(secretStr, baseColor, isWhite, amoled) {
                                buildAnnotatedString {
                                    secretStr.forEachIndexed { index, char ->
                                        val color = if (index % 2 == 1) alternateColor else normalColor
                                        withStyle(SpanStyle(color = color)) {
                                            append(char.toString())
                                        }
                                    }
                                }
                            }
                            Text(
                                text = annotatedSecret,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 18.sp,
                                    letterSpacing = 2.sp
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Label Card (optional)
                item {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 6.dp)
                            .fillMaxWidth()
                            .heightIn(min = 58.dp)
                            .background(
                                color = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(22.dp)
                            )
                            .border(
                                border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else BorderStroke(0.dp, Color.Transparent),
                                shape = RoundedCornerShape(22.dp)
                            )
                            .clickable { editingField = "Label" }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            val headerStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 14.sp)
                            Text(
                                text = "Label (optional)",
                                style = headerStyle,
                                color = if (amoled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = currentLogin.label.ifBlank { "None" },
                                style = MaterialTheme.typography.titleMedium,
                                color = if (amoled) Color.White else lerp(MaterialTheme.colorScheme.primary, Color.White, 0.4f),
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().basicMarquee()
                            )
                        }
                    }
                }




                // Small Divider Line
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(1.dp)
                                .background(
                                    color = if (amoled) Color.Gray.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                // Settings-style Delete Button
                item {
                    Button(
                        onClick = {
                            utilities.deleteFromDataStore(utilities.encodeOtpAuthURL(login))
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .heightIn(min = 50.dp),
                        shape = CircleShape,
                        border = if (amoled) BorderStroke(1.dp, Color.Red.copy(alpha = 0.6f)) else null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                            contentColor = if (amoled) Color.Red else MaterialTheme.colorScheme.onErrorContainer
                        ),
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_outline_delete_24),
                                contentDescription = null,
                                tint = if (amoled) Color.Red else MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "Delete",
                                maxLines = 1,
                                color = if (amoled) Color.Red else MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        editingField?.let { field ->
            val initialValue = when (field) {
                "Issuer" -> currentLogin.issuer
                "Account" -> currentLogin.account
                "Label" -> currentLogin.label
                "Secret" -> currentLogin.secret
                else -> ""
            }
            TextEditorModal(
                title = field,
                initialValue = initialValue,
                amoled = amoled,
                onValueChange = { newValue ->
                    val updated = when (field) {
                        "Issuer" -> currentLogin.copy(issuer = newValue)
                        "Account" -> currentLogin.copy(account = newValue)
                        "Label" -> currentLogin.copy(label = newValue)
                        "Secret" -> currentLogin.copy(secret = newValue)
                        else -> currentLogin
                    }
                    saveLogin(updated)
                },
                onDismiss = { editingField = null }
            )
        }
    }
}

@Composable
fun ParameterRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).padding(horizontal = 18.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            softWrap = true
        )
    }
}

@Composable
fun BrandLogo(
    issuer: String,
    amoled: Boolean,
    modifier: Modifier = Modifier,
    showCircle: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.titleLarge,
    secret: String? = null,
    utilities: Utilities? = null,
    dataVersion: Int = 0,
    overrideTint: Color? = null
) {
    val customIconKey = secret?.let { utilities?.db?.getString("CUSTOM_ICON_$it", null) }
    val brandIcon = remember(issuer, secret, customIconKey, dataVersion) {
        getBrandIcon(issuer, secret, utilities)
    }
    val themeColor = MaterialTheme.colorScheme.primary
    val containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    
    Box(
        modifier = modifier.background(if (amoled || !showCircle) Color.Transparent else themeColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val defaultTint = if (amoled || !showCircle) themeColor else containerColor
        val tint = overrideTint ?: defaultTint
        if (brandIcon != null) {
            Icon(
                imageVector = brandIcon,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(if (showCircle) 0.6f else 0.55f),
                tint = tint
            )
        } else {
            Text(
                text = issuer.take(1).uppercase(),
                color = tint,
                style = textStyle,
            )
        }
    }
}

@Composable
fun IconPickerOverlay(
    secret: String,
    utilities: Utilities,
    amoled: Boolean,
    onDataChanged: () -> Unit,
    onIconPicked: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    var searchQuery by remember { mutableStateOf("") }

    var resetItemYInWindow by remember { mutableFloatStateOf(0f) }
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val screenRadius = screenHeightPx / 2f

    val resetBtnPaddingDp by remember {
        derivedStateOf {
            if (screenRadius > 0f && resetItemYInWindow > 0f) {
                val dy = kotlin.math.abs(resetItemYInWindow - screenRadius)
                val normalizedDy = (dy / screenRadius).coerceIn(0f, 1f)
                val widthFactor = kotlin.math.sqrt(1f - normalizedDy * normalizedDy)
                val availableWidthDp = screenWidthDp * widthFactor
                val requiredPaddingDp = (screenWidthDp - availableWidthDp) / 2f
                requiredPaddingDp.coerceAtLeast(0.dp)
            } else {
                0.dp
            }
        }
    }

    val filteredIcons = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            Services.allServiceIcons
        } else {
            Services.allServiceIcons.filter {
                it.name.lowercase().contains(searchQuery.lowercase()) ||
                it.key.lowercase().contains(searchQuery.lowercase())
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { onDismiss() }
    ) {
        ScreenScaffold(
            scrollState = listState,
            edgeButton = {
                EdgeButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null,
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
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                state = listState,
                anchorType = ScalingLazyListAnchorType.ItemStart,
                contentPadding = PaddingValues(
                    top = 28.dp,
                    start = 10.dp,
                    end = 10.dp,
                    bottom = 84.dp
                ),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                autoCentering = null,
                scalingParams = ScalingLazyColumnDefaults.scalingParams(
                    edgeScale = 0.35f,
                    edgeAlpha = 0.3f,
                    minTransitionArea = 0.35f,
                    maxTransitionArea = 0.65f
                )
            ) {
            item {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    amoled = amoled,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            item {
                Box(
                    modifier = Modifier
                        .onGloballyPositioned { coordinates ->
                            resetItemYInWindow = coordinates.boundsInWindow().center.y
                        }
                        .padding(horizontal = resetBtnPaddingDp)
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            utilities.db.edit { remove("CUSTOM_ICON_$secret") }
                            onDataChanged()
                            onIconPicked()
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                            contentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_baseline_settings_backup_restore_24),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Reset to default",
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            items(filteredIcons.chunked(3)) { chunk ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
                ) {
                    chunk.forEach { item ->
                        Button(
                            onClick = {
                                utilities.db.edit { putString("CUSTOM_ICON_$secret", item.key) }
                                onDataChanged()
                                onIconPicked()
                                onDismiss()
                            },
                            modifier = Modifier.size(54.dp),
                            border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                                contentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.name,
                                    modifier = Modifier.fillMaxSize(0.55f),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(4.dp)) }
        }

        // Sleek EdgeButton inside ScreenScaffold
        }
    }
}

@Composable
fun RolodexDigit(
    targetDigit: Int,
    digitStyle: TextStyle,
    baseColor: Color = Color.White
) {
    val density = LocalDensity.current
    val fontSize = digitStyle.fontSize
    val itemHeightPx = with(density) { fontSize.toPx() * 0.6f }
    
    val scrollY = remember { Animatable(targetDigit.toFloat()) }
    
    LaunchedEffect(targetDigit) {
        val current = scrollY.value
        val currentDigit = ((current.roundToInt() % 10) + 10) % 10
        
        var diff = targetDigit - currentDigit
        if (diff > 5) diff -= 10
        if (diff < -5) diff += 10
        
        scrollY.animateTo(
            targetValue = current + diff,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = Modifier
            .height(with(density) { (itemHeightPx * 2.5f).toDp() })
            .width(with(density) { (fontSize.toPx() * 0.75f).toDp() }),
        contentAlignment = Alignment.Center
    ) {
        val currentVal = scrollY.value
        val baseIndex = kotlin.math.floor(currentVal).toInt()
        
        for (i in baseIndex - 2..baseIndex + 2) {
            val displayDigit = (i % 10 + 10) % 10
            val distance = i - currentVal
            val absDist = abs(distance)
            
            if (absDist <= 1.8f) {
                val scale = 1f - (absDist * 0.25f).coerceAtMost(0.4f)
                
                // Continuous smooth alpha curve without threshold popping
                val alphaFactor = (1f - (absDist / 1.8f)).coerceIn(0f, 1f)
                val alpha = alphaFactor * alphaFactor
                
                // Continuous smooth color interpolation between baseColor and LightGray
                val colorRatio = (1f - absDist * 1.5f).coerceIn(0f, 1f)
                val textColor = lerp(Color.LightGray, baseColor, colorRatio)
                
                val rotationX = distance * -50f
                val shadowAlpha = (absDist * 0.5f).coerceIn(0f, 0.7f)
                
                Text(
                    text = displayDigit.toString(),
                    style = digitStyle,
                    color = textColor.copy(alpha = alpha),
                    modifier = Modifier
                        .graphicsLayer {
                            translationY = distance * itemHeightPx
                            this.scaleX = scale
                            this.scaleY = scale
                            this.rotationX = rotationX
                        }
                        .drawWithContent {
                            drawContent()
                            if (shadowAlpha > 0.01f) {
                                if (distance < 0f) {
                                    drawRect(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = shadowAlpha),
                                                Color.Transparent
                                            ),
                                            startY = 0f,
                                            endY = size.height * 0.5f
                                        )
                                    )
                                } else {
                                    drawRect(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = shadowAlpha)
                                            ),
                                            startY = size.height * 0.5f,
                                            endY = size.height
                                        )
                                    )
                                }
                            }
                        }
                )
            }
        }
    }
}

@Composable
fun ListLoginCard(login: Utilities.MfaCode, amoled: Boolean, onLoginClick: (Utilities.MfaCode, Rect?) -> Unit, modifier: Modifier = Modifier, utilities: Utilities? = null, dataVersion: Int = 0) {
    var bounds by remember { mutableStateOf<Rect?>(null) }
    Card(
        onClick = {
            onLoginClick(login, bounds)
        },
        modifier = modifier
            .onGloballyPositioned { bounds = it.boundsInWindow() }
            .fillMaxWidth()
            .height(50.dp),
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
            contentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
        ),
        border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null,
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp)
        ) {
            BrandLogo(
                issuer = login.issuer,
                amoled = amoled,
                modifier = Modifier.size(32.dp),
                secret = login.secret,
                utilities = utilities,
                dataVersion = dataVersion
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = login.issuer,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (amoled) Color.White else lerp(MaterialTheme.colorScheme.primary, Color.White, 0.4f),
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false).padding(end = 4.dp).basicMarquee()
                    )

                    if (login.label.isNotBlank()) {
                        Text(
                            text = login.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (amoled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            maxLines = 1,
                            modifier = Modifier.weight(1f, fill = false).basicMarquee()
                        )
                    }
                }

                Text(
                    text = login.account.ifBlank { stringResource(R.string.account) },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (amoled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    maxLines = 1,
                    modifier = Modifier.basicMarquee()
                )
            }
        }
    }
}

@Composable
fun DashLoginCard(login: Utilities.MfaCode, amoled: Boolean, onLoginClick: (Utilities.MfaCode, Rect?) -> Unit, modifier: Modifier = Modifier, utilities: Utilities? = null, dataVersion: Int = 0) {
    var bounds by remember { mutableStateOf<Rect?>(null) }
    Card(
        onClick = {
            onLoginClick(login, bounds)
        },
        modifier = modifier
            .onGloballyPositioned { bounds = it.boundsInWindow() }
            .aspectRatio(1f),
        border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null,
        colors = CardDefaults.cardColors(
            containerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
            contentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(2.dp)
        ) {
            BrandLogo(
                issuer = login.issuer,
                amoled = amoled,
                modifier = Modifier.size(26.dp),
                textStyle = MaterialTheme.typography.labelSmall,
                secret = login.secret,
                utilities = utilities,
                dataVersion = dataVersion
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = login.issuer,
                style = MaterialTheme.typography.labelSmall,
                color = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().basicMarquee()
            )
        }
    }
}

@Composable
fun BubbleLoginCard(login: Utilities.MfaCode, amoled: Boolean, onLoginClick: (Utilities.MfaCode, Rect?) -> Unit, modifier: Modifier = Modifier, utilities: Utilities? = null, dataVersion: Int = 0) {
    var bounds by remember { mutableStateOf<Rect?>(null) }
    Button(
        onClick = {
            onLoginClick(login, bounds)
        },
        modifier = modifier
            .onGloballyPositioned { bounds = it.boundsInWindow() }
            .size(54.dp),
        border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
            contentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        BrandLogo(
            issuer = login.issuer,
            amoled = amoled,
            modifier = Modifier.fillMaxSize(),
            showCircle = false,
            textStyle = MaterialTheme.typography.displaySmall,
            secret = login.secret,
            utilities = utilities,
            dataVersion = dataVersion
        )
    }
}

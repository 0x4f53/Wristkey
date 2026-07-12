package zeroxfourf.wristkey

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
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
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
                AppScaffold(
                    timeText = { if (selectedLogin == null) TimeText() }
                ) {
                    MainScreen(
                        utilities = utilities,
                        selectedLogin = selectedLogin,
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

@Composable
fun MainScreen(
    utilities: Utilities,
    selectedLogin: Utilities.MfaCode?,
    onLoginSelected: (Utilities.MfaCode?) -> Unit,
    onAddClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    val logins = remember { utilities.getData().otpauth.mapNotNull { utilities.decodeOtpAuthURL(it) } }
    
    var viewType by remember {
        mutableIntStateOf(utilities.db.getInt(utilities.SETTINGS_VIEW_TYPE, 0))
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewType = utilities.db.getInt(utilities.SETTINGS_VIEW_TYPE, 0)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var searchQuery by remember { mutableStateOf("") }

    val filteredLogins = if (searchQuery.isEmpty()) {
        logins
    } else {
        logins.filter {
            ("${it.issuer} ${it.account} ${it.label}").lowercase().contains(searchQuery.lowercase())
        }
    }

    val amoled = LocalAmoledEnabled.current

    val rotation by remember {
        derivedStateOf {
            // Increased sensitivity for a more pronounced "spin" as you scroll
            (listState.centerItemIndex * 89f + listState.centerItemScrollOffset / 2f)
        }
    }

    ScreenScaffold(
        scrollState = listState,
        edgeButton = {
            EdgeButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
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
                top = 30.dp,
                start = 10.dp,
                end = 10.dp,
                bottom = 84.dp
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            autoCentering = null
        ) {
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
                            Row(modifier = Modifier.fillMaxWidth()) {
                                chunk.forEach { login ->
                                    DashLoginCard(login, utilities, amoled, { onLoginSelected(it) }, Modifier.weight(1f))
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
                                    BubbleLoginCard(login, amoled, { onLoginSelected(it) })
                                }
                            }
                        }
                    }
                    else -> {
                        items(filteredLogins) { login ->
                            ListLoginCard(login, amoled, { onLoginSelected(it) })
                        }
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = onAddClick,
                        modifier = Modifier
                            .size(80.dp)
                            .graphicsLayer(rotationZ = rotation),
                        shape = RoundedCornerShape(24.dp),
                        border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
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

    AnimatedVisibility(
        visible = selectedLogin != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        if (selectedLogin != null) {
            val initialIndex = filteredLogins.indexOf(selectedLogin)
            LoginDetailOverlay(
                logins = filteredLogins,
                initialIndex = if (initialIndex != -1) initialIndex else 0,
                utilities = utilities,
                amoled = amoled,
                onDismiss = { onLoginSelected(null) }
            )
        }
    }
}

@Composable
fun LoginDetailOverlay(
    logins: List<Utilities.MfaCode>,
    initialIndex: Int,
    utilities: Utilities,
    amoled: Boolean,
    onDismiss: () -> Unit
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
fun LoginDetailContent(
    login: Utilities.MfaCode,
    utilities: Utilities,
    amoled: Boolean,
    onDismiss: () -> Unit,
    onHorizontalPageChanged: (Int) -> Unit
) {
    val context = LocalContext.current
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    
    // Timer that updates every 50ms for buttery-smooth movement
    LaunchedEffect(Unit) {
        while(true) {
            currentTime = System.currentTimeMillis()
            kotlinx.coroutines.delay(50)
        }
    }

    val periodMillis = remember(login.period) { login.period * 1000L }
    
    // Code only recalculates when the period changes
    val code = remember(login, (currentTime / periodMillis)) {
        val generated = if (login.mode.contains("totp")) {
            utilities.generateTotp(login.secret, login.algorithm, login.digits, login.period)
        } else {
            utilities.generateHotp(login.secret, login.algorithm, login.digits, login.counter)
        }
        "${generated.substring(0, generated.length / 2)} ${generated.substring(generated.length / 2)}"
    }

    val pagerState = rememberPagerState(pageCount = { 2 })

    LaunchedEffect(pagerState.currentPage) {
        onHorizontalPageChanged(pagerState.currentPage)
    }

    var showSwipeHint by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000)
        showSwipeHint = true
        kotlinx.coroutines.delay(3000)
        showSwipeHint = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            Box(modifier = Modifier.fillMaxSize()) {
                when (page) {
                    0 -> {
                        if (login.mode.contains("totp")) {
                            CircularProgressIndicator(
                                progress = {
                                    val cycleMillis = (currentTime % periodMillis)
                                    val zoomDuration = 450f
                                    val buffer = 150f // Stay away a bit longer to hide refill
                                    // Pre-load logic: stay static during transitions to hide refill
                                    when {
                                        cycleMillis < (zoomDuration + buffer) -> 1f // Full while zooming in
                                        cycleMillis > (periodMillis - zoomDuration - buffer) -> 0f // Empty while zooming out
                                        else -> 1f - (cycleMillis.toFloat() / periodMillis.toFloat())
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        val cycleMillis = (currentTime % periodMillis)
                                        val zoomDuration = 450f
                                        val overshootEasing = androidx.compose.animation.core.CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.4f)
                                        
                                        val (zoomScale, zoomAlpha) = when {
                                            cycleMillis < zoomDuration -> {
                                                val raw = ((cycleMillis - 150f) / (zoomDuration - 150f)).coerceIn(0f, 1f)
                                                val eased = overshootEasing.transform(raw)
                                                (1.4f - (0.4f * eased)) to raw
                                            }
                                            cycleMillis > (periodMillis - zoomDuration) -> {
                                                val raw = ((periodMillis - cycleMillis - 150f) / (zoomDuration - 150f)).coerceIn(0f, 1f)
                                                val eased = overshootEasing.transform(1f - raw)
                                                (1.0f + (0.4f * eased)) to raw
                                            }
                                            else -> 1.0f to 1.0f
                                        }

                                        scaleX = zoomScale
                                        scaleY = zoomScale
                                        alpha = zoomAlpha
                                    },
                                colors = ProgressIndicatorDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                )
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
                                .height(62.dp)
                                .padding(bottom = 8.dp),
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
                                onDismiss = onDismiss
                            )
                        }
                    }
                }
            }
        }

        // Static Page Indicator
        HorizontalPageIndicator(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 20.dp),
            pagerState = pagerState,
            selectedColor = MaterialTheme.colorScheme.primary,
            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )

        // Hint for horizontal navigation (Animated in after 3s, overlays dots)
        AnimatedVisibility(
            visible = showSwipeHint,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 20.dp)
        ) {
            Text(
                text = "◀ Swipe for more",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color = Color.Gray,
                modifier = Modifier.background(Color.Black)
            )
        }
    }
}

@Composable
fun ManualEntryScreen(
    login: Utilities.MfaCode,
    utilities: Utilities,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val listState = rememberScalingLazyListState()
    val amoled = LocalAmoledEnabled.current

    ScreenScaffold(
        scrollState = listState,
        timeText = { TimeText() },
        edgeButton = {
            EdgeButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
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
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            state = listState,
            anchorType = ScalingLazyListAnchorType.ItemStart,
            contentPadding = PaddingValues(
                top = 50.dp,
                start = 10.dp,
                end = 10.dp,
                bottom = 84.dp // Restored to standard Settings-style padding
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            autoCentering = null
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).padding(horizontal = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BrandLogo(
                        issuer = login.issuer,
                        amoled = amoled,
                        modifier = Modifier.size(54.dp),
                        textStyle = MaterialTheme.typography.displayMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = login.issuer,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item { ParameterRow(stringResource(R.string.mode), if (login.mode.contains("totp")) stringResource(R.string.time) else stringResource(R.string.counter)) }
            item { ParameterRow(stringResource(R.string.account), login.account.ifBlank { "None" }) }
            if (login.label.isNotBlank()) {
                item { ParameterRow(stringResource(R.string.label), login.label) }
            }
            
            item { ParameterRow(stringResource(R.string.algorithm), login.algorithm) }
            item { ParameterRow(stringResource(R.string.digits), "${login.digits}") }
            if (login.mode.contains("totp")) {
                item { ParameterRow(stringResource(R.string.validity), "${login.period}s") }
            } else {
                item { ParameterRow(stringResource(R.string.count), "${login.counter}") }
            }

            item { Spacer(Modifier.height(24.dp)) }

            item {
                Button(
                    onClick = {
                        val intent = Intent(context, QRCodeActivity::class.java)
                        intent.putExtra(utilities.INTENT_QR_DATA, utilities.encodeOtpAuthURL(login))
                        intent.putExtra(utilities.INTENT_QR_METADATA, login.issuer)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                    icon = { Icon(painterResource(R.drawable.ic_outline_qr_code_2_24), contentDescription = null, modifier = Modifier.size(24.dp), tint = Color.White) },
                    label = { Text("Show QR code", maxLines = 2, softWrap = true) }
                )
            }

            item {
                Button(
                    onClick = {
                        val intent = Intent(context, ManualEntryActivity::class.java)
                        intent.putExtra(utilities.INTENT_EDIT, utilities.encodeOtpAuthURL(login))
                        context.startActivity(intent)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                    icon = { Icon(painterResource(R.drawable.ic_baseline_edit_24), contentDescription = null, modifier = Modifier.size(24.dp), tint = Color.White) },
                    label = { Text("Edit item", maxLines = 2, softWrap = true) }
                )
            }
            item { Spacer(Modifier.height((0).dp)) }
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
    textStyle: TextStyle = MaterialTheme.typography.titleLarge
) {
    val brandIcon = remember(issuer) { getBrandIcon(issuer) }
    val themeColor = MaterialTheme.colorScheme.primary
    val containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    
    Box(
        modifier = modifier.background(if (amoled || !showCircle) Color.Transparent else themeColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val tint = if (amoled || !showCircle) themeColor else containerColor
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

fun getBrandIcon(issuer: String): ImageVector? {
    return when (issuer.lowercase()) {
        "google" -> SimpleIcons.Google
        "github" -> SimpleIcons.Github
        "microsoft", "outlook", "azure" -> SimpleIcons.Microsoft
        "facebook" -> SimpleIcons.Facebook
        "amazon", "aws" -> SimpleIcons.Amazon
        "apple", "icloud" -> SimpleIcons.Apple
        "discord" -> SimpleIcons.Discord
        "dropbox" -> SimpleIcons.Dropbox
        "ebay" -> SimpleIcons.Ebay
        "instagram" -> SimpleIcons.Instagram
        "linkedin" -> SimpleIcons.Linkedin
        "paypal" -> SimpleIcons.Paypal
        "reddit" -> SimpleIcons.Reddit
        "salesforce" -> SimpleIcons.Salesforce
        "slack" -> SimpleIcons.Slack
        "snapchat" -> SimpleIcons.Snapchat
        "spotify" -> SimpleIcons.Spotify
        "twitch" -> SimpleIcons.Twitch
        "twitter", "x" -> SimpleIcons.Twitter
        "whatsapp" -> SimpleIcons.Whatsapp
        "cloudflare" -> SimpleIcons.Cloudflare
        "digitalocean" -> SimpleIcons.Digitalocean
        "docker" -> SimpleIcons.Docker
        "gitlab" -> SimpleIcons.Gitlab
        "heroku" -> SimpleIcons.Heroku
        "netflix" -> SimpleIcons.Netflix
        "nintendo" -> SimpleIcons.Nintendo
        "nvidia" -> SimpleIcons.Nvidia
        "playstation" -> SimpleIcons.Playstation
        "steam" -> SimpleIcons.Steam
        "xbox" -> SimpleIcons.Xbox
        "zoom" -> SimpleIcons.Zoom
        "coinbase" -> SimpleIcons.Coinbase
        "bitbucket" -> SimpleIcons.Bitbucket
        "atlassian" -> SimpleIcons.Atlassian
        "jira" -> SimpleIcons.Jira
        "confluence" -> SimpleIcons.Confluence
        "trello" -> SimpleIcons.Trello
        "adobe" -> SimpleIcons.Adobe
        "figma" -> SimpleIcons.Figma
        "uber" -> SimpleIcons.Uber
        "lyft" -> SimpleIcons.Lyft
        "airbnb" -> SimpleIcons.Airbnb
        else -> null
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
    val itemHeightPx = with(density) { fontSize.toPx() * 0.6f } // Further decreased to bring digits even closer
    
    // We animate a float value to simulate continuous scrolling through intermediate digits
    val scrollY = remember { Animatable(targetDigit.toFloat()) }
    
    LaunchedEffect(targetDigit) {
        val current = scrollY.value
        val currentDigit = ((current.roundToInt() % 10) + 10) % 10
        
        // Shortest path circular logic
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
            .height(with(density) { (itemHeightPx * 2.5f).toDp() }) // Adjusted height for tighter spacing
            .width(with(density) { (fontSize.toPx() * 0.75f).toDp() }),
        contentAlignment = Alignment.Center
    ) {
        val currentVal = scrollY.value
        val centerDigit = currentVal.roundToInt()
        
        // Draw the current digit and its neighbors to create the "Rolodex" effect
        for (i in centerDigit - 2..centerDigit + 2) {
            val displayDigit = (i % 10 + 10) % 10
            val distance = i - currentVal
            
            // Only draw if visible enough to save resources
            if (abs(distance) < 2.0f) {
                val scale = 1f - (abs(distance) * 0.3f).coerceAtMost(0.5f)
                val alpha = 1f - (abs(distance) * 0.9f).coerceAtMost(0.95f)
                val rotationX = distance * -55f
                
                // Only the center digit (distance ~ 0) should use the dynamic baseColor (flashing)
                // Neighboring digits stay dimmed and static
                val textColor = if (abs(distance) < 0.1f) baseColor else Color.Gray
                
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
                )
            }
        }
    }
}

@Composable
fun ListLoginCard(login: Utilities.MfaCode, amoled: Boolean, onLoginClick: (Utilities.MfaCode) -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = {
            onLoginClick(login)
        },
        modifier = modifier.fillMaxWidth().height(50.dp),
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
            contentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
        ),
        border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
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
                modifier = Modifier.size(32.dp)
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
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )

                    if (login.label.isNotBlank()) {
                        Text(
                            text = login.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (amoled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }

                Text(
                    text = login.account.ifBlank { stringResource(R.string.account) },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (amoled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun DashLoginCard(login: Utilities.MfaCode, utilities: Utilities, amoled: Boolean, onLoginClick: (Utilities.MfaCode) -> Unit, modifier: Modifier = Modifier) {
    val code = remember(login) {
        val generated = if (login.mode.contains("totp")) {
            utilities.generateTotp(login.secret, login.algorithm, login.digits, login.period)
        } else {
            utilities.generateHotp(login.secret, login.algorithm, login.digits, login.counter)
        }
        "${generated.substring(0, generated.length / 2)} ${generated.substring(generated.length / 2)}"
    }

    Card(
        onClick = {
            onLoginClick(login)
        },
        modifier = modifier.padding(2.dp),
        border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
        colors = CardDefaults.cardColors(
            containerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
            contentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        ) {
            BrandLogo(
                issuer = login.issuer,
                amoled = amoled,
                modifier = Modifier.size(24.dp),
                textStyle = MaterialTheme.typography.labelSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = login.issuer,
                style = MaterialTheme.typography.labelSmall,
                color = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = code.replace(" ", ""),
                style = MaterialTheme.typography.titleMedium,
                color = if (amoled) MaterialTheme.colorScheme.primary else lerp(MaterialTheme.colorScheme.primary, Color.White, 0.4f)
            )
        }
    }
}

@Composable
fun BubbleLoginCard(login: Utilities.MfaCode, amoled: Boolean, onLoginClick: (Utilities.MfaCode) -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = {
            onLoginClick(login)
        },
        modifier = modifier.size(54.dp),
        border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
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
            textStyle = MaterialTheme.typography.displaySmall
        )
    }
}

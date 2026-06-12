package net.cacko.shumen.ui.noise

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.delay
import net.cacko.shumen.ui.theme.ShumenTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorScreen(
    viewModel: NoiseViewModel = viewModel(),
    onSettingsClick: () -> Unit = {}
) {
    val currentDb by viewModel.currentDb.collectAsStateWithLifecycle()
    val threshold by viewModel.threshold.collectAsStateWithLifecycle()
    val isQuietModeActive by viewModel.isQuietModeActive.collectAsStateWithLifecycle()
    val isAlarmActive by viewModel.isAlarmActive.collectAsStateWithLifecycle()
    val clockOnlyMode by viewModel.clockOnlyMode.collectAsStateWithLifecycle()

    DisposableEffect(viewModel) {
        viewModel.startMonitoring()
        onDispose {
            viewModel.stopMonitoring()
        }
    }

    val alertColor by animateColorAsState(
        targetValue = when {
            isAlarmActive -> Color.Red
            currentDb > threshold + 15 -> Color.Red
            currentDb > threshold -> Color.Yellow
            else -> MaterialTheme.colorScheme.primary
        },
        animationSpec = tween(300),
        label = "AlertColor"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Dynamic background glow/overlay
            val animatedAlpha by animateFloatAsState(
                targetValue = if (currentDb > threshold || isAlarmActive) 0.15f else 0f,
                label = "OverlayAlpha"
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(alertColor.copy(alpha = animatedAlpha))
            )

            BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 32.dp)) {
                val isVertical = maxHeight > maxWidth
                
                if (clockOnlyMode) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        DigitalClock()
                    }
                } else if (isVertical) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Top: Meter
                        val meterSize = if (this@BoxWithConstraints.maxWidth < 400.dp) this@BoxWithConstraints.maxWidth else 400.dp
                        GaugeDbMeter(
                            db = currentDb.toFloat(),
                            threshold = threshold.toFloat(),
                            modifier = Modifier.size(meterSize)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Bottom: Info
                        MonitorInfo(currentDb, alertColor, threshold, isAlarmActive)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Half: Meter
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            GaugeDbMeter(
                                db = currentDb.toFloat(),
                                threshold = threshold.toFloat(),
                                modifier = Modifier
                                    .size(400.dp)
                            )
                        }

                        // Right Half: Info
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            MonitorInfo(currentDb, alertColor, threshold, isAlarmActive)
                        }
                    }
                }
            }

            // Alarm Overlay
            AnimatedVisibility(
                visible = isAlarmActive,
                enter = fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.8f),
                exit = fadeOut(tween(300)) + scaleOut(tween(300), targetScale = 0.8f)
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Red.copy(alpha = 0.4f))
                        .clickable { onSettingsClick() }, // Quick access via remote CLICK
                    contentAlignment = Alignment.Center
                ) {
                    val scaleFactor = (minOf(maxWidth, maxHeight) / 400.dp).coerceAtLeast(0.5f)
                    
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "AlarmScale")
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.2f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(500, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "IconScale"
                        )
                        
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = null,
                            modifier = Modifier.size(150.dp * scaleFactor).graphicsLayer(scaleX = scale, scaleY = scale),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.height(24.dp * scaleFactor))
                        Text(
                            text = "ALARM!",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 90.sp * scaleFactor
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "The room is too loud! Please lower your noise.",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = MaterialTheme.typography.headlineSmall.fontSize * scaleFactor
                            ),
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Quiet Mode Overlay
            AnimatedVisibility(
                visible = isQuietModeActive && !isAlarmActive,
                enter = fadeIn(tween(1000)),
                exit = fadeOut(tween(1000))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = null,
                            modifier = Modifier.size(120.dp),
                            tint = Color.Yellow
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "QUIET PLEASE",
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.Yellow
                        )
                        Text(
                            text = "Ambient noise is disrupting the experience",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Settings Button - Optimized for TV Focus
            var isFabFocused by remember { mutableStateOf(false) }
            
            FloatingActionButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(48.dp)
                    .zIndex(10f) // Keep above all overlays
                    .onFocusChanged { isFabFocused = it.isFocused }
                    .focusable(),
                containerColor = if (isFabFocused) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.secondaryContainer,
                contentColor = if (isFabFocused) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(
                    Icons.Rounded.Settings, 
                    contentDescription = "Settings",
                    modifier = Modifier.size(if (isFabFocused) 32.dp else 24.dp)
                )
            }
        }
    }
}

@Composable
fun MonitorInfo(
    currentDb: Double,
    alertColor: Color,
    threshold: Double,
    isAlarmActive: Boolean
) {
    BoxWithConstraints(contentAlignment = Alignment.Center) {
        val scaleFactor = (minOf(maxWidth, maxHeight) / 400.dp).coerceAtLeast(0.4f)
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${currentDb.toInt()}",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 120.sp * scaleFactor
                    ),
                    color = alertColor,
                    modifier = Modifier.width(200.dp * scaleFactor),
                    textAlign = TextAlign.End
                )
                Text(
                    text = "dB",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = MaterialTheme.typography.headlineLarge.fontSize * scaleFactor
                    ),
                    modifier = Modifier.padding(bottom = 24.dp * scaleFactor, start = 8.dp * scaleFactor),
                    color = alertColor.copy(alpha = 0.7f)
                )
            }

            Text(
                text = when {
                    currentDb < 40 -> "SILENT"
                    currentDb < 55 -> "QUIET"
                    currentDb < 75 -> "MODERATE"
                    currentDb < 90 -> "LOUD"
                    else -> "EXTREME"
                },
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp * scaleFactor,
                    fontSize = MaterialTheme.typography.headlineMedium.fontSize * scaleFactor
                ),
                color = alertColor.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 8.dp * scaleFactor)
            )

            AnimatedVisibility(
                visible = currentDb > threshold || isAlarmActive,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(top = 16.dp * scaleFactor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp * scaleFactor, vertical = 12.dp * scaleFactor),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(24.dp * scaleFactor)
                        )
                        Spacer(modifier = Modifier.width(12.dp * scaleFactor))
                        Text(
                            text = if (isAlarmActive) "ALARM ACTIVE!" else "NOISE LIMIT EXCEEDED",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = MaterialTheme.typography.labelLarge.fontSize * scaleFactor
                            ),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GaugeDbMeter(
    db: Float,
    threshold: Float,
    modifier: Modifier = Modifier
) {
    val animatedDb by animateFloatAsState(
        targetValue = db,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "DbAnimation"
    )

    Box(modifier = modifier.padding(16.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size
            val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
            val radius = canvasSize.minDimension / 2f
            val innerRadius = radius * 0.85f
            
            // 1. Static Outer Futuristic Ring
            drawArc(
                color = Color.Cyan.copy(alpha = 0.1f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 1.dp.toPx())
            )

            // 2. Track (Background)
            val startAngle = 135f
            val sweepTotal = 270f
            drawArc(
                color = Color.DarkGray.copy(alpha = 0.15f),
                startAngle = startAngle,
                sweepAngle = sweepTotal,
                useCenter = false,
                style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
            )

            // 3. Precision Digital Ticks
            val tickCount = 51
            for (i in 0 until tickCount) {
                val angle = startAngle + (i.toFloat() / (tickCount - 1)) * sweepTotal
                val angleRad = Math.toRadians(angle.toDouble())
                val isMajor = i % 5 == 0
                val tickLength = if (isMajor) 14.dp.toPx() else 8.dp.toPx()
                val tickWidth = if (isMajor) 3.dp.toPx() else 1.5.dp.toPx()
                val tickAlpha = if (isMajor) 0.5f else 0.2f
                
                val start = Offset(
                    x = center.x + (radius * 0.92f * Math.cos(angleRad)).toFloat(),
                    y = center.y + (radius * 0.92f * Math.sin(angleRad)).toFloat()
                )
                val end = Offset(
                    x = center.x + ((radius * 0.92f + tickLength) * Math.cos(angleRad)).toFloat(),
                    y = center.y + ((radius * 0.92f + tickLength) * Math.sin(angleRad)).toFloat()
                )
                drawLine(
                    color = Color.White.copy(alpha = tickAlpha),
                    start = start,
                    end = end,
                    strokeWidth = tickWidth
                )
            }

            // 4. Dynamic Progress with Neon Glow
            val progressSweep = (animatedDb.coerceIn(0f, 100f) / 100f) * sweepTotal
            val progressColor = when {
                animatedDb > threshold + 15 -> Color.Red
                animatedDb > threshold -> Color.Yellow
                else -> Color.Cyan
            }

            // Outer Glow (Multiple layered arcs)
            drawArc(
                color = progressColor.copy(alpha = 0.1f),
                startAngle = startAngle,
                sweepAngle = progressSweep,
                useCenter = false,
                style = Stroke(width = 36.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = progressColor.copy(alpha = 0.2f),
                startAngle = startAngle,
                sweepAngle = progressSweep,
                useCenter = false,
                style = Stroke(width = 28.dp.toPx(), cap = StrokeCap.Round)
            )
            
            // Primary Progress Arc
            drawArc(
                brush = Brush.sweepGradient(
                    0f to progressColor.copy(alpha = 0.3f),
                    1f to progressColor,
                    center = center
                ),
                startAngle = startAngle,
                sweepAngle = progressSweep,
                useCenter = false,
                style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
            )

            // 5. Futuristic Floating Needle
            val needleAngle = startAngle + progressSweep
            val needleRad = Math.toRadians(needleAngle.toDouble())
            val needleInner = innerRadius * 0.2f
            val needleOuter = innerRadius * 0.95f
            
            val nStart = Offset(
                x = center.x + (needleInner * Math.cos(needleRad)).toFloat(),
                y = center.y + (needleInner * Math.sin(needleRad)).toFloat()
            )
            val nEnd = Offset(
                x = center.x + (needleOuter * Math.cos(needleRad)).toFloat(),
                y = center.y + (needleOuter * Math.sin(needleRad)).toFloat()
            )
            
            // Needle Glow
            drawLine(
                color = progressColor.copy(alpha = 0.3f),
                start = nStart,
                end = nEnd,
                strokeWidth = 14.dp.toPx(),
                cap = StrokeCap.Round
            )
            // Needle Core
            drawLine(
                color = Color.White,
                start = nStart,
                end = nEnd,
                strokeWidth = 5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // 6. Threshold Marker (Digital line)
            val tAngle = startAngle + (threshold / 100f) * sweepTotal
            val tRad = Math.toRadians(tAngle.toDouble())
            val tStart = Offset(
                x = center.x + (radius * 0.85f * Math.cos(tRad)).toFloat(),
                y = center.y + (radius * 0.85f * Math.sin(tRad)).toFloat()
            )
            val tEnd = Offset(
                x = center.x + (radius * 1.15f * Math.cos(tRad)).toFloat(),
                y = center.y + (radius * 1.15f * Math.sin(tRad)).toFloat()
            )
            drawLine(
                color = Color.White,
                start = tStart,
                end = tEnd,
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Square
            )
            
            // 7. Core Hub (Futuristic digital center)
            drawCircle(
                color = Color.Black,
                radius = 14.dp.toPx(),
                center = center
            )
            drawCircle(
                color = progressColor,
                radius = 8.dp.toPx(),
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}

@Composable
fun DigitalClock(modifier: Modifier = Modifier) {
    var time by remember { mutableStateOf(LocalTime.now()) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var colonVisible by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        while (true) {
            time = LocalTime.now()
            date = LocalDate.now()
            // Quick blink logic: Visible for 850ms, Dimmed for 150ms
            colonVisible = true
            delay(850)
            colonVisible = false
            delay(150)
        }
    }
    
    val hourFormatter = DateTimeFormatter.ofPattern("HH")
    val minuteFormatter = DateTimeFormatter.ofPattern("mm")
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())
    
    BoxWithConstraints(
        modifier = modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val isVertical = maxHeight > maxWidth
        val clockScale = if (isVertical) {
            (maxWidth / 300.dp).coerceAtMost(1.5f)
        } else {
            (maxHeight / 300.dp).coerceAtMost(2.0f)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Day of Week and Date
            Text(
                text = date.format(dateFormatter).uppercase(),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp * clockScale,
                    fontSize = (if (isVertical) 18.sp else 22.sp) * clockScale,
                    fontFamily = FontFamily.SansSerif
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 12.dp * clockScale)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Hours - Bold & Primary
                Text(
                    text = time.format(hourFormatter),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = (if (isVertical) 110.sp else 160.sp) * clockScale,
                        letterSpacing = (-4).sp * clockScale,
                        fontFamily = FontFamily.SansSerif
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Colon - Animated "Flicker"
                val colonAlpha by animateFloatAsState(
                    targetValue = if (colonVisible) 1f else 0.1f,
                    animationSpec = tween(durationMillis = 100),
                    label = "ColonBlink"
                )
                
                Text(
                    text = ":",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.ExtraLight,
                        fontSize = (if (isVertical) 110.sp else 160.sp) * clockScale,
                        fontFamily = FontFamily.SansSerif
                    ),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = colonAlpha),
                    modifier = Modifier.padding(horizontal = 2.dp * clockScale)
                )

                // Minutes - Thin & Primary (Same color as hours)
                Text(
                    text = time.format(minuteFormatter),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.ExtraLight,
                        fontSize = (if (isVertical) 110.sp else 160.sp) * clockScale,
                        letterSpacing = (-4).sp * clockScale,
                        fontFamily = FontFamily.SansSerif
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true, device = "id:tv_1080p")
@Composable
fun MonitorScreenPreview() {
    ShumenTheme {
        MonitorScreen()
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun MonitorScreenVerticalPreview() {
    ShumenTheme {
        MonitorScreen()
    }
}

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
                
                if (isVertical) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Top: Meter
                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            LinearDbMeter(
                                db = if (isAlarmActive) 100f else currentDb.toFloat(),
                                threshold = threshold.toFloat(),
                                modifier = Modifier
                                    .width(100.dp)
                                    .fillMaxHeight(0.8f)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Bottom: Info
                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            MonitorInfo(currentDb, alertColor, threshold, isAlarmActive)
                        }
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
                            LinearDbMeter(
                                db = if (isAlarmActive) 100f else currentDb.toFloat(),
                                threshold = threshold.toFloat(),
                                modifier = Modifier
                                    .width(120.dp)
                                    .fillMaxHeight(0.7f)
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Red.copy(alpha = 0.4f))
                        .clickable { onSettingsClick() }, // Quick access via remote CLICK
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                            modifier = Modifier.size(150.dp).graphicsLayer(scaleX = scale, scaleY = scale),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "ALARM!",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 100.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "The room is too loud! Please lower your noise.",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White.copy(alpha = 0.8f)
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "${currentDb.toInt()}",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 120.sp
                ),
                color = alertColor,
                modifier = Modifier.width(200.dp),
                textAlign = TextAlign.End
            )
            Text(
                text = "dB",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 24.dp, start = 8.dp),
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
                letterSpacing = 2.sp
            ),
            color = alertColor.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 8.dp)
        )

        AnimatedVisibility(
            visible = currentDb > threshold || isAlarmActive,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.VolumeOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isAlarmActive) "ALARM ACTIVE!" else "NOISE LIMIT EXCEEDED",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
fun LinearDbMeter(
    db: Float,
    threshold: Float,
    modifier: Modifier = Modifier
) {
    val animatedDb by animateFloatAsState(
        targetValue = db,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "DbAnimation"
    )

    val segmentCount = 20
    val segmentGap = 4.dp

    Box(modifier = modifier.background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(8.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            val totalGapHeight = (segmentCount - 1) * segmentGap.toPx()
            val segmentHeight = (canvasHeight - totalGapHeight) / segmentCount
            
            // Draw background segments (LEDs off)
            for (i in 0 until segmentCount) {
                val y = canvasHeight - (i + 1) * (segmentHeight + segmentGap.toPx()) + segmentGap.toPx()
                drawRoundRect(
                    color = Color.DarkGray.copy(alpha = 0.3f),
                    topLeft = Offset(0f, y),
                    size = Size(canvasWidth, segmentHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }

            // Draw active segments (LEDs on)
            val activeSegments = ((animatedDb / 100f) * segmentCount).toInt()
            for (i in 0 until activeSegments.coerceAtMost(segmentCount)) {
                val y = canvasHeight - (i + 1) * (segmentHeight + segmentGap.toPx()) + segmentGap.toPx()
                val segmentColor = when {
                    i > (0.85f * segmentCount).toInt() -> Color.Red
                    i > (0.65f * segmentCount).toInt() -> Color.Yellow
                    else -> Color.Green
                }
                
                drawRoundRect(
                    color = segmentColor,
                    topLeft = Offset(0f, y),
                    size = Size(canvasWidth, segmentHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }

            // Threshold marker
            val thresholdY = canvasHeight - (threshold / 100f) * canvasHeight
            drawLine(
                color = Color.White,
                start = Offset(-8.dp.toPx(), thresholdY),
                end = Offset(canvasWidth + 8.dp.toPx(), thresholdY),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
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

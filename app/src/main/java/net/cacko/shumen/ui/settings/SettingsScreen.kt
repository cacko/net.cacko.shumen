package net.cacko.shumen.ui.settings

import android.media.RingtoneManager
import android.net.Uri
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import net.cacko.shumen.ui.noise.NoiseViewModel
import net.cacko.shumen.ui.theme.ShumenTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: NoiseViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val threshold by viewModel.threshold.collectAsStateWithLifecycle()
    val sensitivity by viewModel.sensitivity.collectAsStateWithLifecycle()
    val alarmDuration by viewModel.alarmDuration.collectAsStateWithLifecycle()
    val alarmEnabled by viewModel.alarmEnabled.collectAsStateWithLifecycle()
    val alarmSoundUri by viewModel.alarmSoundUri.collectAsStateWithLifecycle()
    val alarmVolume by viewModel.alarmVolume.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    DisposableEffect(viewModel) {
        viewModel.setInSettings(true)
        onDispose {
            viewModel.setInSettings(false)
        }
    }

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            viewModel.setAlarmSoundUri(uri?.toString())
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            // Take persistable URI permission so the file remains accessible after app restart
            try {
                val contentResolver = context.contentResolver
                contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            viewModel.setAlarmSoundUri(uri.toString())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            val isVertical = maxHeight > maxWidth
            val horizontalPadding = if (isVertical) 24.dp else 64.dp
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(48.dp)
            ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Alert Level Setting - TV Optimized
            Column {
                Text(
                    text = "Alert Level (dB): ${threshold.toInt()} dB",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Set the decibel level that triggers the alarm and visual alerts. Use LEFT/RIGHT on your remote to adjust.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                var isSliderFocused by remember { mutableStateOf(false) }
                // Use local state for immediate visual feedback during adjustment
                var sliderValue by remember(threshold) { mutableStateOf(threshold.toFloat()) }
                
                Slider(
                    value = sliderValue,
                    onValueChange = { 
                        sliderValue = it
                        viewModel.setThreshold(it.toDouble()) 
                    },
                    valueRange = 40f..100f,
                    steps = 59,
                    modifier = Modifier
                        .onFocusChanged { isSliderFocused = it.isFocused }
                        .onKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown) {
                                when (event.key) {
                                    Key.DirectionLeft -> {
                                        val newValue = (sliderValue - 1f).coerceAtLeast(40f)
                                        sliderValue = newValue
                                        viewModel.setThreshold(newValue.toDouble())
                                        true
                                    }
                                    Key.DirectionRight -> {
                                        val newValue = (sliderValue + 1f).coerceAtMost(100f)
                                        sliderValue = newValue
                                        viewModel.setThreshold(newValue.toDouble())
                                        true
                                    }
                                    else -> false
                                }
                            } else {
                                false
                            }
                        }
                        .focusable(),
                    colors = SliderDefaults.colors(
                        thumbColor = if (isSliderFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            // Audible Alarm Toggle - TV Optimized
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Audible Alarm",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Play a loud siren when the alert level is exceeded.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    var isSwitchFocused by remember { mutableStateOf(false) }
                    Switch(
                        checked = alarmEnabled,
                        onCheckedChange = { viewModel.setAlarmEnabled(it) },
                        modifier = Modifier
                            .onFocusChanged { isSwitchFocused = it.isFocused }
                            .padding(start = 16.dp),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = if (isSwitchFocused) Color.White else MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = if (isSwitchFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }

            // Alarm Sound Selection - TV Optimized
            Column {
                Text(
                    text = "Alarm Sound",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Current: ${if (alarmSoundUri == null) "Industrial Siren (Default)" else "Custom Selection"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                val buttonContent = @Composable {
                    Button(
                        onClick = {
                            val intent = android.content.Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Sound")
                                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, alarmSoundUri?.let { Uri.parse(it) })
                            }
                            ringtonePickerLauncher.launch(intent)
                        },
                        modifier = if (isVertical) Modifier.fillMaxWidth() else Modifier
                    ) {
                        Text("System Sounds")
                    }

                    Button(
                        onClick = { filePickerLauncher.launch(arrayOf("audio/*")) },
                        modifier = if (isVertical) Modifier.fillMaxWidth() else Modifier
                    ) {
                        Text("Custom File")
                    }

                    if (alarmSoundUri != null) {
                        TextButton(
                            onClick = { viewModel.setAlarmSoundUri(null) },
                            modifier = if (isVertical) Modifier.fillMaxWidth() else Modifier
                        ) {
                            Text("Reset to Default", textAlign = TextAlign.Center)
                        }
                    }
                }

                if (isVertical) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        buttonContent()
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        buttonContent()
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Alarm Volume Setting
                Text(
                    text = "Alarm Volume: ${(alarmVolume * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                var isVolSliderFocused by remember { mutableStateOf(false) }
                var volSliderValue by remember(alarmVolume) { mutableStateOf(alarmVolume.toFloat()) }

                Slider(
                    value = volSliderValue,
                    onValueChange = {
                        volSliderValue = it
                        viewModel.setAlarmVolume(it.toDouble())
                    },
                    valueRange = 0f..1f,
                    steps = 10,
                    modifier = Modifier
                        .onFocusChanged { isVolSliderFocused = it.isFocused }
                        .onKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown) {
                                when (event.key) {
                                    Key.DirectionLeft -> {
                                        val newValue = (volSliderValue - 0.1f).coerceAtLeast(0f)
                                        volSliderValue = newValue
                                        viewModel.setAlarmVolume(newValue.toDouble())
                                        true
                                    }
                                    Key.DirectionRight -> {
                                        val newValue = (volSliderValue + 0.1f).coerceAtMost(1f)
                                        volSliderValue = newValue
                                        viewModel.setAlarmVolume(newValue.toDouble())
                                        true
                                    }
                                    else -> false
                                }
                            } else {
                                false
                            }
                        }
                        .focusable(),
                    colors = SliderDefaults.colors(
                        thumbColor = if (isVolSliderFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            // Alarm Duration Setting - TV Optimized
            Column {
                Text(
                    text = "Alarm Duration: ${alarmDuration.toInt()} seconds",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Set how long the alarm and visual alert stay on screen. Use LEFT/RIGHT on your remote to adjust.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))

                var isDurationSliderFocused by remember { mutableStateOf(false) }
                var durationSliderValue by remember(alarmDuration) { mutableStateOf(alarmDuration.toFloat()) }

                Slider(
                    value = durationSliderValue,
                    onValueChange = {
                        durationSliderValue = it
                        viewModel.setAlarmDuration(it.toDouble())
                    },
                    valueRange = 1f..10f,
                    steps = 8,
                    modifier = Modifier
                        .onFocusChanged { isDurationSliderFocused = it.isFocused }
                        .onKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown) {
                                when (event.key) {
                                    Key.DirectionLeft -> {
                                        val newValue = (durationSliderValue - 1f).coerceAtLeast(1f)
                                        durationSliderValue = newValue
                                        viewModel.setAlarmDuration(newValue.toDouble())
                                        true
                                    }
                                    Key.DirectionRight -> {
                                        val newValue = (durationSliderValue + 1f).coerceAtMost(10f)
                                        durationSliderValue = newValue
                                        viewModel.setAlarmDuration(newValue.toDouble())
                                        true
                                    }
                                    else -> false
                                }
                            } else {
                                false
                            }
                        }
                        .focusable(),
                    colors = SliderDefaults.colors(
                        thumbColor = if (isDurationSliderFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            // Sensitivity Setting - TV Optimized Slider
            Column {
                Text(
                    text = "Microphone Sensitivity: ${String.format(Locale.US, "%.1f", sensitivity)}x",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Increase if the app isn't picking up quiet noises, or decrease if it's too sensitive. Use LEFT/RIGHT on your remote to adjust.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))

                var isSensSliderFocused by remember { mutableStateOf(false) }
                var sensSliderValue by remember(sensitivity) { mutableStateOf(sensitivity.toFloat()) }

                Slider(
                    value = sensSliderValue,
                    onValueChange = {
                        sensSliderValue = it
                        viewModel.setSensitivity(it.toDouble())
                    },
                    valueRange = 0.1f..3.0f,
                    steps = 28,
                    modifier = Modifier
                        .onFocusChanged { isSensSliderFocused = it.isFocused }
                        .onKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown) {
                                when (event.key) {
                                    Key.DirectionLeft -> {
                                        val newValue = (sensSliderValue - 0.1f).coerceAtLeast(0.1f)
                                        sensSliderValue = newValue
                                        viewModel.setSensitivity(newValue.toDouble())
                                        true
                                    }
                                    Key.DirectionRight -> {
                                        val newValue = (sensSliderValue + 0.1f).coerceAtMost(3.0f)
                                        sensSliderValue = newValue
                                        viewModel.setSensitivity(newValue.toDouble())
                                        true
                                    }
                                    else -> false
                                }
                            } else {
                                false
                            }
                        }
                        .focusable(),
                    colors = SliderDefaults.colors(
                        thumbColor = if (isSensSliderFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = "Changes are saved automatically.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}
}

@Preview(showBackground = true, device = "id:tv_1080p")
@Composable
fun SettingsScreenPreview() {
    ShumenTheme {
        SettingsScreen()
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun SettingsScreenVerticalPreview() {
    ShumenTheme {
        SettingsScreen()
    }
}

package net.cacko.shumen.ui.settings

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import net.cacko.shumen.ui.noise.NoiseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: NoiseViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val threshold by viewModel.threshold.collectAsStateWithLifecycle()
    val sensitivity by viewModel.sensitivity.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 64.dp)
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

            // Sensitivity Setting - TV Optimized Chips
            Column {
                Text(
                    text = "Microphone Sensitivity",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Increase if the app isn't picking up quiet noises, or decrease if it's too sensitive.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    listOf(0.5, 1.0, 1.5, 2.0).forEach { value ->
                        var isChipFocused by remember { mutableStateOf(false) }
                        FilterChip(
                            selected = sensitivity == value,
                            onClick = { viewModel.setSensitivity(value) },
                            label = { Text("${value}x") },
                            modifier = Modifier.onFocusChanged { isChipFocused = it.isFocused },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (isChipFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                                containerColor = if (isChipFocused) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
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

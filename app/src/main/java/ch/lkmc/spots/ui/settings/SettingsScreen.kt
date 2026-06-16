package ch.lkmc.spots.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.lkmc.spots.data.DetectionSource
import ch.lkmc.spots.data.SPOT_PALETTE
import ch.lkmc.spots.engine.EdgeMode
import ch.lkmc.spots.ui.SpotsViewModel
import ch.lkmc.spots.ui.components.LabeledSlider
import ch.lkmc.spots.ui.components.SectionCard
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: SpotsViewModel, onBack: () -> Unit) {
    val s by vm.settings.collectAsStateWithLifecycle()

    val activityPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            vm.setDetectionSource(
                if (granted) DetectionSource.ACTIVITY_RECOGNITION else DetectionSource.SENSORS_ONLY,
            )
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SectionCard(title = "Responsiveness") {
                LabeledSlider(
                    label = "Sensitivity",
                    value = s.sensitivity,
                    onValueChange = vm::setSensitivity,
                    valueText = sensitivityLabel(s.sensitivity),
                )
                Text(
                    "How strongly the dots react to the car's motion.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SectionCard(title = "Appearance") {
                LabeledSlider(
                    label = "Number of dots",
                    value = s.dotCount.toFloat(),
                    onValueChange = { vm.setDotCount(it.roundToInt()) },
                    valueText = s.dotCount.toString(),
                    valueRange = 8f..80f,
                )
                LabeledSlider(
                    label = "Dot size",
                    value = s.dotSize,
                    onValueChange = vm::setDotSize,
                    valueText = "${(s.dotSize * 100).roundToInt()}%",
                )
                LabeledSlider(
                    label = "Opacity",
                    value = s.opacity,
                    onValueChange = vm::setOpacity,
                    valueText = "${(s.opacity * 100).roundToInt()}%",
                    valueRange = 0.05f..1f,
                )

                Text("Colour", style = MaterialTheme.typography.bodyLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SPOT_PALETTE.forEach { swatch ->
                        val selected = (s.colorRgb and 0xFFFFFF) == (swatch.rgb and 0xFFFFFF)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(
                                        Color(0xFF000000.toInt() or swatch.rgb),
                                        CircleShape,
                                    )
                                    .border(
                                        width = if (selected) 3.dp else 1.dp,
                                        color = if (selected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outline
                                        },
                                        shape = CircleShape,
                                    )
                                    .clickable { vm.setColor(swatch.rgb) },
                            )
                        }
                    }
                }

                ToggleRow(
                    title = "Dynamic pattern",
                    subtitle = "A livelier, organic shimmer.",
                    checked = s.dynamic,
                    onCheckedChange = vm::setDynamic,
                )

                Text("Edges", style = MaterialTheme.typography.bodyLarge)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val options = listOf(EdgeMode.ALL, EdgeMode.SIDES_ONLY)
                    options.forEachIndexed { i, e ->
                        SegmentedButton(
                            selected = s.edges == e,
                            onClick = { vm.setEdges(e) },
                            shape = SegmentedButtonDefaults.itemShape(i, options.size),
                            label = { Text(if (e == EdgeMode.ALL) "All edges" else "Sides only") },
                        )
                    }
                }
            }

            SectionCard(title = "Automatic detection") {
                Text(
                    "How Spots decides you're in a moving vehicle (Automatic mode).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val options = listOf(DetectionSource.SENSORS_ONLY, DetectionSource.ACTIVITY_RECOGNITION)
                    options.forEachIndexed { i, d ->
                        SegmentedButton(
                            selected = s.detectionSource == d,
                            onClick = {
                                if (d == DetectionSource.ACTIVITY_RECOGNITION &&
                                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                                ) {
                                    activityPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                                } else {
                                    vm.setDetectionSource(d)
                                }
                            },
                            shape = SegmentedButtonDefaults.itemShape(i, options.size),
                            label = {
                                Text(if (d == DetectionSource.SENSORS_ONLY) "Sensors" else "Activity")
                            },
                        )
                    }
                }
                Text(
                    "“Sensors” needs no permissions and works on any device. “Activity” " +
                        "uses Google Play services for a stronger signal.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SectionCard(title = "Advanced") {
                ToggleRow(
                    title = "Keep screen on",
                    subtitle = "Prevent the screen from sleeping while active.",
                    checked = s.keepScreenOn,
                    onCheckedChange = vm::setKeepScreenOn,
                )
                ToggleRow(
                    title = "Show comfort meter",
                    subtitle = "A live, ISO-2631-style read-out of how provocative the ride is.",
                    checked = s.showComfortMeter,
                    onCheckedChange = vm::setShowComfortMeter,
                )
            }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun sensitivityLabel(v: Float): String = when {
    v < 0.33f -> "Subtle"
    v < 0.66f -> "Balanced"
    else -> "Strong"
}

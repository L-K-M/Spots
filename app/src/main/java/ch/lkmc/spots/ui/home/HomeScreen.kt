package ch.lkmc.spots.ui.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.lkmc.spots.data.CueMode
import ch.lkmc.spots.ui.SpotsViewModel
import ch.lkmc.spots.ui.components.LivePreview
import ch.lkmc.spots.ui.components.SectionCard
import ch.lkmc.spots.ui.needsNotificationPermission
import ch.lkmc.spots.ui.overlayPermissionIntent
import ch.lkmc.spots.ui.rememberNotificationPermissionGranted
import ch.lkmc.spots.ui.rememberOverlayPermissionGranted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: SpotsViewModel,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    val context = LocalContext.current
    val settings by vm.settings.collectAsStateWithLifecycle()
    val overlay by vm.overlayState.collectAsStateWithLifecycle()
    val overlayGranted by rememberOverlayPermissionGranted()
    val notifGranted by rememberNotificationPermissionGranted()

    val overlayLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
    val notifLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    // True when the user picked an on-mode but still owes us the overlay
    // permission; once granted, we start the service automatically.
    var pendingStart by remember { mutableStateOf(false) }
    var demoPreview by remember { mutableStateOf(false) }

    fun selectMode(mode: CueMode) {
        vm.setMode(mode)
        if (mode == CueMode.OFF) {
            pendingStart = false
            vm.stopService()
            return
        }
        if (needsNotificationPermission() && !notifGranted) {
            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (overlayGranted) {
            vm.startService()
        } else {
            pendingStart = true
            overlayLauncher.launch(overlayPermissionIntent(context))
        }
    }

    LaunchedEffect(overlayGranted) {
        if (overlayGranted && pendingStart) {
            pendingStart = false
            vm.startService()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spots") },
                actions = {
                    IconButton(onClick = onOpenAbout) {
                        Icon(Icons.Filled.Info, contentDescription = "About")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(4.dp))
            Text(
                "Vehicle motion cues for Android",
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                "Spots paints gentle dots at the edges of your screen that move with " +
                    "your car's motion, so reading or watching as a passenger is easier " +
                    "on your stomach — in any app.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (!overlayGranted) {
                PermissionCard(
                    title = "Allow “Display over other apps”",
                    body = "Spots needs this to draw the dots on top of whatever you're " +
                        "using. Tap Grant, enable Spots, then come back.",
                    cta = "Grant",
                    onClick = { overlayLauncher.launch(overlayPermissionIntent(context)) },
                )
            }
            if (needsNotificationPermission() && !notifGranted) {
                PermissionCard(
                    title = "Allow notifications",
                    body = "A small ongoing notification is required while Spots runs in " +
                        "the background. It's silent.",
                    cta = "Allow",
                    onClick = { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                )
            }

            SectionCard(title = "Mode") {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val options = listOf(CueMode.OFF, CueMode.ON, CueMode.AUTOMATIC)
                    options.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = settings.mode == mode,
                            onClick = { selectMode(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index, options.size),
                            label = { Text(modeLabel(mode)) },
                        )
                    }
                }
                Text(
                    text = statusLine(settings.mode, overlay.running, overlay.overlayVisible),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = when (settings.mode) {
                        CueMode.OFF -> "The overlay is off."
                        CueMode.ON -> "The dots show whenever Spots is running."
                        CueMode.AUTOMATIC -> "The dots appear automatically when Spots " +
                            "detects you're moving in a vehicle, and hide when you stop."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SectionCard(title = "Live preview") {
                LivePreview(
                    settings = settings,
                    demo = demoPreview,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Demo drive", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            if (demoPreview) {
                                "Playing a synthetic ride."
                            } else {
                                "Or just tilt and move your phone to see the dots react."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(checked = demoPreview, onCheckedChange = { demoPreview = it })
                }
            }

            if (settings.showComfortMeter) {
                ComfortMeterCard(
                    running = overlay.running,
                    level = overlay.comfortLevel,
                    illnessPercent = overlay.illnessPercent,
                )
            }

            Text(
                text = "Spots is an experimental comfort aid, not a medical device. " +
                    "For passengers only — never for the driver.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp),
            )
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    body: String,
    cta: String,
    onClick: () -> Unit,
) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
            )
            Spacer(Modifier.width(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onClick, modifier = Modifier.align(Alignment.End)) { Text(cta) }
    }
}

@Composable
private fun ComfortMeterCard(running: Boolean, level: Float, illnessPercent: Float) {
    SectionCard(title = "Comfort meter") {
        if (!running) {
            Text(
                "Start Spots to measure how provocative the ride is.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@SectionCard
        }
        // Weighted acceleration ~0..2 m/s² maps to a 0..1 bar.
        LinearProgressIndicator(
            progress = { (level / 2f).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            "Frequency-weighted motion (peaks near 0.2 Hz, the most nauseogenic band). " +
                "Estimated discomfort so far: ${illnessPercent.toInt()}%.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun modeLabel(mode: CueMode): String = when (mode) {
    CueMode.OFF -> "Off"
    CueMode.ON -> "On"
    CueMode.AUTOMATIC -> "Automatic"
}

private fun statusLine(mode: CueMode, running: Boolean, visible: Boolean): String = when {
    mode == CueMode.OFF || !running -> "Off"
    visible -> "Active — showing motion cues"
    mode == CueMode.AUTOMATIC -> "Armed — waiting for vehicle motion"
    else -> "Active"
}

package ch.lkmc.spots.ui.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ch.lkmc.spots.BuildConfig
import ch.lkmc.spots.ui.components.SectionCard

private const val REPO_URL = "https://github.com/L-K-M/Spots"
private const val RESEARCH_URL = "https://github.com/L-K-M/Spots/tree/main/docs/research"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    fun open(url: String) {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About Spots") },
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
            SectionCard(title = "How it works") {
                Body(
                    "Reading in a moving car can make you queasy because your eyes see a " +
                        "still screen while your inner ear feels the car accelerate, brake " +
                        "and turn. Your brain dislikes that mismatch.\n\n" +
                        "Spots adds gentle dots to the edges of your screen that stream the " +
                        "way the world appears to move: forward acceleration sends them " +
                        "down, braking lifts them up, a left turn slides them right. Your " +
                        "peripheral vision — which is very sensitive to motion — gets a " +
                        "signal that matches what your body feels, while the centre of the " +
                        "screen stays clear for whatever you're doing.",
                )
            }

            SectionCard(title = "The science, in brief") {
                Body(
                    "The idea rests on the sensory-conflict account of motion sickness and " +
                        "on the fact that peripheral, congruent visual motion can reduce that " +
                        "conflict. It mirrors Apple's “Vehicle Motion Cues” and the 2019 " +
                        "University of Salzburg “Bubble Margin” study.\n\n" +
                        "The supporting evidence is real but still preliminary — small " +
                        "studies, modest effects, individual variation. Spots is an " +
                        "experimental comfort aid, not a medical device.",
                )
                OutlinedButton(onClick = { open(RESEARCH_URL) }) { Text("Read the research") }
            }

            SectionCard(title = "Privacy") {
                Body(
                    "Everything happens on your device. Spots has no internet permission, " +
                        "no account, no analytics and no ads. Motion data never leaves the " +
                        "phone.",
                )
            }

            SectionCard(title = "Safety") {
                Body(
                    "Spots is for passengers only — never use it while driving. If you feel " +
                        "unwell, stop using your device and look at the horizon.",
                )
            }

            SectionCard(title = "About") {
                Body("Version ${BuildConfig.VERSION_NAME}\nMIT-licensed, open source.")
                OutlinedButton(onClick = { open(REPO_URL) }) { Text("Open on GitHub") }
            }
        }
    }
}

@Composable
private fun Body(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

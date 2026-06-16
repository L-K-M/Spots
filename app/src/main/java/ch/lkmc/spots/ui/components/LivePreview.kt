package ch.lkmc.spots.ui.components

import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import ch.lkmc.spots.data.SpotsSettings
import ch.lkmc.spots.engine.MotionCueEngine
import ch.lkmc.spots.motion.MotionSample
import ch.lkmc.spots.motion.SensorMotionSource
import java.util.concurrent.atomic.AtomicReference

private fun displayRotation(context: Context): Int {
    val dm = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    return dm.getDisplay(Display.DEFAULT_DISPLAY)?.rotation ?: 0
}

/**
 * A live, in-app preview of the cue: it runs the **real** [MotionCueEngine] off the
 * phone's actual sensors, so tilting or moving the device streams the spots exactly
 * as the overlay would. Lets users dial in the look — and feel the effect — before
 * a trip, with no overlay permission required.
 */
@Composable
fun LivePreview(
    settings: SpotsSettings,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val engine = remember { MotionCueEngine() }
    val latest = remember { AtomicReference(MotionSample.ZERO) }

    LaunchedEffect(settings) {
        engine.setAppearance(settings.toAppearance())
        engine.config = settings.toCueConfig()
    }

    DisposableEffect(lifecycleOwner) {
        val source = SensorMotionSource(
            context = context,
            rotationProvider = { displayRotation(context) },
            listener = { latest.set(it) },
        )
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> source.start()
                Lifecycle.Event.ON_PAUSE -> source.stop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            source.stop()
        }
    }

    var frame by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        var last = 0L
        while (true) {
            androidx.compose.runtime.withFrameNanos { now ->
                val dt = if (last == 0L) 0f else ((now - last) / 1_000_000_000f).coerceIn(0f, 0.05f)
                last = now
                engine.update(dt, latest.get())
                frame = now
            }
        }
    }

    val alpha = settings.opacity.coerceIn(0.05f, 1f)
    val dotColor = Color(
        ((alpha * 255f).toInt() shl 24) or (settings.colorRgb and 0xFFFFFF),
    )

    Box(
        modifier = modifier.clip(RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Move or tilt your phone",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(24.dp),
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            frame // read to redraw each frame
            val w = size.width
            val h = size.height
            val r = settings.dotRadiusDp.dp.toPx()
            for (d in engine.dots) {
                drawCircle(
                    color = dotColor,
                    radius = r * d.sizeFactor,
                    center = Offset(d.x * w, d.y * h),
                )
            }
        }
    }
}

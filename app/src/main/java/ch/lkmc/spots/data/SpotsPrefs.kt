package ch.lkmc.spots.data

import ch.lkmc.spots.engine.Appearance
import ch.lkmc.spots.engine.CueConfig
import ch.lkmc.spots.engine.EdgeMode

/** How the overlay is governed. */
enum class CueMode {
    /** Never show. */
    OFF,

    /** Always show while the service runs. */
    ON,

    /** Show only when vehicle motion is detected. */
    AUTOMATIC,
}

/** Where the "are we in a vehicle?" signal comes from in [CueMode.AUTOMATIC]. */
enum class DetectionSource {
    /** Sensors only — no permissions, no Google Play services. */
    SENSORS_ONLY,

    /** Activity Recognition (optional; needs the permission + Play services). */
    ACTIVITY_RECOGNITION,
}

/** A named spot colour for the settings palette. Stored as an RGB int. */
data class SpotSwatch(val name: String, val rgb: Int)

val SPOT_PALETTE: List<SpotSwatch> = listOf(
    SpotSwatch("Grayscale", 0xE8E8EA.toInt()),
    SpotSwatch("White", 0xFFFFFF.toInt()),
    SpotSwatch("Amber", 0xF5C77E.toInt()),
    SpotSwatch("Cyan", 0x6FD6E8.toInt()),
    SpotSwatch("Mint", 0x86E0B0.toInt()),
    SpotSwatch("Rose", 0xF1A0B4.toInt()),
)

/**
 * All user-facing, persisted settings. Pure data; the framework persistence lives
 * in [SettingsRepository]. Helper accessors translate these into the engine's
 * [Appearance] and [CueConfig].
 */
data class SpotsSettings(
    val mode: CueMode = CueMode.AUTOMATIC,
    val sensitivity: Float = 0.5f,      // 0..1
    val dotCount: Int = 28,
    val dotSize: Float = 0.5f,          // 0..1 → radius 3..9 dp
    val opacity: Float = 0.55f,         // 0..1
    val colorRgb: Int = 0xE8E8EA.toInt(),
    val dynamic: Boolean = false,
    val edges: EdgeMode = EdgeMode.ALL,
    val detectionSource: DetectionSource = DetectionSource.SENSORS_ONLY,
    val keepScreenOn: Boolean = false,
    val showComfortMeter: Boolean = false,
) {
    val dotRadiusDp: Float get() = 3f + dotSize.coerceIn(0f, 1f) * 6f

    fun toAppearance(): Appearance = Appearance(
        dotCount = dotCount,
        dotRadiusDp = dotRadiusDp,
        opacity = opacity.coerceIn(0.05f, 1f),
        colorArgb = (0xFF shl 24) or (colorRgb and 0xFFFFFF),
        edges = edges,
        dynamic = dynamic,
    )

    fun toCueConfig(): CueConfig = CueConfig.fromSensitivity(sensitivity)
}

package ch.lkmc.spots.engine

/** Which screen edges carry spots. */
enum class EdgeMode { ALL, SIDES_ONLY }

/**
 * The **physics** config for the cue — how motion maps to dot streaming. Pure
 * numbers; built from user settings in `data/SpotsPrefs.kt`.
 *
 * Gains are expressed in *screen fractions per second* of dot drift produced per
 * unit of input (m/s² for acceleration, rad/s for yaw), before the overall
 * [sensitivity] multiplier.
 */
data class CueConfig(
    val sensitivity: Float = 1f,
    val longitudinalGain: Float = 0.045f, // forward/brake → vertical streaming
    val lateralGain: Float = 0.045f,      // sideways accel → horizontal streaming
    val yawGain: Float = 0.12f,           // turn rate → horizontal streaming
    val deadband: Float = 0.15f,          // m/s² below which input is ignored
    val maxFlow: Float = 0.40f,           // cap on drift speed (screen-fraction/s)
    val responseTime: Float = 0.18f,      // smoothing of flow onset/offset (s)
    val homeReturnTime: Float = 0.6f,     // how fast dots re-settle at rest (s)
    val restFlow: Float = 0.012f,         // |flow| below this counts as "at rest"
) {
    companion object {
        /**
         * @param sensitivity01 user slider 0..1 → a 0.35..1.8 gain multiplier.
         */
        fun fromSensitivity(sensitivity01: Float): CueConfig =
            CueConfig(sensitivity = 0.35f + 1.45f * sensitivity01.coerceIn(0f, 1f))
    }
}

/**
 * The **appearance** config — how the spots look and where they sit. Separate from
 * [CueConfig] because looks and physics are tuned independently.
 */
data class Appearance(
    val dotCount: Int = 28,
    val dotRadiusDp: Float = 5f,
    val opacity: Float = 0.55f,
    val colorArgb: Int = 0xFFE8E8EA.toInt(), // soft grayscale, Apple-like default
    val edges: EdgeMode = EdgeMode.ALL,
    val dynamic: Boolean = false,
    /** Side band width as a fraction of screen width. */
    val sideBandFraction: Float = 0.13f,
    /** Top/bottom band height as a fraction of screen height. */
    val endBandFraction: Float = 0.10f,
) {
    val safeDotCount: Int get() = dotCount.coerceIn(MIN_DOTS, MAX_DOTS)

    companion object {
        const val MIN_DOTS = 8
        const val MAX_DOTS = 80
    }
}

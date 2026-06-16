package ch.lkmc.spots.overlay

import ch.lkmc.spots.data.CueMode

/** A snapshot of the service for the in-app UI to observe. */
data class OverlayState(
    val running: Boolean = false,
    val overlayVisible: Boolean = false,
    val inVehicle: Boolean = false,
    val mode: CueMode = CueMode.AUTOMATIC,
    /** Smoothed weighted-acceleration level (m/s²) for the comfort meter. */
    val comfortLevel: Float = 0f,
    /** Estimated % of people who'd feel ill at this dose (MSDV-based). */
    val illnessPercent: Float = 0f,
)

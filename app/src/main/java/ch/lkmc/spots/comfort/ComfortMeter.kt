package ch.lkmc.spots.comfort

import ch.lkmc.spots.motion.HighPassFilter
import ch.lkmc.spots.motion.LowPassFilter
import kotlin.math.max
import kotlin.math.sqrt

/**
 * A lightweight, research-grounded "comfort meter" — an approximation of the
 * ISO 2631-1 / BS 6841 frequency-weighted motion-sickness reading.
 *
 * Motion is most nauseogenic around **0.2 Hz** (Golding et al. 2001; see research
 * doc 02 §5.1), so we band-pass the vertical (heave) acceleration around that
 * frequency, accumulate a Motion Sickness Dose Value (MSDV = √∫a_w²·dt), and apply
 * the Lawther & Griffin relation (predicted % who would be ill ≈ ⅓ · MSDV).
 *
 * This is a deliberately simplified estimate for *awareness*, not a calibrated
 * instrument. Pure and unit-tested.
 */
class ComfortMeter(
    // Band-pass approximating the ISO Wf weighting (peak ≈ 0.2 Hz):
    // high-pass ~0.1 Hz (τ≈1.6 s) and low-pass ~0.5 Hz (τ≈0.32 s).
    highPassTau: Float = 1.6f,
    lowPassTau: Float = 0.32f,
) {
    private val highPass = HighPassFilter(highPassTau)
    private val lowPass = LowPassFilter(lowPassTau)
    private val levelSmoother = LowPassFilter(2f)

    /** Accumulated MSDV² over the session (m²/s³). */
    var msdvSquared: Float = 0f
        private set

    /** A smoothed instantaneous weighted-acceleration level (m/s²) for a live bar. */
    var weightedLevel: Float = 0f
        private set

    fun update(heave: Float, dt: Float) {
        if (dt <= 0f) return
        val hp = highPass.update(heave, dt)
        val weighted = lowPass.update(hp, dt)
        msdvSquared += weighted * weighted * dt
        val smoothedSquare = levelSmoother.update(weighted * weighted, dt)
        weightedLevel = sqrt(max(0f, smoothedSquare))
    }

    /** MSDV = √∫a_w²·dt (m/s^1.5). */
    val msdv: Float get() = sqrt(msdvSquared)

    /** Lawther & Griffin: predicted percentage of people who would feel ill. */
    val estimatedIllnessPercent: Float get() = (msdv / 3f).coerceIn(0f, 100f)

    fun reset() {
        highPass.reset()
        lowPass.reset()
        levelSmoother.reset()
        msdvSquared = 0f
        weightedLevel = 0f
    }
}

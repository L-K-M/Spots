package ch.lkmc.spots.motion

import kotlin.math.abs

/**
 * Small, pure, frame-rate-correct DSP filters for smoothing sensor channels.
 *
 * All of them take the real elapsed time `dt` so they behave consistently whether
 * the sensor delivers at 50 Hz or the display runs at 120 Hz, rather than assuming
 * a fixed timestep (see research doc 03 §1/§4).
 */

/**
 * First-order low-pass (exponential smoothing) with a time constant in seconds.
 * Larger [timeConstant] = smoother and laggier. `update` returns the smoothed
 * value; [value] holds the current state.
 */
class LowPassFilter(var timeConstant: Float, initial: Float = 0f) {
    var value: Float = initial
        private set
    private var primed = false

    fun reset(initial: Float = 0f) {
        value = initial
        primed = false
    }

    fun update(sample: Float, dt: Float): Float {
        if (!primed || dt <= 0f) {
            value = sample
            primed = true
            return value
        }
        val alpha = dt / (timeConstant + dt)
        value += alpha * (sample - value)
        return value
    }
}

/**
 * First-order high-pass: the input minus its low-passed (slow) component. Used to
 * strip slow sensor bias / a persistent lean so the cue drifts back to neutral
 * when real motion stops.
 */
class HighPassFilter(timeConstant: Float) {
    private val lowPass = LowPassFilter(timeConstant)

    fun reset() = lowPass.reset()

    fun update(sample: Float, dt: Float): Float {
        val slow = lowPass.update(sample, dt)
        return sample - slow
    }
}

/**
 * A small dead-band: values with magnitude below [threshold] are squashed to zero
 * (so a phone resting on a lap doesn't make the dots jitter), with the remaining
 * range rescaled so there is no discontinuity at the edge of the band.
 */
fun deadband(value: Float, threshold: Float): Float {
    if (threshold <= 0f) return value
    val m = abs(value)
    if (m <= threshold) return 0f
    val sign = if (value >= 0f) 1f else -1f
    return sign * (m - threshold)
}

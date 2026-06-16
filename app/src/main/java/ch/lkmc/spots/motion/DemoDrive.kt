package ch.lkmc.spots.motion

import kotlin.math.PI
import kotlin.math.sin

/**
 * A scripted, looping "test drive" used by the in-app preview's Demo mode, so the
 * spots animate on their own — handy for understanding the effect indoors and for
 * screenshots. Pure, so it's deterministic and unit-tested.
 *
 * One 16-second lap: accelerate, cruise, turn left, cruise, turn right, cruise,
 * brake, settle — with a gentle 0.2 Hz road texture on the vertical channel (the
 * most nauseogenic band, so the comfort meter has something to show).
 */
object DemoDrive {

    const val PERIOD_SECONDS = 16f

    fun sampleAt(elapsedSeconds: Float): MotionSample {
        val t = elapsedSeconds.mod(PERIOD_SECONDS)
        val longitudinal = pulse(t, 0f, 2f, 2.0f) + pulse(t, 13f, 15f, -2.5f)
        val lateral = pulse(t, 5f, 7f, -1.5f) + pulse(t, 9f, 11f, 1.5f)
        val yawLeftRate = pulse(t, 5f, 7f, 0.5f) + pulse(t, 9f, 11f, -0.5f)
        val heave = 0.3f * sin(2.0 * PI * 0.2 * t).toFloat() // 0.2 Hz road texture
        return MotionSample(
            longitudinal = longitudinal,
            lateral = lateral,
            heave = heave,
            yawLeftRate = yawLeftRate,
            timestampNanos = (elapsedSeconds * 1_000_000_000.0).toLong(),
        )
    }

    /** A smooth 0→peak→0 hump over [t0, t1], zero elsewhere. */
    private fun pulse(t: Float, t0: Float, t1: Float, peak: Float): Float {
        if (t <= t0 || t >= t1) return 0f
        val phase = (t - t0) / (t1 - t0)
        return peak * sin(PI * phase).toFloat()
    }
}

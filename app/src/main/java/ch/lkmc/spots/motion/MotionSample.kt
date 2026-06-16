package ch.lkmc.spots.motion

/**
 * One processed motion reading, expressed in the **passenger's screen frame** and
 * already separated into the channels the cue cares about.
 *
 * Sign conventions (chosen to match the optic-flow mapping in
 * `engine/CueMapping.kt` and Apple's reported behaviour — see research doc 01 §2b):
 *
 *  - [longitudinal] m/s²  — `+` = the vehicle is **accelerating forward**
 *                           (you are pushed back), `-` = **braking**.
 *  - [lateral]      m/s²  — `+` = acceleration to the **right**
 *                           (which you feel on a **right**-leaning manoeuvre).
 *  - [heave]        m/s²  — `+` = acceleration **upward** (cresting a bump).
 *  - [yawLeftRate]  rad/s — `+` = the vehicle is **turning left**
 *                           (counter-clockwise seen from above).
 */
data class MotionSample(
    val longitudinal: Float,
    val lateral: Float,
    val heave: Float,
    val yawLeftRate: Float,
    val timestampNanos: Long,
) {
    companion object {
        val ZERO = MotionSample(0f, 0f, 0f, 0f, 0L)
    }
}

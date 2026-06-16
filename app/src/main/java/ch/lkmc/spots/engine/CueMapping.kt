package ch.lkmc.spots.engine

import ch.lkmc.spots.motion.MotionSample
import ch.lkmc.spots.motion.deadband
import kotlin.math.sqrt

/**
 * A 2-D dot-drift velocity in **normalized screen units per second**:
 * `vx` is fractions of screen width, `vy` fractions of screen height. `+vx` is
 * rightward; `+vy` is downward (screen y grows downward).
 */
data class FlowVelocity(val vx: Float, val vy: Float) {
    val magnitude: Float get() = sqrt(vx * vx + vy * vy)

    companion object {
        val ZERO = FlowVelocity(0f, 0f)
    }
}

/**
 * The optic-flow mapping: turn a [MotionSample] into the velocity at which the
 * spots should stream, so their on-screen motion matches how the world *appears*
 * to move to a forward-facing passenger (research doc 01 §2b).
 *
 *  - Accelerating **forward** → the world streams **backward** → dots move **down**.
 *  - **Braking** → dots move **up**.
 *  - Turning **left** → the scene swings **right** → dots move **right**.
 *  - Turning **right** → dots move **left**.
 *  - **Constant velocity** → no acceleration, no yaw → dots **stop**.
 *
 * Pure and exhaustively unit-tested (the full direction table).
 */
object CueMapping {

    fun flowVelocity(sample: MotionSample, config: CueConfig): FlowVelocity {
        val lon = deadband(sample.longitudinal, config.deadband)
        val lat = deadband(sample.lateral, config.deadband)
        val yaw = sample.yawLeftRate
        val g = config.sensitivity

        // Forward acceleration pushes the spots down the screen. longitudinalSign
        // flips this for a rear-facing seat.
        var vy = config.longitudinalGain * lon * g * config.longitudinalSign

        // A left turn (centripetal acceleration to the left → lat < 0, and/or a
        // positive left-yaw rate) slides the spots to the right.
        var vx = (-config.lateralGain * lat + config.yawGain * yaw) * g

        // Clamp the overall drift speed so violent bumps can't fling the dots.
        val mag = sqrt(vx * vx + vy * vy)
        if (mag > config.maxFlow && mag > 0f) {
            val k = config.maxFlow / mag
            vx *= k
            vy *= k
        }
        return FlowVelocity(vx, vy)
    }
}

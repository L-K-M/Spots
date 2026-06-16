package ch.lkmc.spots.engine

import kotlin.math.max

/** Result of one critically-damped step (for the pure, allocation-friendly API). */
data class Damped(val value: Float, val velocity: Float)

/**
 * Critically-damped smoothing — the classic "SmoothDamp" (Game Programming Gems 4)
 * used by, among others, Unity. It eases a value toward a target with no overshoot
 * and continuous velocity even when the target keeps changing, which is exactly the
 * behaviour we want for dots chasing a moving cue.
 *
 * Pure and unit-tested; [Damper] is the in-place, zero-allocation form used in the
 * per-frame draw loop.
 */
object Spring {

    /**
     * One step toward [target]. [smoothTime] is roughly the time (seconds) to reach
     * the target; smaller is snappier. Returns the new value and velocity.
     */
    fun smoothDamp(
        current: Float,
        target: Float,
        currentVelocity: Float,
        smoothTime: Float,
        dt: Float,
    ): Damped {
        if (dt <= 0f) return Damped(current, currentVelocity)
        val st = max(0.0001f, smoothTime)
        val omega = 2f / st
        val x = omega * dt
        val exp = 1f / (1f + x + 0.48f * x * x + 0.235f * x * x * x)
        val change = current - target
        val temp = (currentVelocity + omega * change) * dt
        var newVel = (currentVelocity - omega * temp) * exp
        var output = target + (change + temp) * exp
        // Clamp overshoot: if we crossed the target, snap to it.
        if ((target - current > 0f) == (output > target)) {
            output = target
            newVel = (output - target) / dt
        }
        return Damped(output, newVel)
    }
}

/**
 * Mutable, allocation-free critically-damped value. Hold one of these per animated
 * quantity and call [update] each frame.
 */
class Damper(var value: Float = 0f, var velocity: Float = 0f) {

    fun reset(initial: Float = 0f) {
        value = initial
        velocity = 0f
    }

    fun update(target: Float, smoothTime: Float, dt: Float): Float {
        val d = Spring.smoothDamp(value, target, velocity, smoothTime, dt)
        value = d.value
        velocity = d.velocity
        return value
    }
}

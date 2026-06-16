package ch.lkmc.spots.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpringTest {

    @Test
    fun settlesAtTarget() {
        val d = Damper(value = 0f)
        repeat(600) { d.update(target = 10f, smoothTime = 0.2f, dt = 0.016f) }
        assertEquals(10f, d.value, 1e-2f)
    }

    @Test
    fun criticallyDampedDoesNotOvershoot() {
        val d = Damper(value = 0f)
        var maxValue = 0f
        repeat(600) {
            d.update(target = 10f, smoothTime = 0.2f, dt = 0.016f)
            if (d.value > maxValue) maxValue = d.value
        }
        // Critically damped: never exceeds the target by a meaningful margin.
        assertTrue("overshoot detected: $maxValue", maxValue <= 10f + 1e-3f)
    }

    @Test
    fun monotonicApproachForStepTarget() {
        val d = Damper(value = 0f)
        var previous = -1f
        repeat(100) {
            val v = d.update(target = 5f, smoothTime = 0.3f, dt = 0.016f)
            assertTrue("should not decrease while approaching", v >= previous - 1e-4f)
            previous = v
        }
    }

    @Test
    fun zeroDtIsNoOp() {
        val d = Damper(value = 2f, velocity = 9f)
        d.update(target = 100f, smoothTime = 0.2f, dt = 0f)
        assertEquals(2f, d.value, 0f)
    }
}

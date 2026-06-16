package ch.lkmc.spots.motion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoDriveTest {

    private fun overOneLap(step: Float = 0.05f): List<MotionSample> {
        val out = ArrayList<MotionSample>()
        var t = 0f
        while (t < DemoDrive.PERIOD_SECONDS) {
            out += DemoDrive.sampleAt(t)
            t += step
        }
        return out
    }

    @Test
    fun lapIncludesBothAccelerationAndBraking() {
        val lap = overOneLap()
        assertTrue("should accelerate forward", lap.maxOf { it.longitudinal } > 1f)
        assertTrue("should brake", lap.minOf { it.longitudinal } < -1f)
    }

    @Test
    fun lapIncludesLeftAndRightTurns() {
        val lap = overOneLap()
        assertTrue(lap.maxOf { it.yawLeftRate } > 0.1f)
        assertTrue(lap.minOf { it.yawLeftRate } < -0.1f)
        assertTrue(lap.maxOf { it.lateral } > 0.1f)
        assertTrue(lap.minOf { it.lateral } < -0.1f)
    }

    @Test
    fun cruiseSectionsAreCalm() {
        // ~3.5 s is mid-cruise: little longitudinal or lateral input.
        val s = DemoDrive.sampleAt(3.5f)
        assertEquals(0f, s.longitudinal, 0.05f)
        assertEquals(0f, s.lateral, 0.05f)
    }

    @Test
    fun loopIsPeriodic() {
        val a = DemoDrive.sampleAt(4f)
        val b = DemoDrive.sampleAt(4f + DemoDrive.PERIOD_SECONDS)
        assertEquals(a.longitudinal, b.longitudinal, 1e-3f)
        assertEquals(a.lateral, b.lateral, 1e-3f)
        assertEquals(a.yawLeftRate, b.yawLeftRate, 1e-3f)
    }
}

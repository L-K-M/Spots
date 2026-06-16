package ch.lkmc.spots.detect

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VehicleMotionClassifierTest {

    @Test
    fun armsAfterSustainedMotion() {
        val c = VehicleMotionClassifier()
        assertFalse(c.inVehicle)
        repeat(8) { c.update(1.5f, dt = 1f) } // > onDelay (6 s)
        assertTrue(c.inVehicle)
    }

    @Test
    fun doesNotArmOnBriefMotion() {
        val c = VehicleMotionClassifier()
        repeat(3) { c.update(1.5f, dt = 1f) } // < onDelay
        assertFalse(c.inVehicle)
    }

    @Test
    fun ridesOutBriefStops() {
        val c = VehicleMotionClassifier()
        repeat(8) { c.update(1.5f, dt = 1f) }
        assertTrue(c.inVehicle)
        // A few seconds of quiet (a traffic light) should NOT disarm it.
        repeat(5) { c.update(0f, dt = 1f) }
        assertTrue("should ride out a short stop", c.inVehicle)
    }

    @Test
    fun disarmsAfterSustainedQuiet() {
        val c = VehicleMotionClassifier()
        repeat(8) { c.update(1.5f, dt = 1f) }
        assertTrue(c.inVehicle)
        repeat(40) { c.update(0f, dt = 1f) } // well beyond offDelay (25 s)
        assertFalse(c.inVehicle)
    }

    @Test
    fun resetClearsState() {
        val c = VehicleMotionClassifier()
        repeat(8) { c.update(1.5f, dt = 1f) }
        c.reset()
        assertFalse(c.inVehicle)
    }
}

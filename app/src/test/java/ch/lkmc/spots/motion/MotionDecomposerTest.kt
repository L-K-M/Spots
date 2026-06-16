package ch.lkmc.spots.motion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

class MotionDecomposerTest {

    private val g = 9.81f
    private val uprightGravity = Vec3(0f, g, 0f) // portrait, top up, screen vertical

    @Test
    fun forwardAccelerationIsPositiveLongitudinal() {
        // Phone held upright; "forward" (away from the user) is device −Z.
        val forwardAccel = Vec3(0f, 0f, -2f)
        val s = MotionDecomposer.decompose(forwardAccel, uprightGravity, Vec3.ZERO, 0, 0L)
        assertEquals(2f, s.longitudinal, 1e-3f)
        assertEquals(0f, s.lateral, 1e-3f)
    }

    @Test
    fun brakingIsNegativeLongitudinal() {
        val brake = Vec3(0f, 0f, 2f)
        val s = MotionDecomposer.decompose(brake, uprightGravity, Vec3.ZERO, 0, 0L)
        assertTrue(s.longitudinal < 0f)
    }

    @Test
    fun rightwardAccelerationIsPositiveLateral() {
        val rightAccel = Vec3(2f, 0f, 0f)
        val s = MotionDecomposer.decompose(rightAccel, uprightGravity, Vec3.ZERO, 0, 0L)
        assertEquals(2f, s.lateral, 1e-3f)
    }

    @Test
    fun leftYawIsPositive() {
        // Counter-clockwise about the up axis = turning left.
        val gyro = Vec3(0f, 1.2f, 0f)
        val s = MotionDecomposer.decompose(Vec3.ZERO, uprightGravity, gyro, 0, 0L)
        assertTrue(s.yawLeftRate > 0f)
    }

    @Test
    fun decompositionIsRobustToReadingTilt() {
        // Phone tilted back 45°: gravity (sky-pointing) gains a +Z component.
        val k = sqrt(0.5f)
        val tiltedGravity = Vec3(0f, g * k, g * k)
        // A 2 m/s² forward acceleration, expressed in this tilted device frame,
        // points along the tilted forward axis (0, +k, −k).
        val forwardInDevice = Vec3(0f, 2f * k, -2f * k)
        val s = MotionDecomposer.decompose(forwardInDevice, tiltedGravity, Vec3.ZERO, 0, 0L)
        assertEquals(2f, s.longitudinal, 1e-2f)
        assertEquals(0f, s.lateral, 1e-2f)
        assertEquals(0f, s.heave, 1e-2f) // a purely horizontal push has no heave
    }

    @Test
    fun flatPhoneHasNoForwardCue() {
        // Screen up, flat on a table: there is no in-screen "forward" direction.
        val flatGravity = Vec3(0f, 0f, g)
        val anyHorizontal = Vec3(1f, 1f, 0f)
        val s = MotionDecomposer.decompose(anyHorizontal, flatGravity, Vec3.ZERO, 0, 0L)
        assertEquals(0f, s.longitudinal, 1e-3f)
    }

    @Test
    fun landscapeRotationSwapsRightAxis() {
        // ROTATION_90: screen-right maps to device +Y.
        val right = MotionDecomposer.screenRightDevice(1)
        assertEquals(Vec3(0f, 1f, 0f), right)
    }
}

package ch.lkmc.spots.engine

import ch.lkmc.spots.motion.MotionSample
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CueMappingTest {

    private val config = CueConfig()

    private fun sample(
        longitudinal: Float = 0f,
        lateral: Float = 0f,
        yawLeftRate: Float = 0f,
    ) = MotionSample(longitudinal, lateral, 0f, yawLeftRate, 0L)

    @Test
    fun forwardAccelerationStreamsDotsDown() {
        val flow = CueMapping.flowVelocity(sample(longitudinal = 3f), config)
        assertTrue("forward → dots down (+y)", flow.vy > 0f)
    }

    @Test
    fun brakingStreamsDotsUp() {
        val flow = CueMapping.flowVelocity(sample(longitudinal = -3f), config)
        assertTrue("brake → dots up (−y)", flow.vy < 0f)
    }

    @Test
    fun leftTurnStreamsDotsRight() {
        // Left turn: centripetal acceleration points left (lateral < 0).
        val flow = CueMapping.flowVelocity(sample(lateral = -3f), config)
        assertTrue("left turn → dots right (+x)", flow.vx > 0f)
    }

    @Test
    fun rightTurnStreamsDotsLeft() {
        val flow = CueMapping.flowVelocity(sample(lateral = 3f), config)
        assertTrue("right turn → dots left (−x)", flow.vx < 0f)
    }

    @Test
    fun leftYawStreamsDotsRight() {
        val flow = CueMapping.flowVelocity(sample(yawLeftRate = 0.8f), config)
        assertTrue("left yaw → dots right (+x)", flow.vx > 0f)
    }

    @Test
    fun constantVelocityProducesNoFlow() {
        val flow = CueMapping.flowVelocity(sample(), config)
        assertEquals(0f, flow.magnitude, 1e-6f)
    }

    @Test
    fun smallInputInsideDeadbandIsIgnored() {
        val flow = CueMapping.flowVelocity(sample(longitudinal = 0.1f), config) // < 0.15 deadband
        assertEquals(0f, flow.vy, 1e-6f)
    }

    @Test
    fun violentMotionIsClampedToMaxFlow() {
        val flow = CueMapping.flowVelocity(sample(longitudinal = 100f, lateral = 100f), config)
        assertTrue("clamped to maxFlow", flow.magnitude <= config.maxFlow + 1e-4f)
    }

    @Test
    fun rearFacingSeatFlipsLongitudinalCue() {
        val reversed = CueConfig.fromSensitivity(0.5f, seatReversed = true)
        // Facing backward, forward acceleration should send dots UP, not down.
        val flow = CueMapping.flowVelocity(sample(longitudinal = 3f), reversed)
        assertTrue("rear-facing: forward → dots up (−y)", flow.vy < 0f)
        // Turning is unaffected by seat direction.
        val turn = CueMapping.flowVelocity(sample(lateral = -3f), reversed)
        assertTrue("turning is unchanged", turn.vx > 0f)
    }
}

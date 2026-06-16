package ch.lkmc.spots.engine

import ch.lkmc.spots.motion.MotionSample
import org.junit.Assert.assertTrue
import org.junit.Test

class MotionCueEngineTest {

    private fun forward(a: Float) = MotionSample(a, 0f, 0f, 0f, 0L)
    private val still = MotionSample.ZERO

    @Test
    fun forwardAccelerationDrivesPositiveVerticalFlow() {
        val engine = MotionCueEngine(appearance = Appearance(dotCount = 20), seed = 1L)
        repeat(120) { engine.update(0.016f, forward(3f)) } // ~2 s
        assertTrue("flow should stream downward", engine.flow.vy > 0.05f)
    }

    @Test
    fun dotsActuallyMoveUnderSustainedMotion() {
        val engine = MotionCueEngine(appearance = Appearance(dotCount = 20), seed = 5L)
        val leftDot = engine.dots.first { it.edge == Edge.LEFT }
        val startY = leftDot.y
        repeat(120) { engine.update(0.016f, forward(3f)) }
        assertTrue("a side dot should have moved", kotlin.math.abs(leftDot.y - startY) > 1e-3f)
    }

    @Test
    fun flowDecaysToRestWhenMotionStops() {
        val engine = MotionCueEngine(appearance = Appearance(dotCount = 20), seed = 2L)
        repeat(120) { engine.update(0.016f, forward(3f)) }
        repeat(300) { engine.update(0.016f, still) } // ~5 s of stillness
        assertTrue("flow should relax to rest", engine.flow.magnitude < engine.config.restFlow)
    }

    @Test
    fun dotsReturnHomeAtRest() {
        val engine = MotionCueEngine(appearance = Appearance(dotCount = 20), seed = 9L)
        val dot = engine.dots.first()
        repeat(120) { engine.update(0.016f, forward(3f)) }
        repeat(600) { engine.update(0.016f, still) } // ~10 s
        assertTrue(kotlin.math.abs(dot.x - dot.homeX) < 1e-2f)
        assertTrue(kotlin.math.abs(dot.y - dot.homeY) < 1e-2f)
    }

    @Test
    fun changingDotCountRebuildsField() {
        val engine = MotionCueEngine(appearance = Appearance(dotCount = 20), seed = 1L)
        engine.setAppearance(Appearance(dotCount = 40))
        assertTrue(engine.dots.size == 40)
    }

    @Test
    fun verticalBandsStreamMoreThanHorizontalUnderForwardMotion() {
        val engine = MotionCueEngine(appearance = Appearance(dotCount = 60), seed = 4L)
        val startY = engine.dots.map { it.y }
        repeat(10) { engine.update(0.016f, forward(3f)) } // short, no wrap
        var vertical = 0f
        var verticalN = 0
        var horizontal = 0f
        var horizontalN = 0
        engine.dots.forEachIndexed { i, d ->
            val dy = kotlin.math.abs(d.y - startY[i])
            if (d.edge == Edge.LEFT || d.edge == Edge.RIGHT) {
                vertical += dy; verticalN++
            } else {
                horizontal += dy; horizontalN++
            }
        }
        val vAvg = vertical / verticalN
        val hAvg = horizontal / horizontalN
        assertTrue("sides should stream more vertically than top/bottom ($vAvg vs $hAvg)", vAvg > hAvg)
    }
}

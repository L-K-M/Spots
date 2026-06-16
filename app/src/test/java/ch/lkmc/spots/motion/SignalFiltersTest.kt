package ch.lkmc.spots.motion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SignalFiltersTest {

    @Test
    fun lowPassConvergesToConstantInput() {
        val lp = LowPassFilter(timeConstant = 0.2f)
        repeat(200) { lp.update(5f, 0.02f) }
        assertEquals(5f, lp.value, 1e-2f)
    }

    @Test
    fun lowPassFirstSampleIsPassedThrough() {
        val lp = LowPassFilter(timeConstant = 1f)
        assertEquals(3f, lp.update(3f, 0.02f), 1e-6f)
    }

    @Test
    fun highPassRemovesDcOffset() {
        val hp = HighPassFilter(timeConstant = 0.2f)
        var last = 0f
        repeat(500) { last = hp.update(7f, 0.02f) }
        assertTrue("high-pass should reject a constant", kotlin.math.abs(last) < 0.1f)
    }

    @Test
    fun deadbandZeroesSmallValuesAndRescales() {
        assertEquals(0f, deadband(0.1f, 0.2f), 1e-6f)
        assertEquals(0f, deadband(-0.2f, 0.2f), 1e-6f)
        // Above the band, the output is continuous (no jump).
        assertEquals(0.3f, deadband(0.5f, 0.2f), 1e-6f)
        assertEquals(-0.3f, deadband(-0.5f, 0.2f), 1e-6f)
    }
}

package ch.lkmc.spots.comfort

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class ComfortMeterTest {

    private fun runSine(freqHz: Float, amp: Float, seconds: Float, dt: Float = 0.01f): ComfortMeter {
        val meter = ComfortMeter()
        var t = 0f
        val n = (seconds / dt).toInt()
        repeat(n) {
            val a = amp * sin(2.0 * PI * freqHz * t).toFloat()
            meter.update(a, dt)
            t += dt
        }
        return meter
    }

    @Test
    fun stillnessAccumulatesNothing() {
        val meter = ComfortMeter()
        repeat(500) { meter.update(0f, 0.01f) }
        assertEquals(0f, meter.msdv, 1e-4f)
        assertEquals(0f, meter.estimatedIllnessPercent, 1e-4f)
    }

    @Test
    fun lowFrequencyIsMoreProvocativeThanHigh() {
        // Per ISO 2631 / Golding et al., ~0.2 Hz is far more nauseogenic than ~2 Hz.
        val low = runSine(freqHz = 0.2f, amp = 1f, seconds = 30f)
        val high = runSine(freqHz = 2.0f, amp = 1f, seconds = 30f)
        assertTrue(
            "0.2 Hz (${low.weightedLevel}) should weigh more than 2 Hz (${high.weightedLevel})",
            low.weightedLevel > high.weightedLevel,
        )
    }

    @Test
    fun doseAccumulatesOverTime() {
        val meter = runSine(freqHz = 0.2f, amp = 1f, seconds = 20f)
        assertTrue(meter.msdv > 0f)
        assertTrue(meter.estimatedIllnessPercent > 0f)
    }

    @Test
    fun resetClearsDose() {
        val meter = runSine(freqHz = 0.2f, amp = 1f, seconds = 10f)
        meter.reset()
        assertEquals(0f, meter.msdv, 1e-6f)
        assertEquals(0f, meter.weightedLevel, 1e-6f)
    }
}

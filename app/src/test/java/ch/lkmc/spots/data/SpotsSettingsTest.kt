package ch.lkmc.spots.data

import ch.lkmc.spots.engine.Appearance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpotsSettingsTest {

    @Test
    fun dotRadiusMapsAcrossRange() {
        assertEquals(3f, SpotsSettings(dotSize = 0f).dotRadiusDp, 1e-4f)
        assertEquals(9f, SpotsSettings(dotSize = 1f).dotRadiusDp, 1e-4f)
        assertEquals(6f, SpotsSettings(dotSize = 0.5f).dotRadiusDp, 1e-4f)
    }

    @Test
    fun appearanceAppliesOpacityToAlpha() {
        val a: Appearance = SpotsSettings(colorRgb = 0x123456, opacity = 1f).toAppearance()
        assertEquals(0xFF123456.toInt(), a.colorArgb)
    }

    @Test
    fun appearanceClampsOpacityFloor() {
        val a = SpotsSettings(opacity = 0f).toAppearance()
        // Fully invisible dots are pointless; opacity is floored.
        assertTrue(a.opacity >= 0.05f)
    }

    @Test
    fun sensitivityMappingIsMonotonicAndPositive() {
        val low = SpotsSettings(sensitivity = 0f).toCueConfig()
        val high = SpotsSettings(sensitivity = 1f).toCueConfig()
        assertTrue(low.sensitivity > 0f)
        assertTrue(high.sensitivity > low.sensitivity)
    }
}

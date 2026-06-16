package ch.lkmc.spots.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DotFieldTest {

    @Test
    fun generatesRequestedCount() {
        val dots = DotField.generate(Appearance(dotCount = 32), seed = 1L)
        assertEquals(32, dots.size)
    }

    @Test
    fun countIsClampedToBounds() {
        val tooMany = DotField.generate(Appearance(dotCount = 9999), seed = 1L)
        assertEquals(Appearance.MAX_DOTS, tooMany.size)
        val tooFew = DotField.generate(Appearance(dotCount = 1), seed = 1L)
        assertEquals(Appearance.MIN_DOTS, tooFew.size)
    }

    @Test
    fun everyDotStartsWithinItsBand() {
        val dots = DotField.generate(Appearance(dotCount = 60), seed = 7L)
        for (d in dots) {
            assertTrue(d.x in d.bandMinX..d.bandMaxX)
            assertTrue(d.y in d.bandMinY..d.bandMaxY)
        }
    }

    @Test
    fun sidesOnlyUsesNoTopOrBottomDots() {
        val dots = DotField.generate(Appearance(dotCount = 40, edges = EdgeMode.SIDES_ONLY), seed = 3L)
        assertTrue(dots.all { it.edge == Edge.LEFT || it.edge == Edge.RIGHT })
    }

    @Test
    fun allEdgesUsesEveryEdge() {
        val dots = DotField.generate(Appearance(dotCount = 40, edges = EdgeMode.ALL), seed = 3L)
        val edges = dots.map { it.edge }.toSet()
        assertEquals(setOf(Edge.LEFT, Edge.RIGHT, Edge.TOP, Edge.BOTTOM), edges)
    }

    @Test
    fun generationIsDeterministicForSeed() {
        val a = DotField.generate(Appearance(dotCount = 20), seed = 42L)
        val b = DotField.generate(Appearance(dotCount = 20), seed = 42L)
        for (i in a.indices) {
            assertEquals(a[i].homeX, b[i].homeX, 0f)
            assertEquals(a[i].homeY, b[i].homeY, 0f)
        }
    }

    @Test
    fun wrapKeepsValuesInBand() {
        assertEquals(0.1f, wrap(0.1f, 0f, 1f), 1e-6f)
        assertEquals(0.1f, wrap(1.1f, 0f, 1f), 1e-6f)
        assertEquals(0.9f, wrap(-0.1f, 0f, 1f), 1e-6f)
        // Within a sub-band.
        assertEquals(0.05f, wrap(0.15f, 0f, 0.1f), 1e-6f)
    }
}

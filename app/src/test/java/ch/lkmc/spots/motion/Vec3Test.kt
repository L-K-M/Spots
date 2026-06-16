package ch.lkmc.spots.motion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Vec3Test {

    @Test
    fun dotProduct() {
        assertEquals(32f, Vec3(1f, 2f, 3f).dot(Vec3(4f, 5f, 6f)), 1e-5f)
    }

    @Test
    fun normalizedIsUnitLength() {
        val n = Vec3(3f, 0f, 4f).normalized()
        assertEquals(1f, n.length, 1e-5f)
        assertEquals(0.6f, n.x, 1e-5f)
        assertEquals(0.8f, n.z, 1e-5f)
    }

    @Test
    fun zeroVectorNormalizesToZero() {
        assertEquals(Vec3.ZERO, Vec3(0f, 0f, 0f).normalized())
    }

    @Test
    fun projectedOntoPlaneRemovesNormalComponent() {
        val up = Vec3(0f, 1f, 0f)
        val v = Vec3(2f, 5f, -3f)
        val p = v.projectedOntoPlane(up)
        assertEquals(2f, p.x, 1e-5f)
        assertEquals(0f, p.y, 1e-5f) // the +Y component is gone
        assertEquals(-3f, p.z, 1e-5f)
        // Result is perpendicular to the normal.
        assertTrue(kotlin.math.abs(p.dot(up)) < 1e-5f)
    }
}

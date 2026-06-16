package ch.lkmc.spots.motion

import kotlin.math.sqrt

/**
 * A tiny immutable 3-vector for the motion math.
 *
 * Deliberately `android`-free so the whole decomposition pipeline runs under
 * plain JVM unit tests (see `src/test`). All quantities are in SI units
 * (m/s² for acceleration, dimensionless for unit vectors).
 */
data class Vec3(val x: Float, val y: Float, val z: Float) {

    operator fun plus(o: Vec3) = Vec3(x + o.x, y + o.y, z + o.z)
    operator fun minus(o: Vec3) = Vec3(x - o.x, y - o.y, z - o.z)
    operator fun times(s: Float) = Vec3(x * s, y * s, z * s)

    fun dot(o: Vec3): Float = x * o.x + y * o.y + z * o.z

    val length: Float get() = sqrt(x * x + y * y + z * z)

    /** Unit vector, or [ZERO] if this vector is (near) zero-length. */
    fun normalized(): Vec3 {
        val len = length
        return if (len < EPSILON) ZERO else Vec3(x / len, y / len, z / len)
    }

    /**
     * This vector with the component along [unitNormal] removed — i.e. its
     * projection onto the plane whose normal is [unitNormal]. [unitNormal] must
     * already be a unit vector.
     */
    fun projectedOntoPlane(unitNormal: Vec3): Vec3 {
        val d = dot(unitNormal)
        return Vec3(x - unitNormal.x * d, y - unitNormal.y * d, z - unitNormal.z * d)
    }

    companion object {
        const val EPSILON = 1e-6f
        val ZERO = Vec3(0f, 0f, 0f)
    }
}

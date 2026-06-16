package ch.lkmc.spots.engine

import kotlin.random.Random

/** Which edge band a dot lives in. */
enum class Edge { LEFT, RIGHT, TOP, BOTTOM }

/**
 * One spot. Positions are **normalized**: `x` in 0..1 of screen width, `y` in
 * 0..1 of screen height. A dot drifts under the flow field but is confined to its
 * edge [Edge] band by toroidal wrapping, so the centre of the screen always stays
 * clear.
 */
class Dot(
    val edge: Edge,
    val homeX: Float,
    val homeY: Float,
    val bandMinX: Float,
    val bandMaxX: Float,
    val bandMinY: Float,
    val bandMaxY: Float,
    /** 0.7..1.0 size multiplier for organic variety. */
    val sizeFactor: Float,
    /** Phase used only by the optional "dynamic" shimmer. */
    val phase: Float,
) {
    var x: Float = homeX
    var y: Float = homeY

    fun resetToHome() {
        x = homeX
        y = homeY
    }
}

/** Confine [v] to the half-open band `[lo, hi)` by wrapping (toroidal). */
fun wrap(v: Float, lo: Float, hi: Float): Float {
    val span = hi - lo
    if (span <= 0f) return lo
    var r = (v - lo) % span
    if (r < 0f) r += span
    return lo + r
}

/**
 * Builds the edge bands of dots from an [Appearance]. Deterministic for a given
 * [seed] so the layout is reproducible (and unit-testable).
 */
object DotField {

    fun generate(appearance: Appearance, seed: Long = 0L): List<Dot> {
        val rng = Random(seed)
        val side = appearance.sideBandFraction.coerceIn(0.04f, 0.45f)
        val end = appearance.endBandFraction.coerceIn(0.04f, 0.45f)

        val edges = when (appearance.edges) {
            EdgeMode.ALL -> listOf(Edge.LEFT, Edge.RIGHT, Edge.TOP, Edge.BOTTOM)
            EdgeMode.SIDES_ONLY -> listOf(Edge.LEFT, Edge.RIGHT)
        }

        val total = appearance.safeDotCount
        val dots = ArrayList<Dot>(total)
        for (i in 0 until total) {
            val edge = edges[i % edges.size]
            dots += newDot(edge, side, end, rng)
        }
        return dots
    }

    private fun newDot(edge: Edge, side: Float, end: Float, rng: Random): Dot {
        val (minX, maxX, minY, maxY) = bandBounds(edge, side, end)
        val hx = minX + rng.nextFloat() * (maxX - minX)
        val hy = minY + rng.nextFloat() * (maxY - minY)
        val sizeFactor = 0.7f + rng.nextFloat() * 0.3f
        val phase = rng.nextFloat() * (2f * Math.PI.toFloat())
        return Dot(edge, hx, hy, minX, maxX, minY, maxY, sizeFactor, phase)
    }

    private data class Bounds(val minX: Float, val maxX: Float, val minY: Float, val maxY: Float)

    private fun bandBounds(edge: Edge, side: Float, end: Float): Bounds = when (edge) {
        Edge.LEFT -> Bounds(0f, side, 0f, 1f)
        Edge.RIGHT -> Bounds(1f - side, 1f, 0f, 1f)
        Edge.TOP -> Bounds(0f, 1f, 0f, end)
        Edge.BOTTOM -> Bounds(0f, 1f, 1f - end, 1f)
    }
}

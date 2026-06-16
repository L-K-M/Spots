package ch.lkmc.spots.engine

import ch.lkmc.spots.motion.MotionSample

/**
 * The cue, in pure form. It owns the dot field and advances it each frame from the
 * latest [MotionSample]. No `android.*` here, so the whole thing is unit-testable:
 * feed it samples and assert the dots stream the right way and settle at rest.
 *
 * Threading: [update] mutates the dots and should be called from a single thread
 * (the draw loop). The latest sample is passed in by value, so the producer
 * (sensor) thread only ever publishes an immutable [MotionSample].
 */
class MotionCueEngine(
    config: CueConfig = CueConfig(),
    appearance: Appearance = Appearance(),
    private val seed: Long = 0L,
) {
    var config: CueConfig = config

    var appearance: Appearance = appearance
        private set

    var dots: List<Dot> = DotField.generate(appearance, seed)
        private set

    /** The current smoothed flow velocity (normalized units/s); exposed for the UI. */
    var flow: FlowVelocity = FlowVelocity.ZERO
        private set

    private val flowX = Damper()
    private val flowY = Damper()

    /** Swap the appearance and rebuild the dot field if the layout changed. */
    fun setAppearance(newAppearance: Appearance) {
        val layoutChanged = newAppearance.safeDotCount != appearance.safeDotCount ||
            newAppearance.edges != appearance.edges ||
            newAppearance.sideBandFraction != appearance.sideBandFraction ||
            newAppearance.endBandFraction != appearance.endBandFraction
        appearance = newAppearance
        if (layoutChanged) {
            dots = DotField.generate(appearance, seed)
        }
    }

    /** Advance the field by [dt] seconds using [sample]. */
    fun update(dt: Float, sample: MotionSample) {
        if (dt <= 0f) return
        val target = CueMapping.flowVelocity(sample, config)
        val vx = flowX.update(target.vx, config.responseTime, dt)
        val vy = flowY.update(target.vy, config.responseTime, dt)
        flow = FlowVelocity(vx, vy)

        if (flow.magnitude < config.restFlow) {
            // At rest: ease the lattice back home so it stays tidy.
            val alpha = (dt / (config.homeReturnTime + dt)).coerceIn(0f, 1f)
            for (d in dots) {
                d.x += (d.homeX - d.x) * alpha
                d.y += (d.homeY - d.y) * alpha
            }
        } else {
            // Streaming: drift each dot, confined to its edge band.
            for (d in dots) {
                d.x = wrap(d.x + vx * dt, d.bandMinX, d.bandMaxX)
                d.y = wrap(d.y + vy * dt, d.bandMinY, d.bandMaxY)
            }
        }
    }

    fun reset() {
        for (d in dots) d.resetToHome()
        flowX.reset()
        flowY.reset()
        flow = FlowVelocity.ZERO
    }
}

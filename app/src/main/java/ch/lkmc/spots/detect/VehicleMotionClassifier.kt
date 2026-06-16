package ch.lkmc.spots.detect

import ch.lkmc.spots.motion.LowPassFilter

/**
 * A permission-free, Play-services-free "are we moving in a vehicle?" classifier,
 * driven purely by the horizontal-acceleration energy already computed for the cue.
 *
 * It uses **hysteresis** so the overlay doesn't flicker on and off: it arms only
 * after sustained motion, and — crucially — keeps the overlay up through brief
 * stops (traffic lights, junctions) by disarming only after a longer quiet period.
 *
 * Pure and unit-tested. The optional [ActivityRecognitionDetector] can be layered
 * on top for a stronger signal, but the app never depends on it.
 */
class VehicleMotionClassifier(
    /** Smoothed horizontal-acceleration level (m/s²) to arm above. */
    val onThreshold: Float = 0.55f,
    /** …and to consider "quiet" below. */
    val offThreshold: Float = 0.22f,
    /** Sustained seconds above [onThreshold] before arming. */
    val onDelaySec: Float = 6f,
    /** Sustained seconds below [offThreshold] before disarming (rides out stops). */
    val offDelaySec: Float = 25f,
    energyTau: Float = 1.5f,
) {
    private val energy = LowPassFilter(energyTau)
    private var aboveTime = 0f
    private var belowTime = 0f

    var inVehicle: Boolean = false
        private set

    /** The current smoothed energy estimate (m/s²) — useful for UI/diagnostics. */
    val smoothedEnergy: Float get() = energy.value

    /**
     * @param horizontalAccelMag √(longitudinal² + lateral²), m/s².
     * @return the (possibly unchanged) [inVehicle] decision.
     */
    fun update(horizontalAccelMag: Float, dt: Float): Boolean {
        if (dt <= 0f) return inVehicle
        val e = energy.update(horizontalAccelMag, dt)
        if (!inVehicle) {
            if (e > onThreshold) {
                aboveTime += dt
                if (aboveTime >= onDelaySec) {
                    inVehicle = true
                    belowTime = 0f
                }
            } else {
                aboveTime = 0f
            }
        } else {
            if (e < offThreshold) {
                belowTime += dt
                if (belowTime >= offDelaySec) {
                    inVehicle = false
                    aboveTime = 0f
                }
            } else {
                belowTime = 0f
            }
        }
        return inVehicle
    }

    fun reset() {
        energy.reset()
        aboveTime = 0f
        belowTime = 0f
        inVehicle = false
    }
}

package ch.lkmc.spots.motion

/**
 * Turns raw device-frame sensor vectors into a [MotionSample] expressed in the
 * passenger's screen frame, robust to how the phone is tilted while reading.
 *
 * The method (see research doc 03 §1, "Device → world frame"):
 *  - `ĝ` is the measured gravity unit vector (points toward the sky).
 *  - The **forward** axis is the device's into-screen direction (−Z) projected
 *    onto the horizontal plane. This is robust to reading tilt: whether the phone
 *    is upright or tilted back, its horizontal projection still points along the
 *    direction the forward-facing passenger looks.
 *  - The **right** axis is the screen's right edge (which depends on the display
 *    rotation) projected onto the horizontal plane.
 *  - Longitudinal/lateral acceleration are the components of linear acceleration
 *    along those axes; heave is the component along gravity; yaw is the gyroscope
 *    component about the up axis.
 *
 * Everything here is pure and unit-tested.
 */
object MotionDecomposer {

    /** Device-frame −Z, i.e. "into the screen, away from a forward-facing user". */
    private val INTO_SCREEN = Vec3(0f, 0f, -1f)

    /**
     * Screen-right in device coordinates for each [android.view.Surface] rotation
     * constant (0/1/2/3 = 0°/90°/180°/270°), following `remapCoordinateSystem`.
     */
    fun screenRightDevice(surfaceRotation: Int): Vec3 = when (surfaceRotation) {
        1 -> Vec3(0f, 1f, 0f)   // ROTATION_90
        2 -> Vec3(-1f, 0f, 0f)  // ROTATION_180
        3 -> Vec3(0f, -1f, 0f)  // ROTATION_270
        else -> Vec3(1f, 0f, 0f) // ROTATION_0
    }

    /**
     * @param linAccel device-frame linear acceleration (gravity already removed), m/s².
     * @param gravity  device-frame gravity vector (TYPE_GRAVITY), m/s².
     * @param gyro     device-frame angular velocity (TYPE_GYROSCOPE), rad/s.
     * @param surfaceRotation current display rotation (0/1/2/3).
     */
    fun decompose(
        linAccel: Vec3,
        gravity: Vec3,
        gyro: Vec3,
        surfaceRotation: Int,
        timestampNanos: Long,
    ): MotionSample {
        val up = gravity.normalized()
        if (up == Vec3.ZERO) {
            // No gravity reading yet — can't orient; report no horizontal motion.
            return MotionSample(0f, 0f, 0f, 0f, timestampNanos)
        }

        val forward = INTO_SCREEN.projectedOntoPlane(up).normalized()
        val right = screenRightDevice(surfaceRotation).projectedOntoPlane(up).normalized()

        val longitudinal = linAccel.dot(forward)
        val lateral = linAccel.dot(right)
        val heave = linAccel.dot(up)
        val yawLeftRate = gyro.dot(up)

        return MotionSample(
            longitudinal = longitudinal,
            lateral = lateral,
            heave = heave,
            yawLeftRate = yawLeftRate,
            timestampNanos = timestampNanos,
        )
    }
}

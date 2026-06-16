package ch.lkmc.spots.motion

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.HandlerThread

/**
 * Reads the device motion sensors and emits a stream of screen-frame
 * [MotionSample]s via [listener]. All sensor work happens on a dedicated
 * [HandlerThread] so it never competes with the UI/draw thread; the listener is
 * called on that thread with an immutable sample.
 *
 * Prefers `TYPE_LINEAR_ACCELERATION` + `TYPE_GRAVITY`; on devices that lack them
 * it falls back to a low-pass split of the raw accelerometer (research doc 03 §1).
 */
class SensorMotionSource(
    context: Context,
    private val rotationProvider: () -> Int,
    private val listener: (MotionSample) -> Unit,
) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val linearSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    private val gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
    private val gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    /** True when neither linear-acceleration nor gravity virtual sensors exist. */
    private val useRawFallback = linearSensor == null || gravitySensor == null

    private var thread: HandlerThread? = null
    private var handler: Handler? = null

    // Latest vectors (only touched on the sensor thread).
    private var gravity = Vec3(0f, 0f, 9.81f)
    private var gyro = Vec3.ZERO
    private var linear = Vec3.ZERO

    // Fallback gravity low-pass (~0.5 s time constant), per axis.
    private val gxLp = LowPassFilter(0.5f)
    private val gyLp = LowPassFilter(0.5f)
    private val gzLp = LowPassFilter(0.5f)
    private var lastRawTs = 0L

    fun start() {
        if (thread != null) return
        val t = HandlerThread("spots-sensors").also { it.start() }
        val h = Handler(t.looper)
        thread = t
        handler = h
        val rate = SensorManager.SENSOR_DELAY_GAME
        if (useRawFallback) {
            accelSensor?.let { sensorManager.registerListener(this, it, rate, h) }
        } else {
            sensorManager.registerListener(this, linearSensor, rate, h)
            sensorManager.registerListener(this, gravitySensor, rate, h)
        }
        gyroSensor?.let { sensorManager.registerListener(this, it, rate, h) }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        thread?.quitSafely()
        thread = null
        handler = null
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_GYROSCOPE -> {
                gyro = Vec3(event.values[0], event.values[1], event.values[2])
            }
            Sensor.TYPE_GRAVITY -> {
                gravity = Vec3(event.values[0], event.values[1], event.values[2])
            }
            Sensor.TYPE_LINEAR_ACCELERATION -> {
                linear = Vec3(event.values[0], event.values[1], event.values[2])
                emit(event.timestamp)
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val raw = Vec3(event.values[0], event.values[1], event.values[2])
                val dt = if (lastRawTs == 0L) 0f else (event.timestamp - lastRawTs) / 1e9f
                lastRawTs = event.timestamp
                gravity = Vec3(
                    gxLp.update(raw.x, dt),
                    gyLp.update(raw.y, dt),
                    gzLp.update(raw.z, dt),
                )
                linear = raw - gravity
                emit(event.timestamp)
            }
        }
    }

    private fun emit(timestampNanos: Long) {
        val sample = MotionDecomposer.decompose(
            linAccel = linear,
            gravity = gravity,
            gyro = gyro,
            surfaceRotation = rotationProvider(),
            timestampNanos = timestampNanos,
        )
        listener(sample)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { /* unused */ }
}

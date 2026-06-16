package ch.lkmc.spots.overlay

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Display
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import ch.lkmc.spots.MainActivity
import ch.lkmc.spots.R
import ch.lkmc.spots.SpotsApp
import ch.lkmc.spots.comfort.ComfortMeter
import ch.lkmc.spots.data.CueMode
import ch.lkmc.spots.data.DetectionSource
import ch.lkmc.spots.data.SettingsRepository
import ch.lkmc.spots.data.SpotsSettings
import ch.lkmc.spots.detect.ActivityRecognitionDetector
import ch.lkmc.spots.detect.VehicleMotionClassifier
import ch.lkmc.spots.motion.MotionSample
import ch.lkmc.spots.motion.SensorMotionSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.sqrt

/**
 * The heart of the app at runtime: a `specialUse` foreground service that owns the
 * spots overlay window and the sensor pipeline.
 *
 * Threading: lifecycle + window operations run on the main thread; sensor samples
 * arrive on the [SensorMotionSource] thread, update the detector/comfort meter, and
 * publish the latest sample for the overlay view's frame loop to read.
 */
class OverlayService : Service() {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private lateinit var settingsRepo: SettingsRepository
    private lateinit var windowManager: WindowManager
    private var overlayView: DotsOverlayView? = null

    private var sensorSource: SensorMotionSource? = null
    private val classifier = VehicleMotionClassifier()
    private val comfort = ComfortMeter()
    private val activityDetector by lazy { ActivityRecognitionDetector(applicationContext) }

    @Volatile private var latestSample: MotionSample = MotionSample.ZERO
    @Volatile private var settings: SpotsSettings = SpotsSettings()

    private var lastSampleTs = 0L
    private var overlayVisible = false
    private var stateThrottle = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        settingsRepo = SettingsRepository(applicationContext)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        settingsRepo.settings
            .onEach { s ->
                val previous = settings
                settings = s
                applySettings(previous, s)
                if (s.mode == CueMode.OFF) stopEverything()
            }
            .launchIn(scope)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopEverything()
                return START_NOT_STICKY
            }
        }
        startForegroundCompat(armedNotification(armed = true, active = false))
        startPipeline()
        return START_STICKY
    }

    private fun startPipeline() {
        if (sensorSource != null) return
        val source = SensorMotionSource(
            context = applicationContext,
            rotationProvider = ::currentRotation,
            listener = ::onSample,
        )
        sensorSource = source
        source.start()
        // Start at a low rate while merely armed; speed up once the overlay shows.
        source.setRate(SensorManager.SENSOR_DELAY_UI)
        if (settings.detectionSource == DetectionSource.ACTIVITY_RECOGNITION) {
            runCatching { activityDetector.start() }
        }
        publishState()
    }

    /** Runs on the sensor thread. */
    private fun onSample(sample: MotionSample) {
        latestSample = sample
        val dt = if (lastSampleTs == 0L) 0f else (sample.timestampNanos - lastSampleTs) / 1e9f
        lastSampleTs = sample.timestampNanos
        if (dt > 0f) {
            val horizontal = sqrt(sample.longitudinal * sample.longitudinal + sample.lateral * sample.lateral)
            classifier.update(horizontal, dt)
            comfort.update(sample.heave, dt)
        }

        val desiredVisible = shouldShow()
        if (desiredVisible != overlayVisible) {
            mainHandler.post { setOverlayVisible(desiredVisible) }
        }
        if (stateThrottle++ % 10 == 0) publishState()
    }

    private fun shouldShow(): Boolean = when (settings.mode) {
        CueMode.OFF -> false
        CueMode.ON -> true
        CueMode.AUTOMATIC -> classifier.inVehicle ||
            (settings.detectionSource == DetectionSource.ACTIVITY_RECOGNITION && activityDetector.inVehicle)
    }

    // --- main-thread window management -------------------------------------------------

    private fun setOverlayVisible(visible: Boolean) {
        if (visible) addOverlay() else removeOverlay()
        // Fast sampling only while the cue is actually on screen.
        sensorSource?.setRate(
            if (overlayVisible) SensorManager.SENSOR_DELAY_GAME else SensorManager.SENSOR_DELAY_UI,
        )
    }

    private fun addOverlay() {
        if (overlayVisible) return
        if (!Settings.canDrawOverlays(this)) return
        val view = overlayView ?: DotsOverlayView(this).also { v ->
            v.sampleProvider = { latestSample }
            v.applyAppearance(settings.toAppearance())
            v.applyConfig(settings.toCueConfig())
            overlayView = v
        }
        val params = buildLayoutParams()
        runCatching { windowManager.addView(view, params) }
            .onSuccess {
                overlayVisible = true
                updateNotification()
                publishState()
            }
    }

    private fun removeOverlay() {
        val view = overlayView ?: return
        if (!overlayVisible) return
        runCatching { windowManager.removeView(view) }
        overlayVisible = false
        updateNotification()
        publishState()
    }

    private fun buildLayoutParams(): WindowManager.LayoutParams {
        var flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        if (settings.keepScreenOn) {
            flags = flags or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        }
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            flags,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
        }
    }

    private fun applySettings(previous: SpotsSettings, s: SpotsSettings) {
        overlayView?.let { v ->
            val windowFlagsChanged = previous.keepScreenOn != s.keepScreenOn
            v.applyAppearance(s.toAppearance())
            v.applyConfig(s.toCueConfig())
            if (overlayVisible && windowFlagsChanged) {
                runCatching { windowManager.updateViewLayout(v, buildLayoutParams()) }
            }
        }
        if (s.detectionSource != previous.detectionSource) {
            if (s.detectionSource == DetectionSource.ACTIVITY_RECOGNITION) {
                runCatching { activityDetector.start() }
            } else {
                runCatching { activityDetector.stop() }
            }
        }
        // Re-evaluate visibility immediately when the mode changes.
        val desired = shouldShow()
        if (desired != overlayVisible) setOverlayVisible(desired)
    }

    private fun currentRotation(): Int {
        val dm = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val display = dm.getDisplay(Display.DEFAULT_DISPLAY)
        return display?.rotation ?: 0
    }

    // --- notification ------------------------------------------------------------------

    private fun startForegroundCompat(notification: android.app.Notification) {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        ServiceCompat.startForeground(this, NOTIF_ID, notification, type)
    }

    private fun updateNotification() {
        val notif = armedNotification(armed = !overlayVisible, active = overlayVisible)
        SpotsApp.notificationManager(this).notify(NOTIF_ID, notif)
    }

    private fun armedNotification(armed: Boolean, active: Boolean): android.app.Notification {
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val stopIntent = PendingIntent.getService(
            this, 1,
            Intent(this, OverlayService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val title = if (active) getString(R.string.notif_active_title) else getString(R.string.notif_armed_title)
        val text = if (active) getString(R.string.notif_active_text) else getString(R.string.notif_armed_text)
        return NotificationCompat.Builder(this, SpotsApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_spots)
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(contentIntent)
            .addAction(0, getString(R.string.notif_action_stop), stopIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    // --- state publishing --------------------------------------------------------------

    private fun publishState() {
        _state.value = OverlayState(
            running = true,
            overlayVisible = overlayVisible,
            inVehicle = classifier.inVehicle ||
                (settings.detectionSource == DetectionSource.ACTIVITY_RECOGNITION && activityDetector.inVehicle),
            mode = settings.mode,
            comfortLevel = comfort.weightedLevel,
            illnessPercent = comfort.estimatedIllnessPercent,
        )
    }

    private fun stopEverything() {
        sensorSource?.stop()
        sensorSource = null
        runCatching { activityDetector.stop() }
        removeOverlay()
        _state.value = OverlayState(running = false)
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        sensorSource?.stop()
        sensorSource = null
        runCatching { activityDetector.stop() }
        if (overlayVisible) runCatching { overlayView?.let { windowManager.removeView(it) } }
        overlayVisible = false
        overlayView = null
        _state.value = OverlayState(running = false)
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "ch.lkmc.spots.action.START"
        const val ACTION_STOP = "ch.lkmc.spots.action.STOP"
        private const val NOTIF_ID = 1001

        private val _state = MutableStateFlow(OverlayState())
        val state: StateFlow<OverlayState> = _state.asStateFlow()

        fun start(context: Context) {
            val intent = Intent(context, OverlayService::class.java).setAction(ACTION_START)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, OverlayService::class.java).setAction(ACTION_STOP)
            context.startService(intent)
        }
    }
}

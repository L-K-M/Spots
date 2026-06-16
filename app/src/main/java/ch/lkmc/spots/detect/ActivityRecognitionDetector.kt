package ch.lkmc.spots.detect

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity

/**
 * Optional vehicle detection via Google Play services' Activity Recognition.
 *
 * This is a best-effort *enhancement*: the app's default detection is the
 * permission-free [VehicleMotionClassifier]. Every call here is guarded, so on a
 * device without Play services (or without the runtime permission) it silently
 * does nothing and the sensors-only path carries on.
 */
class ActivityRecognitionDetector(private val context: Context) {

    private var pendingIntent: PendingIntent? = null

    /** Latest IN_VEHICLE state from the system, or false if unavailable. */
    val inVehicle: Boolean get() = ActivityTransitionReceiver.inVehicle

    private fun hasPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
        return context.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) ==
            PackageManager.PERMISSION_GRANTED
    }

    // Guarded by hasPermission() and runCatching (handles SecurityException); the
    // permission is requested in the UI before this path is used.
    @SuppressLint("MissingPermission")
    fun start() {
        if (!hasPermission()) return
        runCatching {
            val transitions = listOf(
                build(DetectedActivity.IN_VEHICLE, ActivityTransition.ACTIVITY_TRANSITION_ENTER),
                build(DetectedActivity.IN_VEHICLE, ActivityTransition.ACTIVITY_TRANSITION_EXIT),
            )
            val request = ActivityTransitionRequest(transitions)
            val pi = buildPendingIntent()
            pendingIntent = pi
            ActivityRecognition.getClient(context).requestActivityTransitionUpdates(request, pi)
        }
    }

    @SuppressLint("MissingPermission")
    fun stop() {
        runCatching {
            pendingIntent?.let {
                ActivityRecognition.getClient(context).removeActivityTransitionUpdates(it)
            }
        }
        pendingIntent = null
        ActivityTransitionReceiver.inVehicle = false
    }

    private fun build(activity: Int, transition: Int) =
        ActivityTransition.Builder()
            .setActivityType(activity)
            .setActivityTransition(transition)
            .build()

    private fun buildPendingIntent(): PendingIntent {
        val intent = Intent(context, ActivityTransitionReceiver::class.java)
        // Activity Recognition needs a mutable PendingIntent to deliver results.
        var flags = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags = flags or PendingIntent.FLAG_MUTABLE
        }
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flags)
    }

    private companion object {
        const val REQUEST_CODE = 4201
    }
}

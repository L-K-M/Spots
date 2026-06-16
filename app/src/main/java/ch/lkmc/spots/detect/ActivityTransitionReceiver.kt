package ch.lkmc.spots.detect

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity

/**
 * Receives IN_VEHICLE enter/exit transitions from the optional Activity Recognition
 * detector. Everything is wrapped defensively so a device without Google Play
 * services can never crash the app — the receiver simply never fires there.
 */
class ActivityTransitionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        runCatching {
            if (!ActivityTransitionResult.hasResult(intent)) return
            val result = ActivityTransitionResult.extractResult(intent) ?: return
            for (event in result.transitionEvents) {
                if (event.activityType == DetectedActivity.IN_VEHICLE) {
                    inVehicle = event.transitionType ==
                        com.google.android.gms.location.ActivityTransition.ACTIVITY_TRANSITION_ENTER
                }
            }
        }
    }

    companion object {
        /** Latest IN_VEHICLE state, read by [ActivityRecognitionDetector]. */
        @Volatile
        var inVehicle: Boolean = false
    }
}

package ch.lkmc.spots.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ch.lkmc.spots.data.CueMode
import ch.lkmc.spots.data.DetectionSource
import ch.lkmc.spots.data.SettingsRepository
import ch.lkmc.spots.data.SpotsSettings
import ch.lkmc.spots.engine.Appearance
import ch.lkmc.spots.engine.EdgeMode
import ch.lkmc.spots.overlay.OverlayService
import ch.lkmc.spots.overlay.OverlayState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Holds the app's settings + live overlay state and exposes the small set of
 * actions the UI needs. No DI framework — just the repository built from the
 * application context.
 */
class SpotsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = SettingsRepository(app)

    val settings: StateFlow<SpotsSettings> =
        repo.settings.stateIn(viewModelScope, SharingStarted.Eagerly, SpotsSettings())

    val overlayState: StateFlow<OverlayState> = OverlayService.state

    /** Persist the chosen mode (does not start/stop the service — see start/stop). */
    fun setMode(mode: CueMode) = launchEdit { repo.setMode(mode) }

    fun startService() = OverlayService.start(getApplication<Application>())
    fun stopService() = OverlayService.stop(getApplication<Application>())

    fun setSensitivity(v: Float) = launchEdit { repo.setSensitivity(v) }
    fun setDotCount(v: Int) = launchEdit { repo.setDotCount(v.coerceIn(Appearance.MIN_DOTS, Appearance.MAX_DOTS)) }
    fun setDotSize(v: Float) = launchEdit { repo.setDotSize(v) }
    fun setOpacity(v: Float) = launchEdit { repo.setOpacity(v) }
    fun setColor(rgb: Int) = launchEdit { repo.setColor(rgb) }
    fun setDynamic(v: Boolean) = launchEdit { repo.setDynamic(v) }
    fun setHollow(v: Boolean) = launchEdit { repo.setHollow(v) }
    fun setEdges(v: EdgeMode) = launchEdit { repo.setEdges(v) }
    fun setDetectionSource(v: DetectionSource) = launchEdit { repo.setDetectionSource(v) }
    fun setSeatReversed(v: Boolean) = launchEdit { repo.setSeatReversed(v) }
    fun setKeepScreenOn(v: Boolean) = launchEdit { repo.setKeepScreenOn(v) }
    fun setShowComfortMeter(v: Boolean) = launchEdit { repo.setShowComfortMeter(v) }

    private inline fun launchEdit(crossinline block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}

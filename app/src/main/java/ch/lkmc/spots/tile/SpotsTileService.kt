package ch.lkmc.spots.tile

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import ch.lkmc.spots.MainActivity
import ch.lkmc.spots.R
import ch.lkmc.spots.data.CueMode
import ch.lkmc.spots.data.SettingsRepository
import ch.lkmc.spots.overlay.OverlayService
import kotlinx.coroutines.runBlocking

/**
 * A Quick Settings tile — the Android analogue of Apple's Control Center toggle.
 * Tapping it cycles Spots off ↔ on (Automatic). If the overlay permission isn't
 * granted yet, it opens the app instead.
 */
class SpotsTileService : TileService() {

    private val repo get() = SettingsRepository(applicationContext)

    override fun onStartListening() {
        super.onStartListening()
        refresh()
    }

    override fun onClick() {
        super.onClick()
        val ctx = applicationContext
        if (!Settings.canDrawOverlays(ctx)) {
            openApp()
            return
        }
        runBlocking {
            val mode = repo.settingsOnce().mode
            if (mode == CueMode.OFF) {
                repo.setMode(CueMode.AUTOMATIC)
                OverlayService.start(ctx)
            } else {
                repo.setMode(CueMode.OFF)
                OverlayService.stop(ctx)
            }
        }
        refresh()
    }

    private fun refresh() {
        val tile = qsTile ?: return
        val mode = runBlocking { repo.settingsOnce().mode }
        tile.state = if (mode == CueMode.OFF) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
        tile.icon = Icon.createWithResource(this, R.drawable.ic_stat_spots)
        tile.label = getString(R.string.app_name)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = when (mode) {
                CueMode.OFF -> "Off"
                CueMode.ON -> "On"
                CueMode.AUTOMATIC -> "Automatic"
            }
        }
        tile.updateTile()
    }

    private fun openApp() {
        val intent = Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            startActivityAndCollapse(pi)
        } else {
            @Suppress("DEPRECATION", "StartActivityAndCollapseDeprecated")
            startActivityAndCollapse(intent)
        }
    }
}

package ch.lkmc.spots.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ch.lkmc.spots.engine.EdgeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "spots_settings")

/**
 * Persists [SpotsSettings] in a Preferences DataStore and exposes them as a Flow.
 * No secrets here — just small UI preferences.
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val mode = stringPreferencesKey("mode")
        val sensitivity = floatPreferencesKey("sensitivity")
        val dotCount = intPreferencesKey("dot_count")
        val dotSize = floatPreferencesKey("dot_size")
        val opacity = floatPreferencesKey("opacity")
        val colorRgb = intPreferencesKey("color_rgb")
        val dynamic = booleanPreferencesKey("dynamic")
        val edges = stringPreferencesKey("edges")
        val detection = stringPreferencesKey("detection")
        val keepScreenOn = booleanPreferencesKey("keep_screen_on")
        val showComfortMeter = booleanPreferencesKey("show_comfort_meter")
    }

    val settings: Flow<SpotsSettings> = context.dataStore.data.map { it.toSettings() }

    suspend fun settingsOnce(): SpotsSettings = settings.first()

    private fun Preferences.toSettings(): SpotsSettings {
        val defaults = SpotsSettings()
        return SpotsSettings(
            mode = this[Keys.mode]?.let { runCatching { CueMode.valueOf(it) }.getOrNull() } ?: defaults.mode,
            sensitivity = this[Keys.sensitivity] ?: defaults.sensitivity,
            dotCount = this[Keys.dotCount] ?: defaults.dotCount,
            dotSize = this[Keys.dotSize] ?: defaults.dotSize,
            opacity = this[Keys.opacity] ?: defaults.opacity,
            colorRgb = this[Keys.colorRgb] ?: defaults.colorRgb,
            dynamic = this[Keys.dynamic] ?: defaults.dynamic,
            edges = this[Keys.edges]?.let { runCatching { EdgeMode.valueOf(it) }.getOrNull() } ?: defaults.edges,
            detectionSource = this[Keys.detection]?.let { runCatching { DetectionSource.valueOf(it) }.getOrNull() }
                ?: defaults.detectionSource,
            keepScreenOn = this[Keys.keepScreenOn] ?: defaults.keepScreenOn,
            showComfortMeter = this[Keys.showComfortMeter] ?: defaults.showComfortMeter,
        )
    }

    suspend fun setMode(mode: CueMode) = edit { it[Keys.mode] = mode.name }
    suspend fun setSensitivity(v: Float) = edit { it[Keys.sensitivity] = v.coerceIn(0f, 1f) }
    suspend fun setDotCount(v: Int) = edit { it[Keys.dotCount] = v }
    suspend fun setDotSize(v: Float) = edit { it[Keys.dotSize] = v.coerceIn(0f, 1f) }
    suspend fun setOpacity(v: Float) = edit { it[Keys.opacity] = v.coerceIn(0.05f, 1f) }
    suspend fun setColor(rgb: Int) = edit { it[Keys.colorRgb] = rgb and 0xFFFFFF }
    suspend fun setDynamic(v: Boolean) = edit { it[Keys.dynamic] = v }
    suspend fun setEdges(v: EdgeMode) = edit { it[Keys.edges] = v.name }
    suspend fun setDetectionSource(v: DetectionSource) = edit { it[Keys.detection] = v.name }
    suspend fun setKeepScreenOn(v: Boolean) = edit { it[Keys.keepScreenOn] = v }
    suspend fun setShowComfortMeter(v: Boolean) = edit { it[Keys.showComfortMeter] = v }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit(block)
    }
}

package ch.lkmc.spots.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.Choreographer
import android.view.View
import ch.lkmc.spots.engine.Appearance
import ch.lkmc.spots.engine.CueConfig
import ch.lkmc.spots.engine.MotionCueEngine
import ch.lkmc.spots.motion.MotionSample
import kotlin.math.sin

/**
 * The on-screen spots. A plain [View] (not Compose — see research doc 03 §4) whose
 * [MotionCueEngine] is advanced once per display frame by a [Choreographer]
 * callback using the true frame delta, then drawn with `Canvas.drawCircle`.
 *
 * No allocation happens in [onDraw] or the frame loop.
 */
class DotsOverlayView(context: Context) : View(context) {

    val engine = MotionCueEngine()

    /** Supplies the latest motion sample each frame (published by the service). */
    var sampleProvider: () -> MotionSample = { MotionSample.ZERO }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val density = resources.displayMetrics.density

    private var radiusPx = 5f * density
    private var baseColorRgb = 0xE8E8EA
    private var opacity = 0.55f
    private var dynamic = false

    private val choreographer = Choreographer.getInstance()
    private var running = false
    private var lastFrameNanos = 0L
    private var dynamicTimeSec = 0f

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!running) return
            val dt = if (lastFrameNanos == 0L) {
                0f
            } else {
                ((frameTimeNanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
            }
            lastFrameNanos = frameTimeNanos
            dynamicTimeSec += dt
            engine.update(dt, sampleProvider())
            invalidate()
            choreographer.postFrameCallback(this)
        }
    }

    fun applyAppearance(appearance: Appearance) {
        engine.setAppearance(appearance)
        radiusPx = appearance.dotRadiusDp * density
        baseColorRgb = appearance.colorArgb and 0xFFFFFF
        opacity = appearance.opacity
        dynamic = appearance.dynamic
        updatePaint()
    }

    fun applyConfig(config: CueConfig) {
        engine.config = config
    }

    private fun updatePaint() {
        val alpha = (opacity.coerceIn(0f, 1f) * 255f).toInt()
        paint.color = (alpha shl 24) or baseColorRgb
    }

    private fun start() {
        if (running) return
        running = true
        lastFrameNanos = 0L
        choreographer.postFrameCallback(frameCallback)
    }

    private fun stop() {
        running = false
        choreographer.removeFrameCallback(frameCallback)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updatePaint()
        start()
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return
        for (d in engine.dots) {
            var cx = d.x * w
            var cy = d.y * h
            var r = radiusPx * d.sizeFactor
            if (dynamic) {
                // A gentle organic shimmer (the iOS-26-style "Dynamic" pattern):
                // a slow size pulse and a small perpendicular wobble, per-dot phase.
                val phase = d.phase + dynamicTimeSec * 1.6f
                r *= (0.82f + 0.18f * sin(phase))
                cx += sin(phase * 0.7f) * radiusPx * 0.6f
                cy += sin(phase * 0.9f + 1.3f) * radiusPx * 0.6f
            }
            canvas.drawCircle(cx, cy, r, paint)
        }
    }
}

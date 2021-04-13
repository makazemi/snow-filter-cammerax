package com.google.mlkit.vision.camerasample.camerax

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.OrientationEventListener
import android.view.View
import androidx.camera.core.CameraSelector
import com.bumptech.glide.load.ImageHeaderParser
import kotlin.math.ceil
import timber.log.Timber
import java.util.*

open class GraphicOverlay(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private val lock = Any()
    private val graphics: MutableList<Graphic> = ArrayList()
    var mScale: Float? = null
    var mOffsetX: Float? = null
    var mOffsetY: Float? = null
    var cameraSelector: Int = CameraSelector.LENS_FACING_BACK
    lateinit var processBitmap: Bitmap
    lateinit var processCanvas: Canvas
    private var currentRotation = 0

    private val orientationEventListener by lazy {
        object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ImageHeaderParser.UNKNOWN_ORIENTATION) {
                    return
                }
                val rotation = when (orientation) {
                    in 45 until 135 -> 270
                    in 135 until 225 -> 180
                    in 225 until 315 -> 90
                    else -> 0
                }
                if (currentRotation != rotation) {
                    graphics.forEach {
                        it.onOrientationChanged(rotation)
                    }
                    Timber.d("onOrientationChanged=$rotation")
                    currentRotation = rotation
                }

            }
        }
    }


    init {
        Timber.d("init")
        orientationEventListener.enable()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        graphics.forEach {
            it.resize(w, h, oldw, oldh)
        }

    }

    abstract class Graphic(private val overlay: GraphicOverlay) {

        abstract fun resize(w: Int, h: Int, oldw: Int, oldh: Int)

        abstract fun draw(canvas: Canvas?)

        abstract fun onOrientationChanged(rotation: Int)

        fun calculateRect(height: Float, width: Float, boundingBoxT: Rect): RectF {

            // for land scape
            fun isLandScapeMode(): Boolean {
                return overlay.context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            }

            fun whenLandScapeModeWidth(): Float {
                return when (isLandScapeMode()) {
                    true -> width
                    false -> height
                }
            }

            fun whenLandScapeModeHeight(): Float {
                return when (isLandScapeMode()) {
                    true -> height
                    false -> width
                }
            }

            val scaleX = overlay.width.toFloat() / whenLandScapeModeWidth()
            val scaleY = overlay.height.toFloat() / whenLandScapeModeHeight()
            val scale = scaleX.coerceAtLeast(scaleY)
            overlay.mScale = scale

            // Calculate offset (we need to center the overlay on the target)
            val offsetX = (overlay.width.toFloat() - ceil(whenLandScapeModeWidth() * scale)) / 2.0f
            val offsetY =
                (overlay.height.toFloat() - ceil(whenLandScapeModeHeight() * scale)) / 2.0f

            overlay.mOffsetX = offsetX
            overlay.mOffsetY = offsetY

            val mappedBox = RectF().apply {
                left = boundingBoxT.right * scale + offsetX
                top = boundingBoxT.top * scale + offsetY
                right = boundingBoxT.left * scale + offsetX
                bottom = boundingBoxT.bottom * scale + offsetY
            }

            // for front mode
            if (overlay.isFrontMode()) {
                val centerX = overlay.width.toFloat() / 2
                mappedBox.apply {
                    left = centerX + (centerX - left)
                    right = centerX - (right - centerX)
                }
            }
            return mappedBox
        }

        fun translateX(horizontal: Float): Float {
            return if (overlay.mScale != null && overlay.mOffsetX != null && !overlay.isFrontMode()) {
                (horizontal * overlay.mScale!!) + overlay.mOffsetX!!
            } else if (overlay.mScale != null && overlay.mOffsetX != null && overlay.isFrontMode()) {
                val centerX = overlay.width.toFloat() / 2
                centerX - ((horizontal * overlay.mScale!!) + overlay.mOffsetX!! - centerX)
            } else {
                horizontal
            }
        }

        fun translateY(vertical: Float): Float {
            return if (overlay.mScale != null && overlay.mOffsetY != null) {
                (vertical * overlay.mScale!!) + overlay.mOffsetY!!
            } else {
                vertical
            }
        }

    }

    fun isFrontMode() = cameraSelector == CameraSelector.LENS_FACING_FRONT

    fun toggleSelector() {
        cameraSelector =
            if (cameraSelector == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT
            else CameraSelector.LENS_FACING_BACK
    }

    fun clear() {
        synchronized(lock) { graphics.clear() }
        postInvalidate()
    }

    fun add(graphic: Graphic) {
        synchronized(lock) { graphics.add(graphic) }
    }

    fun remove(graphic: Graphic) {
        synchronized(lock) { graphics.remove(graphic) }
        postInvalidate()
    }

    private fun initProcessCanvas() {
        processBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
        processCanvas = Canvas(processBitmap)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        Timber.d("onDraw")
        synchronized(lock) {
            initProcessCanvas()
            graphics.forEach {
                it.draw(canvas)
                it.draw(processCanvas)
            }
        }
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        orientationEventListener.disable()
        Timber.d("ondetach")
    }

}
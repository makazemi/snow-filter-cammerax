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

//open class GraphicOverlay(context: Context?, attrs: AttributeSet?) :
//    View(context, attrs) {
//    private val lock = Any()
//    private val graphics: MutableList<Graphic> = ArrayList()
//
//    // Matrix for transforming from image coordinates to overlay view coordinates.
//    private val transformationMatrix = Matrix()
//    var imageWidth = 0
//        private set
//    var imageHeight = 0
//        private set
//
//    // The factor of overlay View size to image size. Anything in the image coordinates need to be
//    // scaled by this amount to fit with the area of overlay View.
//    private var scaleFactor = 1.0f
//
//    // The number of horizontal pixels needed to be cropped on each side to fit the image with the
//    // area of overlay View after scaling.
//    private var postScaleWidthOffset = 0f
//
//    // The number of vertical pixels needed to be cropped on each side to fit the image with the
//    // area of overlay View after scaling.
//    private var postScaleHeightOffset = 0f
//    private var isImageFlipped = false
//    private var needUpdateTransformation = true
//
//
//    var cameraSelector: Int = CameraSelector.LENS_FACING_BACK
//    private var currentRotation = 0
//        lateinit var processBitmap: Bitmap
//    lateinit var processCanvas: Canvas
//    private val orientationEventListener by lazy {
//        object : OrientationEventListener(context) {
//            override fun onOrientationChanged(orientation: Int) {
//                if (orientation == ImageHeaderParser.UNKNOWN_ORIENTATION) {
//                    return
//                }
//                needUpdateTransformation = true
//                val rotation = when (orientation) {
//                    in 45 until 135 -> 270
//                    in 135 until 225 -> 180
//                    in 225 until 315 -> 90
//                    else -> 0
//                }
//                if (currentRotation != rotation) {
//                    graphics.forEach {
//                        it.onOrientationChanged(rotation)
//                    }
//                    Timber.d("onOrientationChanged=$rotation")
//                    currentRotation = rotation
//                }
//
//            }
//        }
//    }
//
//
//    init {
//        Timber.d("init")
//        orientationEventListener.enable()
//    }
//
//    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
//        super.onSizeChanged(w, h, oldw, oldh)
//        graphics.forEach {
//            it.resize(w, h, oldw, oldh)
//        }
//
//    }
//
//    /**
//     * Base class for a custom graphics object to be rendered within the graphic overlay. Subclass
//     * this and implement the [Graphic.draw] method to define the graphics element. Add
//     * instances to the overlay using [GraphicOverlay.add].
//     */
//    abstract class Graphic(private val overlay: GraphicOverlay) {
//        /**
//         * Draw the graphic on the supplied canvas. Drawing should use the following methods to convert
//         * to view coordinates for the graphics that are drawn:
//         *
//         *
//         *  1. [Graphic.scale] adjusts the size of the supplied value from the image
//         * scale to the view scale.
//         *  1. [Graphic.translateX] and [Graphic.translateY] adjust the
//         * coordinate from the image's coordinate system to the view coordinate system.
//         *
//         *
//         * @param canvas drawing canvas
//         */
//
//        abstract fun resize(w: Int, h: Int, oldw: Int, oldh: Int)
//
//        abstract fun onOrientationChanged(rotation: Int)
//        abstract fun draw(canvas: Canvas?)
//
//        /** Adjusts the supplied value from the image scale to the view scale.  */
//        fun scale(imagePixel: Float): Float {
//            return imagePixel * overlay.scaleFactor
//        }
//
//        /** Returns the application context of the app.  */
//        val applicationContext: Context
//            get() = overlay.context.applicationContext
//
//        fun isImageFlipped(): Boolean {
//            return overlay.isImageFlipped
//        }
//
//        /**
//         * Adjusts the x coordinate from the image's coordinate system to the view coordinate system.
//         */
//        fun translateX(x: Float): Float {
//            return if (overlay.isImageFlipped) {
//                overlay.width - (scale(x) - overlay.postScaleWidthOffset)
//            } else {
//                scale(x) - overlay.postScaleWidthOffset
//            }
//        }
//
//        /**
//         * Adjusts the y coordinate from the image's coordinate system to the view coordinate system.
//         */
//        fun translateY(y: Float): Float {
//            return scale(y) - overlay.postScaleHeightOffset
//        }
//
//        /**
//         * Returns a [Matrix] for transforming from image coordinates to overlay view coordinates.
//         */
//        fun getTransformationMatrix(): Matrix {
//            return overlay.transformationMatrix
//        }
//
//        fun postInvalidate() {
//            overlay.postInvalidate()
//        }
//    }
//
//    /** Removes all graphics from the overlay.  */
//    fun clear() {
//        synchronized(lock) { graphics.clear() }
//        postInvalidate()
//    }
//
//    /** Adds a graphic to the overlay.  */
//    fun add(graphic: Graphic) {
//        synchronized(lock) { graphics.add(graphic) }
//    }
//
//    /** Removes a graphic from the overlay.  */
//    fun remove(graphic: Graphic) {
//        synchronized(lock) { graphics.remove(graphic) }
//        postInvalidate()
//    }
//
//    /**
//     * Sets the source information of the image being processed by detectors, including size and
//     * whether it is flipped, which informs how to transform image coordinates later.
//     *
//     * @param imageWidth the width of the image sent to ML Kit detectors
//     * @param imageHeight the height of the image sent to ML Kit detectors
//     * @param isFlipped whether the image is flipped. Should set it to true when the image is from the
//     * front camera.
//     */
//    fun setImageSourceInfo(imageWidth: Int, imageHeight: Int, isFlipped: Boolean) {
//        Preconditions.checkState(imageWidth > 0, "image width must be positive")
//        Preconditions.checkState(imageHeight > 0, "image height must be positive")
//        synchronized(lock) {
//            this.imageWidth = imageWidth
//            this.imageHeight = imageHeight
//            isImageFlipped = isFlipped
//            needUpdateTransformation = true
//        }
//        postInvalidate()
//    }
//
//    private fun updateTransformationIfNeeded() {
//        if (!needUpdateTransformation || imageWidth <= 0 || imageHeight <= 0) {
//            return
//        }
//        val viewAspectRatio = width.toFloat() / height
//        val imageAspectRatio = imageWidth.toFloat() / imageHeight
//        postScaleWidthOffset = 0f
//        postScaleHeightOffset = 0f
//        if (viewAspectRatio > imageAspectRatio) {
//            // The image needs to be vertically cropped to be displayed in this view.
//            scaleFactor = width.toFloat() / imageWidth
//            postScaleHeightOffset = (width.toFloat() / imageAspectRatio - height) / 2
//        } else {
//            // The image needs to be horizontally cropped to be displayed in this view.
//            scaleFactor = height.toFloat() / imageHeight
//            postScaleWidthOffset = (height.toFloat() * imageAspectRatio - width) / 2
//        }
//        transformationMatrix.reset()
//        transformationMatrix.setScale(scaleFactor, scaleFactor)
//        transformationMatrix.postTranslate(-postScaleWidthOffset, -postScaleHeightOffset)
//        if (isImageFlipped) {
//            transformationMatrix.postScale(-1f, 1f, width / 2f, height / 2f)
//        }
//        needUpdateTransformation = false
//    }
//
//    /** Draws the overlay with its associated graphic objects.  */
//    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
//        synchronized(lock) {
//            updateTransformationIfNeeded()
//            initProcessCanvas()
//            for (graphic in graphics) {
//                graphic.draw(canvas)
//                graphic.draw(processCanvas)
//            }
//        }
//    }
//
//        private fun initProcessCanvas() {
//        processBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
//        processCanvas = Canvas(processBitmap)
//    }
//
//        fun toggleSelector() {
//        cameraSelector =
//            if (cameraSelector == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT
//            else CameraSelector.LENS_FACING_BACK
//    }
//
//}
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
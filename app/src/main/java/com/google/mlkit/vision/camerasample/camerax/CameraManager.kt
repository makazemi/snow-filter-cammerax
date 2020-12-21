package com.google.mlkit.vision.camerasample.camerax

import android.annotation.SuppressLint
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.ScaleGestureDetector
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.camerasample.detector.face.FaceContourDetectionProcessor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CameraManager(
    private val context: Context,
    private val finderView: PreviewView,
    private val lifecycleOwner: LifecycleOwner,
    private val graphicOverlay: GraphicOverlay

) {

    private var preview: Preview? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalyzer: ImageAnalysis? = null


    // default barcode scanner
    private var analyzerVisionType: VisionType = VisionType.NONE

    lateinit var cameraExecutor: ExecutorService
    lateinit var imageCapture: ImageCapture
    lateinit var metrics: DisplayMetrics

    var rotation = 0F
    private var targetRotation=0
    var cameraSelectorOption = CameraSelector.LENS_FACING_BACK

    init {
        createNewExecutor()
    }

    private fun createNewExecutor() {
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun selectAnalyzer(): ImageAnalysis.Analyzer {
        return when (analyzerVisionType) {
            VisionType.Face ->  FaceContourDetectionProcessor(graphicOverlay,context)
            else -> FaceContourDetectionProcessor(graphicOverlay,context)
        }
    }

    private fun setCameraConfig(
        cameraProvider: ProcessCameraProvider?,
        cameraSelector: CameraSelector
    ) {
        try {
            cameraProvider?.unbindAll()
            if(analyzerVisionType==VisionType.NONE) {
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            }
            else {
                camera = cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture,
                    imageAnalyzer
                )
            }
            preview?.setSurfaceProvider(
                finderView.surfaceProvider
            )
        } catch (e: Exception) {
            Log.e(TAG, "Use case binding failed", e)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpPinchToZoom() {
        val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val currentZoomRatio: Float = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 1F
                val delta = detector.scaleFactor
                camera?.cameraControl?.setZoomRatio(currentZoomRatio * delta)
                return true
            }
        }
        val scaleGestureDetector = ScaleGestureDetector(context, listener)
        finderView.setOnTouchListener { _, event ->
            finderView.post {
                scaleGestureDetector.onTouchEvent(event)
            }
            return@setOnTouchListener true
        }
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(
            {
                cameraProvider = cameraProviderFuture.get()
                preview = Preview.Builder().build()

                if(analyzerVisionType!=VisionType.NONE) {
                    imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor, selectAnalyzer())
                        }

                    Log.d(TAG,"vision type!=none")
                }
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(cameraSelectorOption)
                    .build()

            //    metrics =  DisplayMetrics().also { finderView.display.getRealMetrics(it) }
               // val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)

                imageCapture =
                    ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetRotation(targetRotation)
                       // .setTargetAspectRatio(screenAspectRatio)
                        .build()

                setUpPinchToZoom()
                setCameraConfig(cameraProvider, cameraSelector)

            }, ContextCompat.getMainExecutor(context)
        )
    }

    fun changeCameraSelector() {
        cameraProvider?.unbindAll()
        cameraSelectorOption =
            if (cameraSelectorOption == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT
            else CameraSelector.LENS_FACING_BACK
       graphicOverlay.toggleSelector()
        startCamera()
    }

    fun changeAnalyzer(visionType: VisionType) {
        if (analyzerVisionType != visionType) {
            cameraProvider?.unbindAll()
            analyzerVisionType = visionType
            startCamera()
        }
    }

    fun isHorizontalMode() : Boolean {
        return rotation == 90f || rotation == 270f
    }

    fun isFrontMode() : Boolean {
        return cameraSelectorOption == CameraSelector.LENS_FACING_FRONT
    }

    fun setTargetRotation(rotation:Int){
        targetRotation=rotation
        imageCapture.targetRotation=rotation
        imageAnalyzer?.targetRotation=rotation
    }

    companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
        private const val TAG = "CameraXBasic"
    }

}
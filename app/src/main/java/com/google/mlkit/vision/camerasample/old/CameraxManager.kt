package com.google.mlkit.vision.camerasample.old

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.camerasample.camerax.GraphicOverlay
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraXManager(
        private val context: Context,
        private val finderView: PreviewView,
        private val lifecycleOwner: LifecycleOwner,
        private val graphicOverlay: GraphicOverlay,
        private val isAllPermissionsGranted:Boolean
   // var imageProcessor:VisionProcessorBase<List<Face>>?
) {

    private var analysisUseCase: ImageAnalysis? = null

    private var previewUseCase: Preview? = null
    var imageProcessor: VisionImageProcessor? = null

    var imageCapture: ImageCapture? = null
    private var lensFacing = CameraSelector.LENS_FACING_FRONT

    private var cameraSelector: CameraSelector=CameraSelector.Builder().requireLensFacing(lensFacing).build()

    private var needUpdateGraphicOverlayImageSourceInfo = false

    private var cameraProvider: ProcessCameraProvider?=null

    lateinit var cameraExecutor: ExecutorService

    init {
        createNewExecutor()
    }


    fun startCamera() {
        if(imageProcessor==null){
            Log.d(TAG,"imagerprocessor==null")
        }
        if(finderView==null){
            Log.d(TAG,"finderview==null")
        }
        if(graphicOverlay==null){
            Log.d(TAG,"graphicoverlya==null")
        }
        Log.d(TAG,"ispermissrequ=$isAllPermissionsGranted")

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner

            cameraProvider=cameraProviderFuture.get()

            imageCapture = ImageCapture.Builder()
                .build()
            Log.d(TAG,"imageCapture=$imageCapture")


            if (isAllPermissionsGranted) {
                bindAllCameraUseCases()
            }


        }, ContextCompat.getMainExecutor(context))
    }

    fun bindAllCameraUseCases() {
        cameraProvider?.let { provider->
            provider.unbindAll()
            bindPreviewUseCase()
            bindAnalysisUseCase()
        }
    }

    private fun bindPreviewUseCase() {
        if (cameraProvider == null) {
            Log.d(TAG,"camera provider==null")
            return
        }
        if (previewUseCase != null) {
            Log.d(TAG,"previewUseCase != null")
            cameraProvider?.unbind(previewUseCase)
        }

        val builder = Preview.Builder()

        previewUseCase = builder.build()
        previewUseCase?.setSurfaceProvider(finderView.surfaceProvider)

        cameraProvider?.bindToLifecycle(/* lifecycleOwner= */lifecycleOwner, cameraSelector, previewUseCase,imageCapture)
        Log.d(TAG,"bindPreviewUseCase")
    }

    //@SuppressLint("UnsafeExperimentalUsageError")
    private fun bindAnalysisUseCase() {
        if (cameraProvider == null) {
            Log.d(TAG,"cameraprovider==null")
            return
        }
        if (analysisUseCase != null) {
            cameraProvider?.unbind(analysisUseCase)
        }
        if (imageProcessor != null) {
            imageProcessor?.stop()
        }

         try {

            Log.d(TAG,
                    "Using Face Detector Processor")
            //  val faceDetectorOptions = PreferenceUtils.getFaceDetectorOptionsForLivePreview(this)
           FaceDetectorProcessor(context = context)

         //   imageProcessor?.start()

        } catch (e: Exception) {
            Log.d(
                  TAG,
                    "Can not create image processor: $",
                    e
            )
            Toast.makeText(
                    context,
                    "Can not create image processor: " + e.localizedMessage,
                    Toast.LENGTH_LONG
            )
                    .show()
            return
        }

        val builder = ImageAnalysis.Builder()

        analysisUseCase = builder.build()

        needUpdateGraphicOverlayImageSourceInfo = true

        analysisUseCase?.setAnalyzer(
            // imageProcessor.processImageProxy will use another thread to run the detection underneath,
            // thus we can just runs the analyzer itself on main thread.
            cameraExecutor, { imageProxy: ImageProxy ->
                if (needUpdateGraphicOverlayImageSourceInfo) {
                    val isImageFlipped =
                        lensFacing == CameraSelector.LENS_FACING_FRONT
                    val rotationDegrees =
                        imageProxy.imageInfo.rotationDegrees
                    if (rotationDegrees == 0 || rotationDegrees == 180) {
//                        graphicOverlay.setImageSourceInfo(
//                            imageProxy.width, imageProxy.height, isImageFlipped
//                        )
                    } else {
//                        graphicOverlay.setImageSourceInfo(
//                            imageProxy.height, imageProxy.width, isImageFlipped
//                        )
                    }
                    needUpdateGraphicOverlayImageSourceInfo = false
                }
                try {
                    imageProcessor?.processImageProxy(imageProxy,graphicOverlay)
                    Log.d(TAG,"  imageProcessor?.processImageProxy")
                } catch (e: MlKitException) {
                    Log.d(
                       TAG,
                        "Failed to process image. Error: " + e.localizedMessage
                    )
                    Toast.makeText(
                        context,
                        e.localizedMessage,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        )
        val a =cameraProvider?.bindToLifecycle( /* lifecycleOwner= */lifecycleOwner, cameraSelector, analysisUseCase)
        if(a==null)
            Log.d(TAG,"a==null")
    }

    fun onChangedCameraSelector(){
        if (cameraProvider == null) {
            return
        }
        cameraProvider?.let { provider->
            val newLensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                CameraSelector.LENS_FACING_BACK
            } else {
                CameraSelector.LENS_FACING_FRONT
            }
            val newCameraSelector =
                    CameraSelector.Builder().requireLensFacing(newLensFacing).build()
            try {
                if (provider.hasCamera(newCameraSelector)) {
                    lensFacing = newLensFacing
                    cameraSelector = newCameraSelector
                    bindAllCameraUseCases()
                    return
                }
            } catch (e: CameraInfoUnavailableException) {
                // Falls through
            }
            Toast.makeText(
                    context, "This device does not have lens with facing: $newLensFacing",
                    Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun createNewExecutor() {
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    companion object{
        const val TAG="CameraXManager"
    }
}
package com.google.mlkit.vision.camerasample

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.face.Face

class CameraXManager(
    private val context: Context,
    private val finderView: PreviewView,
    private val lifecycleOwner: LifecycleOwner,
    private val graphicOverlay: GraphicOverlay,
    private val isAllPermissionsGranted:Boolean,
    private var imageProcessor:VisionProcessorBase<List<Face>>
) {

    private var analysisUseCase: ImageAnalysis? = null

    //private var imageProcessor: VisionImageProcessor? = null

    var imageCapture: ImageCapture? = null
    private var lensFacing = CameraSelector.LENS_FACING_FRONT

    private var cameraSelector: CameraSelector=CameraSelector.Builder().requireLensFacing(lensFacing).build()

    private var needUpdateGraphicOverlayImageSourceInfo = false

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            imageCapture = ImageCapture.Builder()
                .build()
            Log.d(TAG,"imagecamplive=$imageCapture")



            if (isAllPermissionsGranted) {
                bindAllCameraUseCases(cameraProvider)
            }


        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindAllCameraUseCases(cameraProvider: ProcessCameraProvider) {
            cameraProvider.unbindAll()
            bindPreviewUseCase(cameraProvider)
            bindAnalysisUseCase(cameraProvider)
    }

    private fun bindPreviewUseCase(cameraProvider: ProcessCameraProvider) {
        val preview: Preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(finderView.surfaceProvider)
            }
       // cameraProvider.unbind(previewUseCase)

        cameraProvider.bindToLifecycle(/* lifecycleOwner= */lifecycleOwner, cameraSelector, preview,imageCapture)
        Log.d(TAG,"bindPreviewUseCase")
    }

    private fun bindAnalysisUseCase(cameraProvider: ProcessCameraProvider) {

        if (analysisUseCase != null) {
            cameraProvider.unbind(analysisUseCase)
        }
        if (imageProcessor != null) {
            imageProcessor?.stop()
        }
        imageProcessor = try {

            Log.i(
             TAG,
                "Using Face Detector Processor")
            //  val faceDetectorOptions = PreferenceUtils.getFaceDetectorOptionsForLivePreview(this)
            FaceDetectorProcessor(context = context)


        } catch (e: Exception) {

            Toast.makeText(
                context,
                "Can not create image processor: " + e.localizedMessage,
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val builder = ImageAnalysis.Builder()

        analysisUseCase = builder.build()

        needUpdateGraphicOverlayImageSourceInfo = true

        analysisUseCase?.setAnalyzer(
            // imageProcessor.processImageProxy will use another thread to run the detection underneath,
            // thus we can just runs the analyzer itself on main thread.
            ContextCompat.getMainExecutor(context),
            ImageAnalysis.Analyzer { imageProxy: ImageProxy ->
                if (needUpdateGraphicOverlayImageSourceInfo) {
                    val isImageFlipped =
                        lensFacing == CameraSelector.LENS_FACING_FRONT
                    val rotationDegrees =
                        imageProxy.imageInfo.rotationDegrees
                    if (rotationDegrees == 0 || rotationDegrees == 180) {
                        graphicOverlay.setImageSourceInfo(
                            imageProxy.width, imageProxy.height, isImageFlipped
                        )
                    } else {
                        graphicOverlay.setImageSourceInfo(
                            imageProxy.height, imageProxy.width, isImageFlipped
                        )
                    }
                    needUpdateGraphicOverlayImageSourceInfo = false
                }
                try {
                    imageProcessor?.processImageProxy(imageProxy,graphicOverlay)
                } catch (e: MlKitException) {
                    Log.e(
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
        cameraProvider.bindToLifecycle( /* lifecycleOwner= */lifecycleOwner, cameraSelector, analysisUseCase)
    }


    companion object{
        const val TAG="CameraXManager"
    }
}
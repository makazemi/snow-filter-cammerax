package com.google.mlkit.vision.camerasample

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.media.Image
import android.net.Uri
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.CompoundButton
import android.widget.Toast
import android.widget.ToggleButton
import androidx.annotation.RequiresApi
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.annotation.KeepName
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.camerasample.databinding.ActivityMainBinding
import com.google.mlkit.vision.camerasample.preference.PreferenceUtils
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.File.separator
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

/** Live preview demo app for ML Kit APIs using CameraX.  */
@KeepName
@RequiresApi(VERSION_CODES.LOLLIPOP)
class CameraActivity :
        AppCompatActivity(),
        ActivityCompat.OnRequestPermissionsResultCallback,
        CompoundButton.OnCheckedChangeListener {


    private lateinit var binding: ActivityMainBinding


    private var cameraProvider: ProcessCameraProvider? = null
    private var previewUseCase: Preview? = null
    private var analysisUseCase: ImageAnalysis? = null
    private var imageProcessor: VisionImageProcessor? = null
    private var needUpdateGraphicOverlayImageSourceInfo = false
    private var selectedModel = OBJECT_DETECTION
    private var lensFacing = CameraSelector.LENS_FACING_FRONT
    private lateinit var cameraSelector: CameraSelector

    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")


        if (savedInstanceState != null) {
            selectedModel =
                    savedInstanceState.getString(
                            STATE_SELECTED_MODEL,
                            OBJECT_DETECTION
                    )
            lensFacing =
                    savedInstanceState.getInt(
                            STATE_LENS_FACING,
                            CameraSelector.LENS_FACING_FRONT
                    )
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        outputDirectory = getOutputDirectory()




        val facingSwitch =
                findViewById<ToggleButton>(R.id.facing_switch)
        facingSwitch.setOnCheckedChangeListener(this)


        startCamera()

        if (!allPermissionsGranted()) {
            runtimePermissions
        }

        btn_take_picture.setOnClickListener {
            takePhoto()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            cameraProvider = cameraProviderFuture.get()

            imageCapture = ImageCapture.Builder()
                    .build()
            Log.d(TAG,"imagecamplive=$imageCapture")

            if (allPermissionsGranted()) {
                bindAllCameraUseCases()
            }


        }, ContextCompat.getMainExecutor(this))
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        bundle.putString(STATE_SELECTED_MODEL, selectedModel)
        bundle.putInt(STATE_LENS_FACING, lensFacing)
    }


    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        Log.d(TAG, "Set facing")
        if (cameraProvider == null) {
            return
        }
        val newLensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }
        val newCameraSelector =
                CameraSelector.Builder().requireLensFacing(newLensFacing).build()
        try {
            if (cameraProvider!!.hasCamera(newCameraSelector)) {
                lensFacing = newLensFacing
                cameraSelector = newCameraSelector
                bindAllCameraUseCases()
                return
            }
        } catch (e: CameraInfoUnavailableException) {
            // Falls through
        }
        Toast.makeText(
                applicationContext, "This device does not have lens with facing: $newLensFacing",
                Toast.LENGTH_SHORT
        )
                .show()
    }


    public override fun onResume() {
        super.onResume()
        bindAllCameraUseCases()
    }

    override fun onPause() {
        super.onPause()

        imageProcessor?.run {
            this.stop()
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        imageProcessor?.run {
            this.stop()
        }
    }

    private fun bindAllCameraUseCases() {
        if (cameraProvider != null) {
            // As required by CameraX API, unbinds all use cases before trying to re-bind any of them.
            cameraProvider?.unbindAll()
            bindPreviewUseCase()
            bindAnalysisUseCase()
        }
    }

    private fun bindPreviewUseCase() {
//        if (!PreferenceUtils.isCameraLiveViewportEnabled(this)) {
//            Log.d(TAG,"!PreferenceUtils")
//            return
//        }
        if (cameraProvider == null) {
            Log.d(TAG,"cameraProvider == null")
            return
        }
        if (previewUseCase != null) {
            Log.d(TAG,"previewUseCase != null")
            cameraProvider?.unbind(previewUseCase)
        }

        val builder = Preview.Builder()
        val targetResolution = PreferenceUtils.getCameraXTargetResolution(this)
        targetResolution?.let {
            builder.setTargetResolution(it)
        }
        previewUseCase = builder.build()
        previewUseCase?.setSurfaceProvider(binding.previewView.surfaceProvider)
        cameraProvider?.bindToLifecycle(/* lifecycleOwner= */this, cameraSelector, previewUseCase,imageCapture)
        Log.d(TAG,"bindPreviewUseCase")
    }

    private fun bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return
        }
        if (analysisUseCase != null) {
            cameraProvider?.unbind(analysisUseCase)
        }
        if (imageProcessor != null) {
            imageProcessor?.stop()
        }
        imageProcessor = try {

            Log.i(
                    TAG,
                    "Using Face Detector Processor")
            //  val faceDetectorOptions = PreferenceUtils.getFaceDetectorOptionsForLivePreview(this)
            FaceDetectorProcessor(context = this)


        } catch (e: Exception) {
            Log.e(
                    TAG,
                    "Can not create image processor: $selectedModel",
                    e
            )
            Toast.makeText(
                    applicationContext,
                    "Can not create image processor: " + e.localizedMessage,
                    Toast.LENGTH_LONG
            )
                    .show()
            return
        }

        val builder = ImageAnalysis.Builder()
        val targetResolution = PreferenceUtils.getCameraXTargetResolution(this)
        targetResolution?.let {
            builder.setTargetResolution(it)
        }
        analysisUseCase = builder.build()

        needUpdateGraphicOverlayImageSourceInfo = true

        analysisUseCase?.setAnalyzer(
                // imageProcessor.processImageProxy will use another thread to run the detection underneath,
                // thus we can just runs the analyzer itself on main thread.
                ContextCompat.getMainExecutor(this),
                ImageAnalysis.Analyzer { imageProxy: ImageProxy ->
                    if (needUpdateGraphicOverlayImageSourceInfo) {
                        val isImageFlipped =
                                lensFacing == CameraSelector.LENS_FACING_FRONT
                        val rotationDegrees =
                                imageProxy.imageInfo.rotationDegrees
                        if (rotationDegrees == 0 || rotationDegrees == 180) {
                            binding.graphicOverlay.setImageSourceInfo(
                                    imageProxy.width, imageProxy.height, isImageFlipped
                            )
                        } else {
                            binding.graphicOverlay.setImageSourceInfo(
                                    imageProxy.height, imageProxy.width, isImageFlipped
                            )
                        }
                        needUpdateGraphicOverlayImageSourceInfo = false
                    }
                    try {
                        imageProcessor?.processImageProxy(imageProxy,  binding.graphicOverlay)
                    } catch (e: MlKitException) {
                        Log.e(
                                TAG,
                                "Failed to process image. Error: " + e.localizedMessage
                        )
                        Toast.makeText(
                                applicationContext,
                                e.localizedMessage,
                                Toast.LENGTH_SHORT
                        )
                                .show()
                    }
                }
        )
        cameraProvider?.bindToLifecycle( /* lifecycleOwner= */this, cameraSelector!!, analysisUseCase)
    }

    private val requiredPermissions: Array<String?>
        get() = try {
            val info = this.packageManager
                    .getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)
            val ps = info.requestedPermissions
            if (ps != null && ps.isNotEmpty()) {
                ps
            } else {
                arrayOfNulls(0)
            }
        } catch (e: Exception) {
            arrayOfNulls(0)
        }

    private fun allPermissionsGranted(): Boolean {
        for (permission in requiredPermissions) {
            if (!isPermissionGranted(this, permission)) {
                return false
            }
        }
        return true
    }

    private val runtimePermissions: Unit
        get() {
            val allNeededPermissions: MutableList<String?> = ArrayList()
            for (permission in requiredPermissions) {
                if (!isPermissionGranted(this, permission)) {
                    allNeededPermissions.add(permission)
                }
            }
            if (allNeededPermissions.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                        this,
                        allNeededPermissions.toTypedArray(),
                        PERMISSION_REQUESTS
                )
            }
        }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        Log.i(TAG, "Permission granted!")
        if (allPermissionsGranted()) {
            bindAllCameraUseCases()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        Log.d(TAG,"imagecapture=$imageCapture")
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
//        val photoFile = File(
//                outputDirectory,
//                SimpleDateFormat(FILENAME_FORMAT, Locale.US
//                ).format(System.currentTimeMillis()) + ".jpg")

        // Create output options object which contains file + metadata
        // val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
                ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageCapturedCallback() {
                    @SuppressLint("UnsafeExperimentalUsageError", "RestrictedApi")
                    override fun onCaptureSuccess(image: ImageProxy) {
                        image.image?.let {
                            imageToBitmapSaveGallery(it)

                        }
                        super.onCaptureSuccess(image)
                    }
                })
//        imageCapture.takePicture(
//                outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
//            override fun onError(exc: ImageCaptureException) {
//                Log.d(TAG, "Photo capture failed: ${exc.message}", exc)
//            }
//
//            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
//             //   val savedUri = Uri.fromFile(photoFile)
//                val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
//                val msg = "Photo capture succeeded: $savedUri"
//                img_result.setImageURI(savedUri)
//                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
//                Log.d(TAG, msg)
//
//                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
//                   sendBroadcast(
//                            Intent(Camera.ACTION_NEW_PICTURE, savedUri)
//                    )
//                }
//
//                // If the folder selected is an external media directory, this is
//                // unnecessary but otherwise other apps will not be able to access our
//                // images unless we scan them using [MediaScannerConnection]
//                val mimeType = MimeTypeMap.getSingleton()
//                        .getMimeTypeFromExtension(savedUri.toFile().extension)
//                MediaScannerConnection.scanFile(
//                        this@CameraXLivePreviewActivity,
//                        arrayOf(savedUri.toFile().absolutePath),
//                        arrayOf(mimeType)
//                ) { _, uri ->
//                    Log.d(TAG, "Image capture scanned into media store: $uri")
//                }
//            }
//        })
    }

    private fun imageToBitmapSaveGallery(image: Image) {

        image.imageToBitmap()
                ?.scaleImage(
                        binding.previewView,
                        false
                )
                ?.let { bitmap ->
                    binding.graphicOverlay.processCanvas?.drawBitmap(
                            bitmap,
                            0f,
                            bitmap.getBaseYByView(
                                    binding.graphicOverlay,
                                    false
                            ),
                            Paint().apply {
                                xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OVER)
                            })
                    // graphicOverlay?.processBitmap?.saveToGallery(this)
                    saveImage( binding.graphicOverlay.processBitmap!!,this,getString(R.string.app_name))
                }
    }


    private fun saveImage(bitmap: Bitmap, context: Context, folderName: String) {
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            val values = contentValues()
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + folderName)
            values.put(MediaStore.Images.Media.IS_PENDING, true)
            // RELATIVE_PATH and IS_PENDING are introduced in API 29.

            val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                saveImageToStream(bitmap, context.contentResolver.openOutputStream(uri))
                values.put(MediaStore.Images.Media.IS_PENDING, false)
                context.contentResolver.update(uri, values, null, null)
            }
        } else {
            val directory = File(Environment.getExternalStorageDirectory().toString() + separator + folderName)
            // getExternalStorageDirectory is deprecated in API 29

            if (!directory.exists()) {
                directory.mkdirs()
            }
            val fileName = System.currentTimeMillis().toString() + ".png"
            val file = File(directory, fileName)
            saveImageToStream(bitmap, FileOutputStream(file))
            if (file.absolutePath != null) {
                val values = contentValues()
                values.put(MediaStore.Images.Media.DATA, file.absolutePath)
                // .DATA is deprecated in API 29
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            }
        }
    }

    private fun contentValues() : ContentValues {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        return values
    }

    private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    private fun getOutputDirectory(): File {
//        val mediaDir = externalMediaDirs.firstOrNull()?.let {
//            File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }
//        this.externalCacheDir
        // external storage.
        val file = File(getExternalFilesDir(
                Environment.DIRECTORY_PICTURES), getString(R.string.app_name))

        return if (!file.mkdirs())
            file else filesDir
    }

    companion object {
        private const val TAG = "CameraXLivePreview"
        private const val PERMISSION_REQUESTS = 1
        private const val OBJECT_DETECTION = "Object Detection"
        private const val STATE_SELECTED_MODEL = "selected_model"
        private const val STATE_LENS_FACING = "lens_facing"

        private fun isPermissionGranted(
                context: Context,
                permission: String?
        ): Boolean {
            if (ContextCompat.checkSelfPermission(context, permission!!)
                    == PackageManager.PERMISSION_GRANTED
            ) {
                Log.i(TAG, "Permission granted: $permission")
                return true
            }
            Log.i(TAG, "Permission NOT granted: $permission")
            return false
        }
    }
}

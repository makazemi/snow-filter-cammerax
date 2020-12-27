/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.mlkit.vision.camerasample.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.hardware.Camera
import android.media.Image
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.webkit.MimeTypeMap
import android.widget.ImageButton
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.ImageHeaderParser.UNKNOWN_ORIENTATION
import com.bumptech.glide.request.RequestOptions
import com.google.mlkit.vision.camerasample.R
import com.google.mlkit.vision.camerasample.camerax.CameraManager
import com.google.mlkit.vision.camerasample.databinding.FragmentCameraBinding
import com.google.mlkit.vision.camerasample.extension.*
import com.google.mlkit.vision.camerasample.ui.CameraViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class CameraFragment : Fragment() {

    private lateinit var outputDirectory: File


    private val viewModel: CameraViewModel by activityViewModels()

    private lateinit var cameraManager: CameraManager

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var tempBitmap: Bitmap

    private var targetRotation = 0

    @Inject
    lateinit var requestManager: RequestManager

    /**
     * We need a display listener for orientation changes that do not trigger a configuration
     * change, for example if we choose to override config change in manifest or for 180-degree
     * orientation changes.
     */
    private val orientationEventListener by lazy {
        object : OrientationEventListener(requireContext()) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == UNKNOWN_ORIENTATION) {
                    return
                }
                val rotation = when (orientation) {
                    in 45 until 135 -> 270
                    in 135 until 225 -> 0
                    in 225 until 315 -> 90
                    else -> 0
                }
                //      cameraManager.targetRotation=rotation
                targetRotation = rotation

                //   Timber.d("oritaina on oredinalie=$orientation")
                //     Timber.d("rotation on oredinalie=$rotation")

            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated")
        init()
    }
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//
//        init()
//    }

    @SuppressLint("MissingPermission")
    private fun init() {
        cameraManager = CameraManager(
            requireContext(),
            binding.viewFinder,
            this,
            binding.graphicOverlay
        )
        outputDirectory = getOutputDirectory()

        // Wait for the views to be properly laid out
        binding.viewFinder.post {


            // Build UI controls
            updateCameraUi()

            // Set up the camera and its use cases
            cameraManager.startCamera()
        }
        binding.root.findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            requireActivity().finish()
        }

        subscribeObserverKeyDownVolumeEvent()

        cameraManager.setCameraSwitchButtonListener {
            updateCameraSwitchButton(it)
        }
        tempBitmap = BitmapFactory.decodeResource(resources, R.drawable.img_blog)
    }

    private fun setGalleryThumbnail(bitmap: Bitmap) {
        // Reference of the view that holds the gallery thumbnail
        val thumbnail = binding.root.findViewById<ImageButton>(R.id.photo_view_button)

        // Run the operations in the view's thread
        thumbnail.post {

            // Remove thumbnail padding
            thumbnail.setPadding(resources.getDimension(R.dimen.stroke_small).toInt())

            // Load thumbnail into circular button using Glide
            requestManager
                .load(bitmap)
                .apply(RequestOptions.circleCropTransform())
                .into(thumbnail)
        }
    }

    private fun setGalleryThumbnail(uri: Uri) {
        // Reference of the view that holds the gallery thumbnail
        val thumbnail = binding.root.findViewById<ImageButton>(R.id.photo_view_button)

        // Run the operations in the view's thread
        thumbnail.post {

            // Remove thumbnail padding
            thumbnail.setPadding(resources.getDimension(R.dimen.stroke_small).toInt())

            // Load thumbnail into circular button using Glide
            Glide.with(thumbnail)
                .load(uri)
                .apply(RequestOptions.circleCropTransform())
                .into(thumbnail)
        }
    }


    /** Method used to re-draw the camera UI controls, called every time configuration changes. */
    private fun updateCameraUi() {

        // Remove previous UI if any
        binding.root.findViewById<ConstraintLayout>(R.id.camera_ui_container)?.let {
            binding.root.removeView(it)
        }

        // Inflate a new view containing all UI for controlling the camera
        val controls = View.inflate(requireContext(), R.layout.camera_ui_container, binding.root)

        // In the background, load latest photo taken (if any) for gallery thumbnail
        lifecycleScope.launch(Dispatchers.IO) {
            outputDirectory.listFiles { file ->
                EXTENSION_WHITELIST.contains(file.extension.toUpperCase(Locale.ROOT))
            }?.max()?.let {
                setGalleryThumbnail(Uri.fromFile(it))
            }
        }

        // Listener for button used to capture photo
        controls.findViewById<ImageButton>(R.id.camera_capture_button).setOnClickListener {

            takePhoto()
            //takePhotoFile()
        }

        // Setup for button used to switch cameras
        controls.findViewById<ImageButton>(R.id.camera_switch_button).let {
            // Disable the button until the camera is set up
            it.isEnabled = false
            // Listener for button used to switch cameras. Only called if the button is enabled
            it.setOnClickListener {
                cameraManager.changeCameraSelector()
            }
        }

        // Listener for button used to view the most recent photo
        controls.findViewById<ImageButton>(R.id.photo_view_button).setOnClickListener {
            // Only navigate when the gallery has photos
            if (true == outputDirectory.listFiles()?.isNotEmpty()) {
                findNavController().navigate(CameraFragmentDirections.actionCameraToGallery(
                    outputDirectory.absolutePath))
            }
        }
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        cameraManager.imageCapture.let {
            it.takePicture(
                ContextCompat.getMainExecutor(requireContext()),
                object : ImageCapture.OnImageCapturedCallback() {
                    @SuppressLint("UnsafeExperimentalUsageError", "RestrictedApi")
                    override fun onCaptureSuccess(image: ImageProxy) {
                        super.onCaptureSuccess(image)
                        Timber.d("onCaptureSuccess image=$it")
                        image.image?.let {
                            imageToBitmapSaveGallery(it)
                            Timber.d("onCaptureSuccess image.image=$it")

                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        super.onError(exception)
                        Timber.d("onError=$exception")
                    }
                })


            // We can only change the foreground Drawable using API level 23+ API
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//
//                // Display flash animation to indicate that photo was captured
//                binding.root.postDelayed({
//                    binding.root.foreground = ColorDrawable(Color.WHITE)
//                    binding.root.postDelayed(
//                        {  binding.root.foreground = null }, ANIMATION_FAST_MILLIS)
//                }, ANIMATION_SLOW_MILLIS)
//            }
        }


    }



    private fun takePhotoFile() {
        // Get a stable reference of the modifiable image capture use case
        cameraManager.imageCapture.let { imageCapture ->

            // Create output file to hold the image
            val photoFile = createFile(outputDirectory, FILENAME, PHOTO_EXTENSION)

            // Setup image capture metadata
            val metadata = ImageCapture.Metadata().apply {

                // Mirror image when using the front camera
                isReversedHorizontal = cameraManager.isFrontMode()
            }

            // Create output options object which contains file + metadata
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
                .setMetadata(metadata)
                .build()

            // Setup image capture listener which is triggered after photo has been taken
            imageCapture.takePicture(
                outputOptions,
                cameraManager.cameraExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                        // We can only change the foreground Drawable using API level 23+ API
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            // Update the gallery thumbnail with latest picture taken
                            setGalleryThumbnail(savedUri)
                        }

                        // Implicit broadcasts will be ignored for devices running API level >= 24
                        // so if you only target API level 24+ you can remove this statement
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                            requireActivity().sendBroadcast(
                                Intent(Camera.ACTION_NEW_PICTURE, savedUri)
                            )
                        }

                        // If the folder selected is an external media directory, this is
                        // unnecessary but otherwise other apps will not be able to access our
                        // images unless we scan them using [MediaScannerConnection]
                        val mimeType = MimeTypeMap.getSingleton()
                            .getMimeTypeFromExtension(savedUri.toFile().extension)
                        MediaScannerConnection.scanFile(
                            context,
                            arrayOf(savedUri.toFile().absolutePath),
                            arrayOf(mimeType)
                        ) { _, uri ->
                        }
                    }
                })

            // We can only change the foreground Drawable using API level 23+ API
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//
//                // Display flash animation to indicate that photo was captured
//                binding.root.postDelayed({
//                    binding.root.foreground = ColorDrawable(Color.WHITE)
//                    binding.root.postDelayed(
//                        {  binding.root.foreground = null }, ANIMATION_FAST_MILLIS)
//                }, ANIMATION_SLOW_MILLIS)
//            }
        }
    }

    private fun imageToBitmapSaveGallery(image: Image) {
        image.imageToBitmap()
            ?.rotateFlipImage(
                targetRotation.toFloat(),
                cameraManager.isFrontMode()
            )
            ?.scaleImage(
                binding.viewFinder,
                cameraManager.isHorizontalMode()
            )
            ?.let { bitmap ->
                val canvas = binding.graphicOverlay.processCanvas
                canvas.drawBitmap(
                    bitmap,
                    0f,
                    0f,
                    Paint().apply {
                        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OVER)
                    }
                )

                lifecycleScope.launch(Dispatchers.IO) {
                    saveImage(binding.graphicOverlay.processBitmap, getString(R.string.app_name))

                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    setGalleryThumbnail(binding.graphicOverlay.processBitmap)
                }
            }
    }

    private fun updateCameraSwitchButton(isEnable: Boolean) {
        binding.root.findViewById<ImageButton>(R.id.camera_switch_button).isEnabled = isEnable
    }

    /** Volume down button receiver used to trigger shutter */
    private fun subscribeObserverKeyDownVolumeEvent() {
        viewModel.keyDownVolumeEvent.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let {
                val shutter = binding.root
                    .findViewById<ImageButton>(R.id.camera_capture_button)
                shutter.simulateClick()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        orientationEventListener.enable()
    }

    override fun onStop() {
        super.onStop()
        orientationEventListener.disable()
    }

    override fun onResume() {
        super.onResume()
        // Make sure that all permissions are still present, since the
        // user could have removed them while the app was in paused state.
        if (!PermissionsFragment.hasPermissions(requireContext())) {
            Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(
                CameraFragmentDirections.actionCameraToPermissions()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
       _binding = null
    }

    companion object {
        const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        const val PHOTO_EXTENSION = ".jpg"

        /** Helper function used to create a timestamped file */
        private fun createFile(baseFolder: File, format: String, extension: String) =
            File(baseFolder, SimpleDateFormat(format, Locale.US)
                .format(System.currentTimeMillis()) + extension)
    }
}

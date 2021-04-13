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
import android.graphics.*
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import androidx.annotation.RequiresApi
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
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
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class CameraFragment : Fragment() {

    private lateinit var outputDirectory: File


    private val viewModel: CameraViewModel by activityViewModels()

    private lateinit var cameraManager: CameraManager

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!


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
                targetRotation = rotation

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
        init()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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
        binding.backButton.setOnClickListener {
            requireActivity().finish()
        }

        subscribeObserverKeyDownVolumeEvent()

        cameraManager.setCameraSwitchButtonListener {
            updateCameraSwitchButton(it)
        }

    }

    private fun setGalleryThumbnail(bitmap: Bitmap) {
        val thumbnail = binding.root.findViewById<ImageButton>(R.id.photo_view_button)

        // Run the operations in the view's thread
        thumbnail.post {
            // Remove thumbnail padding
            thumbnail.setPadding(resources.getDimension(R.dimen.stroke_small).toInt())
            requestManager
                .load(bitmap)
                .apply(RequestOptions.circleCropTransform())
                .into(thumbnail)
        }
    }

    private fun setGalleryThumbnail(uri: Uri) {
        val thumbnail = binding.root.findViewById<ImageButton>(R.id.photo_view_button)
        // Run the operations in the view's thread
        thumbnail.post {
            // Remove thumbnail padding
            thumbnail.setPadding(resources.getDimension(R.dimen.stroke_small).toInt())
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
            }?.maxOrNull()?.let {
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
        cameraManager.imageCapture.let {
            it.takePicture(
                ContextCompat.getMainExecutor(requireContext()),
                object : ImageCapture.OnImageCapturedCallback() {

                    @SuppressLint("UnsafeExperimentalUsageError", "RestrictedApi")
                    override fun onCaptureSuccess(image: ImageProxy) {
                        super.onCaptureSuccess(image)
                        image.image?.let {
                            imageToBitmapSaveGallery(it)
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        super.onError(exception)
                        Timber.d("onError=$exception")
                    }
                })
        }


    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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

                viewModel.setResultBitmap(binding.graphicOverlay.processBitmap)
                goToResultPhoto()

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

    private fun goToResultPhoto(){
        this.findNavController().navigate(R.id.action_camera_fragment_to_resultPhotoFragment)
    }
}

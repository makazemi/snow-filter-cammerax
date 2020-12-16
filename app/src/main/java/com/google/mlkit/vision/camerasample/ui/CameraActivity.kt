package com.google.mlkit.vision.camerasample.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.media.Image
import android.os.Build.VERSION_CODES
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.CompoundButton
import android.widget.ToggleButton
import androidx.annotation.RequiresApi
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.annotation.KeepName
import com.google.mlkit.vision.camerasample.*
import com.google.mlkit.vision.camerasample.R
import com.google.mlkit.vision.camerasample.camerax.CameraManager
import com.google.mlkit.vision.camerasample.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


@KeepName
@RequiresApi(VERSION_CODES.LOLLIPOP)
class CameraActivity :
        AppCompatActivity(),
        ActivityCompat.OnRequestPermissionsResultCallback,
        CompoundButton.OnCheckedChangeListener {


    private lateinit var binding: ActivityMainBinding

  //  private lateinit var cameraXManager: CameraXManager

    private lateinit var cameraManager: CameraManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")


        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


      //  cameraXManager = CameraXManager(this, binding.previewView, this, binding.graphicOverlay, allPermissionsGranted())
        cameraManager = CameraManager(this, binding.previewView, this, binding.graphicOverlay)


        val facingSwitch =
                findViewById<ToggleButton>(R.id.facing_switch)
        facingSwitch.setOnCheckedChangeListener(this)


       // cameraXManager.startCamera()

        if (allPermissionsGranted()) {
            cameraManager.startCamera()
        }

        if (!allPermissionsGranted()) {
            runtimePermissions
        }

        btn_take_picture.setOnClickListener {
            takePhoto()
        }
    }


    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        Log.d(TAG, "Set facing")
       // cameraXManager.onChangedCameraSelector()
        cameraManager.changeCameraSelector()
    }


    public override fun onResume() {
        super.onResume()
       // cameraXManager.bindAllCameraUseCases()
    }

    override fun onPause() {
        super.onPause()
//        cameraXManager.imageProcessor?.run {
//            this.stop()
//        }
    }

    public override fun onDestroy() {
        super.onDestroy()
//        cameraXManager.imageProcessor?.run {
//            this.stop()
//        }
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
          //  cameraXManager.bindAllCameraUseCases()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun takePhoto() {
        cameraManager.imageCapture?.let {
            it.takePicture(
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
        }
    }

    private fun imageToBitmapSaveGallery(image: Image) {


        image.imageToBitmap()
                ?.rotateFlipImage(
                        cameraManager.rotation,
                        cameraManager.isFrontMode()
                )
                ?.scaleImage(
                        binding.previewView,
                        cameraManager.isHorizontalMode()
                )
                ?.let { bitmap ->
                    binding.graphicOverlay.processCanvas.drawBitmap(
                            bitmap,
                            0f,
                            bitmap.getBaseYByView(
                                    binding.graphicOverlay,
                                    cameraManager.isHorizontalMode()
                            ),
                            Paint().apply {
                                xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OVER)
                            })
                    saveImage(binding.graphicOverlay.processBitmap, getString(R.string.app_name))
                }
//
//        image.imageToBitmap()
//                ?.scaleImage(
//                        binding.previewView,
//                        false
//                )
//                ?.let { bitmap ->
//                    binding.graphicOverlay.processCanvas?.drawBitmap(
//                            bitmap,
//                            0f,
//                            bitmap.getBaseYByView(
//                                    binding.graphicOverlay,
//                                    false
//                            ),
//                            Paint().apply {
//                                xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OVER)
//                            })
//                    saveImage(binding.graphicOverlay.processBitmap, getString(R.string.app_name))
//                }
    }

    companion object {
        private const val TAG = "CameraActivity"
        private const val PERMISSION_REQUESTS = 1

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

package com.google.mlkit.vision.camerasample.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.display.DisplayManager
import android.media.Image
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View.*
import android.widget.CompoundButton
import android.widget.ToggleButton
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.annotation.KeepName
import com.google.mlkit.vision.camerasample.*
import com.google.mlkit.vision.camerasample.R
import com.google.mlkit.vision.camerasample.camerax.CameraManager
import com.google.mlkit.vision.camerasample.databinding.ActivityMainBinding
import com.google.mlkit.vision.camerasample.extension.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


@KeepName
@RequiresApi(VERSION_CODES.LOLLIPOP)
class CameraActivity :
    AppCompatActivity(),
    ActivityCompat.OnRequestPermissionsResultCallback,
    CompoundButton.OnCheckedChangeListener {


    var bgHandler = Handler()
    lateinit var r: Runnable
    var screenW: Int = 0
    var screenH: Int = 0

    //var snowList: ArrayList<Snow> = ArrayList()

    private lateinit var binding: ActivityMainBinding


    private lateinit var cameraManager: CameraManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")


        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        val facingSwitch =
            findViewById<ToggleButton>(R.id.facing_switch)
        facingSwitch.setOnCheckedChangeListener(this)





        btn_take_picture.setOnClickListener {
            takePhoto()
        }

        //  window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_IMMERSIVE_STICKY or SYSTEM_UI_FLAG_FULLSCREEN or SYSTEM_UI_FLAG_HIDE_NAVIGATION
        //  val point = Point()
        //  windowManager.defaultDisplay.getRealSize(point)
        //  binding.rootView.getRealSize(point)
        //     binding.rootView.de.getRealSize(point)

//        screenW=point.x
//        screenH=point.y

//        val vto = binding.rootView.viewTreeObserver.also {
//            it.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
//                override fun onGlobalLayout() {
//                    binding.rootView.viewTreeObserver.removeGlobalOnLayoutListener(this)
//                    screenW = view.measuredWidth
//                    screenH = view.measuredHeight
//
//
//
//                    Log.w("Derp", "9")
//                }
//            })
//        }

        cameraManager = CameraManager(
            this@CameraActivity,
            binding.previewView,
            this@CameraActivity,
            binding.graphicOverlay
        )


        if (allPermissionsGranted()) {
            cameraManager.startCamera()
        }
        if (!allPermissionsGranted()) {
            runtimePermissions
        }


    }

    private fun setUpSnowEffect() {
        // generate 200 snow flake
        //   val container: ViewGroup = binding.rootView.decorView as ViewGroup
//        for (i in 0 until 200) {
//            snowList.add(Snow(baseContext, screenW.toFloat(), screenH.toFloat(), binding.rootView))
//        }
//
//        // setup runnable and postDelay
//        r = Runnable {
//            for (snow: Snow in snowList)
//                snow.update()
//            bgHandler.postDelayed(r, 10)
//        }
//        bgHandler.post(r)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        Log.d(TAG, "Set facing")
        cameraManager.changeCameraSelector()
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
        cameraManager.imageCapture.let {
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
        Log.d(TAG,"rotation=${cameraManager.rotation}")
        Log.d(TAG,"ishorizontalmode=${cameraManager.isHorizontalMode()}")
        image.imageToBitmap()?.let { bitmap ->

                binding.img.setImageBitmap(binding.snowView.processBitmap)

                binding.snowView.processCanvas.drawBitmap(
                    bitmap,
                    0f,
                  bitmap.getBaseYByView(
                        view = binding.snowView,
                        isHorizontalRotation = cameraManager.isHorizontalMode()
                    ),
                    Paint().apply {
                        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OVER)
                    }
                )
//                    binding.graphicOverlay.processCanvas.drawBitmap(
//                        bitmap,
//                        0f,
//                        bitmap.getBaseYByView(
//                            binding.graphicOverlay,
//                            cameraManager.isHorizontalMode()
//                        ),
//                        Paint().apply {
//                            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OVER)
//                        })
                saveImage(binding.snowView.processBitmap, getString(R.string.app_name))
            }
    }
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayChanged(displayId: Int) {
            if (binding.rootView.display.displayId == displayId) {
                val rotation = binding.rootView.display.rotation
               cameraManager.setTargetRotation(rotation)
                Log.d(TAG,"ondispalchange=$rotation")
            }
        }

        override fun onDisplayAdded(displayId: Int) {
        }

        override fun onDisplayRemoved(displayId: Int) {
        }
    }

    override fun onStart() {
        super.onStart()
        val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        displayManager.registerDisplayListener(displayListener, null)
    }
    override fun onStop() {
        super.onStop()
        val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        displayManager.unregisterDisplayListener(displayListener)
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

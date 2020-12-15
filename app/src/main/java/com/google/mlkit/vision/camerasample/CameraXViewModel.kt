package com.google.mlkit.vision.camerasample

import android.app.Application
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.ExecutionException

class CameraXViewModel(application: Application) : AndroidViewModel(application) {
    private val _cameraProvider = MutableLiveData<ProcessCameraProvider>()

    val cameraProvider: LiveData<ProcessCameraProvider> get() = _cameraProvider

    fun setProcessCameraProvider() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(getApplication())
        cameraProviderFuture.addListener(
                {
                    try {
                        val imageCapture = ImageCapture.Builder().build()
                        _cameraProvider.value = cameraProviderFuture.get()
                    } catch (e: ExecutionException) { }
                    catch (e: InterruptedException) { }
                },
                ContextCompat.getMainExecutor(getApplication()))
    }

}

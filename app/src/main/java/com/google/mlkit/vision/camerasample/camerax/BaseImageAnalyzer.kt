package com.google.mlkit.vision.camerasample.camerax

import android.annotation.SuppressLint
import android.graphics.*
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task

abstract class BaseImageAnalyzer<T> : ImageAnalysis.Analyzer {

    abstract val graphicOverlay: GraphicOverlay

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
    }

    abstract fun stop()

    protected abstract fun detectInImage(image: String): Task<T>?

    protected abstract fun onSuccess(
        results: T,
        graphicOverlay: GraphicOverlay,
        rect: Rect
    )

    protected abstract fun onFailure(e: Exception)

}
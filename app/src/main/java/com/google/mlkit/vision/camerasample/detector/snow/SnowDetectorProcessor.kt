package com.google.mlkit.vision.camerasample.detector.snow

import android.graphics.Rect
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.camerasample.camerax.GraphicOverlay
import com.google.mlkit.vision.camerasample.camerax.BaseImageAnalyzer
import timber.log.Timber
import java.io.IOException

class SnowDetectorProcessor (private val view: GraphicOverlay
) :  BaseImageAnalyzer<Any>() {


    override val graphicOverlay: GraphicOverlay
        get() = view

    init {
        Timber.d("init")
        graphicOverlay.clear()
        graphicOverlay.add(SnowGraphic(graphicOverlay))
        graphicOverlay.postInvalidate()
    }



    override fun stop() {

        try {
        } catch (e: IOException) {
            Timber.d("Exception thrown while trying to close Face Detector: $e")
        }
    }

    override fun onSuccess(results: Any, graphicOverlay: GraphicOverlay, rect: Rect) {
    }

    override fun onFailure(e: Exception) {
    }

    override fun detectInImage(image: String): Task<Any>? {
        return null
    }

}
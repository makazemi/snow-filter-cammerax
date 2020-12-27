package com.google.mlkit.vision.camerasample.snow

import android.content.Context
import android.graphics.Rect
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.camerasample.camerax.GraphicOverlay
import com.google.mlkit.vision.camerasample.old.BaseImageAnalyzer
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.*
import timber.log.Timber
import java.io.IOException

class SnowDetectorProcessor (private val view: GraphicOverlay
) :  BaseImageAnalyzer<List<Face>>() {

    private val realTimeOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .build()

    private val detector = FaceDetection.getClient(realTimeOpts)

    override val graphicOverlay: GraphicOverlay
        get() = view

    init {
        Timber.d("init")
        graphicOverlay.clear()
        graphicOverlay.add(SnowGraphic(graphicOverlay,view.resources))
        graphicOverlay.postInvalidate()
    }
    override fun detectInImage(image: InputImage): Task<List<Face>> {
        Timber.d("detect image")
        return detector.process(image)
    }


    override fun stop() {
        Timber.d("onStop")
        try {
            detector.close()
        } catch (e: IOException) {
            Timber.d("Exception thrown while trying to close Face Detector: $e")
        }
    }

    override fun onSuccess(results: List<Face>, graphicOverlay: GraphicOverlay, rect: Rect) {
        Timber.d("onSuccess")
       // graphicOverlay.add(SnowGraphic(graphicOverlay,view.resources))
    }

    override fun onFailure(e: Exception) {
        Timber.d("onFailure=$e")
    }
  
}
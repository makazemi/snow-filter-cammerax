package com.google.mlkit.vision.camerasample.detector.snow

import android.graphics.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import com.google.mlkit.vision.camerasample.R
import com.google.mlkit.vision.camerasample.camerax.GraphicOverlay


class SnowGraphic(private val overlay: GraphicOverlay) :
    GraphicOverlay.Graphic(overlay) {


    companion object {
        private const val NUM_SNOWFLAKES = 300
        private const val DELAY = 5
    }

    private var snowflakes = ArrayList<SnowFlake>()


    private val runnable = Runnable { overlay.invalidate() }


    private val snowStoryBitmapPortrait =
        BitmapFactory.decodeResource(overlay.resources, R.drawable.snow_story)
    private val snowStoryBitmapLandscape =
        BitmapFactory.decodeResource(overlay.resources, R.drawable.snow_story_land)
    private var snowBitmap = snowStoryBitmapPortrait

    private val snowFlakeBitmap =
        AppCompatResources.getDrawable(overlay.context, R.drawable.ic_freezing)?.toBitmap()



    private var rectBitmap: Rect


    var height = 0
    var width = 0


    init {
        width = overlay.width
        height = overlay.height

        rectBitmap = Rect(0, 0, width, height)

        createSnowFlake()
    }

    private fun createSnowFlake() {
        if (snowflakes.isEmpty()) {
            for (i in 0 until NUM_SNOWFLAKES) {
                snowflakes.add(
                    SnowFlake.create(
                        width,
                        height,
                        snowFlakeBitmap
                    )
                )
            }
        }

    }

    override fun resize(w: Int, h: Int, oldw: Int, oldh: Int) {
        width = overlay.width
        height = overlay.height
    }

    override fun draw(canvas: Canvas?) {
        canvas?.drawBitmap(snowBitmap, null, rectBitmap, null)

        for (snowFlake in snowflakes) {
            snowFlake.draw(canvas)
        }
        overlay.handler.postDelayed(runnable, DELAY.toLong())
    }

    override fun onOrientationChanged(rotation: Int) {
        snowBitmap = if (rotation == 90 || rotation == 270) {
            snowStoryBitmapLandscape
        } else {
            snowStoryBitmapPortrait
        }
    }

}
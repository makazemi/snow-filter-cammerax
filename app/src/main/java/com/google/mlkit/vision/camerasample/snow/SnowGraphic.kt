package com.google.mlkit.vision.camerasample.snow

import android.content.res.Resources
import android.graphics.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import com.google.mlkit.vision.camerasample.R
import com.google.mlkit.vision.camerasample.camerax.GraphicOverlay
import timber.log.Timber


class SnowGraphic(private val overlay: GraphicOverlay, resources: Resources) :
    GraphicOverlay.Graphic(overlay) {


    companion object {
        private const val NUM_SNOWFLAKES = 300
        private const val DELAY = 5
    }

    private var snowflakes = ArrayList<SnowFlake>()


    private val runnable = Runnable { overlay.invalidate() }


    private val snowStoryBitmapPortrait =
        BitmapFactory.decodeResource(resources, R.drawable.snow_story)
    private val snowStoryBitmapLandscape =
        BitmapFactory.decodeResource(resources, R.drawable.snow_story_land)
    private var snowBitmap = snowStoryBitmapPortrait

    private val snowFlakeBitmap =
        AppCompatResources.getDrawable(overlay.context, R.drawable.ic_freezing)?.toBitmap()


    private val offset = 100

    private var factorH: Int

    private var factorW: Int

    private var heightGrass: Int

    private var factorHGrass: Int

    private var bottomSnowman: Int

    private var topSnowman: Int


    private var rectBitmap: Rect

    private var paint: Paint

    var height = 0
    var width = 0


    init {

        width = overlay.width
        height = overlay.height

        factorW = width / 4
        factorH = height / 2
        bottomSnowman = height - offset
        topSnowman = height - factorH

        factorHGrass = height / 10
        heightGrass = height - factorHGrass
        rectBitmap = Rect(0, topSnowman, width, height)
        paint = Paint().apply { color = Color.YELLOW }

        Timber.d("overlaywidth=${width} height=${height}")
        Timber.d("topsnowman=$topSnowman, right=$factorH, bottom=$bottomSnowman")

        createSnowFlake()
    }

    private fun createSnowFlake() {
        if (snowflakes.isEmpty()) {
            for (i in 0 until NUM_SNOWFLAKES) {
                snowflakes.add(SnowFlake.create(width,
                    height,
                    snowFlakeBitmap))
            }
        }

    }

    override fun resize(w: Int, h: Int, oldw: Int, oldh: Int) {
        width = overlay.width
        height = overlay.height

        factorW = width / 4
        factorH = height / 2
        bottomSnowman = height - offset
        topSnowman = height - factorH

        factorHGrass = height / 10
        heightGrass = height - factorHGrass
        rectBitmap = Rect(0, topSnowman, width, height)

    }

    override fun draw(canvas: Canvas?) {
        canvas?.drawBitmap(snowBitmap, null, rectBitmap, null)

        for (snowFlake in snowflakes) {
            snowFlake.draw(canvas)
        }
        overlay.handler.postDelayed(runnable, DELAY.toLong())
    }

    override fun onOrientationChanged(rotation: Int) {
        if (rotation == 90 || rotation == 270) {
            rectBitmap = Rect(0, -120, width-width/2, height+120)
            snowBitmap = snowStoryBitmapLandscape
        } else {
            rectBitmap = Rect(0, topSnowman, width, height)
            snowBitmap = snowStoryBitmapPortrait
        }
    }

}
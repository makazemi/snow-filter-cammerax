package com.google.mlkit.vision.camerasample.snow

import android.content.res.Resources
import android.graphics.*
import android.os.Handler
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

    private val snowmanBitmap = BitmapFactory.decodeResource(resources, R.drawable.snowman)
    private val grassBitmap = BitmapFactory.decodeResource(resources, R.drawable.grass)

    private val storyBitmap =AppCompatResources.getDrawable(overlay.context, R.drawable.ic_story)?.toBitmap()
    private var grass: Bitmap = grassBitmap
    private val snowFlakeBitmap =
        AppCompatResources.getDrawable(overlay.context, R.drawable.ic_freezing)?.toBitmap()


    private val offset = 10

    private var factorH: Int

    private var factorW: Int

    private var heightGrass: Int

    private var factorHGrass: Int

    private var bottomSnowman: Int

    private var topSnowman: Int

    private var rectSnowman: Rect

    private var rectGrass: Rect

    private var paint: Paint

    var height = 0
    var width = 0


    init {

        width = overlay.width
        height = overlay.height

        factorW = width / 4
        factorH=height/4
        bottomSnowman = height - offset
        topSnowman = height - factorH
        rectSnowman = Rect(0, topSnowman, factorW, bottomSnowman)
        factorHGrass = height / 10
        heightGrass = height - factorHGrass
        rectGrass = Rect(0, topSnowman, width, height)
        paint = Paint().apply { color = Color.YELLOW }

        Timber.d("overlaywidth=${width} height=${height}")
        Timber.d("topsnowman=$topSnowman, right=$factorH, bottom=$bottomSnowman")
        resize()

    }

    private fun resize() {
        if (snowflakes.isEmpty()) {
            for (i in 0 until NUM_SNOWFLAKES) {
                snowflakes.add(SnowFlake.create(overlay?.width ?: 0,
                    overlay?.height ?: 0,
                    snowFlakeBitmap))
            }
        }

        //    rectGrass=Rect(0,overlay?.height?:0-(overlay?.height?:0/10),overlay?.width?:0,overlay?.height?:0)
        grass = grassBitmap

    }

    override fun draw(canvas: Canvas?) {
        Timber.d("draw")
      //  canvas?.drawBitmap(snowmanBitmap, null, rectSnowman, null)
        //  canvas?.drawRect( rectSnowman, paint)
        storyBitmap?.let {
            canvas?.drawBitmap(it, null, rectGrass, null)
        }

        //canvas?.drawRect( rectGrass, paint)

        for (snowFlake in snowflakes) {
            snowFlake.draw(canvas)
        }
        //processCanvas.rotate(currentRotation.toFloat())
        overlay.handler.postDelayed(runnable, DELAY.toLong())
    }
}
package com.google.mlkit.vision.camerasample.snow

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.OrientationEventListener
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.load.ImageHeaderParser
import com.google.mlkit.vision.camerasample.R
import timber.log.Timber

const val TAG="SnowGraphicOverlay"
class SnowGraphicOverlay(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private val NUM_SNOWFLAKES = 300
    private val DELAY = 5

    private var snowflakes = ArrayList<SnowFlake>()


    private val runnable = Runnable { invalidate() }

    private val snowmanBitmap = BitmapFactory.decodeResource(resources, R.drawable.snowman)
    private val snowFlakeBitmap = AppCompatResources.getDrawable(context!!, R.drawable.ic_freezing)?.toBitmap()



    private val offset = 10

    private var factorH: Int

    private var factorW: Int

    private var bottomSnowman: Int

    private var topSnowman: Int

    private var rectSnowman: Rect

    lateinit var processBitmap: Bitmap
    lateinit var processCanvas: Canvas
    private var currentRotation=0
    private val orientationEventListener by lazy {
        object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ImageHeaderParser.UNKNOWN_ORIENTATION) {
                    return
                }
                val rotation = when (orientation) {
                    in 45 until 135 -> 270
                    in 135 until 225 -> 180
                    in 225 until 315 -> 90
                    else -> 0
                }
            if(currentRotation!=rotation){
                Timber.d("OrientationEventListener=$rotation")
              //  processCanvas.rotate(rotation.toFloat())
                if(rotation==270 || rotation==90) {
                    resize(height, width)
                    Timber.d("width=$width, height=$height")
                }else{
                    resize(width,height)
                }
            }

                currentRotation=rotation

            }
        }
    }

    init {
        factorH = height / 4
        factorW = width / 4
        bottomSnowman = height - offset
        topSnowman = height - factorH
        rectSnowman = Rect(0, topSnowman, factorW, bottomSnowman)
        orientationEventListener.enable()


    }



    private fun resize(width: Int, height: Int) {
        if(snowflakes.isEmpty()){
            for (i in 0 until NUM_SNOWFLAKES) {
                snowflakes.add(SnowFlake.create(width, height,snowFlakeBitmap))
            }
        }
        factorH = height / 4
        factorW = width / 4
        bottomSnowman = height - offset
        topSnowman = height - factorH
        rectSnowman = Rect(0, topSnowman, factorW, bottomSnowman)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w != oldw || h != oldh) {
            Timber.d("w=$w,h=$h,oldw=$oldw,oldh=$oldh")
            resize(w, h)
        }
    }

    private fun initProcessCanvas () {
        processBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
        processCanvas = Canvas(processBitmap)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        initProcessCanvas()
        canvas?.drawBitmap(snowmanBitmap, null, rectSnowman, null)
        processCanvas.drawBitmap(snowmanBitmap, null, rectSnowman, null)
        for (snowFlakeme in snowflakes) {
            snowFlakeme.draw(canvas)
            snowFlakeme.draw(processCanvas)
        }
        handler.postDelayed(runnable, DELAY.toLong())
    }



}
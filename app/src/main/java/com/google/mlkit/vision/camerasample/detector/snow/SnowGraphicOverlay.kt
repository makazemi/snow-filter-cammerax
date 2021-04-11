package com.google.mlkit.vision.camerasample.detector.snow

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

    companion object{
        private const val NUM_SNOWFLAKES = 300
        private const val DELAY = 5
    }

    private var snowflakes = ArrayList<SnowFlake>()


    private val runnable = Runnable { invalidate() }

    private val snowmanBitmap = BitmapFactory.decodeResource(resources, R.drawable.snowman)
    private val grassBitmap=BitmapFactory.decodeResource(resources,R.drawable.grass)
    private val grassRotatedBitmap=BitmapFactory.decodeResource(resources,R.drawable.grass90)
    private var grass:Bitmap=grassBitmap
    private val snowFlakeBitmap = AppCompatResources.getDrawable(context!!, R.drawable.ic_freezing)?.toBitmap()



    private val offset = 10

    private var factorH: Int

    private var factorW: Int

    private var heightGrass:Int

    private var factorHGrass:Int

    private var bottomSnowman: Int

    private var topSnowman: Int

    private var rectSnowman: Rect

    private var rectGrass:Rect

    private var paint:Paint

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
                //Timber.d("OrientationEventListener=$rotation")
//            if(currentRotation!=rotation){
//                if(rotation==270 || rotation==90) {
//                    resize(height, width,rotation)
//                }else{
//                    resize(width,height,rotation)
//                }
//                currentRotation=rotation
//            }



            }
        }
    }

    init {
        factorH = height / 4
        factorW = width / 4
        bottomSnowman = height - offset
        topSnowman = height - factorH
        rectSnowman = Rect(0, topSnowman, factorW, bottomSnowman)
        factorHGrass=height/10
        heightGrass=height-factorHGrass
        rectGrass=Rect(0,heightGrass,width,height)
        paint=Paint().apply { color=Color.YELLOW }
        orientationEventListener.enable()
    }



    private fun resize(newWidth: Int, newHeight: Int,rotation:Int=0) {
        if(snowflakes.isEmpty()){
            for (i in 0 until NUM_SNOWFLAKES) {
                snowflakes.add(SnowFlake.create(newWidth, newHeight,snowFlakeBitmap))
            }
        }
//        factorH = newHeight / 4
//        factorW = newWidth / 4
//        bottomSnowman = newHeight - offset
//        topSnowman = newHeight - factorH
//        rectSnowman = Rect(0, topSnowman, factorW, bottomSnowman)
//        factorHGrass=newHeight/10
//        heightGrass=newHeight-factorHGrass
        Timber.d("currenroa=$currentRotation")
        if(rotation==90 || rotation==270){
            rectGrass=Rect(0,0,height/10,height)
            grass=grassRotatedBitmap
        }
        else{
            rectGrass=Rect(0,height-height/10,width,height)
            grass=grassBitmap
        }
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
        canvas?.drawBitmap(grass, null, rectGrass, null)
        processCanvas.drawBitmap(snowmanBitmap, null, rectSnowman, null)
        processCanvas.drawBitmap(grass,null,rectGrass,null)

        for (snowFlake in snowflakes) {
            snowFlake.draw(canvas)
            snowFlake.draw(processCanvas)
        }
        //processCanvas.rotate(currentRotation.toFloat())
        handler.postDelayed(runnable, DELAY.toLong())
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        orientationEventListener.disable()
        Timber.d("ondetach")
    }
}
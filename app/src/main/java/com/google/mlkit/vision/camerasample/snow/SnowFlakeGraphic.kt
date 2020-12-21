package com.google.mlkit.vision.camerasample.snow

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.core.view.ViewCompat.postInvalidateOnAnimation
import com.google.mlkit.vision.camerasample.camerax.GraphicOverlay
import com.google.mlkit.vision.camerasample.old.TAG
import java.util.ArrayList

//class SnowFlakeGraphic (
//    private val context: Context,
//  private val  overlay: GraphicOverlay
//) : GraphicOverlay.Graphic(overlay) {
//
//
//
//    private val DEFAULT_SNOWFLAKES_NUM = 200
//    private val DEFAULT_SNOWFLAKE_ALPHA_MIN = 150
//    private val DEFAULT_SNOWFLAKE_ALPHA_MAX = 250
//    private val DEFAULT_SNOWFLAKE_ANGLE_MAX = 10
//    private val DEFAULT_SNOWFLAKE_SIZE_MIN_IN_DP = 2
//    private val DEFAULT_SNOWFLAKE_SIZE_MAX_IN_DP = 8
//    private val DEFAULT_SNOWFLAKE_SPEED_MIN = 2
//    private val DEFAULT_SNOWFLAKE_SPEED_MAX = 8
//    private val DEFAULT_SNOWFLAKES_FADING_ENABLED = false
//    private val DEFAULT_SNOWFLAKES_ALREADY_FALLING = false
//
//    private val snowflakesNum: Int
//    private val snowflakeImage: Bitmap?
//    private val snowflakeAlphaMin: Int
//    private val snowflakeAlphaMax: Int
//    private val snowflakeAngleMax: Int
//    private val snowflakeSizeMinInPx: Int
//    private val snowflakeSizeMaxInPx: Int
//    private val snowflakeSpeedMin: Int
//    private val snowflakeSpeedMax: Int
//    private val snowflakesFadingEnabled: Boolean
//    private val snowflakesAlreadyFalling: Boolean
//
//    private lateinit var updateSnowflakesThread: UpdateSnowflakesThread
//    private var snowflakes: Array<Snowflake>? = null
//
//    init {
//        val a = context.obtainStyledAttributes(attrs, R.styleable.SnowfallView)
//        try {
//            snowflakesNum = a.getInt(R.styleable.SnowfallView_snowflakesNum, DEFAULT_SNOWFLAKES_NUM)
//            snowflakeImage = a.getDrawable(R.styleable.SnowfallView_snowflakeImage)?.toBitmap()
//            snowflakeAlphaMin = a.getInt(R.styleable.SnowfallView_snowflakeAlphaMin, DEFAULT_SNOWFLAKE_ALPHA_MIN)
//            snowflakeAlphaMax = a.getInt(R.styleable.SnowfallView_snowflakeAlphaMax, DEFAULT_SNOWFLAKE_ALPHA_MAX)
//            snowflakeAngleMax = a.getInt(R.styleable.SnowfallView_snowflakeAngleMax, DEFAULT_SNOWFLAKE_ANGLE_MAX)
//            snowflakeSizeMinInPx = a.getDimensionPixelSize(R.styleable.SnowfallView_snowflakeSizeMin, dpToPx(DEFAULT_SNOWFLAKE_SIZE_MIN_IN_DP))
//            snowflakeSizeMaxInPx = a.getDimensionPixelSize(R.styleable.SnowfallView_snowflakeSizeMax, dpToPx(DEFAULT_SNOWFLAKE_SIZE_MAX_IN_DP))
//            snowflakeSpeedMin = a.getInt(R.styleable.SnowfallView_snowflakeSpeedMin, DEFAULT_SNOWFLAKE_SPEED_MIN)
//            snowflakeSpeedMax = a.getInt(R.styleable.SnowfallView_snowflakeSpeedMax, DEFAULT_SNOWFLAKE_SPEED_MAX)
//            snowflakesFadingEnabled = a.getBoolean(R.styleable.SnowfallView_snowflakesFadingEnabled, DEFAULT_SNOWFLAKES_FADING_ENABLED)
//            snowflakesAlreadyFalling = a.getBoolean(R.styleable.SnowfallView_snowflakesAlreadyFalling, DEFAULT_SNOWFLAKES_ALREADY_FALLING)
//        } finally {
//            a.recycle()
//        }
//
//    }
//
//
//    override fun draw(canvas: Canvas?) {
//        if (isInEditMode) {
//            return
//        }
//        val fallingSnowflakes = snowflakes?.filter { it.isStillFalling() }
//        if (fallingSnowflakes?.isNotEmpty() == true) {
//            fallingSnowflakes.forEach { it.draw(canvas!!) }
//            updateSnowflakes()
//        }
//        else {
//            visibility = GONE
//        }
//    }
//
//    fun stopFalling() {
//        snowflakes?.forEach { it.shouldRecycleFalling = false }
//    }
//
//    fun restartFalling() {
//        snowflakes?.forEach { it.shouldRecycleFalling = true }
//    }
//
//    private fun createSnowflakes(): Array<Snowflake> {
//        val snowflakeParams = Snowflake.Params(
//            parentWidth = width,
//            parentHeight = height,
//            image = snowflakeImage,
//            alphaMin = snowflakeAlphaMin,
//            alphaMax = snowflakeAlphaMax,
//            angleMax = snowflakeAngleMax,
//            sizeMinInPx = snowflakeSizeMinInPx,
//            sizeMaxInPx = snowflakeSizeMaxInPx,
//            speedMin = snowflakeSpeedMin,
//            speedMax = snowflakeSpeedMax,
//            fadingEnabled = snowflakesFadingEnabled,
//            alreadyFalling = snowflakesAlreadyFalling)
//        return Array(snowflakesNum, { Snowflake(snowflakeParams) })
//    }
//
//    private fun updateSnowflakes() {
//        val fallingSnowflakes = snowflakes?.filter { it.isStillFalling() }
//        if (fallingSnowflakes?.isNotEmpty() == true) {
//            updateSnowflakesThread.handler.post {
//                fallingSnowflakes.forEach { it.update() }
//                postInvalidateOnAnimation()
//            }
//        }
//    }
//
//    private class UpdateSnowflakesThread : HandlerThread("SnowflakesComputations") {
//        val handler by lazy { Handler(looper) }
//
//        init {
//            start()
//        }
//    }
//    private  var snowflakes= ArrayList<SnowFlakeme>()
//
//    private val NUM_SNOWFLAKES = 150
//
//    private val snowPaint:Paint = Paint(Paint.ANTI_ALIAS_FLAG)
//
//    private val DELAY = 5
//
//
//    private val runnable = Runnable { overlay.invalidate() }
//
//    private val handler= Handler()
//
//    init {
//
//        snowPaint.color = Color.WHITE
//        snowPaint.style = Paint.Style.FILL
//    }
//    override fun resize(width: Int, height: Int) {
//        for (i in 0 until NUM_SNOWFLAKES) {
//            snowflakes.add(SnowFlakeme.create(translateX(width.toFloat()).toInt(),translateY(height.toFloat()).toInt(),snowPaint))
//        }
//    }
//
//    override fun draw(canvas: Canvas?) {
//        for (snowFlake in snowflakes) {
//            snowFlake.draw(canvas)
//        }
//
//        canvas?.drawCircle(0f,0f,34f,snowPaint)
//        Log.d(TAG,"drwa")
//
//        handler.postDelayed(runnable,DELAY.toLong())
//    }
//}
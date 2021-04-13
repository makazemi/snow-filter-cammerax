package com.google.mlkit.vision.camerasample.detector.snow

import android.graphics.*
import kotlin.math.cos
import kotlin.math.sin

class SnowFlake internal constructor(
    private val position: Point,
    private var angle: Float,
    private val increment: Float,
    private val flakeSize: Float,
    private val snowFlakeBitmap: Bitmap?
) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val rectBitmap = Rect(
        position.x, position.y, position.x
                + OFFSET, position.y + OFFSET
    )

    private val probabilityBitmap = Random.getRandom(25)



    init {
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
    }

    private fun move(width: Int, height: Int) {
        val x = position.x + increment * cos(angle.toDouble())
        val y = position.y + increment * sin(angle.toDouble())
        angle += Random.getRandom(-ANGLE_SEED, ANGLE_SEED) / ANGLE_DIVISOR
        position[x.toInt()] = y.toInt()
        if (!isInside(width, height)) {
            reset(width)
        }
    }

    private fun isInside(width: Int, height: Int): Boolean {
        val x = position.x
        val y = position.y
        return x >= -flakeSize - 1 && x + flakeSize <= width && y >= -flakeSize - 1 && y - flakeSize < height
    }

    private fun reset(width: Int) {
        position.x = Random.getRandom(width)
        position.y = (-flakeSize - 1).toInt()
        angle = Random.getRandom(ANGLE_SEED) / ANGLE_SEED * ANGE_RANGE + HALF_PI - HALF_ANGLE_RANGE
        rectBitmap.left = position.x
        rectBitmap.top = position.y
        rectBitmap.right = position.x + OFFSET
        rectBitmap.bottom = position.y + OFFSET
    }

    fun draw(canvas: Canvas?) {
        canvas?.let { ca ->
            val width = ca.width
            val height = ca.height
            move(width, height)

            if (probabilityBitmap == 0 && snowFlakeBitmap != null) {
                ca.drawBitmap(snowFlakeBitmap, position.x.toFloat(), position.y.toFloat(), null)
            } else {
                ca.drawCircle(position.x.toFloat(), position.y.toFloat(), flakeSize, paint)
            }
        }

    }


    companion object {
        private const val ANGE_RANGE = 0.1f
        private const val HALF_ANGLE_RANGE = ANGE_RANGE / 2f
        private const val HALF_PI = Math.PI.toFloat() / 2f
        private const val ANGLE_SEED = 25f
        private const val ANGLE_DIVISOR = 10000f
        private const val INCREMENT_LOWER = 2f
        private const val OFFSET = 10
        private const val INCREMENT_UPPER = 4f
        private const val FLAKE_SIZE_LOWER = 2f
        private const val FLAKE_SIZE_UPPER = 6f
        fun create(width: Int, height: Int, bitmap: Bitmap?): SnowFlake {
            val x = Random.getRandom(width)
            val y = Random.getRandom(height)
            val position = Point(x, y)
            val angle =
                Random.getRandom(ANGLE_SEED) / ANGLE_SEED * ANGE_RANGE + HALF_PI - HALF_ANGLE_RANGE
            val increment = Random.getRandom(INCREMENT_LOWER, INCREMENT_UPPER)
            val flakeSize = Random.getRandom(FLAKE_SIZE_LOWER, FLAKE_SIZE_UPPER)
            return SnowFlake(position, angle, increment, flakeSize, bitmap)
        }
    }
}

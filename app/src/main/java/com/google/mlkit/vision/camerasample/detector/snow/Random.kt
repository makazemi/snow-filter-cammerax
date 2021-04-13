package com.google.mlkit.vision.camerasample.detector.snow

import java.util.Random

object Random {
    fun getRandom(lower: Float, upper: Float): Float {
        val min = Math.min(lower, upper)
        val max = Math.max(lower, upper)
        return getRandom(max - min) + min
    }

    fun getRandom(upper: Float): Float {
        return RANDOM.nextFloat() * upper
    }

    fun getRandom(upper: Int): Int {
        return RANDOM.nextInt(upper)
    }


        private val RANDOM = Random()
}
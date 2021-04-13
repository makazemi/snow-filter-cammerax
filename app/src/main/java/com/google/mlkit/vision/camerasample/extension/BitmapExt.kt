package com.google.mlkit.vision.camerasample.extension



import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.Image
import android.view.View


fun Bitmap.rotateFlipImage(degree: Float, isFrontMode: Boolean): Bitmap? {
    val realRotation = when (degree) {
        0f -> 90f
        else ->degree
    }
    val matrix = Matrix().apply {
        if (isFrontMode) {
            preScale(-1.0f, 1.0f)
        }
        postRotate(realRotation)
    }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, false)
}

fun Bitmap.scaleImage(view: View, isHorizontalRotation: Boolean): Bitmap? {
    val ratio = view.width.toFloat() / view.height.toFloat()
    val newHeight = (view.width * ratio).toInt()

    return when (isHorizontalRotation) {
        true -> Bitmap.createScaledBitmap(this, view.width, newHeight, false)
        false -> Bitmap.createScaledBitmap(this, view.width, view.height, false)
    }
}
fun Image.imageToBitmap(): Bitmap? {
    val buffer = this.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
}

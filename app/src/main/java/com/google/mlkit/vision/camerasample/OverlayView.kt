package com.google.mlkit.vision.camerasample

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.google.android.gms.vision.face.Landmark
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceLandmark
import java.lang.Exception

const val TAG="OverlayView"
class OverlayView(overlay: GraphicOverlay?, private val face: Face,resources: Resources) : GraphicOverlay.Graphic(overlay)  {


    private val facePositionPaint: Paint = Paint()


    init {
        facePositionPaint.color = Color.WHITE
    }
    // The glasses bitmap
    private val glassesBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.glasses)

    // The cigarette bitmap
    private val cigaretteBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.cigarette)

    /**
     * Draw glasses on top of eyes
     */
    private fun drawGlasses(canvas: Canvas, face: Face) {

        val leftEye = face.getLandmark(Landmark.LEFT_EYE)
        val rightEye = face.getLandmark(Landmark.RIGHT_EYE)


        if (leftEye != null && rightEye != null) {
            val eyeDistance = rightEye.position.x - leftEye.position.x
            val delta = (scaleFactor * eyeDistance / 2).toInt()

            val glassesRect = RectF(
                    translateX(rightEye.position.x)-delta,
                    translateY(leftEye.position.y)-(delta/2) ,
                    translateX(leftEye.position.x)+delta,
                    translateY(leftEye.position.y)+delta)

            //canvas.drawRect(glassesRect, facePositionPaint)
            canvas.drawBitmap(glassesBitmap, null, glassesRect, null)
        }
    }

    /**
     * Draw cigarette at the left mouth
     */
    private fun drawCigarette(canvas: Canvas, face: Face) {
        val rightMouth = face.getLandmark(Landmark.RIGHT_MOUTH)
        val leftMouth = face.getLandmark(Landmark.LEFT_MOUTH)

        if (leftMouth != null && rightMouth != null) {
            val mouthLength = ((rightMouth.position.x - leftMouth.position.x) * scaleFactor)
            val cigaretteRect = RectF(
                    translateX(rightMouth.position.x) - mouthLength,
                    translateY(leftMouth.position.y),
                    translateX(rightMouth.position.x),
                    translateY(leftMouth.position.y) + mouthLength
            )

            Log.d(TAG,"mouthlengh=$mouthLength")
            Log.d(TAG,"cigaretteRect=$cigaretteRect")
            canvas.drawBitmap(cigaretteBitmap, null, cigaretteRect, null)



        }
    }

    override fun draw(canvas: Canvas?) {

        if (face != null && canvas != null ) {

            // Calculate the scale factor
           // widthScaleFactor = canvas.width.toFloat()
           // heightScaleFactor = canvas.height.toFloat()

            drawGlasses(canvas, face)
            drawCigarette(canvas, face)
        }
    }

}
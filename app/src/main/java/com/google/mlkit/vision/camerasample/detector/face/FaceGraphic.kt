package com.google.mlkit.vision.camerasample.detector.face

import android.graphics.*
import com.google.mlkit.vision.camerasample.camerax.GraphicOverlay
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour

class FaceGraphic constructor(overlay: GraphicOverlay, private val face: Face,private val imageRect: Rect) : GraphicOverlay.Graphic(overlay)  {


    private var paint:Paint = Paint()
    private var paint2:Paint = Paint()
    private var paint3:Paint = Paint()
    private var paint4:Paint = Paint()

    init {
        paint.color=Color.WHITE
        paint2.color=Color.YELLOW
        paint3.color=Color.BLACK
        paint4.color=Color.BLUE
    }

    override fun draw(canvas: Canvas?) {

        val rect = calculateRect(
            imageRect.height().toFloat(),
            imageRect.width().toFloat(),
            face.boundingBox
        )
       // canvas?.drawRect(rect, paint2)

        canvas?.drawEye()

    }

    private fun Canvas.drawEye(){
        var left=0f
        var right=0f
        var top=0f
        var bottom=0f
        val eyeLeftContour=face.getContour(FaceContour.LEFT_EYE)?.points

        eyeLeftContour?.let { list ->
            right=list.maxByOrNull { it.x }?.x?:0f
            left=list.minByOrNull { it.x }?.x?:0f
            bottom=list.maxByOrNull { it.y }?.y?:0f
            top=list.minByOrNull { it.y }?.y?:0f



//            val maxX=list.maxByOrNull { it.x }?: PointF()
//            val minX=list.minByOrNull { it.x }?: PointF()
//            val maxY=list.maxByOrNull { it.y }?: PointF()
//            val minY=list.minByOrNull { it.y }?: PointF()
//
//            drawCircle(translateX(maxX.x),translateY(maxX.y),8f,paint)
//            drawCircle(translateX(minX.x),translateY(minX.y),8f,paint2)
//            drawCircle(translateX(maxY.x),translateY(maxY.y),8f,paint3)
//            drawCircle(translateX(minY.x),translateY(minY.y),8f,paint4)





        }

        val rectLeftEye=RectF(translateX(left),translateY(top),translateX(right),translateY(bottom))

        drawRect(rectLeftEye,paint)
    }

    override fun onOrientationChanged(rotation: Int) {

    }

    override fun resize(w: Int, h: Int, oldw: Int, oldh: Int) {

    }
}
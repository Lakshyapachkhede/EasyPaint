package com.pachkhede.easypaint

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs
import kotlin.ranges.rangeTo


class TextMoveView @JvmOverloads constructor(
    context: Context, attributeSet: AttributeSet?,
    val paint: Paint,
    val text: String,
    var x1: Float,
    var y1: Float


) :
    View(context, attributeSet) {

    private var borderPaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }

    private var borderPath = Path()
    var w = paint.measureText(text)
    var h =  paint.fontMetrics.bottom - paint.fontMetrics.top


    var x2 = x1 + w
    var y2 = y1 + h



    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawText(text, 0, text.length, x1, y2, paint)
        drawBorder()
        drawBorderCircles(canvas)
        canvas.drawPath(borderPath, borderPaint)
    }
    private fun drawBorder() {
        borderPath.reset()
        borderPath.moveTo(x1, y1)
        borderPath.lineTo(x2, y1)
        borderPath.lineTo(x2, y2)
        borderPath.lineTo(x1, y2)
        borderPath.close()

    }

    private fun drawBorderCircles(canvas: Canvas) {
        val circlePaint = Paint().apply {
            color = borderPaint.color
            strokeWidth = 5f
            style = Paint.Style.FILL
        }

        val radius = 10f

        val points = listOf(
            Pair(x1, y1),
            Pair(x2, y2),
            Pair(x1, y2),
            Pair(x2, y1),
            Pair((x1 + x2) / 2, y1),
            Pair((x1 + x2) / 2, y2),
            Pair(x1, (y1 + y2) / 2),
            Pair(x2, (y1 + y2) / 2),

            )

        for (point in points) {
            canvas.drawCircle(point.first, point.second, radius, circlePaint)
        }

    }


    fun isInsideTouch(touchX: Float, touchY: Float) : Boolean{
        return (touchX in x1..x2 && touchY in y1..y2 )
    }





    fun setPosition(x: Float, y: Float) {
        x1 = x
        y1 = y
        x2 = x + w
        y2 = y + h
        invalidate()
    }


    fun update(){
        w = paint.measureText(text)
        h =  paint.fontMetrics.bottom - paint.fontMetrics.top
        x2 = x1 + w
        y2 = y1 + h
        invalidate()
    }

}
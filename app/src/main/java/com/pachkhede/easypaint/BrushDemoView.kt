package com.pachkhede.easypaint

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import com.pachkhede.easypaint.DrawingView.Tools
import kotlin.random.Random

class BrushDemoView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

   var tool : Tools = Tools.SOLID_BRUSH
       set(value) {
            field = value

            when(value){

                Tools.SOLID_BRUSH -> changeBrushToSolid()
                Tools.CALLIGRAPHY_BRUSH -> changeBrushToCalligraphy()
                Tools.DASHED_BRUSH -> changeBrushToDashed()
                Tools.NEON_BRUSH -> changeBrushToNeon()
                Tools.SPRAY_BRUSH_CAN -> changeBrushToSprayCan()
                Tools.BLUR_BRUSH -> changeBrushToBlur()
                else -> {
                    changeBrushToSolid()
                }
            }


        }

    private var currStrokeWidth = 8f
    private var currColor = Color.BLACK
    private var paint = Paint()
    private val path = Path()


    private fun changeBrushToSolid() {
        paint = Paint().apply {
            this.color = currColor
            strokeWidth = currStrokeWidth
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
            pathEffect = null
            maskFilter = null
        }
    }

    private fun changeBrushToCalligraphy() {
        paint = Paint().apply {
            strokeWidth = currStrokeWidth
            color = currColor
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.SQUARE
            strokeJoin = Paint.Join.BEVEL
            isAntiAlias = true
            pathEffect = CornerPathEffect(10f)
            maskFilter = null
        }
    }


    private fun changeBrushToDashed() {


        paint = Paint().apply {
            strokeWidth = currStrokeWidth
            color = currColor
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
            maskFilter = null
            pathEffect =
                DashPathEffect(floatArrayOf(5f * currStrokeWidth, 3f * currStrokeWidth), 0f)
        }
    }


    private fun changeBrushToSprayCan() {


        paint = Paint().apply {
            strokeWidth = currStrokeWidth
            color = currColor
            style = Paint.Style.FILL_AND_STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            maskFilter = BlurMaskFilter(15f, BlurMaskFilter.Blur.NORMAL)
            pathEffect = null

        }
    }

    private fun changeBrushToNeon() {
        paint = Paint().apply {
            strokeWidth = currStrokeWidth
            color = currColor
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            style = Paint.Style.STROKE
            maskFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.OUTER)
            pathEffect = null
        }
    }

    private fun changeBrushToBlur() {
        paint = Paint().apply {
            color = currColor
            strokeWidth = currStrokeWidth
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
            alpha = 80 // Make it semi-transparent for a smoother blur effect
            maskFilter = BlurMaskFilter(15f, BlurMaskFilter.Blur.NORMAL) // Apply blur effect
        }
    }



    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        path.reset()

        val x1 = 10f
        val y1 = height / 2f
        path.moveTo(x1, y1)

        val x2 = width.toFloat() - 10f
        val y2 = height / 2f


        if (tool == Tools.SPRAY_BRUSH){
            for (i in 10..width) {
                val offsetX = Random.nextInt(i, i+10)
                val offsetY = Random.nextInt(10, height-10)
                path.addCircle(offsetX.toFloat(), offsetY.toFloat(), 1f, Path.Direction.CW)
            }
        } else if (tool == Tools.SPRAY_BRUSH_CAN){
            path.lineTo(x2, y2)

        }

        else {
            val cx1 = width / 4f
            val cy1 = 0f

            val cx2 = width * 3 / 4f
            val cy2 = height.toFloat()




            path.cubicTo(cx1, cy1, cx2, cy2, x2, y2)
        }


        canvas.drawPath(path, paint)

    }

}
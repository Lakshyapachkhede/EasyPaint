package com.pachkhede.easypaint

import android.R.attr
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.util.AttributeSet
import android.view.View
import com.pachkhede.easypaint.DrawingView.Tools
import kotlinx.coroutines.flow.emptyFlow
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


class ShapeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    val shape: Tools,
    shapePaint: Paint,
    private var x1: Float,
    private var y1: Float,
    private var x2: Float,
    private var y2: Float,
) : View(context, attrs) {

    enum class ShapeRegion {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT,
        LEFT, TOP, RIGHT, BOTTOM,
        INSIDE, OUTSIDE
    }

    val touchMargin = 30f

    var paint: Paint = shapePaint
    var path = Path()

    private var borderPaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }

    private var borderPath = Path()
    var w = x2 - x1
    var h = y2 - y1

//    var rotationAngle = 0f

    private fun updatePath() {

        path.reset()
        when (shape) {
            Tools.LINE -> {
                path.moveTo(x1, y1)
                path.lineTo(x2, y2)
            }

            Tools.CIRCLE ->  drawCircle()

            Tools.RECTANGLE -> drawRectangle()

            Tools.RECTANGLE_ROUND -> drawRectangleRoundCorners()

            Tools.TRIANGLE -> drawTriangle()

            Tools.RIGHT_TRIANGLE -> drawRightTriangle()

            Tools.DIAMOND -> drawDiamond()

            Tools.PENTAGON -> drawPentagon()

            Tools.ARROW_MARK -> drawArrowMark()

            Tools.ARROW_DOUBLE -> drawArrowMarkDouble()

            Tools.ARROW -> drawArrowOutline()


            else -> {

            }

        }


    }


    // draws circle as ellipse
    private fun drawCircle() {
        path.reset()

        // Center of ellipse
        val h = (x1 + x2) / 2
        val k = (y1 + y2) / 2

        //Semi-major and Semi-minor Axes
        val a = abs(x2-x1)/2
        val b = abs(y2-y1)/2


        for (i in 0..360) {
            val theta = Math.toRadians(i.toDouble()) // Convert degrees to radians
            val x = h + a * cos(theta)
            val y = k + b * sin(theta)
            if (i == 0) path.moveTo(x.toFloat(), y.toFloat()) else path.lineTo(x.toFloat(), y.toFloat())

        }
        
        path.close()
    }

    private fun drawRectangle(){
        path.moveTo(x1, y1)
        path.lineTo(x2, y1)
        path.lineTo(x2, y2)
        path.lineTo(x1, y2)
        path.close()
    }

    private fun drawRectangleRoundCorners() {

        val r = 50f

        val rx = if (x2 < x1) -r else r
        val ry = if (y2 < y1) -r else r

        path.moveTo(x1 + rx, y1)

        path.lineTo(x2 - rx, y1)
        path.quadTo(x2, y1, x2, y1+ry)

        path.lineTo(x2, y2 - ry)
        path.quadTo(x2, y2, x2 - rx, y2)

        path.lineTo(x1 +rx, y2)
        path.quadTo(x1, y2, x1 , y2 - ry)

        path.lineTo(x1, y1 + ry)
        path.quadTo(x1, y1, x1 + rx, y1)

        path.close()
    }

    private fun drawDiamond(){
        val cx =( x1 + x2 ) /2
        val cy = (y1 + y2) / 2

        path.moveTo(cx, y1)
        path.lineTo(x2, cy)
        path.lineTo(cx, y2)
        path.lineTo(x1, cy)
        path.close()

    }

    private fun drawTriangle() {
        val cx =( x1 + x2 ) /2

        path.moveTo(cx, y1)
        path.lineTo(x2, y2)
        path.lineTo(x1, y2)
        path.close()

    }

    private fun drawRightTriangle() {

        path.moveTo(x1, y1)
        path.lineTo(x2, y2)
        path.lineTo(x1, y2)
        path.close()

    }

    private fun drawPentagon() {

        val cx = (x1 + x2 ) /2
        val cy = (y1 + y2) /2


        path.close()


    }

    private fun drawArrowMark() {
        path.reset()

        val arrowHeadLength = 30f  // Length of the arrowhead
        val angle = Math.toRadians(45.0)  // Arrowhead angle (30°)

        // Draw main arrow line
        path.moveTo(x1, y1)
        path.lineTo(x2, y2)

        // Calculate direction of the arrow
        val dx = x2 - x1
        val dy = y2 - y1
        val angleLine = atan2(dy.toDouble(), dx.toDouble())  // Angle of main line

        // Compute arrowhead points
        val xHead1 = x2 - arrowHeadLength * cos(angleLine - angle).toFloat()
        val yHead1 = y2 - arrowHeadLength * sin(angleLine - angle).toFloat()

        val xHead2 = x2 - arrowHeadLength * cos(angleLine + angle).toFloat()
        val yHead2 = y2 - arrowHeadLength * sin(angleLine + angle).toFloat()

        // Draw arrowhead
        path.moveTo(x2, y2)
        path.lineTo(xHead1, yHead1)
        path.moveTo(x2, y2)
        path.lineTo(xHead2, yHead2)
    }

    private fun drawArrowMarkDouble() {

        path.reset()

        val arrowHeadLength = 30f  // Length of the arrowhead
        val angle = Math.toRadians(45.0)  // Arrowhead angle (30°)

        // Draw main arrow line
        path.moveTo(x1, y1)
        path.lineTo(x2, y2)

        // Calculate direction of the arrow
        val dx = x2 - x1
        val dy = y2 - y1
        val angleLine = atan2(dy.toDouble(), dx.toDouble())  // Angle of main line

        // Compute arrowhead points
        val xHead1 = x2 - arrowHeadLength * cos(angleLine - angle).toFloat()
        val yHead1 = y2 - arrowHeadLength * sin(angleLine - angle).toFloat()

        val xHead2 = x2 - arrowHeadLength * cos(angleLine + angle).toFloat()
        val yHead2 = y2 - arrowHeadLength * sin(angleLine + angle).toFloat()

        // Draw arrowhead
        path.moveTo(x2, y2)
        path.lineTo(xHead1, yHead1)
        path.moveTo(x2, y2)
        path.lineTo(xHead2, yHead2)



        // Compute arrowhead points
        val xHead21 = x1 + arrowHeadLength * cos(angleLine - angle).toFloat()
        val yHead21 = y1 + arrowHeadLength * sin(angleLine - angle).toFloat()

        val xHead22 = x1 + arrowHeadLength * cos(angleLine + angle).toFloat()
        val yHead22 = y1 + arrowHeadLength * sin(angleLine + angle).toFloat()

        path.moveTo(x1, y1)
        path.lineTo(xHead21, yHead21)
        path.moveTo(x1, y1)
        path.lineTo(xHead22, yHead22)

    }

    private fun drawArrowOutline() {

        path.reset()
        val cx = (x1 + x2 ) /2
        val cy = (y1 + y2) /2

        path.moveTo(x1, (y1 + cy )/2)
        path.lineTo(x1, (y2 + cy )/2)

        path.lineTo(cx, (y2 + cy )/2)

        path.lineTo(cx, y2)
        path.lineTo(x2, cy)

        path.lineTo(cx, y1)
        path.lineTo(cx, (y1 + cy) / 2)
        path.lineTo(x1, (y1 + cy) / 2)

        path.close()

    }

//    private fun drawStarFour() {
//
//        val cx = x1 + w / 2
//        val cy = y1 + h / 2
//
//        val top = Pair(cx, y1)
//        val bottom = Pair(cx, y2)
//        val left = Pair(x1, cy)
//        val right = Pair(x2, cy)
//
//        val inTopLeft = Pair(x1 + w / 4, y1 + h/4)
//        val inTopRight = Pair(x1 + 3 * w / 4, y1 + h /4)
//        val inBottomLeft = Pair(x1 + w/4, y1 + 3 * h / 4)
//        val inBottomRight = Pair(x1 + 3 * w /4, y1 + 3 * h/4)
//
//        path.moveTo(top.first, top.second)
//
//        path.lineTo(inTopRight.first, inTopRight.second)
//        path.lineTo(right.first, right.second)
//        path.lineTo(inBottomRight.first, inBottomRight.second)
//        path.lineTo(bottom.first, bottom.second)
//
//        path.lineTo(inBottomLeft.first, inBottomLeft.second)
//
//        path.lineTo(left.first, left.second)
//
//        path.lineTo(inTopLeft.first, inTopLeft.second)
//        path.lineTo(top.first, top.second)
//
//        path.close()
//    }





    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        updatePath()
        drawBorder()
        drawBorderCircles(canvas)
        canvas.drawPath(borderPath, borderPaint)
        canvas.drawPath(path, paint)
        canvas.restore()
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

    fun setPosition(x: Float, y: Float) {
        x1 = x
        y1 = y
        x2 = x + w
        y2 = y + h
        updatePath()

    }

    fun setTop(y: Float) {
//        y1 = y
        if (y1 < y2) y1 = y else y2 = y
        h = y2 - y1
    }

    fun setBottom(y: Float) {
//        y2 = y
        if (y2 > y1) y2 = y else y1 = y
        h = y2 - y1


    }

    fun setLeft(x: Float) {
//        x1 = x
        if (x1 < x2) x1 = x else x2 = x
        w = x2 - x1
    }

    fun setRight(x: Float) {
//        x2 = x
        if (x2 > x1) x2 = x else x1 = x
        w = x2 - x1
    }

    fun getTouchShapeRegion(touchX: Float, touchY: Float): ShapeRegion {
        val left = minOf(x1, x2)
        val right = maxOf(x1, x2)
        val top = minOf(y1, y2)
        val bottom = maxOf(y1, y2)

        return when {

            checkCornerTouched(touchX, touchY, left, top) -> ShapeRegion.TOP_LEFT
            checkCornerTouched(touchX, touchY, right, top) -> ShapeRegion.TOP_RIGHT
            checkCornerTouched(touchX, touchY, left, bottom) -> ShapeRegion.BOTTOM_LEFT
            checkCornerTouched(touchX, touchY, right, bottom) -> ShapeRegion.BOTTOM_RIGHT

            checkHorizontalSideTouched(touchX, touchY, left, right, top) -> ShapeRegion.TOP
            checkHorizontalSideTouched(touchX, touchY, left, right, bottom) -> ShapeRegion.BOTTOM

            checkVerticalSideTouched(touchX, touchY, top, bottom, left) -> ShapeRegion.LEFT
            checkVerticalSideTouched(touchX, touchY, top, bottom, right) -> ShapeRegion.RIGHT


            touchX in left..right && touchY in top..bottom -> ShapeRegion.INSIDE

            else -> ShapeRegion.OUTSIDE
        }

    }

    private fun checkCornerTouched(tx: Float, ty: Float, px: Float, py: Float): Boolean {
        return abs(tx - px) <= touchMargin && abs(ty - py) <= touchMargin
    }

    private fun checkHorizontalSideTouched(
        tx: Float,
        ty: Float,
        left: Float,
        right: Float,
        y: Float
    ): Boolean {
        return tx in (left - touchMargin)..(right + touchMargin) && abs(ty - y) <= touchMargin
    }

    private fun checkVerticalSideTouched(
        tx: Float,
        ty: Float,
        top: Float,
        bottom: Float,
        x: Float
    ): Boolean {
        return ty in (top - touchMargin)..(bottom + touchMargin) && abs(tx - x) <= touchMargin
    }
}


//            Tools.STAR_FOUR -> {
//                path.reset()
//
//                // Calculate center of the star
//                val cx = (x1 + touchX) / 2
//                val cy = (y1 + touchY) / 2
//
//                // Compute outer radius (distance from center to touch point)
//                val R_outer = sqrt((touchX - cx).pow(2) + (touchY - cy).pow(2))
//
//                // Compute inner radius (adjustable, generally half of outer radius)
//                val R_inner = R_outer * 0.5f  // You can tweak this ratio
//
//                // Define angles for 4-point star (outer & inner points alternating)
//                val angles = arrayOf(0.0, 45.0, 90.0, 135.0, 180.0, 225.0, 270.0, 315.0)
//
//                for (i in angles.indices) {
//                    val angleRad = Math.toRadians(angles[i]) // Convert to radians
//                    val radius = if (i % 2 == 0) R_outer else R_inner // Alternate radius
//
//                    val x = cx + (radius * cos(angleRad)).toFloat()
//                    val y = cy + (radius * sin(angleRad)).toFloat()
//
//                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
//                }
//
//                path.close() // Close the star shape
//
//
//            }
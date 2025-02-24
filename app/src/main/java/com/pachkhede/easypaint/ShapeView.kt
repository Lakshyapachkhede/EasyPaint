package com.pachkhede.easypaint

import android.R.attr
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.pachkhede.easypaint.DrawingView.Tools
import kotlin.math.abs
import kotlin.math.cos
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

    var rotationAngle = 0f

    private fun updatePath() {
        path.reset()
        path.moveTo(x1, y1)
        when (shape) {
            Tools.LINE -> {

                path.lineTo(x2, y2)
            }
//            Tools.CIRCLE -> {
//                path.reset()
//                val radius =
//                    sqrt(((touchX - x1).pow(2) + (touchY - y1).pow(2)).toDouble()).toFloat()
//                path.addCircle(x1, y1, radius, Path.Direction.CW)
//            }
//
//            Tools.RECTANGLE -> {
//                path.reset()
//                path.moveTo(x1, y1)
//                path.lineTo(touchX, y1)
//                path.lineTo(touchX, touchY)
//                path.lineTo(x1, touchY)
//                path.close()
//
//            }
//
//            Tools.SQUARE -> {
//                val w = abs(touchX - x1)
//                val h = abs(touchY - y1)
//                val s = maxOf(w, h)
//
//                val newX1 = if (touchX < x1) x1 - s else x1
//                val newY1 = if (touchY < y1) y1 - s else y1
//
//                path.reset()
//                path.moveTo(newX1, newY1)
//                path.lineTo(newX1 + s, newY1)
//                path.lineTo(newX1 + s, newY1 + s)
//                path.lineTo(newX1, newY1 + s)
//                path.close()
//
//            }
//
//            Tools.RECTANGLE_ROUND -> {
//                val r = 50f
//                path.reset()
//
//
//                val rx = if (touchX < x1) -r else r
//                val ry = if (touchY < y1) -r else r
//
//                path.moveTo(x1 + rx, y1)
//
//
//                path.lineTo(touchX - rx, y1)
//                path.quadTo(touchX, y1, touchX, y1 + ry)
//
//
//                path.lineTo(touchX, touchY - ry)
//                path.quadTo(touchX, touchY, touchX - rx, touchY)
//
//
//                path.lineTo(x1 + rx, touchY)
//                path.quadTo(x1, touchY, x1, touchY - ry)
//
//
//                path.lineTo(x1, y1 + ry)
//                path.quadTo(x1, y1, x1 + rx, y1)
//
//                path.close()
//
//            }
//
//            Tools.TRIANGLE -> {
//
//                val x2 = (x1 + touchX) / 2 // Midpoint for the base
//                val y2 = y1 - abs(touchX - x1) * sqrt(3.0).toFloat() / 2 // Calculate height
//
//                path.reset()
//                path.moveTo(x1, y1)
//                path.lineTo(touchX, y1)
//                path.lineTo(x2, y2)
//                path.close()
//
//            }
//
//            Tools.RIGHT_TRIANGLE -> {
//
//                path.reset()
//                path.moveTo(x1, y1)
//                path.lineTo(touchX, touchY)
//                path.lineTo(x1, touchY)
//                path.close()
//
//            }
//
//            Tools.DIAMOND -> {
//
//                path.reset()
//
//                val cx = (x1 + touchX) / 2
//                val cy = (y1 + touchY) / 2
//
//                val left = Pair(x1, cy)
//                val top = Pair(cx, y1)
//                val right = Pair(touchX, cy)
//                val bottom = Pair(cx, touchY)
//
//
//                path.moveTo(left.first, left.second)
//                path.lineTo(top.first, top.second)
//                path.lineTo(right.first, right.second)
//                path.lineTo(bottom.first, bottom.second)
//                path.close()
//
//
//            }
//
//            Tools.PENTAGON -> {
//                val coordinates: List<Pair<Float, Float>> = calculatePentagonPoints(
//                    x1,
//                    y1,
//                    sqrt(((touchX - x1).pow(2) + (touchY - y1).pow(2)).toDouble()).toFloat()
//                )
//                path.reset()
//                path.moveTo(coordinates[0].first, coordinates[0].second)
//                for (coordinate in coordinates) {
//                    path.lineTo(coordinate.first, coordinate.second)
//                }
//                path.close()
//            }
//
//            Tools.HEXAGON -> {
//                val coordinates: List<Pair<Float, Float>> = calculateHexagonPoints(
//                    x1,
//                    y1,
//                    sqrt(((touchX - x1).pow(2) + (touchY - y1).pow(2)).toDouble()).toFloat()
//                )
//                path.reset()
//                path.moveTo(coordinates[0].first, coordinates[0].second)
//                for (coordinate in coordinates) {
//                    path.lineTo(coordinate.first, coordinate.second)
//                }
//                path.close()
//            }
//
//            Tools.ARROW_MARK -> {
//                path.reset()
//
//                // Draw main arrow line
//                path.moveTo(x1, y1)
//                path.lineTo(touchX, touchY)
//
//                // Arrow mark size
//                val markSize = 30f
//
//                // Calculate direction vector
//                val dx = touchX - x1
//                val dy = touchY - y1
//                val length = sqrt(dx * dx + dy * dy)
//                val unitX = dx / length
//                val unitY = dy / length
//
//                // Calculate arrow mark points (small lines at arrow tip)
//                val markX1 = touchX - markSize * unitX + markSize * unitY
//                val markY1 = touchY - markSize * unitY - markSize * unitX
//
//                val markX2 = touchX - markSize * unitX - markSize * unitY
//                val markY2 = touchY - markSize * unitY + markSize * unitX
//
//                // Draw two short diagonal lines forming "→" at the arrow tip
//                path.moveTo(touchX, touchY)
//                path.lineTo(markX1, markY1)
//
//                path.moveTo(touchX, touchY)
//                path.lineTo(markX2, markY2)
//            }
//
//            Tools.PENCIL -> {
//
//                path.reset()
//
//                // Define arrow dimensions
//                val shaftWidth = 30f  // Width of the arrow shaft
//                val arrowHeadSize = 60f  // Size of the arrowhead
//                val tailWidth = 50f  // Width of the tail
//                val tailHeight = 40f  // Height of the tail
//
//                // Calculate direction vector
//                val dx = touchX - x1
//                val dy = touchY - y1
//                val length = sqrt(dx * dx + dy * dy)
//                val unitX = dx / length
//                val unitY = dy / length
//
//                // Perpendicular vector for width calculations
//                val perpX = -unitY * shaftWidth / 2
//                val perpY = unitX * shaftWidth / 2
//
//                // Arrowhead points
//                val tipX = touchX
//                val tipY = touchY
//                val leftHeadX = touchX - arrowHeadSize * unitX + perpX
//                val leftHeadY = touchY - arrowHeadSize * unitY + perpY
//                val rightHeadX = touchX - arrowHeadSize * unitX - perpX
//                val rightHeadY = touchY - arrowHeadSize * unitY - perpY
//
//                // Shaft points
//                val leftShaftX = x1 + perpX
//                val leftShaftY = y1 + perpY
//                val rightShaftX = x1 - perpX
//                val rightShaftY = y1 - perpY
//
//                // Tail points
//                val tailLeftX = x1 + perpX - tailWidth * unitX
//                val tailLeftY = y1 + perpY - tailWidth * unitY
//                val tailRightX = x1 - perpX - tailWidth * unitX
//                val tailRightY = y1 - perpY - tailWidth * unitY
//
//                // Draw the outlined arrow shape
//                path.moveTo(tipX, tipY)  // Move to arrow tip
//                path.lineTo(leftHeadX, leftHeadY)  // Left side of arrowhead
//                path.lineTo(leftShaftX, leftShaftY)  // Left side of shaft
//                path.lineTo(tailLeftX, tailLeftY)  // Left side of tail
//                path.lineTo(tailRightX, tailRightY)  // Right side of tail
//                path.lineTo(rightShaftX, rightShaftY)  // Right side of shaft
//                path.lineTo(rightHeadX, rightHeadY)  // Right side of arrowhead
//                path.lineTo(tipX, tipY)  // Close the path
//
//                path.close()  // Close the arrow outline
//
//
//            }
//
//            Tools.ARROW -> {}
//
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

            else -> {

            }

        }


    }

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

//    Tools.LINE -> {
//        canvas?.drawLine(x1, y1, touchX, touchY, paint)
//    }
//
//    Tools.CIRCLE, Tools.RECTANGLE, Tools.SQUARE, Tools.RECTANGLE_ROUND, Tools.TRIANGLE, Tools.RIGHT_TRIANGLE, Tools.DIAMOND, Tools.PENTAGON, Tools.HEXAGON, Tools.ARROW_MARK, Tools.ARROW, Tools.PENCIL, Tools.STAR_FOUR -> {
//        canvas?.drawPath(path, paint)
//    }


    private fun calculatePentagonPoints(cx: Float, cy: Float, R: Float): List<Pair<Float, Float>> {
        val points = mutableListOf<Pair<Float, Float>>()
        val angleOffset = Math.PI / 2  // Rotates so the top vertex is centered

        for (i in 0 until 5) {
            val angle = (2 * Math.PI * i / 5) - angleOffset
            val x = cx + R * cos(angle).toFloat()
            val y = cy + R * sin(angle).toFloat()
            points.add(Pair(x, y))
        }
        return points
    }

    fun calculateHexagonPoints(cx: Float, cy: Float, R: Float): List<Pair<Float, Float>> {
        val points = mutableListOf<Pair<Float, Float>>()
        val angleOffset = Math.PI / 6  // Rotates so the flat sides are aligned properly

        for (i in 0 until 6) {
            val angle = (2 * Math.PI * i / 6) - angleOffset
            val x = cx + R * cos(angle).toFloat()
            val y = cy + R * sin(angle).toFloat()
            points.add(Pair(x, y))
        }
        return points
    }
}
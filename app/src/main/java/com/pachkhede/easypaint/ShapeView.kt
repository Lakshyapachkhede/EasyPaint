package com.pachkhede.easypaint

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
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


class ShapeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    val shape: Tools,
    shapePaint: Paint,
    var x1: Float,
    var y1: Float,
    private var x2: Float,
    private var y2: Float,
) : View(context, attrs) {

    enum class ShapeRegion {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT,
        LEFT, TOP, RIGHT, BOTTOM,
        INSIDE, OUTSIDE
    }

    private val touchMargin = 30f

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
    var w = abs(x2 - x1)
    var h = abs(y2 - y1)

//    var rotationAngle = 0f

    private fun updatePath() {

        path.reset()
        when (shape) {
            Tools.LINE -> {
                path.moveTo(x1, y1)
                path.lineTo(x2, y2)
            }

            Tools.CIRCLE -> drawCircle()

            Tools.RECTANGLE -> drawRectangle()

            Tools.RECTANGLE_ROUND -> drawRectangleRoundCorners()

            Tools.TRIANGLE -> drawTriangle()

            Tools.RIGHT_TRIANGLE -> drawRightTriangle()

            Tools.DIAMOND -> drawDiamond()

            Tools.ARROW_MARK -> drawArrowMark()

            Tools.ARROW_DOUBLE -> drawArrowMarkDouble()

            Tools.ARROW -> drawArrowOutline()

            Tools.ARROW2 -> drawArrowOutlineUp()

            Tools.PENTAGON -> drawPentagon()

            Tools.HEXAGON -> drawHexagon()

            Tools.STAR_FOUR -> drawStarFour()

            Tools.STAR_FIVE -> drawStarFive()

            Tools.STAR_SIX -> drawStarSix()

            Tools.HEART -> drawHeart()

            Tools.CHAT -> drawChat()

            Tools.LIGHTNING -> drawLightning()

            else -> {

            }

        }


    }

    private fun getEllipsePoints(degrees: Int): Pair<Double, Double> {
        // Center of ellipse
        val h = (x1 + x2) / 2
        val k = (y1 + y2) / 2

        //Semi-major and Semi-minor Axes
        val a = abs(x2 - x1) / 2
        val b = abs(y2 - y1) / 2

        val theta = Math.toRadians(degrees.toDouble()) // Convert degrees to radians
        val x = h + a * cos(theta)
        val y = k - b * sin(theta)

        return Pair(x, y)
    }

    // draws circle as ellipse
    private fun drawCircle() {
        path.reset()

        for (i in 0..360) {
            val point = getEllipsePoints(i)
            if (i == 0) path.moveTo(point.first.toFloat(), point.second.toFloat()) else path.lineTo(
                point.first.toFloat(),
                point.second.toFloat()
            )
        }

        path.close()
    }

    private fun drawPentagon() {
        val angles = listOf(90, 162, 234, 306, 378)

        for (angle in angles) {
            val point = getEllipsePoints(angle)

            if (angle == 90) path.moveTo(
                point.first.toFloat(),
                point.second.toFloat()
            ) else path.lineTo(point.first.toFloat(), point.second.toFloat())

        }

        path.close()

    }

    private fun drawHexagon() {
        val angles = listOf(90, 150, 210, 270, 330, 390)

        for (angle in angles) {
            val point = getEllipsePoints(angle)

            if (angle == 90) path.moveTo(
                point.first.toFloat(),
                point.second.toFloat()
            ) else path.lineTo(point.first.toFloat(), point.second.toFloat())

        }
        path.close()

    }


    private fun drawRectangle() {
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
        path.quadTo(x2, y1, x2, y1 + ry)

        path.lineTo(x2, y2 - ry)
        path.quadTo(x2, y2, x2 - rx, y2)

        path.lineTo(x1 + rx, y2)
        path.quadTo(x1, y2, x1, y2 - ry)

        path.lineTo(x1, y1 + ry)
        path.quadTo(x1, y1, x1 + rx, y1)

        path.close()
    }

    private fun drawDiamond() {
        val cx = (x1 + x2) / 2
        val cy = (y1 + y2) / 2

        path.moveTo(cx, y1)
        path.lineTo(x2, cy)
        path.lineTo(cx, y2)
        path.lineTo(x1, cy)
        path.close()

    }

    private fun drawTriangle() {
        val cx = (x1 + x2) / 2

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
        val cx = (x1 + x2) / 2
        val cy = (y1 + y2) / 2

        path.moveTo(x1, (y1 + cy) / 2)
        path.lineTo(x1, (y2 + cy) / 2)

        path.lineTo(cx, (y2 + cy) / 2)

        path.lineTo(cx, y2)
        path.lineTo(x2, cy)

        path.lineTo(cx, y1)
        path.lineTo(cx, (y1 + cy) / 2)
        path.lineTo(x1, (y1 + cy) / 2)

        path.close()

    }

    private fun drawArrowOutlineUp() {
        path.reset()
        val cx = (x1 + x2) / 2
        val cy = (y1 + y2) / 2

        path.moveTo((x1 + cx) / 2, y2)

        path.lineTo((x2 + cx) / 2, y2)

        path.lineTo((x2 + cx) / 2, cy)
        path.lineTo(x2, cy)
        path.lineTo(cx, y1)
        path.lineTo(x1, cy)
        path.lineTo((x1 + cx) / 2, cy)
        path.lineTo((x1 + cx) / 2, y2)


        path.close()
    }


    private fun getStarFourInnerPoints(degrees: Int): Pair<Double, Double> {

        // inner rectangle calculation
        val multiplicationFactor = 0.293

        val inx1 = ((w - (w * multiplicationFactor)) / 2) + min(x1, x2)
        val iny1 = ((h - (h * multiplicationFactor)) / 2) + min(y1, y2)

        val inx2 = inx1 + w * multiplicationFactor
        val iny2 = iny1 + h * multiplicationFactor


        // getting ellipse in inner rectangle
        val h = ((inx1 + inx2) / 2)
        val k = ((iny1 + iny2) / 2)

        //Semi-major and Semi-minor Axes
        val a = (abs(inx2 - inx1) / 2)
        val b = (abs(iny2 - iny1) / 2)

        val theta = Math.toRadians(degrees.toDouble()) // Convert degrees to radians
        val x = h + a * cos(theta)
        val y = k - b * sin(theta)

        return Pair(x, y)
    }

    private fun drawStarFour() {
        val angles = listOf(0, 45, 90, 135, 180, 225, 270, 315)

        for (i in angles.indices) {
            val isInside = i % 2 == 1

            val point: Pair<Double, Double>
            if (isInside) {
                point = getStarFourInnerPoints(angles[i])
            } else {
                point = getEllipsePoints(angles[i])
            }


            if (i == 0) path.moveTo(point.first.toFloat(), point.second.toFloat())
            else path.lineTo(point.first.toFloat(), point.second.toFloat())
        }

        path.close()

    }

    private fun drawStarFive() {
        val angles = listOf(90, 234, 378, 162, 306)

        for (angle in angles) {
            val point = getEllipsePoints(angle)


            if (angle == 90) path.moveTo(
                point.first.toFloat(),
                point.second.toFloat()
            ) else path.lineTo(point.first.toFloat(), point.second.toFloat())

        }

        path.close()

    }

    private fun drawStarSix() {
        val angles = listOf(90, 210, 330, 150, 270, 30)

        for (i in 0..2) {
            val point = getEllipsePoints(angles[i])
            if (i == 0) path.moveTo(point.first.toFloat(), point.second.toFloat()) else path.lineTo(
                point.first.toFloat(),
                point.second.toFloat()
            )
        }

        path.close()

        for (i in 3..5) {
            val point = getEllipsePoints(angles[i])
            if (i == 3) path.moveTo(point.first.toFloat(), point.second.toFloat()) else path.lineTo(
                point.first.toFloat(),
                point.second.toFloat()
            )
        }

        path.close()

    }

    private fun drawHeart() {


        path.reset()

        for (i in 0..360) {
            val t = Math.toRadians(i.toDouble())
            val x = ((sqrt(2.0) * sin(t).toDouble().pow(3)).toFloat() * w / 3f) + min(x1, x2) + (w / 2)
            val y = ((-cos(t).pow(3) + cos(t).pow(2) + 2 * cos(t)).toFloat() * h / 3f) + (min(y1, y2) - h * 0.3f) + (h / 2)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        path.close()

    }

    private fun drawChat() {

        path.moveTo(getEllipsePoints(0).first.toFloat(), getEllipsePoints(0).second.toFloat())

        for (i in 0..250) {
            val point = getEllipsePoints(i)
            path.lineTo(point.first.toFloat(), point.second.toFloat())
        }


        path.lineTo(getEllipsePoints(250).first.toFloat(), (max(y2, y1) + h * 0.3).toFloat())

        path.lineTo(getEllipsePoints(270).first.toFloat(), getEllipsePoints(270).second.toFloat())


        for (i in 270..360) {
            val point = getEllipsePoints(i)
            path.lineTo(point.first.toFloat(), point.second.toFloat())
        }

        path.close()
    }


    private fun drawLightning() {
        path.reset()

        path.moveTo((min(x1, x2) + w * 0.4).toFloat(), min(y1, y2))

        path.lineTo(min(x1, x2), (min(y1, y2) + h * 0.2).toFloat())

        path.lineTo((min(x1, x2) + w * 0.35).toFloat(), (min(y1, y2) + h * 0.4).toFloat())


        path.lineTo((min(x1, x2) + w * 0.25).toFloat(), (min(y1, y2) + h * 0.45).toFloat())

        path.lineTo((min(x1, x2) + w * 0.55).toFloat(), (min(y1, y2) + h * 0.63).toFloat())

        path.lineTo((min(x1, x2) + w * 0.47).toFloat(), (min(y1, y2) + h * 0.68).toFloat())

        path.lineTo(max(x1,x2), max(y1,y2))

        path.lineTo((min(x1, x2) + w * 0.68).toFloat(), (min(y1, y2) + h * 0.58).toFloat())

        path.lineTo((min(x1, x2) + w * 0.75).toFloat(), (min(y1, y2) + h * 0.55).toFloat())

        path.lineTo((min(x1, x2) + w * 0.51).toFloat(), (min(y1, y2) + h * 0.31).toFloat())

        path.lineTo((min(x1, x2) + w * 0.58).toFloat(), (min(y1, y2) + h * 0.27).toFloat())

        path.close()
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
        h = abs(y2 - y1)
    }

    fun setBottom(y: Float) {
//        y2 = y
        if (y2 > y1) y2 = y else y1 = y
        h = abs(y2 - y1)


    }

    fun setLeft(x: Float) {
//        x1 = x
        if (x1 < x2) x1 = x else x2 = x
        w = abs(x2 - x1)
    }

    fun setRight(x: Float) {
//        x2 = x
        if (x2 > x1) x2 = x else x1 = x
        w = abs(x2 - x1)
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


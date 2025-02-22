package com.pachkhede.easypaint

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.graphics.get
import com.google.android.material.internal.TouchObserverFrameLayout
import java.util.LinkedList
import java.util.Queue
import java.util.Stack
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    enum class Tools {
        SOLID_BRUSH, CALLIGRAPHY_BRUSH, SPRAY_BRUSH, BLUR_BRUSH, EMBOSS_BRUSH, DOTTED_BRUSH, NEON_BRUSH, PATTERN_BRUSH,
        ERASER, FILL, TEXT,
        LINE, CIRCLE, SQUARE, RECTANGLE, RECTANGLE_ROUND, TRIANGLE, RIGHT_TRIANGLE, DIAMOND, PENTAGON, HEXAGON, ARROW_MARK, ARROW, STAR_FOUR, STAR_FIVE, STAR_SIX, CHAT, HEART, LIGHTNING
    }


    private var path = Path()
    private var paint = Paint()
    private val bitmapStack = Stack<Bitmap>()
    private val redoBitmapStack = Stack<Bitmap>()
    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null
    var tool: Tools = Tools.SOLID_BRUSH
    private var currColor: Int = Color.BLACK
    private var currStrokeWidth: Float = 8f
//    private var prevColor: Int = Color.BLACK
    private var backColor: Int = Color.WHITE
    private var x1: Float = 0f
    private var y1: Float = 0f

    init {
        changeTool(Tools.SOLID_BRUSH)
    }

    private fun initBitmap() {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap!!)
        bitmapStack.push(bitmap!!.copy(Bitmap.Config.ARGB_8888, true))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        bitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }

        canvas.drawPath(path, paint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initBitmap()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x ?: return false
        val touchY = event.y ?: return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchDown(touchX, touchY)
            }

            MotionEvent.ACTION_MOVE -> {
                touchMove(touchX, touchY)
            }

            MotionEvent.ACTION_UP -> {
                touchUp(touchX, touchY)


            }

        }
        invalidate()
        return true
    }

    private fun pushBitmap() {
        bitmapStack.push(bitmap!!.copy(Bitmap.Config.ARGB_8888, true))
        refresh()
    }

    fun changeTool(newTool: Tools) {
        if (tool == Tools.ERASER && newTool != Tools.ERASER){
            tool = newTool
            changeColor(currColor)

        }
        tool = newTool

        when (newTool) {
            Tools.SOLID_BRUSH -> changeBrushToSolid()
            Tools.ERASER -> useEraser()

            else -> {}

        }


    }

    private fun changeBrushToSolid(color: Int? = null) {
        paint = Paint().apply {
            this.color = color ?: currColor
            strokeWidth = currStrokeWidth
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
        }
    }


    private fun useEraser() {
        paint = Paint().apply {
            color = backColor
            strokeWidth = currStrokeWidth
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
        }
    }

    fun changeColor(color: Int) {
        if (tool != Tools.ERASER) {
            currColor = color
            paint.color = color
        }
    }

    fun changeBrushSize(size: Float) {
        currStrokeWidth = size
        paint.strokeWidth = size
    }

    fun clearCanvas() {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap!!)
        canvas!!.drawColor(backColor)
        bitmapStack.clear()
        redoBitmapStack.clear()
        path.reset()
        pushBitmap()
    }

    fun undo() {
        if (bitmapStack.size > 1) {
            redoBitmapStack.push(bitmapStack.pop())
        }
        refresh()
    }


    fun redo() {
        if (redoBitmapStack.isNotEmpty()) {
            bitmapStack.push(redoBitmapStack.pop())
            refresh()

        }
    }

    private fun refresh() {
        bitmap = bitmapStack.peek().copy(Bitmap.Config.ARGB_8888, true)
        canvas = Canvas(bitmap!!)
        invalidate()
    }

    fun getBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
    }

    fun changeBackColor(color: Int) {
        backColor = color
        clearCanvas()
    }

    private fun floodFill(x: Int, y: Int, targetColor: Int, replacementColor: Int) {
        if (targetColor == replacementColor) return

        val width = bitmap!!.width
        val height = bitmap!!.height
        val queue: Queue<Point> = LinkedList()

        queue.add(Point(x, y))

        // Create a new path to represent the flood fill
        val fillPath = Path()

        while (queue.isNotEmpty()) {
            val node = queue.poll()
            var px = node.x
            var py = node.y

            // Move left until a different color is found
            while (px > 0 && bitmap?.getPixel(px - 1, py) == targetColor) {
                px--
            }

            var spanUp = false
            var spanDown = false

            // Move right, filling pixels and checking for spans
            while (px < width && bitmap?.getPixel(px, py) == targetColor) {
                bitmap!!.setPixel(px, py, replacementColor)

                // Add the filled pixel to the fillPath

                // Check for a span above
                if (!spanUp && py > 0 && bitmap!!.getPixel(px, py - 1) == targetColor) {
                    queue.add(Point(px, py - 1))
                    spanUp = true
                } else if (spanUp && py > 0 && bitmap!!.getPixel(px, py - 1) != targetColor) {
                    spanUp = false
                }

                // Check for a span below
                if (!spanDown && py < height - 1 && bitmap!!.getPixel(px, py + 1) == targetColor) {
                    queue.add(Point(px, py + 1))
                    spanDown = true
                } else if (spanDown && py < height - 1 && bitmap!!.getPixel(
                        px,
                        py + 1
                    ) != targetColor
                ) {
                    spanDown = false
                }

                px++
            }
        }

        // Redraw the canvas
        invalidate()
    }

    private fun touchDown(touchX: Float, touchY: Float) {
        when (tool) {
            Tools.SOLID_BRUSH, Tools.ERASER -> {
                path = Path().apply { moveTo(touchX, touchY) }
                paint = Paint(paint)
            }

            Tools.FILL -> {
                floodFill(
                    touchX.toInt(),
                    touchY.toInt(),
                    bitmap!!.getPixel(touchX.toInt(), touchY.toInt()),
                    paint.color
                )
                pushBitmap()

            }

            Tools.LINE, Tools.CIRCLE, Tools.RECTANGLE, Tools.SQUARE, Tools.RECTANGLE_ROUND, Tools.TRIANGLE, Tools.RIGHT_TRIANGLE, Tools.DIAMOND -> {
                x1 = touchX
                y1= touchY
                path.reset()
                path.moveTo(x1, y1)
            }


            else -> {}
        }
    }

    private fun touchMove(touchX: Float, touchY: Float) {
        when (tool) {
            Tools.SOLID_BRUSH, Tools.ERASER -> {
                path.lineTo(touchX, touchY)
            }

            Tools.LINE -> {
                path.reset()
                path.moveTo(x1, y1)
                path.lineTo(touchX, touchY)
            }

            Tools.CIRCLE -> {
                path.reset()
                val radius = sqrt(((touchX - x1).pow(2) + (touchY - y1).pow(2)).toDouble()).toFloat()
                path.addCircle(x1, y1, radius, Path.Direction.CW)
            }

            Tools.RECTANGLE -> {
                path.reset()
                path.moveTo(x1, y1)
                path.lineTo(touchX, y1)
                path.lineTo(touchX, touchY)
                path.lineTo(x1, touchY)
                path.close()

            }

            Tools.SQUARE -> {
                val w = abs(touchX - x1)
                val h = abs(touchY - y1)
                val s = maxOf(w, h)

                val newX1 = if (touchX < x1) x1 - s else x1
                val newY1 = if (touchY < y1) y1 - s else y1

                path.reset()
                path.moveTo(newX1, newY1)
                path.lineTo(newX1 + s, newY1)
                path.lineTo(newX1 + s, newY1 + s)
                path.lineTo(newX1, newY1 + s)
                path.close()

            }

            Tools.RECTANGLE_ROUND -> {
                val r = 50f
                path.reset()


                val rx = if (touchX < x1) -r else r
                val ry = if (touchY < y1) -r else r

                path.moveTo(x1 + rx, y1)


                path.lineTo(touchX - rx, y1)
                path.quadTo(touchX, y1, touchX, y1 + ry)


                path.lineTo(touchX, touchY - ry)
                path.quadTo(touchX, touchY, touchX - rx, touchY)


                path.lineTo(x1 + rx, touchY)
                path.quadTo(x1, touchY, x1, touchY - ry)


                path.lineTo(x1, y1 + ry)
                path.quadTo(x1, y1, x1 + rx, y1)

                path.close()

            }

            Tools.TRIANGLE -> {

                val x2 = (x1 + touchX) / 2 // Midpoint for the base
                val y2 = y1 - abs(touchX - x1) * sqrt(3.0).toFloat() / 2 // Calculate height

                path.reset()
                path.moveTo(x1, y1)
                path.lineTo(touchX, y1)
                path.lineTo(x2, y2)
                path.close()

            }
            Tools.RIGHT_TRIANGLE -> {

                path.reset()
                path.moveTo(x1, y1)
                path.lineTo(touchX, touchY)
                path.lineTo(x1, touchY)
                path.close()

            }

            Tools.DIAMOND -> {

                path.reset()

                val cx = (x1 + touchX) / 2
                val cy = (y1 + touchY) / 2

                val left = Pair(x1, cy)
                val top = Pair(cx, y1)
                val right = Pair(touchX, cy)
                val bottom = Pair(cx, touchY)


                path.moveTo(left.first, left.second)
                path.lineTo(top.first, top.second)
                path.lineTo(right.first, right.second)
                path.lineTo(bottom.first, bottom.second)
                path.close()


            }


            else -> {}
        }
    }

    private fun touchUp(touchX: Float, touchY: Float) {
        when (tool) {
            Tools.SOLID_BRUSH, Tools.ERASER -> {
                canvas?.drawPath(path, paint)

            }

            Tools.LINE -> {
                canvas?.drawLine(x1, y1, touchX, touchY, paint)
            }

            Tools.CIRCLE, Tools.RECTANGLE, Tools.SQUARE, Tools.RECTANGLE_ROUND, Tools.TRIANGLE, Tools.RIGHT_TRIANGLE,Tools.DIAMOND  -> {
                canvas?.drawPath(path, paint)
            }

            else -> {

            }

        }

        pushBitmap()
        path.reset()
    }

}
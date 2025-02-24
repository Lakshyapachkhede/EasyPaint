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
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.core.graphics.get
import com.google.android.material.internal.TouchObserverFrameLayout
import java.util.LinkedList
import java.util.Queue
import java.util.Stack
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    enum class Tools {
        SOLID_BRUSH, CALLIGRAPHY_BRUSH, SPRAY_BRUSH, BLUR_BRUSH, EMBOSS_BRUSH, DOTTED_BRUSH, NEON_BRUSH, PATTERN_BRUSH,
        ERASER, FILL, TEXT,
        LINE, CIRCLE, SQUARE, RECTANGLE, RECTANGLE_ROUND, TRIANGLE, RIGHT_TRIANGLE, DIAMOND, PENTAGON, HEXAGON, ARROW_MARK, ARROW, STAR_FOUR, STAR_FIVE, STAR_SIX, CHAT, HEART, LIGHTNING, PENCIL
    }

    private val shapes = listOf(
        Tools.LINE,
        Tools.CIRCLE,
        Tools.SQUARE,
        Tools.RECTANGLE,
        Tools.RECTANGLE_ROUND,
        Tools.TRIANGLE,
        Tools.RIGHT_TRIANGLE,
        Tools.DIAMOND,
        Tools.PENTAGON,
        Tools.HEXAGON,
        Tools.ARROW_MARK,
        Tools.ARROW,
        Tools.STAR_FOUR,
        Tools.STAR_FIVE,
        Tools.STAR_SIX,
        Tools.CHAT,
        Tools.HEART,
        Tools.LIGHTNING,
        Tools.PENCIL
    )

    // Drawing View Variables
    private var path = Path()
    private var paint = Paint()
    private val bitmapStack = Stack<Bitmap>()
    private val redoBitmapStack = Stack<Bitmap>()
    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null
    var tool: Tools = Tools.SOLID_BRUSH
    private var currColor: Int = Color.BLACK
    private var currStrokeWidth: Float = 8f
    private var backColor: Int = Color.WHITE
    private var x1: Float = 0f
    private var y1: Float = 0f

    // Shape View Variables
    private var shapeView: ShapeView? = null
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var shapeViewRegion = ShapeView.ShapeRegion.OUTSIDE
    private var isDrawingShape = false

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
        if (isDrawingShape) {
            shapeView?.draw(canvas)
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
                x1 = touchX
                y1 = touchY
                touchDown(touchX, touchY)

                shapeView?.let { shapeViewRegion = it.getTouchShapeRegion(touchX, touchY) }

                if (isDrawingShape && shapeViewRegion == ShapeView.ShapeRegion.OUTSIDE)
                {
                    addShapeToBitmap()
                }

            }

            MotionEvent.ACTION_MOVE -> {
                touchMove(touchX, touchY)

                if (event.pointerCount == 1 && isDrawingShape) {
                    handleShapeTouchMove(touchX, touchY)
                }
            }

            MotionEvent.ACTION_UP -> {
                touchUp(touchX, touchY)


            }

        }
        invalidate()
        return true
    }

    private fun handleShapeTouchMove(touchX: Float, touchY: Float) {

        when (shapeViewRegion) {

            ShapeView.ShapeRegion.TOP_LEFT -> {
                shapeView?.setTop(touchY)
                shapeView?.setLeft(touchX)
            }

            ShapeView.ShapeRegion.TOP_RIGHT -> {
                shapeView?.setTop(touchY)
                shapeView?.setRight(touchX)
            }

            ShapeView.ShapeRegion.BOTTOM_LEFT -> {
                shapeView?.setBottom(touchY)
                shapeView?.setLeft(touchX)
            }

            ShapeView.ShapeRegion.BOTTOM_RIGHT -> {
                shapeView?.setBottom(touchY)
                shapeView?.setRight(touchX)
            }

            ShapeView.ShapeRegion.TOP -> {
                shapeView?.setTop(touchY)
            }

            ShapeView.ShapeRegion.BOTTOM -> {
                shapeView?.setBottom(touchY)
            }

            ShapeView.ShapeRegion.LEFT -> {
                shapeView?.setLeft(touchX)
            }

            ShapeView.ShapeRegion.RIGHT -> {
                shapeView?.setRight(touchX)
            }

            ShapeView.ShapeRegion.INSIDE -> {
                var x3 = touchX - lastTouchX
                var y3 = touchY - lastTouchY
                shapeView?.setPosition(x3 - shapeView?.w!! / 2, y3 - shapeView?.h!! / 2)
            }

            ShapeView.ShapeRegion.OUTSIDE -> {
                shapeView = ShapeView(context, null, Tools.LINE, paint, x1, y1, touchX, touchY)
            }


        }
    }

    private fun addShapeToBitmap() {
        shapeView?.let {
            canvas?.drawPath(it.path, it.paint)
            shapeView = null
            pushBitmap()
        }
    }

    private fun pushBitmap() {
        bitmapStack.push(bitmap!!.copy(Bitmap.Config.ARGB_8888, true))
        refresh()
    }

    fun changeTool(newTool: Tools) {
        if (tool == Tools.ERASER && newTool != Tools.ERASER) {
            tool = newTool
            changeColor(currColor)
        }

        shapeView?.let {
            addShapeToBitmap()
        }

        tool = newTool
        if (tool in shapes) {
            isDrawingShape = true
        } else {
            isDrawingShape = false
        }

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
        invalidate()
    }

    fun changeBrushSize(size: Float) {
        currStrokeWidth = size
        paint.strokeWidth = size
        invalidate()
    }

    fun clearCanvas() {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap!!)
        canvas!!.drawColor(backColor)
        bitmapStack.clear()
        redoBitmapStack.clear()
        shapeView = null
        path.reset()
        pushBitmap()
    }

    fun undo() {
        if (bitmapStack.size > 1) {
            redoBitmapStack.push(bitmapStack.pop())
            refresh()
        }
        else {
            Toast.makeText(context, "No more Undo!", Toast.LENGTH_SHORT).show()
        }
    }


    fun redo() {
        if (redoBitmapStack.isNotEmpty()) {
            bitmapStack.push(redoBitmapStack.pop())
            refresh()
        }
        else {
            Toast.makeText(context, "No more Redo!", Toast.LENGTH_SHORT).show()
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


            else -> {}
        }
    }

    private fun touchMove(touchX: Float, touchY: Float) {
        when (tool) {
            Tools.SOLID_BRUSH, Tools.ERASER -> {
                path.lineTo(touchX, touchY)
            }

            else -> {}
        }
    }

    private fun touchUp(touchX: Float, touchY: Float) {
        when (tool) {
            Tools.SOLID_BRUSH, Tools.ERASER -> {
                canvas?.drawPath(path, paint)
                pushBitmap()

            }


            else -> {

            }

        }


        path.reset()
    }


}
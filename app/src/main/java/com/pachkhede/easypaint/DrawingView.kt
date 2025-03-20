package com.pachkhede.easypaint

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.DashPathEffect
import android.graphics.DiscretePathEffect
import android.graphics.EmbossMaskFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Shader
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast

import androidx.core.graphics.component1
import androidx.core.graphics.component2

import java.util.Deque
import java.util.LinkedList
import java.util.Stack
import androidx.core.graphics.get
import kotlin.random.Random


class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    enum class Tools {
        SOLID_BRUSH, CALLIGRAPHY_BRUSH, SPRAY_BRUSH, SPRAY_BRUSH_CAN, BLUR_BRUSH, DASHED_BRUSH, NEON_BRUSH, OIL_BRUSH, CRAYON_BRUSH, MARKER_BRUSH,
        ERASER, FILL, TEXT,
        LINE, CIRCLE, RECTANGLE, RECTANGLE_ROUND, TRIANGLE, RIGHT_TRIANGLE, DIAMOND, PENTAGON, HEXAGON, ARROW_MARK, ARROW_DOUBLE, ARROW, ARROW2, STAR_FOUR, STAR_FIVE, STAR_SIX, CHAT, HEART, LIGHTNING
    }

    val shapes = listOf(
        Tools.LINE,
        Tools.CIRCLE,
        Tools.RECTANGLE,
        Tools.RECTANGLE_ROUND,
        Tools.TRIANGLE,
        Tools.RIGHT_TRIANGLE,
        Tools.DIAMOND,
        Tools.PENTAGON,
        Tools.HEXAGON,
        Tools.ARROW_MARK,
        Tools.ARROW,
        Tools.ARROW2,
        Tools.ARROW_DOUBLE,
        Tools.STAR_FOUR,
        Tools.STAR_FIVE,
        Tools.STAR_SIX,
        Tools.CHAT,
        Tools.HEART,
        Tools.LIGHTNING,
    )

    val brushes = listOf(
        Tools.SOLID_BRUSH,
        Tools.CALLIGRAPHY_BRUSH,
        Tools.SPRAY_BRUSH,
        Tools.SPRAY_BRUSH_CAN,
        Tools.BLUR_BRUSH,
        Tools.DASHED_BRUSH,
        Tools.NEON_BRUSH,
        Tools.OIL_BRUSH,
        Tools.CRAYON_BRUSH,
        Tools.MARKER_BRUSH,
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
    var currStrokeWidth: Float = 8f
    private var backColor: Int = Color.WHITE
    private var x1: Float = 0f
    private var y1: Float = 0f

    // Shape View Variables
    private var shapeView: ShapeView? = null
    private var shapeViewRegion = ShapeView.ShapeRegion.OUTSIDE
    private var isDrawingShape = false

    val textpaint = Paint().apply {
        color = currColor
        textSize = 40f
        typeface = Typeface.DEFAULT
        isAntiAlias = true
    }

    var text = ""


    init {
        changeTool(Tools.SOLID_BRUSH)
    }

    private fun initBitmap() {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap!!)
        canvas!!.drawColor(backColor)
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


                if (shapeView == null) {
                    shapeViewRegion = ShapeView.ShapeRegion.OUTSIDE
                } else {
                    shapeView?.let { shapeViewRegion = it.getTouchShapeRegion(touchX, touchY) }
                }
                if (isDrawingShape && shapeViewRegion == ShapeView.ShapeRegion.OUTSIDE && shapeView != null) {
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

                shapeView?.setPosition(touchX - shapeView?.w!! / 2, touchY - shapeView?.h!! / 2)
            }

            ShapeView.ShapeRegion.OUTSIDE -> {
                shapeView = ShapeView(context, null, tool, paint, x1, y1, touchX, touchY)
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
            changeBrushToSolid()
        } else {
            isDrawingShape = false

        }

        when (newTool) {
            Tools.SOLID_BRUSH -> changeBrushToSolid()
            Tools.ERASER -> useEraser()

            Tools.CALLIGRAPHY_BRUSH -> changeBrushToCalligraphy()
            Tools.DASHED_BRUSH -> changeBrushToDashed()
            Tools.NEON_BRUSH -> changeBrushToNeon()
            Tools.SPRAY_BRUSH_CAN -> changeBrushToSprayCan()
            Tools.SPRAY_BRUSH -> {
                changeBrushToSolid()
                currStrokeWidth = 2f
                paint.strokeWidth = 2f
                // to reset effects from other brushes drawing is done in onTouchMove
            }

            Tools.BLUR_BRUSH -> {
                changeBrushToBlur()
            }

            Tools.OIL_BRUSH -> {
                changeBrushToOil()
            }
            Tools.CRAYON_BRUSH -> {
                changeBrushToCrayon()
            }

            Tools.MARKER_BRUSH -> {
                changeBrushToMarker()
            }

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
            maskFilter = BlurMaskFilter(strokeWidth, BlurMaskFilter.Blur.NORMAL)
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
            maskFilter = BlurMaskFilter(strokeWidth, BlurMaskFilter.Blur.OUTER)
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
            alpha = 180
            maskFilter =
                BlurMaskFilter(currStrokeWidth, BlurMaskFilter.Blur.SOLID) // Apply blur effect
        }
    }

    private fun changeBrushToCrayon() {
        paint = Paint().apply {

            color = currColor
            strokeWidth = currStrokeWidth
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            val oilTexture = BitmapFactory.decodeResource(resources, R.drawable.crayon_text_2)
            lateinit var mShader: Shader

            oilTexture?.let {
                val textureWidth = it.width.toFloat()
                val textureHeight = it.height.toFloat()

                val scale = strokeWidth / textureHeight

                val shaderMatrix = Matrix()

                shaderMatrix.setScale(scale, scale)
                shaderMatrix.postTranslate(-5f, -5f)

                val shader = BitmapShader(it, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
                shader.setLocalMatrix(shaderMatrix)

                mShader = shader
            }
            shader = mShader
            colorFilter = PorterDuffColorFilter(currColor, PorterDuff.Mode.SRC_IN)
            isAntiAlias = true
        }
    }

    private fun changeBrushToOil() {
        paint = Paint().apply {

            color = currColor
            strokeWidth = currStrokeWidth
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            val oilTexture = BitmapFactory.decodeResource(resources, R.drawable.oil_texture)
            lateinit var mShader: Shader

            oilTexture?.let {
                val textureWidth = it.width.toFloat()
                val textureHeight = it.height.toFloat()

                val scale = strokeWidth / textureHeight

                val shaderMatrix = Matrix()

                shaderMatrix.setScale(scale, scale)
                shaderMatrix.postTranslate(-5f, -5f)

                val shader = BitmapShader(it, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
                shader.setLocalMatrix(shaderMatrix)

                mShader = shader
            }
            shader = mShader
            alpha = 200
            colorFilter = PorterDuffColorFilter(currColor, PorterDuff.Mode.SRC_IN)
            isAntiAlias = true
        }
    }

    private fun changeBrushToMarker() {
        paint = Paint().apply {
            color = currColor
            strokeWidth = currStrokeWidth
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.SQUARE
            strokeJoin = Paint.Join.BEVEL
            isAntiAlias = true
            alpha = 150
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
            textpaint.color = color
        }
        if (tool in brushes) {
            changeTool(tool)
        }
        invalidate()
    }

    fun changeBrushSize(size: Float) {
        currStrokeWidth = size
        paint.strokeWidth = size
        if (tool in brushes) {
            changeTool(tool)
        }
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
        } else {
            Toast.makeText(context, "No more Undo!", Toast.LENGTH_SHORT).show()
        }
    }


    fun redo() {
        if (redoBitmapStack.isNotEmpty()) {
            bitmapStack.push(redoBitmapStack.pop())
            refresh()
        } else {
            Toast.makeText(context, "No more Redo!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refresh() {
        bitmap = bitmapStack.peek().copy(Bitmap.Config.ARGB_8888, true)
        canvas = Canvas(bitmap!!)
        invalidate()
    }

    fun getBitmap(): Bitmap {
        shapeView?.let {
            addShapeToBitmap()
        }
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
        if (targetColor == replacementColor) return  // No need to fill if same color

        val width = bitmap!!.width
        val height = bitmap!!.height
        val pixels = IntArray(width * height)  // Store pixels in an array
        bitmap!!.getPixels(pixels, 0, width, 0, 0, width, height)  // Bulk read pixels

        val queue: Deque<Point> = LinkedList()
        // Kotlin infers the type
        queue.add(Point(x, y))

        while (queue.isNotEmpty()) {
            val (px, py) = queue.pop()

            // Boundary check
            if (px !in 0 until width || py !in 0 until height) continue

            val index = py * width + px
            if (pixels[index] != targetColor) continue  // Skip non-target colors

            pixels[index] = replacementColor  // Apply new color

            queue.add(Point(px, py + 1))  // Down
            queue.add(Point(px, py - 1))  // Up
            queue.add(Point(px + 1, py))  // Right
            queue.add(Point(px - 1, py))  // Left
        }

        // Bulk update pixels in the bitmap
        bitmap!!.setPixels(pixels, 0, width, 0, 0, width, height)

        invalidate()  // Redraw the canvas
    }


    private fun touchDown(touchX: Float, touchY: Float) {
        when (tool) {
            Tools.SOLID_BRUSH, Tools.OIL_BRUSH, Tools.MARKER_BRUSH, Tools.ERASER, Tools.CALLIGRAPHY_BRUSH, Tools.DASHED_BRUSH, Tools.NEON_BRUSH, Tools.SPRAY_BRUSH_CAN, Tools.BLUR_BRUSH, Tools.CRAYON_BRUSH -> {
                path = Path().apply { moveTo(touchX, touchY) }
                paint = Paint(paint)
            }

            Tools.FILL -> {
                if (touchX < bitmap!!.width && touchY < bitmap!!.height &&
                    bitmap!![touchX.toInt(), touchY.toInt()] != paint.color
                ) {

                    floodFill(
                        touchX.toInt(),
                        touchY.toInt(),
                        bitmap!![touchX.toInt(), touchY.toInt()],
                        paint.color
                    )
                    pushBitmap()

                }
            }
            Tools.TEXT -> {
                canvas?.drawText(text, touchX, touchY, textpaint)
                pushBitmap()
            }


            else -> {}
        }
    }

    private fun touchMove(touchX: Float, touchY: Float) {
        when (tool) {
            Tools.SOLID_BRUSH, Tools.ERASER, Tools.MARKER_BRUSH, Tools.OIL_BRUSH, Tools.CALLIGRAPHY_BRUSH, Tools.DASHED_BRUSH, Tools.NEON_BRUSH, Tools.SPRAY_BRUSH_CAN, Tools.BLUR_BRUSH, Tools.CRAYON_BRUSH -> {
                path.lineTo(touchX, touchY)
            }

            Tools.SPRAY_BRUSH -> {
                val random = java.util.Random()
                for (i in 0..10) {
                    val offsetX = random.nextInt(20) - 10
                    val offsetY = random.nextInt(20) - 10
                    path.addCircle(touchX + offsetX, touchY + offsetY, 0.5f, Path.Direction.CW)
                }

            }

            else -> {}
        }
    }

    private fun touchUp(touchX: Float, touchY: Float) {
        when (tool) {
            Tools.SOLID_BRUSH, Tools.ERASER, Tools.OIL_BRUSH, Tools.CALLIGRAPHY_BRUSH,
            Tools.DASHED_BRUSH, Tools.NEON_BRUSH, Tools.SPRAY_BRUSH, Tools.SPRAY_BRUSH_CAN,
            Tools.BLUR_BRUSH, Tools.CRAYON_BRUSH, Tools.MARKER_BRUSH -> {
                canvas?.drawPath(path, paint)
                pushBitmap()

            }


            else -> {

            }

        }


        path.reset()
    }

    fun setBackgroundImage(image: Bitmap) {
        val imageRatio = image.width.toFloat() / image.height.toFloat()

        val imageBitmap = Bitmap.createScaledBitmap(
            (image.copy(Bitmap.Config.ARGB_8888, true)),
            width,
            (width / imageRatio).toInt(),
            false
        )


        bitmap = imageBitmap
        canvas = Canvas(bitmap!!)
        bitmapStack.clear()
        redoBitmapStack.clear()
        shapeView = null
        path.reset()
        pushBitmap()
    }







}
package com.pachkhede.easypaint

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paths = mutableListOf<Pair<Path, Paint>>()
    private var currentPath = Path()
    private var currentPaint = Paint()

    private var canvasBitmap: Bitmap? = null
    private var drawCanvas: Canvas? = null


    init {
        setupPaint()
    }

    private fun setupPaint() {
        currentPaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 10f
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        drawCanvas = Canvas(canvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for ((path, paint) in paths) {
            canvas.drawPath(path, paint)
        }

        canvas.drawPath(currentPath, currentPaint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                currentPath = Path().apply { moveTo(touchX!!, touchY!!) }
                currentPaint = Paint(currentPaint)
            }
            MotionEvent.ACTION_MOVE -> {
                currentPath.lineTo(touchX!!, touchY!!)
            }
            MotionEvent.ACTION_UP -> {
                paths.add(Pair(currentPath, currentPaint))
                currentPath = Path()
            }

        }
        invalidate()
        return true
    }

    fun changeBrushColor(color: Int) {
        currentPaint.color = color
    }

    fun changeBrushSize(size: Float) {
        currentPaint.strokeWidth = size
    }

    fun changeBrushStyle(style: Paint.Style) {
        currentPaint.style = style
    }


    fun changeStrokeJoin(join: Paint.Join) {
        currentPaint.strokeJoin = join
    }


    fun changeStrokeCap(cap: Paint.Cap) {
        currentPaint.strokeCap = cap
    }

    fun clearCanvas() {
        paths.clear()
        invalidate()
    }

    fun undo() {
        if (paths.isNotEmpty()) {
            paths.removeLast()
            invalidate()
        }
    }




}
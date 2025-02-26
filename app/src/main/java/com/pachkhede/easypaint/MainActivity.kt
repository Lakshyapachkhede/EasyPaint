package com.pachkhede.easypaint


import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever.BitmapParams
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.abs
import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.widget.ScrollView
import android.widget.TextView
import org.w3c.dom.Text


class MainActivity : AppCompatActivity() {

    private lateinit var drawingView: DrawingView
    private lateinit var sideMenu: ScrollView
    private lateinit var thicknessSeekBar: SeekBar
    private lateinit var colorIndicator: View
    private lateinit var backColorIndicator: View
    private lateinit var mainView: ConstraintLayout
    private lateinit var imgView: ImageView
    private var img: Int = R.drawable.pen
    val shapes = listOf(

        Item("Line", R.drawable.line, DrawingView.Tools.LINE),
        Item("Circle", R.drawable.circle, DrawingView.Tools.CIRCLE),
        Item("Rectangle", R.drawable.rectangle, DrawingView.Tools.RECTANGLE),
        Item("RectangleRound", R.drawable.rectangle_rounded, DrawingView.Tools.RECTANGLE_ROUND),
        Item("Triangle", R.drawable.triangle, DrawingView.Tools.TRIANGLE),
        Item("RightTriangle", R.drawable.triangle_right, DrawingView.Tools.RIGHT_TRIANGLE),
        Item("Diamond", R.drawable.diamond, DrawingView.Tools.DIAMOND),
        Item("Pentagon", R.drawable.pentagon, DrawingView.Tools.PENTAGON),
        Item("Hexagon", R.drawable.hexagon, DrawingView.Tools.HEXAGON),
        Item("ArrowMark", R.drawable.arrow_mark, DrawingView.Tools.ARROW_MARK),
        Item("ArrowDouble", R.drawable.arrow_double, DrawingView.Tools.ARROW_DOUBLE),
        Item("ArrowOutline", R.drawable.arrow_outline, DrawingView.Tools.ARROW),
        Item("ArrowOutlineUP", R.drawable.arrow_outline2, DrawingView.Tools.ARROW2),
        Item("StarFour", R.drawable.star_four, DrawingView.Tools.STAR_FOUR),
        Item("StarFive", R.drawable.star, DrawingView.Tools.STAR_FIVE),
        Item("StarSix", R.drawable.star2, DrawingView.Tools.STAR_SIX),
        Item("Chat", R.drawable.chat, DrawingView.Tools.CHAT),
        Item("Heart", R.drawable.heart, DrawingView.Tools.HEART),
        Item("Lightning", R.drawable.lightning, DrawingView.Tools.LIGHTNING),
        Item("Pencil", R.drawable.pencil, DrawingView.Tools.PENCIL),
    )


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        drawingView = findViewById(R.id.drawingView)
        sideMenu = findViewById(R.id.sideMenu)
        thicknessSeekBar = findViewById(R.id.thicknessSeekBar)
        colorIndicator = findViewById(R.id.colorIndicator)
        backColorIndicator = findViewById(R.id.backColorIndicator)
        mainView = findViewById<ConstraintLayout>(R.id.main)
        imgView = ImageView(this)

        thicknessSeekBarSetup()

        findViewById<LinearLayout>(R.id.menuBtn).setOnClickListener {
            toggleMenu()
        }

        findViewById<ImageView>(R.id.pen).setOnClickListener {
            img = R.drawable.pen
            imgView.setImageResource(img)
            drawingView.changeTool(DrawingView.Tools.SOLID_BRUSH)
        }

        findViewById<ImageView>(R.id.eraser).setOnClickListener {
            img = R.drawable.eraser
            imgView.setImageResource(img)
            drawingView.changeTool(DrawingView.Tools.ERASER)
        }

        findViewById<ImageView>(R.id.bucket).setOnClickListener {
            img = R.drawable.bucket
            imgView.setImageResource(img)
            drawingView.changeTool(DrawingView.Tools.FILL)
        }

        findViewById<ImageView>(R.id.clear).setOnClickListener {
            drawingView.clearCanvas()
        }

        findViewById<ImageView>(R.id.save).setOnClickListener {
            saveDrawing()
        }

        findViewById<ImageView>(R.id.shapes).setOnClickListener {
            val bottomSheet = ItemBottomSheet(shapes, "Select Shape") { selectedShape ->
                Toast.makeText(this, "Selected: ${selectedShape.name}", Toast.LENGTH_SHORT).show()
                drawingView.changeTool(selectedShape.tool)
                img = R.drawable.pen
                imgView.setImageResource(img)

            }
            bottomSheet.show(supportFragmentManager, "ShapesBottomSheet")
        }

        drawingView.setOnTouchListener { _, event ->

            if (event.action == MotionEvent.ACTION_MOVE) {
                showImageAtTouch(event.x, event.y, img)
            }

            false
        }


        colorIndicator.setOnClickListener {
            openColorPicker(false)
        }

        backColorIndicator.setOnClickListener {
            openColorPicker(true)
        }

        findViewById<LinearLayout>(R.id.undo).setOnClickListener {
            drawingView.undo()
        }
        findViewById<LinearLayout>(R.id.redo).setOnClickListener {
            drawingView.redo()
        }


    }

    private fun toggleMenu() {
        if (sideMenu.visibility == View.GONE) {
            val animation: Animation =
                AnimationUtils.loadAnimation(baseContext, R.anim.side_menu_open)
            sideMenu.visibility = View.VISIBLE
            sideMenu.startAnimation(animation)

        } else {
            val animation: Animation =
                AnimationUtils.loadAnimation(baseContext, R.anim.side_menu_close)
            sideMenu.visibility = View.GONE
            sideMenu.startAnimation(animation)
        }
    }

    private fun thicknessSeekBarSetup() {
        thicknessSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                findViewById<TextView>(R.id.seekBarIndicator).text = p1.toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                drawingView.changeBrushSize(thicknessSeekBar.progress.toFloat())
            }

        })
    }

    private fun openColorPicker(isBackground: Boolean) {
        val title = if (isBackground) "Select Background Color" else "Select Color"

        ColorPickerDialog.Builder(this)
            .setTitle(title)
            .setPreferenceName("MyColorPickerDialog")
            .setPositiveButton("Select", object : ColorEnvelopeListener {
                override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
                    envelope?.color?.let { selectedColor ->
                        if (isBackground) {
                            drawingView.changeBackColor(selectedColor)
                            backColorIndicator.backgroundTintList =
                                ColorStateList.valueOf(selectedColor)
                        } else {
                            drawingView.changeColor(selectedColor)
                            colorIndicator.backgroundTintList =
                                ColorStateList.valueOf(selectedColor)
                        }
                    }
                }
            })
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .attachAlphaSlideBar(true)
            .attachBrightnessSlideBar(true)
            .setBottomSpace(12)
            .show()
    }


    private fun showImageAtTouch(x: Float, y: Float, img: Int) {

        mainView.removeView(imgView)

        imgView.setImageResource(img)
        imgView.layoutParams = LinearLayout.LayoutParams(100, 100)
        imgView.visibility = View.VISIBLE
        imgView.x = x
        imgView.y = y

        mainView.addView(imgView)


    }

    private fun saveDrawing() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // Check if permission is granted
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
                return  // Stop execution until the user grants permission
            }
        }

        val drawingView = findViewById<DrawingView>(R.id.drawingView)
        val bitmap = drawingView.getBitmap()
        val filename = "EasyPaint_${System.currentTimeMillis()}.png"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES
            ) // Saves to Pictures folder
        }

        val uri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            try {
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    Toast.makeText(this, "Drawing saved to Gallery", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to save drawing", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(this, "Failed to save drawing", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveDrawing()
            } else {
                Toast.makeText(this, "Storage permission denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }


}






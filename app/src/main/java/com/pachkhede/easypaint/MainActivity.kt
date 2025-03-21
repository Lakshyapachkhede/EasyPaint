package com.pachkhede.easypaint


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.PersistableBundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pachkhede.easypaint.DrawingView.Tools
import java.io.ByteArrayOutputStream
import java.io.IOException
import android.util.Base64
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.content.FileProvider
import androidx.core.graphics.toColorInt
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import java.io.File
import java.io.FileOutputStream
import com.pachkhede.easypaint.ColorPickerDialog
import androidx.core.view.isGone
import androidx.core.view.isVisible

class MainActivity : AppCompatActivity() {

    private lateinit var drawingView: DrawingView
    private lateinit var sideMenu: ScrollView
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

        )

    private val recentColors =  MutableList<Int>(6) { "#FFFFFF".toColorInt() }



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
        colorIndicator = findViewById(R.id.colorIndicator)
        backColorIndicator = findViewById(R.id.backColorIndicator)
        mainView = findViewById<ConstraintLayout>(R.id.main)
        imgView = ImageView(this)


        findViewById<LinearLayout>(R.id.menuBtn).setOnClickListener {
            toggleMenu()
        }

        findViewById<ImageView>(R.id.pen).setOnClickListener {
            img = R.drawable.pen
            imgView.setImageResource(img)
            drawingView.changeTool(Tools.SOLID_BRUSH)
        }

        findViewById<ImageView>(R.id.width).setOnClickListener {
            showWidthDialog()

        }

        findViewById<ImageView>(R.id.brush).setOnClickListener {
            showBrushesDialog()

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

        findViewById<ImageView>(R.id.text).setOnClickListener {
            openTextDialog()
        }

        findViewById<ImageView>(R.id.save).setOnClickListener {
            saveDrawing()
        }

        findViewById<ImageView>(R.id.share).setOnClickListener {
            shareDrawing()
        }

        findViewById<ImageView>(R.id.image).setOnClickListener {
            openGallery()
        }

        findViewById<ImageView>(R.id.shapes).setOnClickListener {
            val bottomSheet = ItemBottomSheet(shapes, "Select Shape") { selectedShape ->
                drawingView.changeTool(selectedShape.tool)
                img = R.drawable.pen
                imgView.setImageResource(img)

            }
            bottomSheet.show(supportFragmentManager, "ShapesBottomSheet")
        }

        drawingView.setOnTouchListener { _, event ->

            if (event.action == MotionEvent.ACTION_MOVE) {
                showImageAtTouch(event.x, event.y, img)
                if (sideMenu.isVisible){
                    toggleMenu(false)
                }
            }

            false
        }


        colorIndicator.setOnClickListener {
//            openColorPicker(false)
            val dialog =
                ColorPickerDialog("Select Color",
                    ColorPickerDialog.paintColorsDefaultList, recentColors){ color ->
                Toast.makeText(this@MainActivity, color.toString(), Toast.LENGTH_SHORT).show()
                drawingView.changeColor(color)
                colorIndicator.backgroundTintList =
                    ColorStateList.valueOf(color)
                addRecentColor(color)
            }
            dialog.show(supportFragmentManager, "Color Picker Main")
        }

        backColorIndicator.setOnClickListener {
            val dialog =
                ColorPickerDialog("Select Background Color",
                    ColorPickerDialog.paintColorsDefaultList, recentColors){ color ->
                Toast.makeText(this@MainActivity, color.toString(), Toast.LENGTH_SHORT).show()
                drawingView.changeBackColor(color)
                backColorIndicator.backgroundTintList =
                    ColorStateList.valueOf(color)
                addRecentColor(color)
            }
            dialog.show(supportFragmentManager, "Color Picker Background")
        }

        findViewById<LinearLayout>(R.id.undo).setOnClickListener {
            drawingView.undo()
        }
        findViewById<LinearLayout>(R.id.redo).setOnClickListener {
            drawingView.redo()
        }


    }

    private fun toggleMenu(isEnable: Boolean = true) {
        if (sideMenu.isGone && isEnable) {
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


    private fun showWidthDialog() {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.stroke_width_bottom_sheet, null)

        val seekBar = view.findViewById<SeekBar>(R.id.widthSeekBar)
        val progressTv = view.findViewById<TextView>(R.id.seekBarIndicator)

        seekBar.progress = drawingView.currStrokeWidth.toInt()
        progressTv.text = drawingView.currStrokeWidth.toInt().toString()

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                findViewById<TextView>(R.id.seekBarIndicator).text = p1.toString()
                progressTv.text = p1.toString()
                drawingView.changeBrushSize(seekBar.progress.toFloat())
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }

        })

        view.findViewById<ImageView>(R.id.addWidth).setOnClickListener {
            seekBar.progress += 1
        }

        view.findViewById<ImageView>(R.id.substractWidth).setOnClickListener {
            seekBar.progress -= 1
        }

        dialog.setContentView(view)
        dialog.show()

    }

    private fun showBrushesDialog() {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.brushes_bottom_sheet, null)

        val recyclerView = view.findViewById<RecyclerView>(R.id.brushRecyclerView)

        val items = listOf(
            BrushItem("solid", DrawingView.Tools.SOLID_BRUSH),
            BrushItem("calligraphy", DrawingView.Tools.CALLIGRAPHY_BRUSH),
            BrushItem("crayon", DrawingView.Tools.CRAYON_BRUSH),
            BrushItem("spray", DrawingView.Tools.SPRAY_BRUSH),
            BrushItem("air spray", DrawingView.Tools.SPRAY_BRUSH_CAN),
            BrushItem("dotted", DrawingView.Tools.DASHED_BRUSH),
            BrushItem("neon", DrawingView.Tools.NEON_BRUSH),
            BrushItem("Marker", DrawingView.Tools.MARKER_BRUSH),
            BrushItem("blur", DrawingView.Tools.BLUR_BRUSH),
            BrushItem("oil", DrawingView.Tools.OIL_BRUSH),


            )


        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = BrushAdapter(items) { item ->
            if (item.tool in drawingView.brushes) {
                img = R.drawable.pen
                imgView.setImageResource(img)
            }

            drawingView.changeTool(item.tool)
            when (item.tool) {
                Tools.BLUR_BRUSH, Tools.OIL_BRUSH, Tools.CRAYON_BRUSH, Tools.MARKER_BRUSH -> {
                    drawingView.changeBrushSize(50f)
                    findViewById<TextView>(R.id.seekBarIndicator).text = "50"

                }

                else -> {}
            }
            dialog.dismiss()
        }
        recyclerView.adapter = adapter


        dialog.setContentView(view)
        dialog.show()

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

            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
                return
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
            )
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

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        resultLauncher.launch(intent)
    }

    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val imageUri: Uri? = data?.data
                if (imageUri != null) {
                    val bitmap = uriToBitmap(imageUri)
                    drawingView.setBackgroundImage(bitmap)
                }

            }
        }

    private fun uriToBitmap(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }
    }

    override fun onResume() {
        super.onResume()

        drawingView.post {
            val bitmap = loadDrawingFromFile()
            if (bitmap != null) {
                drawingView.setBackgroundImage(bitmap)
            } else {

            }
        }
    }

    override fun onPause() {
        super.onPause()
        saveDrawingToFile()

    }

    private fun saveDrawingToFile() {

        val file = File(filesDir, getString(R.string.saved_bitmap)) // Internal storage path
        FileOutputStream(file).use { fos ->
            drawingView.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, fos)
        }

    }

    private fun loadDrawingFromFile(): Bitmap? {
        val file = File(filesDir, getString(R.string.saved_bitmap))
        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    }


    private fun openTextDialog() {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.add_text_dialog, null)

        val seekBar = view.findViewById<SeekBar>(R.id.textSizeSeekBar)
        val progressTv = view.findViewById<TextView>(R.id.textSizeIndicator)
        val demoTextView = view.findViewById<TextView>(R.id.demoText)
        val inputEditText = view.findViewById<EditText>(R.id.add_text_input)
        val textColor = view.findViewById<View>(R.id.textColorIndicator)
        val colorIndicator = view.findViewById<View>(R.id.textColorIndicator)


        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                progressTv.text = p1.toString()
                drawingView.textpaint.textSize = p1.toFloat()
                demoTextView.textSize = pxToSp(p1.toFloat(), this@MainActivity)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }

        })


        view.findViewById<ImageView>(R.id.textSizeIncrease).setOnClickListener {
            seekBar.progress += 1
        }

        view.findViewById<ImageView>(R.id.textSizeDecrease).setOnClickListener {
            seekBar.progress -= 1
        }

        dialog.setOnDismissListener {
            val text = inputEditText.text.toString()
            drawingView.text = if (text.trim() != "") text else drawingView.text
            drawingView.changeTool(Tools.TEXT)
        }

        inputEditText.doAfterTextChanged { text ->
            demoTextView.setText(if (text.isNullOrEmpty()) "Your Text" else text)
        }

        inputEditText.setOnEditorActionListener {v, actionId, event->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
                val text = inputEditText.text.toString()
                drawingView.text = if (text.trim() != "") text else drawingView.text
                drawingView.changeTool(Tools.TEXT)
                dialog.dismiss()
                true
            } else {
                false
            }
        }


        textColor.setOnClickListener {
            val dialog =
                ColorPickerDialog("Select Text Color",
                    ColorPickerDialog.paintColorsDefaultList, recentColors){ color ->
                    Toast.makeText(this@MainActivity, color.toString(), Toast.LENGTH_SHORT).show()
                    demoTextView.setTextColor(color)
                    drawingView.textpaint.color = color
                    colorIndicator.backgroundTintList =
                        ColorStateList.valueOf(color)
                    addRecentColor(color)
                }
            dialog.show(supportFragmentManager, "Color Picker Text")
        }

        dialog.setContentView(view)
        dialog.findViewById<EditText>(R.id.add_text_input)?.setText(drawingView.text)
        demoTextView.setText(if (drawingView.text.isNullOrEmpty()) "Your Text" else drawingView.text)
        seekBar.progress = drawingView.currStrokeWidth.toInt()
        progressTv.text = drawingView.currStrokeWidth.toInt().toString()
        demoTextView.setTextColor(drawingView.textpaint.color)
        demoTextView.textSize = pxToSp(drawingView.textpaint.textSize, this@MainActivity)
        seekBar.progress = drawingView.textpaint.textSize.toInt()
        colorIndicator.backgroundTintList =
            ColorStateList.valueOf(drawingView.textpaint.color)
        progressTv.text = drawingView.textpaint.textSize.toInt().toString()

        dialog.show()


    }

    fun pxToSp(px: Float, context: Context): Float {
        return px / context.resources.displayMetrics.scaledDensity
    }



    fun addRecentColor(color: Int) {
        if (recentColors.size >= 6) {
            recentColors.removeAt(recentColors.lastIndex)
        }
        recentColors.add(0, color)
    }


    private fun shareDrawing() {
        val bitmap = drawingView.getBitmap()

        try {

            val file = File(cacheDir, "shared_image.png")
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()

            // Get URI from file provider
            val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)

            // Create Share Intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "image/png"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }


            startActivity(Intent.createChooser(shareIntent, "Share Drawing"))

        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to share image", Toast.LENGTH_SHORT).show()
        }
    }
}








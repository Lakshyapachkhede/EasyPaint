package com.pachkhede.easypaint

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.utils.widget.ImageFilterButton
import androidx.core.graphics.toColorInt
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog as ColorChooser
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener


class ColorPickerDialog(
    val title: String,
    val colorList: List<Int>,
    val recentList: List<Int>,
    val listener: (Int) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var view: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var recentRecyclerView: RecyclerView
    private var color: Int = BLACK
    private lateinit var colorInput : EditText;
    private lateinit var doneButton : ImageView
    private lateinit var customTextColorButton : ImageView

    companion object {

        val paintColorsDefaultList = mutableListOf(
            "#000000".toColorInt(),  // Black
            "#808080".toColorInt(),  // Gray
            "#C0C0C0".toColorInt(),  // Silver
            "#800000".toColorInt(),  // Dark Red
            "#FF0000".toColorInt(),  // Red
            "#FFA500".toColorInt(),  // Orange
            "#FFFF00".toColorInt(),  // Yellow
            "#808000".toColorInt(),  // Olive
            "#00FF00".toColorInt(),  // Green
            "#008000".toColorInt(),  // Dark Green
            "#00FFFF".toColorInt(),  // Cyan
            "#008080".toColorInt(),  // Teal
            "#0000FF".toColorInt(),  // Blue
            "#000080".toColorInt(),  // Navy Blue
            "#FF00FF".toColorInt(),  // Magenta
            "#800080".toColorInt(),  // Purple
            "#A52A2A".toColorInt(),  // Brown
            "#D2691E".toColorInt(),  // Chocolate
            "#FFD700".toColorInt(),   // Gold
            "#FFFFFF".toColorInt(),  // White
            "#4B0082".toColorInt(),  // Indigo
            "#FF1493".toColorInt(),  // Deep Pink
            "#2E8B57".toColorInt(),  // Sea Green
            "#4682B4".toColorInt(),  // Steel Blue

        )

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        view = layoutInflater.inflate(R.layout.color_picker_dialog,container,false)

        val titleTextView = view.findViewById<TextView>(R.id.dialogTitle)

        titleTextView.text = title

        val title2 = view.findViewById<TextView>(R.id.dialogTitle2)
        title2.text = "Recent Colors"



        recyclerView = view.findViewById<RecyclerView>(R.id.colorRecyclerView)
        recentRecyclerView = view.findViewById<RecyclerView>(R.id.recentColorRecyclerView)



        recyclerView.layoutManager = GridLayoutManager(context, 6)
        recentRecyclerView.layoutManager = GridLayoutManager(context, 6)


        val adapter = ColorPickerAdapter(colorList) { color ->
            this.color = color
            listener.invoke(color)
            dialog?.dismiss()
        }

        val adapter2 = ColorPickerAdapter(recentList) { color ->
            this.color = color
            listener.invoke(color)
            dialog?.dismiss()
        }

        recyclerView.adapter = adapter
        recentRecyclerView.adapter = adapter2


        colorInput = view.findViewById<EditText>(R.id.color_input)
        doneButton = view.findViewById<ImageView>(R.id.done)

        customTextColorButton = view.findViewById<ImageView>(R.id.customColorIndicator)



        customTextColorButton.setOnClickListener {
            val title = "Select Color"

            ColorChooser.Builder(context)
                .setTitle(title)
                .setPreferenceName("MyColorPickerDialog")
                .setPositiveButton("Select", object : ColorEnvelopeListener {
                    override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
                        envelope?.color?.let { selectedColor ->
                            this@ColorPickerDialog.color = selectedColor

                            colorInput.setText(intToARGB(selectedColor))
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

        doneButton.setOnClickListener {
            doneButtonClick()
        }
        colorInput.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
            doneButtonClick()
                true
            } else {
                false
            }
        }



        return view
    }

    fun intToARGB(color: Int): String {
        return String.format("#%08X", color)
    }

    fun hexToColorInt(hex: String): Int? {
        val regex = Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$") // Validate hex format

        return if (hex.matches(regex)) {
            val hexWithoutHash = hex.substring(1) // Remove the '#'

            val argbHex = if (hexWithoutHash.length == 6) {
                "FF$hexWithoutHash" // Add 'FF' alpha for 6-length hex
            } else {
                hexWithoutHash // Use as is for 8-length hex
            }

            argbHex.toLong(16).toInt() // Convert to Int color
        } else {
            null // Invalid hex
        }
    }

    private fun doneButtonClick(){
        if (hexToColorInt(colorInput.text.toString()) != null) {
            color = hexToColorInt(colorInput.text.toString())!!
            listener.invoke(color)
            dialog?.dismiss()

        } else {
            Toast.makeText(context ,"Enter Valid Color", Toast.LENGTH_SHORT).show()
        }
    }




}
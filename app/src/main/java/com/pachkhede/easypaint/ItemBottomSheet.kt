package com.pachkhede.easypaint

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ItemBottomSheet(
    private val items: List<Item>,
    private val title: String,
    private val listener: (Item) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = layoutInflater.inflate(R.layout.bottom_sheet_dialog, container, false)

        val titleTextView = view.findViewById<TextView>(R.id.dialogTitle)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        titleTextView.text = title
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 4)
        recyclerView.adapter = ItemAdapter(items) { item ->
            listener(item)
            dismiss()
        }

        return view
    }
}
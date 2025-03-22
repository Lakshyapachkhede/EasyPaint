package com.pachkhede.easypaint

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


class ColorPickerAdapter(val list: List<Int>, val touchListener: (Int) -> Unit) :
    RecyclerView.Adapter<ColorPickerAdapter.ColorPickerViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ColorPickerViewHolder {
        return ColorPickerViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.color_item_layout, null)
        )
    }

    override fun onBindViewHolder(
        holder: ColorPickerViewHolder,
        position: Int
    ) {

        holder.colorView.backgroundTintList = ColorStateList.valueOf(list[position])
        holder.itemView.setOnClickListener {
            touchListener.invoke(list[position])
        }

    }

    override fun getItemCount(): Int = list.size
    class ColorPickerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val colorView: View = itemView.findViewById<View>(R.id.color)
    }

}
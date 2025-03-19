package com.pachkhede.easypaint

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BrushAdapter(val list: List<BrushItem>, private val listener: (BrushItem) -> Unit) :
    RecyclerView.Adapter<BrushAdapter.BrushViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BrushViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.brush_item, parent, false)
        return BrushViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: BrushViewHolder,
        position: Int
    ) {
        val item = list[position]
        holder.name.text = item.name

        holder.brushDemoView.tool = item.tool

        holder.itemView.setOnClickListener {
            listener(item)
        }



    }

    override fun getItemCount(): Int = list.size

    class BrushViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val name: TextView = itemView.findViewById<TextView>(R.id.brushName)
        val brushDemoView : BrushDemoView = itemView.findViewById<BrushDemoView>(R.id.brushDemo);

    }

}
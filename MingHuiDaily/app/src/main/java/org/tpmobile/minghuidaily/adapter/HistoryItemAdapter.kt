package org.tpmobile.minghuidaily.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.tpmobile.minghuidaily.R
import org.tpmobile.minghuidaily.data.SelectedStateOfHistoryItem

class HistoryItemAdapter(
    val items: List<SelectedStateOfHistoryItem>,
    private val onItemClick: (yearMonth: String, position: Int) -> Unit
) :
    RecyclerView.Adapter<HistoryItemAdapter.ViewHolder>() {

    class ViewHolder(view: View, val onItemClick: (yearMonth: String, position: Int) -> Unit) :
        RecyclerView.ViewHolder(view) {
        val tvDay: TextView = view.findViewById(R.id.tvDay)

        init {
            view.apply {
                setOnClickListener {
                    isSelected = !isSelected
                    setBackgroundResource(
                        if (isSelected) R.drawable.shape_round_rectangle_selected
                        else R.drawable.shape_round_rectangle_unselected
                    )
                    onItemClick(tvDay.text.toString(), bindingAdapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_day, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.tvDay.text = "${items[position].day}"
        holder.itemView.setBackgroundResource(
            if (items[position].isSelected) R.drawable.shape_round_rectangle_selected
            else R.drawable.shape_round_rectangle_unselected
        )
    }

    override fun getItemCount() = items.size

}
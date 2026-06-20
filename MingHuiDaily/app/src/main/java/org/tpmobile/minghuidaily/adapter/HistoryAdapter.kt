package org.tpmobile.minghuidaily.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.AlignContent
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import org.tpmobile.minghuidaily.R
import org.tpmobile.minghuidaily.data.HistoryItem
import org.tpmobile.minghuidaily.util.Logger

class HistoryAdapter(
    private val items: List<HistoryItem>, private val singleSelection: Boolean = true
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private val TAG = "HistoryAdapter"
    val itemsSelected = mutableSetOf<String>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rvHistoryDayList: RecyclerView = view.findViewById<RecyclerView>(R.id.rvHistoryDayList)
        val tvHistoryYearMonth: TextView = view.findViewById<TextView>(R.id.tvHistoryYearMonth)
        val cvYearMonth: CardView = view.findViewById<CardView>(R.id.cvYearMonth)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvHistoryYearMonth.text = items[position].yearMonth
        holder.rvHistoryDayList.layoutManager = FlexboxLayoutManager(holder.itemView.context).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }
        //= GridLayoutManager(holder.itemView.context, 6)
        holder.rvHistoryDayList.adapter =
            HistoryItemAdapter(items[position].selectedStateList) { yearMonth, itemDayPosition ->
                //dateString = 2026年5月13
                val newValue = !items[position].selectedStateList[itemDayPosition].isSelected
                if (singleSelection && newValue) {
                    items.forEach { item ->
                        for (state in item.selectedStateList) state.isSelected = false
                    }
                }
                items[position].selectedStateList[itemDayPosition].isSelected = newValue

                val dateString =
                    "${holder.tvHistoryYearMonth.text}${items[position].selectedStateList[itemDayPosition].day}"
                Logger.i(TAG, "点击了：$dateString}\n  所有已选列表：${getAllSelectedItem()}")
                if (itemsSelected.contains(dateString)) {
                    items.find { it.yearMonth == yearMonth }?.selectedStateList[itemDayPosition]?.apply {
                        isSelected = !isSelected
                        itemsSelected.remove(dateString)
                    }
                } else {
                    itemsSelected.add(dateString)
                }

                notifyDataSetChanged()
            }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun getAllSelectedItem(): List<String> {
        val list = mutableListOf<String>()
        items.forEach { historyItem ->
            historyItem.selectedStateList.forEach { item ->
                if (item.isSelected) list.add("${historyItem.yearMonth}${item.day}")
            }
        }
        return list
    }

}

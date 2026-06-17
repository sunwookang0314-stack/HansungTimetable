package com.example.hansungtimetable.ui.memo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.hansungtimetable.data.Memo
import com.example.hansungtimetable.databinding.ItemMemoBinding
import com.example.hansungtimetable.util.TimeUtil

class MemoAdapter(
    private var items: List<Memo>,
    private val onClick: (Memo) -> Unit
) : RecyclerView.Adapter<MemoAdapter.VH>() {

    class VH(val b: ItemMemoBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemMemoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.b.tvTitle.text = item.title
        holder.b.tvContent.text = item.content
        holder.b.tvDate.text = TimeUtil.formatDateTime(item.createdAt)
        holder.b.root.setBackgroundColor(item.colorArgb)
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<Memo>) {
        items = newItems
        notifyDataSetChanged()
    }
}

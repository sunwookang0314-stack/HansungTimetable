package com.example.hansungtimetable.ui.assignment

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.hansungtimetable.R
import com.example.hansungtimetable.data.Assignment
import com.example.hansungtimetable.databinding.ItemAssignmentBinding
import com.example.hansungtimetable.util.TimeUtil

class AssignmentAdapter(
    private var items: List<Assignment>,
    private val onClick: (Assignment) -> Unit
) : RecyclerView.Adapter<AssignmentAdapter.VH>() {

    class VH(val b: ItemAssignmentBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemAssignmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.b.tvTitle.text = item.title
        holder.b.tvDue.text = "마감: ${TimeUtil.formatDateTime(item.dueAtMillis)}"

        if (item.memo.isNotBlank()) {
            holder.b.tvMemo.text = item.memo
            holder.b.tvMemo.visibility = android.view.View.VISIBLE
        } else {
            holder.b.tvMemo.visibility = android.view.View.GONE
        }

        // 오른쪽 위 뱃지: 완료한 과제는 완료 표시, 아직이면 마감까지 D-day 표시.
        // 마감이 임박할수록 색을 빨갛게 해서 눈에 띄게 했다.
        if (item.done) {
            holder.b.tvStatus.text = holder.itemView.context.getString(R.string.label_status_done)
            holder.b.tvStatus.setBackgroundColor(Color.parseColor("#C8E6C9")) // 초록 - 완료
            holder.b.tvStatus.setTextColor(Color.parseColor("#2E7D32"))
        } else {
            val days = TimeUtil.daysUntil(item.dueAtMillis)
            val label: String
            val bg: String
            when {
                days < 0 -> { label = "${-days}일 지남"; bg = "#FFCDD2" } // 빨강 - 마감 지남
                days == 0 -> { label = "D-DAY"; bg = "#FFE0B2" }          // 주황 - 오늘 마감
                days <= 3 -> { label = "D-$days"; bg = "#FFE0B2" }        // 주황 - 곧 마감
                else -> { label = "D-$days"; bg = "#BBDEFB" }             // 파랑 - 여유 있음
            }
            holder.b.tvStatus.text = label
            holder.b.tvStatus.setBackgroundColor(Color.parseColor(bg))
            holder.b.tvStatus.setTextColor(Color.parseColor("#333333"))
        }

        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<Assignment>) {
        items = newItems
        notifyDataSetChanged()
    }
}

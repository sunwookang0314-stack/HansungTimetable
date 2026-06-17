package com.example.hansungtimetable.ui.timetable

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.hansungtimetable.R
import com.example.hansungtimetable.data.DataManager
import com.example.hansungtimetable.data.Subject
import com.example.hansungtimetable.databinding.FragmentTimetableBinding
import com.example.hansungtimetable.util.PrefManager
import com.example.hansungtimetable.util.TimeUtil

class TimetableFragment : Fragment() {

    private var _binding: FragmentTimetableBinding? = null
    private val binding get() = _binding!!

    // 시간표 기본 표시 범위는 오전 9시 ~ 오후 6시.
    // 근데 이 범위 밖(이른 아침이나 늦은 오후) 과목을 추가하면 시간표에 안 보이는
    // 문제가 있어서, 실제 저장된 과목에 맞춰 범위를 늘리도록 drawGrid에서 다시 계산한다.
    private val defaultStartHour = 9
    private val defaultEndHour = 18

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): android.view.View {
        _binding = FragmentTimetableBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshGrid()
    }

    override fun onResume() {
        super.onResume()
        refreshGrid()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 저장된 과목을 다시 읽어서 시간표를 새로 그린다. MainActivity에서도 호출함.
    fun refreshGrid() {
        if (_binding == null) return
        val ctx = context ?: return
        val semester = PrefManager.getSemester(ctx)
        val subjects = DataManager.getSubjects(ctx, semester)
        drawGrid(subjects)
    }

    // GridLayout에 코드로 칸(TextView)을 하나씩 붙여서 시간표를 만든다.
    // XML에 칸을 다 만들어두는 게 아니라, 시간대 x 요일만큼 동적으로 addView 함.
    private fun drawGrid(subjects: List<Subject>) {
        val grid = binding.gridTimetable
        grid.removeAllViews()  // 다시 그릴 때 이전 칸 싹 지우고 시작

        // 기본 범위(9~18시)에서 시작하되, 더 이르거나 늦은 과목이 있으면 거기까지 늘린다.
        // 이렇게 안 하면 오후 늦은 수업을 넣어도 시간표에 안 떠서 한참 헤맸음.
        var startHour = defaultStartHour
        var endHour = defaultEndHour
        for (s in subjects) {
            val sh = s.startMin / 60            // 시작 시각의 시
            val eh = (s.endMin + 59) / 60       // 종료 시각의 시 (분이 남으면 한 시간 올림)
            if (sh < startHour) startHour = sh
            if (eh > endHour) endHour = eh
        }

        grid.columnCount = 6
        grid.rowCount = (endHour - startHour) + 1
        grid.useDefaultMargins = false
        grid.alignmentMode = GridLayout.ALIGN_BOUNDS

        val pad = dp(4)
        val headerBg = requireContext().getColor(R.color.grid_header_bg)
        val days = arrayOf(
            getString(R.string.day_mon), getString(R.string.day_tue),
            getString(R.string.day_wed), getString(R.string.day_thu),
            getString(R.string.day_fri)
        )

        addCell(grid, 0, 0, "", headerBg, true, pad)
        for (d in days.indices) {
            addCell(grid, d + 1, 0, days[d], headerBg, true, pad)
        }

        for (hour in startHour until endHour) {
            val row = hour - startHour + 1
            addCell(grid, 0, row, TimeUtil.minToHHmm(hour * 60), headerBg, true, pad)

            for (day in 1..5) {
                // 이 요일 이 시간에 걸치는 과목이 있는지 확인
                val sub = subjects.find { s ->
                    s.dayOfWeek == day && hour * 60 >= s.startMin && hour * 60 < s.endMin
                }
                if (sub != null) {
                    // 과목 시작 시간 칸에만 이름/강의실/시간을 다 적고,
                    // 그 아래로 이어지는 칸엔 이름만 표시
                    val txt = if (hour * 60 == sub.startMin) {
                        "${sub.name}\n${sub.room}\n" +
                                "${TimeUtil.minToHHmm(sub.startMin)}~${TimeUtil.minToHHmm(sub.endMin)}"
                    } else {
                        sub.name
                    }
                    addCell(grid, day, row, txt, sub.colorArgb, false, pad) {
                        startActivity(Intent(requireContext(), SubjectEditActivity::class.java).apply {
                            putExtra(SubjectEditActivity.EXTRA_ID, sub.id)
                        })
                    }
                } else {
                    addCell(grid, day, row, "", Color.WHITE, false, pad)
                }
            }
        }
    }

    private fun addCell(
        grid: GridLayout, col: Int, row: Int, text: String,
        bg: Int, header: Boolean, pad: Int,
        click: (() -> Unit)? = null
    ) {
        val tv = TextView(requireContext()).apply {
            this.text = text
            setPadding(pad, pad, pad, pad)
            setBackgroundColor(bg)
            gravity = Gravity.CENTER
            setTextColor(requireContext().getColor(R.color.text_primary))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, if (header) 13f else 11f)
            if (header) setTypeface(typeface, android.graphics.Typeface.BOLD)
            minHeight = dp(52)
        }
        click?.let { tv.setOnClickListener { it() } }

        // 0번 열은 시간(09:00 등) 칸이라 글자 크기만큼만,
        // 나머지 요일 칸들은 화면을 똑같이 나눠 갖게 weight(1f)를 줬다.
        // weight 안 주고 그냥 두니까 칸 너비가 제각각이라 보기 안 좋았음.
        val lp = GridLayout.LayoutParams().apply {
            rowSpec = GridLayout.spec(row)
            columnSpec = if (col == 0) {
                GridLayout.spec(col)
            } else {
                GridLayout.spec(col, 1f)
            }
            width = if (col == 0) {
                GridLayout.LayoutParams.WRAP_CONTENT
            } else {
                0  // weight로 너비를 채우려면 width는 0으로 둬야 함
            }
            height = GridLayout.LayoutParams.WRAP_CONTENT
            setMargins(dp(1), dp(1), dp(1), dp(1))
        }
        grid.addView(tv, lp)
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}

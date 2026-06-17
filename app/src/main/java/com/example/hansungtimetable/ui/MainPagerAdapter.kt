package com.example.hansungtimetable.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.hansungtimetable.ui.assignment.AssignmentFragment
import com.example.hansungtimetable.ui.memo.MemoFragment
import com.example.hansungtimetable.ui.timetable.TimetableFragment

class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount() = 3

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> TimetableFragment()
        1 -> AssignmentFragment()
        2 -> MemoFragment()
        else -> TimetableFragment()
    }
}

package com.example.hansungtimetable.util

import android.content.Context
import androidx.preference.PreferenceManager

// 설정 화면이랑 같은 SharedPreferences 씀 (학기만)
object PrefManager {

    const val KEY_SEMESTER = "semester"

    private fun prefs(ctx: Context) =
        PreferenceManager.getDefaultSharedPreferences(ctx)

    fun getSemester(ctx: Context): String {
        val s = prefs(ctx).getString(KEY_SEMESTER, "2026-1")
        return if (s.isNullOrBlank()) "2026-1" else s.trim()
    }
}

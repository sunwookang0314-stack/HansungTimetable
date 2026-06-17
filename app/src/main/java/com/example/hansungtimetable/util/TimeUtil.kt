package com.example.hansungtimetable.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// 시간표는 분 단위로 저장 (540 = 9시)
object TimeUtil {

    fun minToHHmm(min: Int): String {
        val h = min / 60
        val m = min % 60
        return String.format(Locale.US, "%02d:%02d", h, m)
    }

    fun toMin(hour: Int, minute: Int) = hour * 60 + minute

    fun formatDateTime(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    // 오늘부터 마감일까지 며칠 남았는지 계산 (음수면 이미 지난 것)
    // 시/분/초까지 비교하면 오늘 마감인데도 0이 안 나와서
    // 양쪽 다 그날 0시로 맞춘 다음 날짜끼리만 뺐다.
    fun daysUntil(dueMillis: Long): Int {
        val today = midnight(System.currentTimeMillis())
        val due = midnight(dueMillis)
        val oneDay = 1000L * 60 * 60 * 24
        return ((due - today) / oneDay).toInt()
    }

    // 그 날의 자정(0시 0분) 시각으로 잘라준다
    private fun midnight(millis: Long): Long {
        val c = Calendar.getInstance()
        c.timeInMillis = millis
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }
}

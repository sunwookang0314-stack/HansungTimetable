package com.example.hansungtimetable.data

// 시간표 한 칸 (과목)
// dayOfWeek: 1=월 ~ 5=금 (Calendar랑 번호 다름 주의)
data class Subject(
    val id: Long = 0,
    val semester: String,
    val name: String,
    val professor: String,
    val room: String,
    val dayOfWeek: Int,
    val startMin: Int,
    val endMin: Int,
    val colorArgb: Int
)

package com.example.hansungtimetable.data

data class Memo(
    val id: Long = 0,
    val title: String,
    val content: String,
    val createdAt: Long,
    val colorArgb: Int
)

package com.example.hansungtimetable.data

data class Assignment(
    val id: Long = 0,
    val subjectId: Long,
    val title: String,
    val dueAtMillis: Long,
    val done: Boolean,
    val memo: String
)

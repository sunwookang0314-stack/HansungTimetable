package com.example.hansungtimetable.data

import android.content.Context

// SharedPreferences 저장 (과목/과제/메모)
// 키 이름 예: subject_count, subject_0_name, subject_0_day ...
object DataManager {

    private const val PREF_NAME = "hansung_data"
    private const val KEY_NEXT_SUBJECT_ID = "next_subject_id"
    private const val KEY_NEXT_ASSIGNMENT_ID = "next_assignment_id"
    private const val KEY_NEXT_MEMO_ID = "next_memo_id"

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // --- 과목 ---

    fun getSubjects(ctx: Context, semester: String): List<Subject> {
        val key = semester.trim()
        return getAllSubjects(ctx)
            .filter { it.semester.trim() == key || it.semester.isBlank() }
            .sortedWith(compareBy({ it.dayOfWeek }, { it.startMin }))
    }

    fun getSubject(ctx: Context, id: Long): Subject? =
        getAllSubjects(ctx).find { it.id == id }

    fun saveSubject(ctx: Context, subject: Subject): Long {
        val list = getAllSubjects(ctx).toMutableList()
        val id = if (subject.id == 0L) nextId(ctx, KEY_NEXT_SUBJECT_ID) else subject.id
        val saved = subject.copy(id = id)
        list.removeAll { it.id == id }
        list.add(saved)
        saveAllSubjects(ctx, list)
        return id
    }

    fun removeSubject(ctx: Context, id: Long) {
        saveAllSubjects(ctx, getAllSubjects(ctx).filter { it.id != id })
    }

    private fun getAllSubjects(ctx: Context): List<Subject> {
        val p = prefs(ctx)
        val count = p.getInt("subject_count", 0)
        val list = mutableListOf<Subject>()
        for (i in 0 until count) {
            list.add(
                Subject(
                    id = p.getLong(key("subject", i, "id"), 0),
                    semester = p.getString(key("subject", i, "semester"), "") ?: "",
                    name = p.getString(key("subject", i, "name"), "") ?: "",
                    professor = p.getString(key("subject", i, "professor"), "") ?: "",
                    room = p.getString(key("subject", i, "room"), "") ?: "",
                    dayOfWeek = p.getInt(key("subject", i, "day"), 1),
                    startMin = p.getInt(key("subject", i, "start"), 540),
                    endMin = p.getInt(key("subject", i, "end"), 600),
                    colorArgb = p.getInt(key("subject", i, "color"), 0xFFBBDEFB.toInt())
                )
            )
        }
        return list
    }

    private fun saveAllSubjects(ctx: Context, list: List<Subject>) {
        val editor = prefs(ctx).edit()
        editor.putInt("subject_count", list.size)
        list.forEachIndexed { i, s ->
            editor.putLong(key("subject", i, "id"), s.id)
            editor.putString(key("subject", i, "semester"), s.semester)
            editor.putString(key("subject", i, "name"), s.name)
            editor.putString(key("subject", i, "professor"), s.professor)
            editor.putString(key("subject", i, "room"), s.room)
            editor.putInt(key("subject", i, "day"), s.dayOfWeek)
            editor.putInt(key("subject", i, "start"), s.startMin)
            editor.putInt(key("subject", i, "end"), s.endMin)
            editor.putInt(key("subject", i, "color"), s.colorArgb)
        }
        editor.commit()
    }

    // --- 과제 ---

    // 과제 목록. keyword 있으면 제목/메모에서 검색.
    // 정렬: 아직 안 끝낸 과제(done=false)를 위로, 그 안에서는 마감 빠른 순.
    fun getAssignments(ctx: Context, keyword: String = ""): List<Assignment> {
        val list = getAllAssignments(ctx)
        if (keyword.isBlank()) return list.sortedWith(compareBy({ it.done }, { it.dueAtMillis }))
        return list.filter {
            it.title.contains(keyword, ignoreCase = true) ||
                    it.memo.contains(keyword, ignoreCase = true)
        }.sortedWith(compareBy({ it.done }, { it.dueAtMillis }))
    }

    fun getAssignment(ctx: Context, id: Long): Assignment? =
        getAllAssignments(ctx).find { it.id == id }

    fun saveAssignment(ctx: Context, assignment: Assignment): Long {
        val list = getAllAssignments(ctx).toMutableList()
        val id = if (assignment.id == 0L) nextId(ctx, KEY_NEXT_ASSIGNMENT_ID) else assignment.id
        val saved = assignment.copy(id = id)
        list.removeAll { it.id == id }
        list.add(saved)
        saveAllAssignments(ctx, list)
        return id
    }

    fun removeAssignment(ctx: Context, id: Long) {
        saveAllAssignments(ctx, getAllAssignments(ctx).filter { it.id != id })
    }

    private fun getAllAssignments(ctx: Context): List<Assignment> {
        val p = prefs(ctx)
        val count = p.getInt("assignment_count", 0)
        val list = mutableListOf<Assignment>()
        for (i in 0 until count) {
            list.add(
                Assignment(
                    id = p.getLong(key("assignment", i, "id"), 0),
                    subjectId = p.getLong(key("assignment", i, "subject"), 0),
                    title = p.getString(key("assignment", i, "title"), "") ?: "",
                    dueAtMillis = p.getLong(key("assignment", i, "due"), 0),
                    done = p.getBoolean(key("assignment", i, "done"), false),
                    memo = p.getString(key("assignment", i, "memo"), "") ?: ""
                )
            )
        }
        return list
    }

    private fun saveAllAssignments(ctx: Context, list: List<Assignment>) {
        val editor = prefs(ctx).edit()
        editor.putInt("assignment_count", list.size)
        list.forEachIndexed { i, a ->
            editor.putLong(key("assignment", i, "id"), a.id)
            editor.putLong(key("assignment", i, "subject"), a.subjectId)
            editor.putString(key("assignment", i, "title"), a.title)
            editor.putLong(key("assignment", i, "due"), a.dueAtMillis)
            editor.putBoolean(key("assignment", i, "done"), a.done)
            editor.putString(key("assignment", i, "memo"), a.memo)
        }
        editor.commit()
    }

    // --- 메모 ---

    fun getMemos(ctx: Context): List<Memo> =
        getAllMemos(ctx).sortedByDescending { it.createdAt }

    fun getMemo(ctx: Context, id: Long): Memo? =
        getAllMemos(ctx).find { it.id == id }

    fun saveMemo(ctx: Context, memo: Memo): Long {
        val list = getAllMemos(ctx).toMutableList()
        val id = if (memo.id == 0L) nextId(ctx, KEY_NEXT_MEMO_ID) else memo.id
        val saved = memo.copy(id = id)
        list.removeAll { it.id == id }
        list.add(saved)
        saveAllMemos(ctx, list)
        return id
    }

    fun removeMemo(ctx: Context, id: Long) {
        saveAllMemos(ctx, getAllMemos(ctx).filter { it.id != id })
    }

    private fun getAllMemos(ctx: Context): List<Memo> {
        val p = prefs(ctx)
        val count = p.getInt("memo_count", 0)
        val list = mutableListOf<Memo>()
        for (i in 0 until count) {
            list.add(
                Memo(
                    id = p.getLong(key("memo", i, "id"), 0),
                    title = p.getString(key("memo", i, "title"), "") ?: "",
                    content = p.getString(key("memo", i, "content"), "") ?: "",
                    createdAt = p.getLong(key("memo", i, "created"), 0),
                    colorArgb = p.getInt(key("memo", i, "color"), 0xFFFFF9C4.toInt())
                )
            )
        }
        return list
    }

    private fun saveAllMemos(ctx: Context, list: List<Memo>) {
        val editor = prefs(ctx).edit()
        editor.putInt("memo_count", list.size)
        list.forEachIndexed { i, m ->
            editor.putLong(key("memo", i, "id"), m.id)
            editor.putString(key("memo", i, "title"), m.title)
            editor.putString(key("memo", i, "content"), m.content)
            editor.putLong(key("memo", i, "created"), m.createdAt)
            editor.putInt(key("memo", i, "color"), m.colorArgb)
        }
        editor.commit()
    }

    // 키 만드는 규칙: subject_0_name, assignment_2_due 이런 식
    private fun key(prefix: String, index: Int, field: String) = "${prefix}_${index}_$field"

    // 새 항목에 줄 id. 1부터 시작해서 하나 줄 때마다 1씩 올려서 저장해 둠.
    // 이렇게 안 하면 항목 지웠을 때 id가 겹칠 수 있어서 따로 관리함.
    private fun nextId(ctx: Context, key: String): Long {
        val p = prefs(ctx)
        val id = p.getLong(key, 1L)
        p.edit().putLong(key, id + 1).commit()
        return id
    }
}

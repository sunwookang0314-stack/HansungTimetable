package com.example.hansungtimetable.ui.timetable

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.hansungtimetable.R
import com.example.hansungtimetable.data.DataManager
import com.example.hansungtimetable.data.Subject
import com.example.hansungtimetable.databinding.ActivitySubjectEditBinding
import com.example.hansungtimetable.util.EditTextUtil
import com.example.hansungtimetable.util.PrefManager
import com.example.hansungtimetable.util.TimeUtil

class SubjectEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubjectEditBinding
    private var editId = 0L
    private var startMin = TimeUtil.toMin(9, 0)
    private var endMin = TimeUtil.toMin(10, 0)

    companion object {
        const val EXTRA_ID = "subject_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubjectEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        EditTextUtil.disableHandwritingBar(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        editId = intent.getLongExtra(EXTRA_ID, 0)

        binding.btnStartTime.setOnClickListener {
            android.app.TimePickerDialog(this, { _, h, m ->
                startMin = TimeUtil.toMin(h, m)
                binding.btnStartTime.text = "시작: ${TimeUtil.minToHHmm(startMin)}"
            }, startMin / 60, startMin % 60, true).show()
        }
        binding.btnEndTime.setOnClickListener {
            android.app.TimePickerDialog(this, { _, h, m ->
                endMin = TimeUtil.toMin(h, m)
                binding.btnEndTime.text = "종료: ${TimeUtil.minToHHmm(endMin)}"
            }, endMin / 60, endMin % 60, true).show()
        }

        binding.btnSave.setOnClickListener { save() }
        binding.btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete)
                .setMessage(R.string.confirm_delete_msg)
                .setPositiveButton(R.string.action_delete) { _, _ ->
                    DataManager.removeSubject(this, editId)
                    Toast.makeText(this, R.string.toast_subject_deleted, Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setNegativeButton(R.string.action_cancel, null)
                .show()
        }

        if (editId > 0) {
            title = getString(R.string.title_edit_subject)
            binding.btnDelete.visibility = android.view.View.VISIBLE
            load(editId)
        } else {
            title = getString(R.string.title_new_subject)
            binding.btnStartTime.text = "시작: ${TimeUtil.minToHHmm(startMin)}"
            binding.btnEndTime.text = "종료: ${TimeUtil.minToHHmm(endMin)}"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun load(id: Long) {
        val s = DataManager.getSubject(this, id) ?: return
        binding.etName.setText(s.name)
        binding.etProfessor.setText(s.professor)
        binding.etRoom.setText(s.room)
        when (s.dayOfWeek) {
            1 -> binding.rgDay.check(R.id.rbMon)
            2 -> binding.rgDay.check(R.id.rbTue)
            3 -> binding.rgDay.check(R.id.rbWed)
            4 -> binding.rgDay.check(R.id.rbThu)
            5 -> binding.rgDay.check(R.id.rbFri)
        }
        startMin = s.startMin
        endMin = s.endMin
        binding.btnStartTime.text = "시작: ${TimeUtil.minToHHmm(startMin)}"
        binding.btnEndTime.text = "종료: ${TimeUtil.minToHHmm(endMin)}"
    }

    private fun save() {
        // 강의명은 필수, 비어있으면 저장 막기
        val name = binding.etName.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(this, R.string.toast_subject_name_required, Toast.LENGTH_SHORT).show()
            return
        }
        // 종료가 시작보다 빠르면 시간표가 이상하게 그려져서 막음
        if (endMin <= startMin) {
            Toast.makeText(this, R.string.toast_invalid_time, Toast.LENGTH_SHORT).show()
            return
        }

        val day = when (binding.rgDay.checkedRadioButtonId) {
            R.id.rbMon -> 1
            R.id.rbTue -> 2
            R.id.rbWed -> 3
            R.id.rbThu -> 4
            R.id.rbFri -> 5
            else -> 1
        }

        // 색은 따로 고르게 안 하고 요일별로 정해진 색을 자동으로 줬다.
        // (색 고르는 화면까지 만들기엔 시간이 부족해서 일단 이렇게)
        val color = when (day) {
            1 -> getColor(R.color.subject_1)
            2 -> getColor(R.color.subject_2)
            3 -> getColor(R.color.subject_3)
            4 -> getColor(R.color.subject_4)
            5 -> getColor(R.color.subject_5)
            else -> getColor(R.color.subject_1)
        }

        DataManager.saveSubject(
            this,
            Subject(
                id = editId,
                semester = PrefManager.getSemester(this),
                name = name,
                professor = binding.etProfessor.text.toString().trim(),
                room = binding.etRoom.text.toString().trim(),
                dayOfWeek = day,
                startMin = startMin,
                endMin = endMin,
                colorArgb = color
            )
        )
        Toast.makeText(this, R.string.toast_subject_saved, Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
    }
}

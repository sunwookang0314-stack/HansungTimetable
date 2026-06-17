package com.example.hansungtimetable.ui.assignment

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.hansungtimetable.R
import com.example.hansungtimetable.data.Assignment
import com.example.hansungtimetable.data.DataManager
import com.example.hansungtimetable.databinding.ActivityAssignmentEditBinding
import com.example.hansungtimetable.util.EditTextUtil
import com.example.hansungtimetable.util.PrefManager
import com.example.hansungtimetable.util.TimeUtil
import java.util.Calendar

class AssignmentEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAssignmentEditBinding
    private var editId = 0L
    private var dueMillis = System.currentTimeMillis()
    private val subjectIds = mutableListOf<Long>()
    private val subjectNames = mutableListOf<String>()
    private var subjectIdx = 0

    companion object {
        const val EXTRA_ID = "assignment_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssignmentEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        EditTextUtil.disableHandwritingBar(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        editId = intent.getLongExtra(EXTRA_ID, 0)

        loadSubjects()

        binding.btnPickSubject.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.label_subject)
                .setSingleChoiceItems(subjectNames.toTypedArray(), subjectIdx) { dlg, which ->
                    subjectIdx = which
                    binding.btnPickSubject.text = "과목: ${subjectNames[which]}"
                    dlg.dismiss()
                }
                .show()
        }

        binding.btnDueDate.setOnClickListener {
            val cal = Calendar.getInstance().apply { timeInMillis = dueMillis }
            // TODO: 마감 전날에 알림 보내주는 기능 넣고 싶은데 아직 수업에서 안 배워서 보류
            android.app.DatePickerDialog(
                this,
                { _, y, m, d ->
                    // 마감일은 그날 밤 23:59까지로 잡았다 (날짜만 고르니까 시간은 끝으로)
                    dueMillis = Calendar.getInstance().apply {
                        set(y, m, d, 23, 59, 0)
                    }.timeInMillis
                    binding.btnDueDate.text = "마감: ${TimeUtil.formatDateTime(dueMillis)}"
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.btnSave.setOnClickListener { save() }
        binding.btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete)
                .setMessage(R.string.confirm_delete_msg)
                .setPositiveButton(R.string.action_delete) { _, _ ->
                    DataManager.removeAssignment(this, editId)
                    finish()
                }
                .setNegativeButton(R.string.action_cancel, null)
                .show()
        }

        if (editId > 0) {
            title = getString(R.string.title_edit_assignment)
            binding.btnDelete.visibility = android.view.View.VISIBLE
            load(editId)
        } else {
            title = getString(R.string.title_new_assignment)
            binding.btnDueDate.text = "마감: ${TimeUtil.formatDateTime(dueMillis)}"
            binding.btnPickSubject.text = "과목: ${subjectNames[0]}"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadSubjects() {
        subjectIds.clear()
        subjectNames.clear()
        subjectIds.add(0)
        subjectNames.add(getString(R.string.label_no_subject))
        DataManager.getSubjects(this, PrefManager.getSemester(this)).forEach {
            subjectIds.add(it.id)
            subjectNames.add(it.name)
        }
    }

    private fun load(id: Long) {
        val a = DataManager.getAssignment(this, id) ?: return
        binding.etTitle.setText(a.title)
        binding.etMemo.setText(a.memo)
        binding.cbDone.isChecked = a.done
        dueMillis = a.dueAtMillis
        subjectIdx = subjectIds.indexOf(a.subjectId).coerceAtLeast(0)
        binding.btnDueDate.text = "마감: ${TimeUtil.formatDateTime(dueMillis)}"
        binding.btnPickSubject.text = "과목: ${subjectNames[subjectIdx]}"
    }

    private fun save() {
        val title = binding.etTitle.text.toString().trim()
        if (title.isEmpty()) {
            Toast.makeText(this, R.string.toast_title_required, Toast.LENGTH_SHORT).show()
            return
        }

        DataManager.saveAssignment(
            this,
            Assignment(
                id = editId,
                subjectId = subjectIds[subjectIdx],
                title = title,
                dueAtMillis = dueMillis,
                done = binding.cbDone.isChecked,
                memo = binding.etMemo.text.toString().trim()
            )
        )
        Toast.makeText(this, R.string.toast_assignment_saved, Toast.LENGTH_SHORT).show()
        finish()
    }
}

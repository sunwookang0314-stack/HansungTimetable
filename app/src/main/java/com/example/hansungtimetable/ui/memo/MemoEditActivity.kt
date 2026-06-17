package com.example.hansungtimetable.ui.memo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.hansungtimetable.R
import com.example.hansungtimetable.data.DataManager
import com.example.hansungtimetable.data.Memo
import com.example.hansungtimetable.databinding.ActivityMemoEditBinding
import com.example.hansungtimetable.util.EditTextUtil

class MemoEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMemoEditBinding
    private var editId = 0L
    private var createdAt = System.currentTimeMillis()

    companion object {
        const val EXTRA_ID = "memo_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemoEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        EditTextUtil.disableHandwritingBar(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        editId = intent.getLongExtra(EXTRA_ID, 0)

        binding.btnSave.setOnClickListener { save() }
        binding.btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete)
                .setMessage(R.string.confirm_delete_msg)
                .setPositiveButton(R.string.action_delete) { _, _ ->
                    DataManager.removeMemo(this, editId)
                    finish()
                }
                .setNegativeButton(R.string.action_cancel, null)
                .show()
        }

        if (editId > 0) {
            title = getString(R.string.title_edit_memo)
            binding.btnDelete.visibility = android.view.View.VISIBLE
            val m = DataManager.getMemo(this, editId) ?: return
            binding.etTitle.setText(m.title)
            binding.etContent.setText(m.content)
            createdAt = m.createdAt
        } else {
            title = getString(R.string.title_new_memo)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun save() {
        val title = binding.etTitle.text.toString().trim()
        if (title.isEmpty()) {
            Toast.makeText(this, R.string.toast_title_required, Toast.LENGTH_SHORT).show()
            return
        }

        DataManager.saveMemo(
            this,
            Memo(
                id = editId,
                title = title,
                content = binding.etContent.text.toString().trim(),
                createdAt = createdAt,
                colorArgb = getColor(R.color.memo_1)
            )
        )
        Toast.makeText(this, R.string.toast_memo_saved, Toast.LENGTH_SHORT).show()
        finish()
    }
}

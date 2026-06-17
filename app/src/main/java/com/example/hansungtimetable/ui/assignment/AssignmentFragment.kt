package com.example.hansungtimetable.ui.assignment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hansungtimetable.data.DataManager
import com.example.hansungtimetable.databinding.FragmentAssignmentBinding

class AssignmentFragment : Fragment() {

    private var _binding: FragmentAssignmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AssignmentAdapter
    private var keyword = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssignmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = AssignmentAdapter(emptyList()) { a ->
            startActivity(Intent(requireContext(), AssignmentEditActivity::class.java).apply {
                putExtra(AssignmentEditActivity.EXTRA_ID, a.id)
            })
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )
        binding.recyclerView.adapter = adapter
        reload()
    }

    override fun onResume() {
        super.onResume()
        reload()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun onSearch(q: String) {
        keyword = q
        reload()
    }

    private fun reload() {
        if (_binding == null) return
        val list = DataManager.getAssignments(requireContext(), keyword)
        adapter.updateData(list)
        binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
    }
}

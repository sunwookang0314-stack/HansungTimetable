package com.example.hansungtimetable

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.hansungtimetable.databinding.ActivityMainBinding
import com.example.hansungtimetable.ui.MainPagerAdapter
import com.example.hansungtimetable.ui.assignment.AssignmentEditActivity
import com.example.hansungtimetable.ui.assignment.AssignmentFragment
import com.example.hansungtimetable.ui.memo.MemoEditActivity
import com.example.hansungtimetable.ui.settings.SettingsActivity
import com.example.hansungtimetable.ui.timetable.SubjectEditActivity
import com.example.hansungtimetable.ui.timetable.TimetableFragment
import com.example.hansungtimetable.util.EditTextUtil
import com.example.hansungtimetable.util.PrefManager
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle

    // 과목 추가 화면 갔다 오면 시간표 다시 그려야 함.
    // 처음엔 그냥 startActivity 썼는데 돌아와도 시간표가 그대로여서
    // ActivityResultLauncher로 바꿨더니 됐다.
    private val subjectEditLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        refreshTimetable()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        drawerToggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.drawer_open, R.string.drawer_close
        )
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        binding.navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_timetable -> binding.viewPager.currentItem = 0
                R.id.nav_assignment -> binding.viewPager.currentItem = 1
                R.id.nav_memo -> binding.viewPager.currentItem = 2
                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
                R.id.nav_homepage -> {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.hansung.ac.kr")))
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        binding.viewPager.adapter = MainPagerAdapter(this)
        binding.viewPager.offscreenPageLimit = 2
        // ViewPager2랑 위쪽 탭(TabLayout)을 연결해서 탭 글자를 붙여준다.
        // TabLayoutMediator는 수업에서 안 배워서 안드로이드 공식 문서 보고 따라 함.
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, pos ->
            tab.text = when (pos) {
                0 -> getString(R.string.tab_timetable)
                1 -> getString(R.string.tab_assignment)
                2 -> getString(R.string.tab_memo)
                else -> ""
            }
        }.attach()

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                invalidateOptionsMenu()
                if (position == 0) refreshTimetable()
            }
        })

        updateSemesterHeader()
    }

    override fun onResume() {
        super.onResume()
        updateSemesterHeader()
        refreshTimetable()
    }

    private fun refreshTimetable() {
        findTimetableFragment()?.refreshGrid()
    }

    // ViewPager2가 만든 프래그먼트를 직접 찾아서 시간표를 새로고침한다.
    // ViewPager2는 각 페이지 프래그먼트를 f0, f1 같은 태그로 붙여서
    // 0번 탭(시간표)은 f0으로 찾으면 된다. (이거 알아내는 데 한참 걸림)
    // 혹시 못 찾으면 fragments 목록에서 한 번 더 뒤진다.
    private fun findTimetableFragment(): TimetableFragment? {
        val byTag = supportFragmentManager.findFragmentByTag("f0")
        if (byTag is TimetableFragment) return byTag
        for (f in supportFragmentManager.fragments) {
            if (f is TimetableFragment) return f
        }
        return null
    }

    private fun updateSemesterHeader() {
        val header = binding.navigationView.getHeaderView(0)
        header.findViewById<TextView>(R.id.tvSemester).text = PrefManager.getSemester(this)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.queryHint = getString(R.string.action_search)
        searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)?.let {
            EditTextUtil.disableHandwritingBar(it)
        }
        // 검색어가 바뀔 때마다 과제 목록을 필터링
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                findAssignmentFragment()?.onSearch(query ?: "")
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                findAssignmentFragment()?.onSearch(newText ?: "")
                return true
            }
        })
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // 검색 아이콘은 과제 탭(1번)에서만 보이게
        menu.findItem(R.id.action_search)?.isVisible = binding.viewPager.currentItem == 1
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) return true
        if (item.itemId == R.id.action_add) {
            // + 버튼은 지금 보고 있는 탭에 맞는 추가 화면을 띄운다
            when (binding.viewPager.currentItem) {
                0 -> subjectEditLauncher.launch(Intent(this, SubjectEditActivity::class.java))
                1 -> startActivity(Intent(this, AssignmentEditActivity::class.java))
                2 -> startActivity(Intent(this, MemoEditActivity::class.java))
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun findAssignmentFragment(): AssignmentFragment? =
        supportFragmentManager.fragments.filterIsInstance<AssignmentFragment>().firstOrNull()
}

package com.example.hansungtimetable.util

import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.EditText

// Android 14+ / Gboard 손글씨 입력(텍스트 필드에 쓰기) 옆 세로 바 끄기
object EditTextUtil {

    fun disableHandwritingBar(root: View) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return
        when (root) {
            is EditText -> {
                root.setAutoHandwritingEnabled(false)
                root.privateImeOptions = "disableToolbar=true"
            }
            is ViewGroup -> {
                for (i in 0 until root.childCount) {
                    disableHandwritingBar(root.getChildAt(i))
                }
            }
        }
    }
}

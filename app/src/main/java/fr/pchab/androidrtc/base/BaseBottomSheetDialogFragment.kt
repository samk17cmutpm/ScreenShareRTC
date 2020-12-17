package fr.pchab.androidrtc.base

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {
    fun initToolBar(toolbar: Toolbar, icon: Int? = null) {
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        toolbar.setContentInsetsAbsolute(0, 0)
        if (icon != null) {
            (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
            toolbar.setNavigationIcon(icon)
        }
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
    }
}
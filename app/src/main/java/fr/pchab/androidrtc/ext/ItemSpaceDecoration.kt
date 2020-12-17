package fr.pchab.androidrtc.ext

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ItemSpaceDecoration(
    private val leftAndRight: Int,
    private val topAndBottom: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.set(
            leftAndRight,
            topAndBottom,
            leftAndRight,
            topAndBottom
        )
    }
}
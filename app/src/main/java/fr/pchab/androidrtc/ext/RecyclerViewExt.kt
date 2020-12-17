package fr.pchab.androidrtc.ext

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import fr.pchab.androidrtc.R

fun RecyclerView.emptyRC(
    view: Int,
    inflater: LayoutInflater,
    message: String?,
    ic: Int,
    color: Int? = null
) : View? {
    val emptyView = inflater?.inflate(view, this.parent as ViewGroup, false)

    val emptyText = emptyView?.findViewById<TextView>(R.id.empty_text)
    emptyText?.text = message

    val emptyIc = emptyView?.findViewById<AppCompatImageView>(R.id.empty_ic)
    emptyIc?.setImageResource(ic)

    color?.let { tint ->
        emptyIc?.setColorFilter(ContextCompat.getColor(context, tint))
    }
    (this?.adapter as BaseQuickAdapter<*, *>).setEmptyView( emptyView)
    return emptyView
}

fun RecyclerView.notFound(
    inflater: LayoutInflater,
    view: Int,
    message: String?,
    ic: Int
) {
    val emptyView = inflater?.inflate(view, this.parent as ViewGroup, false)

    val emptyText = emptyView?.findViewById<TextView>(R.id.empty_text)
    emptyText?.text = message

    val emptyIc = emptyView?.findViewById<AppCompatImageView>(R.id.empty_ic)
    emptyIc?.setImageResource(ic)

    (this?.adapter as BaseQuickAdapter<*, *>).setEmptyView( emptyView)
}

fun SwipeRefreshLayout.setColors() {
    this.setColorSchemeResources(
        android.R.color.holo_blue_bright,
        android.R.color.holo_green_light,
        android.R.color.holo_orange_light,
        android.R.color.holo_red_light
    )
}
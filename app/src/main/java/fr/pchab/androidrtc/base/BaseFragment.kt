package fr.pchab.androidrtc.base

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment

open class BaseFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun showFragment(
        containerId: Int,
        fragment: Fragment?
    ) {
        fragment?.let {
            val ft = childFragmentManager.beginTransaction()

            ft.replace(containerId, it)
            ft.commitAllowingStateLoss()
        }
    }

    fun addFragment(
        view: View?,
        fragment: Fragment?,
        tag: String?
    ) {
        fragment?.let { frag ->
            val ft = childFragmentManager.beginTransaction()
            view?.id?.let { viewId ->
                ft.add(viewId, frag, tag)
                ft.commitAllowingStateLoss()
            }
        }
    }

    fun generateView(
        fragment: Fragment?,
        id: Int
    ) : View? {
        val frameLayout = FrameLayout(context!!)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            frameLayout.id = View.generateViewId()
        } else {
            frameLayout.id = id
        }

        fragment?.let { frag ->
            val ft = childFragmentManager.beginTransaction()
            ft.replace(frameLayout.id, frag)
            ft.commitAllowingStateLoss()
        }

        return frameLayout
    }

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
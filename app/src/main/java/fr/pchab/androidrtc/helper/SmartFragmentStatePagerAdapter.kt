package fr.pchab.androidrtc.helper

import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager

abstract class SmartFragmentStatePagerAdapter(fm: FragmentManager) : androidx.fragment.app.FragmentStatePagerAdapter(fm) {

    private val registeredFragments = SparseArray<androidx.fragment.app.Fragment>()

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position)
        registeredFragments.put(position, fragment as androidx.fragment.app.Fragment?)
        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        super.destroyItem(container, position, `object`)
    }

    fun getRegisteredFragment(positon: Int) : androidx.fragment.app.Fragment {
        return registeredFragments.get(positon)
    }
}
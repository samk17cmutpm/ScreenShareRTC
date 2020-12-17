package fr.pchab.androidrtc.ext

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.BackgroundColorSpan
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import fr.pchab.androidrtc.R

const val FADING_DURATION = 200L
const val FADING_DURATION_SMALL = 100L
const val FADING_DURATION_TINY = 50L

fun View.visible() {
    visibility = View.VISIBLE
    isClickable = true
}

fun View.invisible() {
    visibility = View.INVISIBLE
    isClickable = false
}

fun View.gone() {
    visibility = View.GONE
    isClickable = false
}

fun View.hideWithFading(duration: Long = FADING_DURATION) {
    isClickable = false
    alpha = 1f
    animate().alpha(0f)
        .setDuration(duration)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                isVisible = false
                gone()
            }
            override fun onAnimationCancel(animation: Animator?) {
                isVisible = false
                gone()
            }
        })
}

fun View.showWithFading(duration: Long = FADING_DURATION) {
    alpha = 0f
    isVisible = true
    animate().alpha(1f).duration = duration
    visible()
}

fun TextView.setHighlight(
    textToHighlight: String
) {
    if (TextUtils.isEmpty(textToHighlight)) return

    val textToHighlightLowerCase = textToHighlight?.toLowerCase()
    val tvt = text.toString().toLowerCase()
    var ofe = tvt?.indexOf(textToHighlightLowerCase, 0)
    val wordToSpan = SpannableString(text)
    var ofs = 0
    while (ofs < tvt.length && ofe != -1) {
        ofe = tvt.indexOf(textToHighlightLowerCase, ofs)
        if (ofe == -1)
            break
        else {
            // set color here
            wordToSpan.setSpan(
                BackgroundColorSpan(ContextCompat.getColor(context!!, R.color.design_default_color_primary)),
                ofe,
                ofe + textToHighlightLowerCase.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setText(wordToSpan, TextView.BufferType.SPANNABLE)
        }
        ofs = ofe + 1
    }
}

fun TextView.setRequired() {
    if (text?.takeLast(1) != "*")
        text = "$text *"
}

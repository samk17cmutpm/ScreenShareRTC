package fr.pchab.androidrtc.helper

import android.content.Context
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener

open class OnSwipeTouchListener(ctx: Context?) : OnTouchListener {
    companion object {
        private const val SWIPE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
    }
    private val gestureDetector: GestureDetector

    var listener: OnListener? = null

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    private inner class GestureListener : SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            var result = false
            try {
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(
                            velocityX
                        ) > SWIPE_VELOCITY_THRESHOLD
                    ) {
                        if (diffX > 0) {
                            listener?.onSwipeRight()
                        } else {
                            listener?.onSwipeLeft()
                        }
                        result = true
                    }
                } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(
                        velocityY
                    ) > SWIPE_VELOCITY_THRESHOLD
                ) {
                    if (diffY > 0) {
                        listener?.onSwipeBottom()
                    } else {
                        listener?.onSwipeTop()
                    }
                    result = true
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            return result
        }
    }

    interface OnListener {
        fun onSwipeRight()
        fun onSwipeLeft()
        fun onSwipeTop()
        fun onSwipeBottom()
    }
    init {
        gestureDetector = GestureDetector(
            ctx,
            GestureListener()
        )
    }
}
package com.example.languageflashcards

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.HorizontalScrollView

class LockableHorizontalScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : HorizontalScrollView(context, attrs) {

    var isScrollingEnabled: Boolean = false

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return isScrollingEnabled && super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return isScrollingEnabled && super.onTouchEvent(ev)
    }
}

package ru.skillbranch.skillarticles.ui.custom.behaviors

import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.math.MathUtils
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import ru.skillbranch.skillarticles.ui.custom.Bottombar

class BottombarBehavior : CoordinatorLayout.Behavior<Bottombar>() {
    private var topBound = 0
    private var bottomBound = 0
    private var interceptingEvents = false
    lateinit var dragHelper: ViewDragHelper

    override fun onLayoutChild(parent: CoordinatorLayout, child: Bottombar, layoutDirection: Int): Boolean {
        // onLayout child on parent
        parent.onLayoutChild(child, layoutDirection)
        if (!::dragHelper.isInitialized) initialize(parent, child)

        // If open add offset
        if (child.isClose) ViewCompat.offsetTopAndBottom(child, bottomBound - topBound)

        // Handle onLayout manually
        return true
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: Bottombar, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    // Not call if visibility gone
    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: Bottombar, target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        // dy < 0 scroll down
        // dy > 0 scroll up
        val offset = MathUtils.clamp(child.translationY + dy, 0f, child.minHeight.toFloat())

        if (child.isClose && offset != child.translationY) {
            child.translationY = offset
            Log.d("My_BottombarBehavior", "dy: $dy translationY: ${child.translationY}")
        }

        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
    }

    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: Bottombar, ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            // If action down in child area -> intercept
            MotionEvent.ACTION_DOWN -> interceptingEvents = parent.isPointInChildBounds(child, ev.x.toInt(), ev.y.toInt())
            // If action cancel or up -> not intercept
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> interceptingEvents = false
        }

        return if (interceptingEvents) dragHelper.shouldInterceptTouchEvent(ev) else false
    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: Bottombar, ev: MotionEvent): Boolean {
        // Delegate handle touch event to drag helper
        dragHelper.processTouchEvent(ev)
        return true
    }

    private fun initialize(parent: CoordinatorLayout, child: Bottombar) {
        
    }
}
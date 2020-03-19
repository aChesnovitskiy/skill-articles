package ru.skillbranch.skillarticles.ui.custom.behaviors

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import ru.skillbranch.skillarticles.R
import kotlin.math.max
import kotlin.math.min

class SubmenuBehavior<V : View>(context: Context, attrs: AttributeSet) :
    CoordinatorLayout.Behavior<V>(context, attrs) {
    private val appContext = context

    // Instruct CoordinatorLayout that we care about vertical scroll events
    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    // Get the scroll event before the nested scrolling child (target) receives it
    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)

        // Calculate the ratio between height and width
        val ratio = child.width / child.height

        // Get margins of submenu
        val margin = appContext.resources.getDimension(R.dimen.spacing_small_8)

        // Set and clamp translationY (vertical location of this view relative to its top position)
        child.translationY =
            max(0f, min(child.height.toFloat() + margin, child.translationY + dy))

        // Set and clamp translationX (horizontal location of this view relative to its left position)
        child.translationX =
            max(0f, min(child.width.toFloat() + margin, child.translationX + dy * ratio))

        Log.d(
            "My_SubmenuBehavior",
            "translationY: ${child.translationY}, translationX: ${child.translationX}"
        )
    }
}
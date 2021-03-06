package ru.skillbranch.skillarticles.extensions

import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.core.view.iterator
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.navigation.NavDestination
import com.google.android.material.bottomnavigation.BottomNavigationView
import ru.skillbranch.skillarticles.R

fun View.setMarginOptionally(
    left: Int = marginLeft,
    top: Int = marginTop,
    right: Int = marginRight,
    bottom: Int = marginBottom
) {
    val marginParams: ViewGroup.MarginLayoutParams =
        this.layoutParams as ViewGroup.MarginLayoutParams
    marginParams.setMargins(left, top, right, bottom)
}

fun View.setPaddingOptionally(
    left: Int = paddingLeft,
    right: Int = paddingRight,
    top: Int = paddingTop,
    bottom: Int = paddingBottom
) {
    setPadding(left, top, right, bottom)
}

fun BottomNavigationView.selectDestination(destination: NavDestination) {
    for (menuItem in menu.iterator()) {
        if (menuItem.itemId == destination.id) menuItem.isChecked = true
    }
}

fun BottomNavigationView.selectItem(itemId: Int?){
    itemId?: return
    for (item in menu.iterator()) {
        if(item.itemId == itemId) {
            item.isChecked = true
            break
        }
    }
}
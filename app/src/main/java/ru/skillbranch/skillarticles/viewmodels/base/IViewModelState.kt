package ru.skillbranch.skillarticles.viewmodels.base

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle

interface IViewModelState {
    /**
     * Override this if need to save state in bundle
     */
    fun save(outState: SavedStateHandle) {
        // Default empty implementation
    }

    /**
     * Override this if need to restore state from bundle
     */
    fun restore(savedState: SavedStateHandle): IViewModelState {
        // Default empty implementation
        return this
    }
}
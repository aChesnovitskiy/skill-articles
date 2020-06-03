package ru.skillbranch.skillarticles.viewmodels.base

import androidx.lifecycle.SavedStateHandle

/**
 * Interface for saving and restoring state of ViewModel in bundle
 */
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
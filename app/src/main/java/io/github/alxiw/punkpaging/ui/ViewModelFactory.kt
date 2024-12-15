package io.github.alxiw.punkpaging.ui

import android.app.Application
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import io.github.alxiw.punkpaging.data.BeersRepository
import io.github.alxiw.punkpaging.ui.beers.BeersViewModel
import javax.inject.Inject

class ViewModelFactory @Inject constructor(
    private val repository: BeersRepository,
    private val application: Application,
    owner: SavedStateRegistryOwner
) : AbstractSavedStateViewModelFactory(owner, null) {

    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        if (modelClass.isAssignableFrom(BeersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BeersViewModel(repository, application, handle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

package io.github.alxiw.punkpaging.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.alxiw.punkpaging.data.BeersRepository
import io.github.alxiw.punkpaging.ui.beers.BeersViewModel
import javax.inject.Inject

class ViewModelFactory @Inject constructor(
    private val repository: BeersRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(BeersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BeersViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

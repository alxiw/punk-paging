package io.github.alxiw.punkpaging.ui.beers

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.alxiw.punkpaging.data.BeersRepository
import io.github.alxiw.punkpaging.data.model.Beer
import kotlinx.coroutines.flow.Flow

class BeersViewModel(
    private val repository: BeersRepository,
    application: Application
) : AndroidViewModel(application) {

    var clickedBeer: Beer? = null

    var currentQuery: String = ""

    private var currentSearchResult: Flow<PagingData<Beer>>? = null

    @ExperimentalPagingApi
    fun searchBeers(query: String): Flow<PagingData<Beer>> {
        val lastResult = currentSearchResult
        if (query == currentQuery && lastResult != null) {
            return lastResult
        }
        currentQuery = query
        val newResult = repository.fetchBeers(currentQuery).cachedIn(viewModelScope)
        currentSearchResult = newResult
        return newResult
    }
}

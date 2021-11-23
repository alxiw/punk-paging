package io.github.alxiw.punkpaging.data

import android.content.SharedPreferences
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import io.github.alxiw.punkpaging.data.api.PunkApi
import io.github.alxiw.punkpaging.data.model.Beer
import io.github.alxiw.punkpaging.data.db.PunkDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BeersRepository @Inject constructor(
    private val api: PunkApi,
    private val database: PunkDatabase,
    private val prefs: SharedPreferences
) {

    @OptIn(ExperimentalPagingApi::class, ExperimentalCoroutinesApi::class)
    fun fetchBeers(query: String?): Flow<PagingData<Beer>> {
        return Pager(
                config = PagingConfig(
                        pageSize = 20,
                        enablePlaceholders = false,
                        prefetchDistance = 2
                ),
                remoteMediator = BeersRemoteMediator(
                        if (query.isNullOrEmpty()) null else query.replace(' ', '_'),
                        api,
                        database
                ),
                pagingSourceFactory = {
                    val dbQuery = if (query.isNullOrEmpty()) "" else query
                    database.beersDao().getBeers("%$dbQuery%".replace(' ', '%'))
                }
        ).flow
    }
}

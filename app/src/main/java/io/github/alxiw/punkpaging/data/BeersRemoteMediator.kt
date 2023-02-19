package io.github.alxiw.punkpaging.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import io.github.alxiw.punkpaging.data.db.PunkDatabase
import io.github.alxiw.punkpaging.data.api.PunkApi
import io.github.alxiw.punkpaging.data.db.keys.RemoteKey
import io.github.alxiw.punkpaging.data.model.Beer
import retrofit2.HttpException
import java.io.IOException

@ExperimentalPagingApi
class BeersRemoteMediator(
    private val query: String?,
    private val api: PunkApi,
    private val database: PunkDatabase
) : RemoteMediator<Int, Beer>() {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Beer>): MediatorResult {

        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKey = getRemoteKeyClosestToCurrentPosition(state)
                remoteKey?.nextKey?.minus(1) ?: STARTING_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                val remoteKey = getRemoteKeyForFirstItem(state)
                // If remoteKeys is null, that means the refresh result is not in the database yet.
                // We can return Success with `endOfPaginationReached = false` because Paging
                // will call this method again if RemoteKeys becomes non-null.
                // If remoteKeys is NOT NULL but its prevKey is null, that means we've reached
                // the end of pagination for prepend.
                val prevKey = remoteKey?.prevKey ?: return MediatorResult.Success(remoteKey != null)
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                // If remoteKeys is null, that means the refresh result is not in the database yet.
                // We can return Success with `endOfPaginationReached = false` because Paging
                // will call this method again if RemoteKeys becomes non-null.
                // If remoteKeys is NOT NULL but its prevKey is null, that means we've reached
                // the end of pagination for append.
                val nextKey = remoteKeys?.nextKey ?: return MediatorResult.Success(remoteKeys != null)
                nextKey
            }
        }

        return try {
            val name = if (query.isNullOrEmpty()) null else query
            val beers = api.fetchBeers(
                beerName = name,
                page = page,
                pageSize = state.config.pageSize
            )
            val endOfPaginationReached = beers.isEmpty()
            insertIntoDatabase(
                page = page,
                loadType = loadType,
                beers = beers,
                endOfPaginationReached = endOfPaginationReached
            )
            MediatorResult.Success(endOfPaginationReached)
        } catch (exception: IOException) {
            MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            MediatorResult.Error(exception)
        }
    }

    private suspend fun insertIntoDatabase(
        page: Int,
        loadType: LoadType,
        beers: List<Beer>,
        endOfPaginationReached: Boolean
    ) {
        database.withTransaction {
            // clear all tables in the database
            if (loadType == LoadType.REFRESH) {
                database.remoteKeysDao().clearRemoteKeys()
                database.beersDao().deleteAll()
            }
            val prevKey = if (page == STARTING_PAGE_INDEX) null else page - 1
            val nextKey = if (endOfPaginationReached) null else page + 1
            val keys = beers.map {
                RemoteKey(beerId = it.id, prevKey = prevKey, nextKey = nextKey)
            }
            database.remoteKeysDao().insertAll(keys)
            database.beersDao().insertAll(beers)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, Beer>): RemoteKey? {
        // Get the last page that was retrieved, that contained items.
        // From that last page, get the last item
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
                ?.let { beer ->
                    // Get the remote keys of the last item retrieved
                    database.remoteKeysDao().remoteKeysBeerId(beer.id)
                }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, Beer>): RemoteKey? {
        // Get the first page that was retrieved, that contained items.
        // From that first page, get the first item
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
                ?.let { beer ->
                    // Get the remote keys of the first items retrieved
                    database.remoteKeysDao().remoteKeysBeerId(beer.id)
                }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
            state: PagingState<Int, Beer>
    ): RemoteKey? {
        // The paging library is trying to load data after the anchor position
        // Get the item closest to the anchor position
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { beerId ->
                database.remoteKeysDao().remoteKeysBeerId(beerId)
            }
        }
    }

    companion object {
        const val STARTING_PAGE_INDEX = 1
    }
}

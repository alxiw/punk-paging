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
import java.io.InvalidObjectException

@OptIn(ExperimentalPagingApi::class)
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
                ?: throw InvalidObjectException("Remote key and the prevKey should not be null")
                // The LoadType is PREPEND so some data was loaded before,
                // so we should have been able to get remote keys
                // If the remoteKeys are null, then we're an invalid state and we have a bug
                // If the previous key is null, then we can't request more data
                if (remoteKey.prevKey == null) {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                remoteKey.prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                if (remoteKeys?.nextKey == null) {
                    val remoteKey = getRemoteKeyClosestToCurrentPosition(state)
                    //return MediatorResult.Error(InvalidObjectException("Remote key should not be null for $loadType"))
                    //throw InvalidObjectException("Remote key should not be null for $loadType")
                    remoteKey?.nextKey?.minus(1) ?: STARTING_PAGE_INDEX
                } else {
                    remoteKeys.nextKey
                }
            }
        }

        try {
            val beers = if (query.isNullOrEmpty()) {
                api.fetchBeers(null, page, state.config.pageSize)
            } else {
                api.fetchBeers(query, page, state.config.pageSize)
            }
            val endOfPaginationReached = beers.isEmpty()
            insertIntoDatabase(page, loadType, beers, endOfPaginationReached)
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
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

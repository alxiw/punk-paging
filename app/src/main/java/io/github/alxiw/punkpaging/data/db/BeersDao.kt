package io.github.alxiw.punkpaging.data.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.alxiw.punkpaging.data.model.Beer

@Dao
interface BeersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(beers: List<Beer>)

    @Query("SELECT * FROM beers WHERE name LIKE :query")
    fun getBeers(query: String): PagingSource<Int, Beer>

    @Query("DELETE FROM beers")
    suspend fun deleteAll()
}

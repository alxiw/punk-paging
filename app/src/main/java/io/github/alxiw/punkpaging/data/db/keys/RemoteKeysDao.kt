package io.github.alxiw.punkpaging.data.db.keys

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RemoteKeysDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKeys: List<RemoteKey>)

    @Query("SELECT * FROM remote_keys WHERE beerId = :beerId")
    suspend fun remoteKeysBeerId(beerId: Int): RemoteKey?

    @Query("DELETE FROM remote_keys")
    suspend fun clearRemoteKeys()
}

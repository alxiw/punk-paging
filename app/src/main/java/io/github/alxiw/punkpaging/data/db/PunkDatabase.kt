package io.github.alxiw.punkpaging.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import io.github.alxiw.punkpaging.data.db.keys.RemoteKey
import io.github.alxiw.punkpaging.data.db.keys.RemoteKeysDao
import io.github.alxiw.punkpaging.data.model.Beer

@Database(
        entities = [Beer::class, RemoteKey::class],
        version = 1,
        exportSchema = false
)
abstract class PunkDatabase : RoomDatabase() {

    abstract fun beersDao(): BeersDao
    abstract fun remoteKeysDao(): RemoteKeysDao
}

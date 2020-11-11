package io.github.alxiw.punkpaging.data.db.keys

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKey(
        @PrimaryKey
        val beerId: Int,
        val prevKey: Int?,
        val nextKey: Int?
)

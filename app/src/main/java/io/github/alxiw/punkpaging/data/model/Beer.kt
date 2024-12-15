package io.github.alxiw.punkpaging.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * {
 *     "id": 1,
 *     "name": "Punk IPA 2007 - 2010",
 *     "tagline": "Post Modern Classic. Spiky. Tropical. Hoppy.",
 *     "first_brewed": "04/2007",
 *     "description": "Our flagship beer that kick started the craft beer revolution. This is
 *         James and Martin's original take on an American IPA, subverted with punchy New Zealand
 *         hops. Layered with new world hops to create an all-out riot of grapefruit, pineapple
 *         and lychee before a spiky, mouth-puckering bitter finish.",
 *     "image": "001.png",
 *     "abv": 6.0
 * }
 */

@Entity(tableName = "beers")
data class Beer(
        @PrimaryKey
        @ColumnInfo(name = "id")
        @SerializedName("id")
        val id: Int,
        @ColumnInfo(name = "name")
        @SerializedName("name")
        val name: String,
        @ColumnInfo(name = "tagline")
        @SerializedName("tagline")
        val tagline: String,
        @ColumnInfo(name = "first_brewed")
        @SerializedName("first_brewed")
        val firstBrewed: String?,
        @ColumnInfo(name = "description")
        @SerializedName("description")
        val description: String,
        @ColumnInfo(name = "image")
        @SerializedName("image")
        val image: String?,
        @ColumnInfo(name = "abv")
        @SerializedName("abv")
        val abv: Double
) : Serializable

package io.github.alxiw.punkpaging.data.api

import io.github.alxiw.punkpaging.data.model.Beer
import retrofit2.http.GET
import retrofit2.http.Query

interface PunkApi {

    @GET("beers")
    suspend fun fetchBeers(
            @Query("beer_name") beerName: String?,
            @Query("page") page: Int,
            @Query("per_page") pageSize: Int
    ): List<Beer>
}

package io.github.alxiw.punkpaging.data.api

import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

private const val CACHE_MAX_AGE = 1
private const val CACHE_HEADER = "Cache-Control"

class OnlineCacheInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val cacheControl = CacheControl.Builder()
            .maxAge(CACHE_MAX_AGE, TimeUnit.HOURS)
            .build()

        return response.newBuilder()
            .header(CACHE_HEADER, cacheControl.toString())
            .build()
    }
}

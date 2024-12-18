package io.github.alxiw.punkpaging.data.api

import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

private const val CACHE_MAX_STALE = 1
private const val CACHE_HEADER = "Cache-Control"

class OfflineCacheInterceptor(private val networkHandler: NetworkHandler) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = if (networkHandler.isConnected) {
            val cacheControl = CacheControl.Builder()
                .maxStale(CACHE_MAX_STALE, TimeUnit.DAYS)
                .build()

            chain.request().newBuilder()
                .header(CACHE_HEADER, cacheControl.toString())
                .build()
        } else {
            chain.request()
        }

        return chain.proceed(request)
    }
}

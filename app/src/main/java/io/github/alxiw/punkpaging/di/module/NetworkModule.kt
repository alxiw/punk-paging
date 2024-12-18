package io.github.alxiw.punkpaging.di.module

import android.content.Context
import android.util.Log
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.squareup.picasso.Downloader
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides
import io.github.alxiw.punkpaging.data.api.NetworkHandler
import io.github.alxiw.punkpaging.data.api.OnlineCacheInterceptor
import io.github.alxiw.punkpaging.data.api.PunkApi
import io.github.alxiw.punkpaging.di.annotations.ApplicationContext
import io.github.alxiw.punkpaging.ui.ImageLoader
import io.github.alxiw.punkpaging.util.CacheUtil.calculateDiskCacheSize
import io.github.alxiw.punkpaging.util.CacheUtil.createDefaultCacheDir
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Logger
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
class NetworkModule {

    companion object {
        private const val BASE_URL = "https://punkapi.online/v3/"
        private const val IMAGE_URL = "${BASE_URL}images/"
    }

    @Provides
    @Named(BASE_URL)
    fun provideBaseUrl() = BASE_URL

    @Provides
    @Named(IMAGE_URL)
    fun provideImageUrl() = IMAGE_URL

    @Provides
    @Singleton
    fun provideNetworkHandler(@ApplicationContext context: Context): NetworkHandler {
        return NetworkHandler(context)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor(object : Logger {
            override fun log(message: String) {
                Log.d("HELLO", message)
            }
        }).apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
    }

    @Provides
    @Singleton
    fun provideCache(@ApplicationContext context: Context): Cache {
        val cacheDir: File = createDefaultCacheDir(context)
        val maxSize = calculateDiskCacheSize(cacheDir)
        return Cache(cacheDir, maxSize)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        cache: Cache,
        networkHandler: NetworkHandler,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .cache(cache)
            .addInterceptor(OnlineCacheInterceptor())
            .addInterceptor(loggingInterceptor)
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
            client: OkHttpClient,
            gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun providePunkApi(retrofit: Retrofit): PunkApi {
        return retrofit.create(PunkApi::class.java)
    }

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        client: OkHttpClient
    ): ImageLoader {
        val downloader = object : Downloader {
            override fun load(request: Request) = client.newCall(request).execute()
            override fun shutdown() { client.cache?.close() }
        }
        return ImageLoader(Picasso.Builder(context).downloader(downloader).build(), IMAGE_URL)
    }
}

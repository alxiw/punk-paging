package io.github.alxiw.punkpaging.di.module

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import io.github.alxiw.punkpaging.BuildConfig
import io.github.alxiw.punkpaging.data.api.PunkApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
class NetworkModule {

    companion object {
        private const val BASE_URL = "https://api.punkapi.com/v2/"
    }

    @Provides
    @Named(BASE_URL)
    fun provideBaseUrl() = BASE_URL

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
        return HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT).also {
            it.level = if (false/*BuildConfig.DEBUG*/) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    fun provideStethoInterceptor(): StethoInterceptor {
        return StethoInterceptor()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
            loggingInterceptor: HttpLoggingInterceptor,
            stethoInterceptor: StethoInterceptor
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .readTimeout(1, TimeUnit.SECONDS)
            .connectTimeout(1, TimeUnit.SECONDS)
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(loggingInterceptor)
            builder.addNetworkInterceptor(stethoInterceptor)
        }
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
            @Named(BASE_URL) baseUrl: String,
            client: OkHttpClient,
            gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun providePunkApi(retrofit: Retrofit): PunkApi {
        return retrofit.create(PunkApi::class.java)
    }
}

package io.github.alxiw.punkpaging.di.module

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import dagger.Module
import dagger.Provides
import io.github.alxiw.punkpaging.data.db.PunkDatabase
import io.github.alxiw.punkpaging.di.annotations.ApplicationContext
import javax.inject.Named
import javax.inject.Singleton

@Module
class DatabaseModule {

    companion object {
        private const val DB_NAME = "punk_paging.db"
        private const val PREFS_NAME = "punk_paging_prefs"
    }

    @Provides
    @Named(DB_NAME)
    fun provideDatabaseName() = DB_NAME

    @Provides
    @Singleton
    fun providesPunkDatabase(@Named(DB_NAME) dbName: String, @ApplicationContext app: Context): PunkDatabase {
        return Room.databaseBuilder(app, PunkDatabase::class.java, dbName).build()
    }

    @Provides
    @Singleton
    fun provideSharedPreference(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}

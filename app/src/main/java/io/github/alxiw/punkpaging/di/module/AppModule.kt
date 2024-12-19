package io.github.alxiw.punkpaging.di.module

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import androidx.savedstate.SavedStateRegistryOwner
import dagger.Module
import dagger.Provides
import io.github.alxiw.punkpaging.App
import io.github.alxiw.punkpaging.data.BeersRepository
import io.github.alxiw.punkpaging.data.api.PunkApi
import io.github.alxiw.punkpaging.data.db.PunkDatabase
import io.github.alxiw.punkpaging.di.annotations.ActivityContext
import io.github.alxiw.punkpaging.di.annotations.ApplicationContext
import io.github.alxiw.punkpaging.ui.ViewModelFactory
import javax.inject.Singleton

@Module(includes = [NetworkModule::class, DatabaseModule::class])
class AppModule(private val app: Application) {

    @Provides
    @Singleton
    fun provideApplication(): Application {
        return app
    }

    @Provides
    @Singleton
    @ApplicationContext
    fun provideContext(): Context {
        return app
    }

    @Provides
    @Singleton
    fun provideRepository(
        api: PunkApi,
        database: PunkDatabase,
        prefs: SharedPreferences
    ): BeersRepository {
        return BeersRepository(api, database, prefs)
    }

    @Provides
    @Singleton
    fun provideViewModelFactory(
            repository: BeersRepository,
            @ApplicationContext context: Context,
            @ActivityContext activity: SavedStateRegistryOwner
    ): ViewModelProvider.Factory {
        return ViewModelFactory(repository, App[context] as Application, activity)
    }
}

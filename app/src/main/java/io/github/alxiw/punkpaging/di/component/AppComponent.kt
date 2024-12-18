package io.github.alxiw.punkpaging.di.component

import android.app.Application
import android.app.SearchManager
import android.content.Context
import dagger.Component
import io.github.alxiw.punkpaging.data.BeersRepository
import io.github.alxiw.punkpaging.di.annotations.ApplicationContext
import io.github.alxiw.punkpaging.di.module.AppModule
import io.github.alxiw.punkpaging.ui.ImageLoader
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    @ApplicationContext
    fun context(): Context
    fun application(): Application
    fun searchManager(): SearchManager
    fun beersRepository(): BeersRepository
    fun imageLoader(): ImageLoader
}

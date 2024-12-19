package io.github.alxiw.punkpaging.di.component

import android.app.Application
import android.content.Context
import dagger.Component
import io.github.alxiw.punkpaging.data.BeersRepository
import io.github.alxiw.punkpaging.di.annotations.ApplicationContext
import io.github.alxiw.punkpaging.di.module.AppModule
import io.github.alxiw.punkpaging.di.module.DatabaseModule
import io.github.alxiw.punkpaging.di.module.NetworkModule
import io.github.alxiw.punkpaging.di.module.ViewModelModule
import io.github.alxiw.punkpaging.ui.CatalogueFragment
import io.github.alxiw.punkpaging.ui.ImageLoader
import io.github.alxiw.punkpaging.ui.MainActivity
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        DatabaseModule::class,
        NetworkModule::class,
        ViewModelModule::class
    ]
)
interface AppComponent {

    fun inject(mainActivity: MainActivity)
    fun inject(catalogueFragment: CatalogueFragment)

    @ApplicationContext
    fun context(): Context
    fun application(): Application
    fun beersRepository(): BeersRepository
    fun imageLoader(): ImageLoader
}

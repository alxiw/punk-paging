package io.github.alxiw.punkpaging.di.component

import androidx.paging.ExperimentalPagingApi
import dagger.Component
import io.github.alxiw.punkpaging.di.module.AppModule
import io.github.alxiw.punkpaging.ui.MainActivity
import javax.inject.Singleton

@ExperimentalPagingApi
@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(target: MainActivity)
}

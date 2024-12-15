package io.github.alxiw.punkpaging

import android.app.Application
import android.content.Context
import io.github.alxiw.punkpaging.di.component.AppComponent
import io.github.alxiw.punkpaging.di.component.DaggerAppComponent
import io.github.alxiw.punkpaging.di.module.AppModule

class App : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = initDagger(this)
    }

    private fun initDagger(app: App): AppComponent {
        return DaggerAppComponent.builder()
            .appModule(AppModule(app))
            .build()
    }

    companion object {
        @JvmStatic
        operator fun get(context: Context): App {
            return context.applicationContext as App
        }
    }
}

package io.github.alxiw.punkpaging

import android.app.Application
import io.github.alxiw.punkpaging.di.component.AppComponent
import io.github.alxiw.punkpaging.di.component.DaggerAppComponent
import io.github.alxiw.punkpaging.di.module.AppModule

class App : Application() {

    companion object {
        lateinit var appComponent: AppComponent
    }

    override fun onCreate() {
        super.onCreate()
        initAppComponent()
    }

    private fun initAppComponent() {
        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()
    }
}

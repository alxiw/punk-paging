package io.github.alxiw.punkpaging

import android.app.Application
import android.content.Context
import androidx.paging.ExperimentalPagingApi
import com.facebook.stetho.Stetho
import io.github.alxiw.punkpaging.di.component.AppComponent
import io.github.alxiw.punkpaging.di.component.DaggerAppComponent
import io.github.alxiw.punkpaging.di.module.AppModule

@ExperimentalPagingApi
class App : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        initStetho()
        appComponent = initDagger(this)
    }

    private fun initStetho() {
        if (!BuildConfig.DEBUG) {
            return
        }
        val initializer = Stetho.newInitializerBuilder(this).apply {
            // Chrome DevTools
            enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this@App))
            // Command line interface
            enableDumpapp(Stetho.defaultDumperPluginsProvider(this@App))
        }.build()
        Stetho.initialize(initializer)
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

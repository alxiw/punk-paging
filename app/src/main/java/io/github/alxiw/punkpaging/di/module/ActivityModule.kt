package io.github.alxiw.punkpaging.di.module

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.savedstate.SavedStateRegistryOwner
import dagger.Module
import dagger.Provides
import io.github.alxiw.punkpaging.di.annotations.ActivityContext

@Module
class ActivityModule(private var activity: AppCompatActivity) {

    @Provides
    fun provideActivity(): Activity {
        return activity
    }

    @Provides
    @ActivityContext
    fun providesContext(): Context {
        return activity
    }

    @Provides
    fun providesSavedStateRegistryOwner(): SavedStateRegistryOwner {
        return activity
    }
}

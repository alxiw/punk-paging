package io.github.alxiw.punkpaging.di.component

import dagger.Subcomponent
import io.github.alxiw.punkpaging.di.annotations.ActivitySpecific
import io.github.alxiw.punkpaging.di.module.ActivityModule
import io.github.alxiw.punkpaging.ui.MainActivity

@ActivitySpecific
@Subcomponent(modules = [ActivityModule::class])
interface ActivityComponent {
    fun inject(mainActivity: MainActivity)
}

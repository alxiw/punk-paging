package io.github.alxiw.punkpaging.di.component

import dagger.Component
import io.github.alxiw.punkpaging.di.annotations.ConfigPersistentSpecific
import io.github.alxiw.punkpaging.di.module.ActivityModule

@ConfigPersistentSpecific
@Component(dependencies = [AppComponent::class])
interface ConfigPersistentComponent {
    fun activityComponent(activityModule: ActivityModule): ActivityComponent
}

package io.github.alxiw.punkpaging.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import io.github.alxiw.punkpaging.ui.ViewModelFactory
import io.github.alxiw.punkpaging.ui.beers.BeersViewModel

@Module
abstract class ViewModelModule {

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @[Binds IntoMap ClassKey(BeersViewModel::class)]
    abstract fun bindBeersViewModel(viewModel: BeersViewModel): ViewModel
}

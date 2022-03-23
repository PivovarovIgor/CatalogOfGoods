package ru.brauer.catalogofgoods.di.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.brauer.catalogofgoods.ui.catalogofgoods.CatalogOfGoodsViewModel
import ru.brauer.catalogofgoods.ui.settings.SettingsViewModel

@Module
abstract class ViewModelModule {

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(CatalogOfGoodsViewModel::class)
    abstract fun splashViewModel(viewModel: CatalogOfGoodsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    abstract fun settingsViewModel(viewModel: SettingsViewModel): ViewModel
}


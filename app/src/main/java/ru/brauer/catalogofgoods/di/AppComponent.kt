package ru.brauer.catalogofgoods.di

import dagger.Component
import ru.brauer.catalogofgoods.di.viewmodel.ViewModelModule
import ru.brauer.catalogofgoods.services.LoadingGoodsService
import ru.brauer.catalogofgoods.ui.MainActivity
import ru.brauer.catalogofgoods.ui.catalogofgoods.CatalogOfGoodsFragment
import ru.brauer.catalogofgoods.ui.settings.SettingsFragment
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        CiceroneModule::class,
        ViewModelModule::class,
        DataModule::class,
        AppModule::class,
        RxModule::class,
        SettingsModule::class,
        NetModule::class
    ]
)
interface AppComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(catalogOfGoodsFragment: CatalogOfGoodsFragment)
    fun inject(loadingGoodsService: LoadingGoodsService)
    fun inject(settingsFragment: SettingsFragment)
}
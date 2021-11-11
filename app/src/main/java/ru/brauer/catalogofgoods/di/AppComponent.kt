package ru.brauer.catalogofgoods.di

import dagger.Component
import ru.brauer.catalogofgoods.di.viewmodel.ViewModelFactory
import ru.brauer.catalogofgoods.di.viewmodel.ViewModelModule
import ru.brauer.catalogofgoods.ui.main.CatalogOfGoodsFragment
import ru.brauer.catalogofgoods.ui.main.MainActivity
import javax.inject.Singleton

@Singleton
@Component(modules = [
    CiceroneModule::class,
    ViewModelModule::class
])
interface AppComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(catalogOfGoodsFragment: CatalogOfGoodsFragment)
}
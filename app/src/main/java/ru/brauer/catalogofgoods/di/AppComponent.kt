package ru.brauer.catalogofgoods.di

import dagger.BindsInstance
import dagger.Component
import ru.brauer.catalogofgoods.App
import ru.brauer.catalogofgoods.di.viewmodel.ViewModelModule
import ru.brauer.catalogofgoods.ui.MainActivity
import ru.brauer.catalogofgoods.ui.catalogofgoods.CatalogOfGoodsFragment
import ru.brauer.catalogofgoods.ui.detailsofgoods.DetailsOfGoodsFragment
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        CiceroneModule::class,
        ViewModelModule::class,
        DataModule::class,
        RxModule::class
    ]
)
interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun context(context: App): Builder
        fun build(): AppComponent
    }

    fun inject(mainActivity: MainActivity)
    fun inject(catalogOfGoodsFragment: CatalogOfGoodsFragment)
    fun inject(detailsOfGoodsFragment: DetailsOfGoodsFragment)
}
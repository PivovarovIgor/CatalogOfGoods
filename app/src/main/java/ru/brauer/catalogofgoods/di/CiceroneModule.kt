package ru.brauer.catalogofgoods.di

import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent
import ru.brauer.catalogofgoods.ui.AndroidScreens
import ru.brauer.catalogofgoods.ui.IScreens
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CiceroneModule {

    private val cicerone = Cicerone.create()

    @Singleton
    @Provides
    fun cicerone(): Cicerone<Router> = cicerone

    @Singleton
    @Provides
    fun navigationHolder(): NavigatorHolder = cicerone.getNavigatorHolder()

    @Singleton
    @Provides
    fun router(): Router = cicerone.router

    @Singleton
    @Provides
    fun screens(): IScreens = AndroidScreens()
}
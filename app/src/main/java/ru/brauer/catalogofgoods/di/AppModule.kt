package ru.brauer.catalogofgoods.di

import dagger.Module
import dagger.Provides
import ru.brauer.catalogofgoods.App
import javax.inject.Singleton

@Module
class AppModule(private val app: App) {

    @Singleton
    @Provides
    fun getApp(): App = app
}
package ru.brauer.catalogofgoods.di

import dagger.Module
import dagger.Provides
import ru.brauer.catalogofgoods.data.MockRepository
import ru.brauer.catalogofgoods.domain.IRepository

@Module
class DataModule {

    @Provides
    fun repository(): IRepository = MockRepository()
}
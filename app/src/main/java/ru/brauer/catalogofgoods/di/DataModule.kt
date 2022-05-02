package ru.brauer.catalogofgoods.di

import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import ru.brauer.catalogofgoods.App
import ru.brauer.catalogofgoods.data.CatalogOfGoodsRepository
import ru.brauer.catalogofgoods.data.commerceml.CommerceMlParser
import ru.brauer.catalogofgoods.data.commerceml.IXmlParserByRule
import ru.brauer.catalogofgoods.data.database.AppDatabase
import ru.brauer.catalogofgoods.data.net.CatalogOfGoodsFtpRetriever
import ru.brauer.catalogofgoods.data.net.ICatalogOfGoodsRetrieverFromNet
import ru.brauer.catalogofgoods.domain.IRepository
import javax.inject.Singleton

@Module(includes = [DataModuleBinds::class])
class DataModule {

    companion object {
        private const val DB_NAME = "appDatabase.db"
    }


    @Singleton
    @Provides
    fun getDatabase(context: App): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DB_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
}

@Module
interface DataModuleBinds {

    @Singleton
    @Binds
    fun getCmParser(com: CommerceMlParser): IXmlParserByRule

    @Singleton
    @Binds
    fun getRepository(repository: CatalogOfGoodsRepository): IRepository

    @Singleton
    @Binds
    fun retrieverFromNet(
        catalogOfGoodsRepository: CatalogOfGoodsFtpRetriever
    ): ICatalogOfGoodsRetrieverFromNet
}
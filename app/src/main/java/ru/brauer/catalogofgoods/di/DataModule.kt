package ru.brauer.catalogofgoods.di

import androidx.room.Room
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

@Module
class DataModule {

    companion object {
        private const val DB_NAME = "appDatabase.db"
    }

    @Singleton
    @Provides
    fun repository(
        retrieverFromNet: ICatalogOfGoodsRetrieverFromNet,
        appDatabase: AppDatabase
    ): IRepository =
        CatalogOfGoodsRepository(retrieverFromNet, appDatabase)

    @Singleton
    @Provides
    fun retrieverFromNet(parser: IXmlParserByRule): ICatalogOfGoodsRetrieverFromNet =
        CatalogOfGoodsFtpRetriever(parser)

    @Singleton
    @Provides
    fun commerceMlParser(): IXmlParserByRule = CommerceMlParser()

    @Singleton
    @Provides
    fun getDatabase(context: App): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DB_NAME
        )
            .build()
}
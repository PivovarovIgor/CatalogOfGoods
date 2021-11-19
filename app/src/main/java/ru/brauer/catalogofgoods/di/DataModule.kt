package ru.brauer.catalogofgoods.di

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
CREATE TABLE 'offers'(
    'id' TEXT NOT NULL, 
    'name' TEXT NOT NULL,
    'goods_id' TEXT NOT NULL, 
    PRIMARY KEY('id'), 
    FOREIGN KEY('goods_id') REFERENCES goods ('id'))"""
                )
            }
        }
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
            .addMigrations(MIGRATION_1_2)
            .build()
}
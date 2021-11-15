package ru.brauer.catalogofgoods.di

import dagger.Module
import dagger.Provides
import ru.brauer.catalogofgoods.data.commerceml.CommerceMlParser
import ru.brauer.catalogofgoods.data.commerceml.IXmlParserByRule
import ru.brauer.catalogofgoods.data.net.CatalogOfGoodsFtpRetriever
import ru.brauer.catalogofgoods.data.net.ICatalogOfGoodsRetrieverFromNet
import ru.brauer.catalogofgoods.data.CatalogOfGoodsRepository
import ru.brauer.catalogofgoods.domain.IRepository
import javax.inject.Singleton

@Module
class DataModule {

    @Singleton
    @Provides
    fun repository(retrieverFromNet: ICatalogOfGoodsRetrieverFromNet): IRepository =
        CatalogOfGoodsRepository(retrieverFromNet)

    @Singleton
    @Provides
    fun retrieverFromNet(parser: IXmlParserByRule): ICatalogOfGoodsRetrieverFromNet =
        CatalogOfGoodsFtpRetriever(parser)

    @Singleton
    @Provides
    fun commerceMlParser(): IXmlParserByRule = CommerceMlParser()

}
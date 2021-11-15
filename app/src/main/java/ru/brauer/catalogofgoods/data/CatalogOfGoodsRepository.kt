package ru.brauer.catalogofgoods.data

import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import ru.brauer.catalogofgoods.data.entities.Goods
import ru.brauer.catalogofgoods.data.net.ICatalogOfGoodsRetrieverFromNet
import ru.brauer.catalogofgoods.domain.IRepository
import javax.inject.Inject

class CatalogOfGoodsRepository @Inject constructor(
    private val catalogOfGoodsRetriever: ICatalogOfGoodsRetrieverFromNet
) :
    IRepository {
    override fun getGoods(): Single<List<Goods>> =
        Single.fromCallable {
            catalogOfGoodsRetriever.retrieve()
        }.subscribeOn(Schedulers.io())
}


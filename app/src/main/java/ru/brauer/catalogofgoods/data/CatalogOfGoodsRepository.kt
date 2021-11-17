package ru.brauer.catalogofgoods.data

import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import ru.brauer.catalogofgoods.data.database.AppDatabase
import ru.brauer.catalogofgoods.data.database.GoodsEnt
import ru.brauer.catalogofgoods.data.entities.Goods
import ru.brauer.catalogofgoods.data.net.ICatalogOfGoodsRetrieverFromNet
import ru.brauer.catalogofgoods.domain.IRepository
import javax.inject.Inject

class CatalogOfGoodsRepository @Inject constructor(
    private val catalogOfGoodsRetriever: ICatalogOfGoodsRetrieverFromNet,
    private val appDatabase: AppDatabase
) :
    IRepository {
    override fun getGoods(): Single<List<Goods>> =
        Single.fromCallable {
            catalogOfGoodsRetriever
                .retrieve()
                .subscribe { appDatabase.goodsDao.insert(it.toDatabaseData()) }
            appDatabase.goodsDao.getAll().toBusinessData()
        }.subscribeOn(Schedulers.io())
}

fun GoodsEnt.toBusinessData(): Goods =
    Goods(
        id = id,
        name = name,
        photoUrl = photoUrl
    )

fun Goods.toDatabaseData(): GoodsEnt =
    GoodsEnt(
        id = id,
        name = name,
        photoUrl = photoUrl
    )

fun List<GoodsEnt>.toBusinessData(): List<Goods> = map { it.toBusinessData() }

fun List<Goods>.toDatabaseData(): List<GoodsEnt> = map { it.toDatabaseData() }